# EVENT_API — Plano de inicialização de categorias via JSON

## 1. Diagnóstico do código atual

### Fluxo de validação de categoria

- `EventService.findActiveCategoryByName()` consulta `CategoryRepository.findByName(name)`, aceita somente uma categoria com `active == true` e, quando ela não existe ou está inativa, lança `ValidationException("Category not found")`.
- O método é usado nos fluxos de criação e atualização de evento. O erro atual chega ao contrato REST como HTTP 404 por meio de `GlobalHandlerException`.
- `EventMapper` converte `categoryName` para maiúsculas antes da consulta. `CategoryMapper` também converte para maiúsculas na criação e atualização de categorias. A consulta Mongo por nome, porém, é exata/case-sensitive.
- A inicialização não deve ser incorporada a `findActiveCategoryByName()`. Esse método deve continuar responsável somente pela consulta e pela regra de categoria ativa, preservando a mensagem, o status HTTP e o comportamento atual.

### Modelo e persistência

- `Category` é um documento MongoDB da collection `category`, com os campos `id` (`Long`, armazenado como `_id`), `name` (`String`) e `active` (`Boolean`). A entidade não possui validações declarativas nem índice único de nome.
- `CategoryRepository` estende `MongoRepository<Category, Long>` e já oferece `count()`, `saveAll()` e as demais operações padrão. Os métodos próprios são `findByName()` e `findTopByOrderByIdDesc()`.
- `CategoryService.create()` evita nome repetido apenas por consulta prévia e calcula o próximo ID com `maior ID + 1`. Essas duas operações não são atômicas sob concorrência. A importação não deve reutilizar esse método registro a registro.
- A chave `_id` já é única no MongoDB. IDs determinísticos vindos do JSON, combinados com inserção (não upsert), ajudam a detectar uma corrida, mas não substituem a coordenação entre instâncias nem a transação.

### Inicialização e transações existentes

- Não há `CommandLineRunner`, `ApplicationRunner`, seed, Flyway, Liquibase ou Mongock para categorias. `EventApiApplication` apenas inicia o Spring Boot.
- `MongoConfig` habilita auditoria, mas não declara explicitamente um `MongoTransactionManager`.
- O projeto já usa `@Transactional` em outro serviço, mas o suporte efetivo a transações Mongo depende de um transaction manager e de um MongoDB executado como replica set/sharded cluster.
- Os `docker-compose*.yml` atuais iniciam `mongo` como servidor standalone. Nesse modo, uma importação de vários documentos não pode atender à exigência de transação multi-documento.
- O teste de repository de categorias usa `MongoDBContainer.getReplicaSetUrl()`, portanto a base de testes já oferece uma referência adequada para testes transacionais.
- Não existe hoje um arquivo de categorias padrão em `src/main/resources`.

### Consequência para a mudança

A regra decisiva deve ser `categoryRepository.count() == 0`, considerando **qualquer** documento, ativo ou inativo. Não se deve usar `findByName`, `countByActive`, a existência da categoria solicitada nem o conteúdo do JSON como gatilho. Uma collection com um único registro já bloqueia completamente a importação.

## 2. Estratégia recomendada de inicialização

### Decisão: inicializar no startup

Usar um `ApplicationRunner` específico, executado antes de a aplicação publicar o estado de pronta, em vez de inicializar na primeira consulta de evento.

Motivos:

- mantém `EventService.findActiveCategoryByName()` e os contratos REST sem alterações;
- elimina latência e efeitos colaterais na primeira requisição;
- aplica a mesma condição antes de qualquer consumidor de categorias;
- permite falhar cedo caso o JSON de um ambiente novo esteja inválido;
- reduz a janela de concorrência com requisições, desde que o balanceador só envie tráfego após a readiness da instância.

A primeira consulta não deve ter fallback de seed. Se a aplicação já estiver pronta e uma categoria não existir, permanece o erro atual, mesmo quando o JSON contenha essa categoria.

### Fluxo proposto

1. O runner verifica `repository.count()`.
2. Se o resultado for maior que zero, registra apenas um log de `seed skipped` e encerra. Nesta ramificação não deve abrir, verificar existência, desserializar ou validar o arquivo JSON.
3. Se o resultado for zero, tenta adquirir um lock distribuído com chave fixa, por exemplo `category-seed`, em uma collection técnica separada.
4. Após obter o lock, verifica novamente `repository.count()`. Se outro processo já inseriu qualquer categoria, encerra sem carregar o JSON.
5. Somente com a collection ainda vazia, lê `classpath:seed/categories.json`.
6. Desserializa em modo estrito e valida a lista inteira em memória, sem escrever no banco.
7. Chama um bean transacional separado. Dentro da transação, verifica novamente se a collection continua vazia e, apenas então, executa uma inserção em lote.
8. Confirma a transação e libera o lock em `finally`.
9. A aplicação conclui o startup. Criação e atualização de eventos continuam chamando a consulta atual de categoria.

As três verificações de vazio têm papéis distintos: caminho rápido sem I/O de arquivo, revalidação depois do lock e garantia final junto da escrita transacional.

### Semântica obrigatória por estado

| Estado observado | Ação |
|---|---|
| `count() > 0`, inclusive somente inativas | Não ler JSON, não adquirir dados dele e não escrever em `category` |
| Vazia, JSON válido | Inserir exatamente o lote validado |
| Vazia, JSON inválido ou ausente | Falhar o startup de forma explícita; manter `category` vazia |
| Outra instância preencheu enquanto esta aguardava | Não importar, não completar e não atualizar |
| Falha de banco durante a inserção | Rollback integral; não aceitar fallback não transacional |

### Transação

- Declarar `MongoTransactionManager` em `MongoConfig` e executar a persistência em um bean diferente do orquestrador, evitando o problema de self-invocation do proxy de `@Transactional`.
- Dentro da transação, usar `MongoTemplate.insert(...)`/inserção em lote, e não `save`, `saveAll` com semântica de upsert, nem `CategoryService.create()` em loop.
- Se surgir qualquer documento entre a última verificação e a escrita, a operação deve ser abortada ou ignorada como `SKIPPED_CONCURRENTLY_POPULATED`; jamais completar a collection.
- Erros de validação acontecem antes da abertura da transação. Erros de persistência abortam toda a transação.
- A aplicação não deve degradar silenciosamente para inserções individuais quando transações não estiverem disponíveis.

### Concorrência entre instâncias

Planejar um lock com lease, armazenado em collection técnica (por exemplo `application_lock`) e identificado pelo `_id` único `category-seed`:

- aquisição atômica com `ownerId` e `expiresAt`;
- somente o proprietário pode renovar ou liberar;
- lease expira após crash, permitindo recuperação;
- instâncias perdedoras aguardam por tempo limitado e reconsultam `category`; ao encontrarem qualquer registro, encerram sem ler o JSON;
- timeout sem progresso deve falhar de forma visível, não prosseguir sem lock.

O lock coordena os seeders; a transação garante atomicidade do lote. IDs determinísticos e validação de duplicidade no arquivo fornecem uma barreira adicional. Durante o bootstrap, o ambiente não deve rotear escritas administrativas de categoria antes da readiness. Esvaziar a collection manualmente em um ambiente já ativo fica fora do fluxo normal e deve exigir janela operacional.

### Política de falha e observabilidade

- Para ambiente novo com tabela vazia, JSON inválido/ausente ou transação indisponível deve impedir a aplicação de ficar pronta. Continuar ativa com uma base vazia apenas adiaria o problema para requisições com 404.
- Criar uma exceção específica, por exemplo `CategorySeedException`, com causa técnica preservada e mensagem sem credenciais nem conteúdo sensível.
- Logs estruturados mínimos: `skipped_non_empty`, `lock_wait`, `started`, `completed` com quantidade importada, `skipped_concurrent_population` e `failed`.
- Não expor endpoint REST de seed e não acoplar seed a autenticação/JWT.

### Configuração

Planejar propriedades com defaults seguros:

```properties
app.category-seed.enabled=true
app.category-seed.resource=classpath:seed/categories.json
app.category-seed.lock-duration=PT30S
app.category-seed.lock-wait-timeout=PT60S
```

`enabled=false` deve existir para testes ou operação emergencial, mas o default da aplicação permanece automático. Desabilitar não pode alterar ou apagar categorias.

## 3. Estrutura do JSON

Formato versionado recomendado:

```json
{
  "schemaVersion": 1,
  "categories": [
    {
      "id": 1,
      "name": "CAPOEIRA",
      "active": true
    },
    {
      "id": 2,
      "name": "WORKSHOP",
      "active": true
    }
  ]
}
```

O exemplo ilustra o formato, não define ainda a lista oficial. A primeira versão deve ser gerada das categorias aprovadas/exportadas e revisada antes da implementação.

### Regras de validação

- `schemaVersion` obrigatório e igual a uma versão suportada; versão desconhecida falha.
- `categories` obrigatório, não nulo e não vazio.
- cada item deve conter somente `id`, `name` e `active`; propriedades desconhecidas falham para detectar erros de digitação;
- `id` obrigatório, positivo, representável como `Long` e único no arquivo;
- `name` obrigatório, sem espaços somente, sem espaços nas extremidades, único no arquivo e em maiúsculas com `Locale.ROOT`, acompanhando os mappers atuais;
- a comparação de duplicidade de nome deve ser case-insensitive para impedir variantes acidentais no próprio arquivo;
- `active` obrigatório e booleano; não assumir `true` quando ausente;
- valores `null`, itens duplicados ou estrutura malformada invalidam o lote inteiro;
- a ordem do arquivo deve ser crescente por `id` para diffs estáveis, embora a importação não dependa dela.

Não gerar IDs durante o seed. Preservar IDs no arquivo torna o dump reproduzível e evita diferenças entre ambientes. Também não converter, reativar ou completar registros já existentes.

## 4. Estratégia de dump e importação

### Dump

Planejar `event_api/scripts/export-categories.ps1`, uma ferramenta explícita e somente leitura que:

1. recebe a URI do Mongo por variável de ambiente/parâmetro seguro e nunca a grava nem a imprime;
2. consulta todos os documentos de `category`, incluindo ativos e inativos;
3. converte `_id` do Mongo para `id` no JSON;
4. ordena por `id` e escreve o envelope com `schemaVersion`;
5. preserva exatamente `id`, `name` e `active`, sem reativar nem normalizar dados silenciosamente;
6. executa as mesmas validações de schema/canonicalização antes de substituir o arquivo destino;
7. grava primeiro em arquivo temporário e faz a troca somente após sucesso;
8. por padrão, recusa exportar uma collection vazia ou sobrescrever um arquivo sem confirmação explícita.

O destino padrão será `event_api/src/main/resources/seed/categories.json`. Depois do dump, revisar o diff e rodar o teste de contrato do arquivo. O script não será endpoint da aplicação e não será executado automaticamente em produção.

Caso dados históricos não estejam em maiúsculas ou violem as regras atuais, o dump deve apontar os itens para revisão e falhar, em vez de alterar o banco ou corrigir o arquivo de forma implícita.

### Importação

- A importação será interna ao startup e somente após a condição de vazio ser confirmada.
- O loader usará o `ObjectMapper` já fornecido pelo Spring/Jackson, configurado localmente para rejeitar propriedades desconhecidas.
- O mapeamento deve criar objetos `Category` diretamente a partir de um DTO de seed validado; não reutilizar DTO REST nem aplicar regras que descartem `active` ou `id`.
- A escrita será uma única unidade transacional em lote.
- Não haverá merge, upsert, atualização, correção, reativação, remoção nem comparação com uma tabela populada.

## 5. Arquivos que serão criados ou modificados

Esta é a proposta para uma etapa posterior; nenhum desses arquivos de implementação é criado agora.

### Criar

- `event_api/src/main/resources/seed/categories.json` — dados iniciais versionados.
- `event_api/src/main/java/com/br/capoeira/eventos/event_api/seed/CategorySeedRunner.java` — gatilho de startup e caminho rápido de tabela populada.
- `event_api/src/main/java/com/br/capoeira/eventos/event_api/seed/CategorySeedService.java` — orquestra lock, leitura e validação.
- `event_api/src/main/java/com/br/capoeira/eventos/event_api/seed/CategorySeedTransactionalImporter.java` — revalidação e inserção transacional em bean separado.
- `event_api/src/main/java/com/br/capoeira/eventos/event_api/seed/CategorySeedFile.java` e `CategorySeedItem.java` — contrato do JSON.
- `event_api/src/main/java/com/br/capoeira/eventos/event_api/seed/CategorySeedProperties.java` — propriedades tipadas.
- `event_api/src/main/java/com/br/capoeira/eventos/event_api/seed/CategorySeedLock.java` — coordenação Mongo com lease.
- `event_api/src/main/java/com/br/capoeira/eventos/event_api/config/exception/CategorySeedException.java` — falha controlada de bootstrap.
- `event_api/scripts/export-categories.ps1` — dump reproduzível e somente leitura.
- testes unitários e de integração correspondentes em `event_api/src/test`.

### Modificar

- `event_api/src/main/java/com/br/capoeira/eventos/event_api/config/MongoConfig.java` — declarar o transaction manager Mongo.
- `event_api/src/main/resources/application.properties` — propriedades do seed.
- configuração local/homologação do Mongo — habilitar replica set para suportar a transação; a alteração exata deve ser revisada separadamente.

### Manter sem alteração funcional

- `EventService`, especialmente `findActiveCategoryByName()`;
- `CategoryService`, `CategoryController`, DTOs e endpoints REST;
- `Category` e `CategoryRepository`, salvo se a implementação demonstrar necessidade de uma consulta otimizada; `count()` já está disponível;
- autenticação, `JwtConfig`, segurança e os demais microsserviços;
- banco e configuração de produção nesta etapa.

Não adicionar índice único em `name` automaticamente como parte deste seed. Embora seja recomendável para a consistência geral, criar esse índice em uma collection existente pode falhar com dados históricos duplicados e constitui uma migração independente. A unicidade interna do JSON será validada, e `_id` já é único.

## 6. Plano de testes

### Testes unitários do orquestrador/loader

| Situação | Verificações principais |
|---|---|
| Tabela vazia, JSON válido | loader chamado; lista inteira validada; importer chamado uma vez |
| Tabela vazia, JSON inválido | `CategorySeedException`; importer e Mongo sem chamadas de escrita |
| JSON ausente/malformado | falha controlada; zero escrita |
| IDs ou nomes duplicados | lote rejeitado antes da transação |
| Campos nulos/desconhecidos/versão inválida | lote rejeitado por completo |
| Tabela populada | loader nunca chamado; lock/importer nunca escrevem |
| Tabela apenas com inativas | mesmo comportamento de tabela populada |
| Tabela parcialmente populada | não completa registros, mesmo que o JSON tenha os ausentes |
| Revalidação após lock encontra registro | não lê JSON nem importa |
| Seed desabilitado | não lê nem escreve |

Usar um `Resource`/loader espião nos testes de tabela populada para provar a regra “não carregar o JSON”, e não apenas verificar ausência de `save`.

### Testes transacionais de integração com MongoDB

Usar Testcontainers com replica set, como o teste existente já faz:

1. **Vazia + válido:** persistir exatamente a quantidade, IDs, nomes e flags do fixture.
2. **Vazia + inválido:** confirmar collection vazia após a falha.
3. **Falha durante o lote:** provocar erro de persistência e confirmar rollback de todos os documentos.
4. **Populada + existente:** seed não altera o snapshot e a consulta retorna normalmente.
5. **Populada + inexistente:** seed não roda e `EventService` mantém `ValidationException("Category not found")`.
6. **Somente inativas:** nenhuma reativação e nenhum novo registro.
7. **Parcialmente populada:** nenhum registro ausente é completado.
8. **Duas inicializações simultâneas:** sincronizar duas execuções com `CountDownLatch`; ao final deve existir exatamente um lote, sem duplicatas.
9. **Nova execução:** capturar os documentos antes/depois e provar igualdade total.
10. **Concorrência que popula antes do commit:** abortar/ignorar o seed e não completar a tabela.
11. **Mongo sem suporte transacional:** startup falha com diagnóstico claro; não ocorre fallback parcial.

### Testes de regressão

- Manter os testes atuais de categoria ativa, categoria inexistente e categoria inativa em `EventServiceTest`.
- Acrescentar asserções de que a camada REST continua retornando o mesmo status e corpo de erro para categoria inexistente/inativa.
- Confirmar que nenhum endpoint, autorização, payload REST ou mensagem RabbitMQ mudou.
- Validar o `categories.json` versionado em um teste dedicado para impedir merge de um seed inválido.

## 7. Riscos e critérios de aceite

### Riscos e mitigação

| Risco | Mitigação planejada |
|---|---|
| Mongo standalone não suporta a transação exigida | tornar replica set pré-requisito; declarar transaction manager; falhar sem fallback |
| Duas réplicas detectam tabela vazia | lock distribuído com lease, rechecagens e IDs determinísticos |
| Instância cai mantendo lock | `expiresAt`, owner e renovação/liberação segura |
| JSON parcialmente válido | validar tudo antes de escrever; transação do lote inteiro |
| `@Transactional` não ser aplicado por self-invocation | importer em bean Spring separado |
| Dados existentes serem alterados | retorno imediato em `count() > 0`; testes que comparam snapshot antes/depois |
| Nome do seed não casar com o fluxo de eventos | validar maiúsculas, acompanhando `EventMapper`/`CategoryMapper` |
| Dump modificar dados históricos silenciosamente | exportação somente leitura e validação que exige revisão |
| Inserção administrativa durante bootstrap | seed antes da readiness; rechecagem transacional; janela operacional para ambiente vazio |
| Lease menor que o tempo de importação | timeout configurável e renovação enquanto o owner estiver ativo |
| Índice único novo quebrar dados existentes | não incluir migração de índice neste escopo |

### Pré-requisitos para implementação

- Confirmar que todos os ambientes onde o seed poderá rodar usam MongoDB replica set ou cluster compatível com transações.
- Definir e aprovar a lista oficial de categorias, preferencialmente a partir do dump revisado.
- Definir o comportamento operacional de readiness para impedir tráfego antes da conclusão do runner.
- Testar primeiro em local/homologação; nenhuma alteração de banco de produção faz parte deste plano.

### Critérios de aceite

- O JSON só é aberto quando `category` está vazia após aquisição do lock.
- Uma collection com qualquer documento, ativo ou inativo, permanece byte a byte/semanticamente inalterada após o startup.
- JSON inválido ou falha de escrita nunca deixa importação parcial.
- Duas ou mais instâncias simultâneas produzem exatamente um lote, sem duplicatas.
- Uma segunda execução não altera nenhum registro.
- Categoria importada e ativa pode ser consultada normalmente pelo fluxo atual.
- Categoria inexistente ou inativa continua gerando `ValidationException("Category not found")` e o mesmo contrato REST atual.
- Não há merge, upsert, reativação, correção ou complementação automática.
- Não há mudanças em Android, outros microsserviços, autenticação, JWT, endpoints ou banco de produção.
- O dump é reproduzível, ordenado, não altera o banco e gera um arquivo validado antes de substituir o versionado.

## Ordem sugerida para a futura implementação

1. Adequar e validar o Mongo local/homologação como replica set.
2. Criar contrato, loader e validador do JSON com testes unitários.
3. Criar importer transacional e testes de rollback.
4. Implementar lock/lease e testes de concorrência.
5. Integrar o runner ao startup e validar readiness/fail-fast.
6. Criar o script de dump, gerar a lista oficial e revisar o diff.
7. Executar a suíte de regressão do `event_api` antes de qualquer decisão sobre produção.
