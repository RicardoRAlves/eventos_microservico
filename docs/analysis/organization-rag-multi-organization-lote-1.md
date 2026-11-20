# Lote 1 — Análise do Organization API para RAG multi-organização

Data da análise: 2026-11-14  
Escopo principal: `organization_api`  
Serviços relacionados: `user_api`, `event_api`, `processor_api`, `notification_api` e `ai_agent_api`

## 1. Resumo executivo

O `organization_api` ainda não possui `organization_key` nem outro identificador persistente equivalente. A identidade atual da organização é um `Long id` gerado pelo PostgreSQL com `GenerationType.IDENTITY`. Nome, descrição, logo e estado ativo são mutáveis e não há qualquer validação de unicidade de nome, nem no serviço, nem no repository, nem no banco.

Existe no `S3Service` um normalizador privado de nome para slug, usado apenas para formar o caminho da imagem da organização. Ele remove acentos, converte para minúsculas e troca sequências não alfanuméricas por hífen. Esse slug não é armazenado, não é consultável, não tem restrição de unicidade e usa UUID aleatório quando o resultado fica vazio. Portanto, é uma referência útil para a futura regra, mas não é equivalente a `organization_key` e não pode ser reutilizado sem ajustes.

O banco é alterado hoje por Hibernate com `spring.jpa.hibernate.ddl-auto=update`. Não há Flyway, Liquibase nem migrations de domínio. O único SQL encontrado cria bancos vazios. Essa configuração é o maior bloqueio técnico para introduzir a chave com backfill e unicidade de forma auditável e segura.

Também não existe seed de organização. “Bonfim” aparece somente em dados de teste. O padrão de seed de categorias do `event_api` pode inspirar configuração, inicialização, concorrência e testes, mas sua regra de executar apenas quando toda a coleção está vazia não deve ser copiada: o Bonfim precisa ser reconciliado individualmente mesmo quando já existem outras organizações.

Não existe exclusão de organização ou unidade nos endpoints atuais. O campo `active` pode ser alterado pelo `PUT`, o que funciona como desativação informal, sem regra de cascata. Uma exclusão física direta da organização tende a ser impedida pela FK das unidades, pois não há `cascade` nem `orphanRemoval`; porém usuários, eventos e projeções de outros serviços usam apenas IDs lógicos e podem ficar órfãos.

RabbitMQ já está disponível na infraestrutura e é usado por `event_api`, `user_api`, `processor_api` e `notification_api`. O `organization_api` não tem dependência AMQP, configuração, producer, outbox ou propriedades RabbitMQ. O `ai_agent_api` também não tem persistência, mensageria, segurança, Dockerfile, participação no Compose ou job no CI.

Conclusão: a arquitetura proposta é viável, desde que o Lote 2 comece por migrations versionadas, uma especificação congelada de normalização e uma política explícita de ciclo de vida. Para evitar apropriação de acervo, `organization_key` não deve ser a única prova de identidade no `ai_agent_api`: a base precisa manter também a procedência da organização (`source_organization_id` e versão/geração de ciclo de vida), embora documentos e embeddings continuem vinculados somente ao ID interno de `knowledge_base`.

## 2. Limites da análise

- Nenhum código de produção, DTO, endpoint, schema, migration ou seed foi alterado.
- Nenhum seed foi executado.
- Nenhum commit foi criado.
- O conteúdo real dos bancos local, homologação e produção não está versionado no repositório e não pôde ser auditado. As colisões descritas abaixo precisam de um preflight somente leitura em cada ambiente antes do Lote 2.
- Os testes existentes foram inspecionados, mas não executados neste lote.
- O arquivo indicado `ai_agent_api/src/main/java/com/culturaconecta/ai/service/EventSummaryService.java` foi analisado; ele usa somente o provider de IA e não possui integração RAG, persistência ou contexto de organização.

## 3. Estado atual do `organization_api`

### 3.1 Entidades e IDs

#### `Organization`

Arquivo: `organization_api/src/main/java/com/br/capoeira/eventos/organization_api/model/Organization.java`

- Tabela `organization`.
- PK `id: Long`, `@GeneratedValue(strategy = GenerationType.IDENTITY)`.
- Campos obrigatórios: `name`, `description`, `logoUrl`, `active`, `createdAt`, `updatedAt`.
- `createdAt` e `updatedAt` são preenchidos por callbacks JPA.
- Relação `@OneToMany(mappedBy = "organization", fetch = LAZY)` com `OrganizationUnit`.
- Sem `cascade`, sem `orphanRemoval`, sem `organization_key`, sem índice declarado, sem `@Version` e sem unicidade de nome.

#### `OrganizationUnit`

Arquivo: `organization_api/src/main/java/com/br/capoeira/eventos/organization_api/model/OrganizationUnit.java`

- Tabela `organization_unit`.
- PK `id: Long`, também por `IDENTITY`.
- FK obrigatória `organization_id` para `Organization`.
- Cidade pertence à unidade, não à organização. Logo, a futura unicidade global da chave será naturalmente independente de cidade.
- `joinCode` possui unicidade no banco.
- O join code é aleatório, com seis caracteres e até vinte tentativas programáticas. A constraint do banco é a proteção final contra corrida.
- Não há chave de organização duplicada na unidade.

### 3.2 Repository

Arquivos:

- `OrganizationRepository.java`
- `OrganizationUnitRepository.java`

`OrganizationRepository` estende apenas `JpaRepository<Organization, Long>` e não possui métodos por nome ou chave. Não existem hoje `existsByName`, `findByName`, `findByOrganizationKey` ou equivalentes.

`OrganizationUnitRepository` pesquisa por organização, por join code e verifica existência do join code. Não há regra de nome único para unidade.

### 3.3 Service

Arquivo: `organization_api/src/main/java/com/br/capoeira/eventos/organization_api/service/OrganizationService.java`

Fluxos atuais:

1. `createWithMainUnit` mapeia e valida a organização.
2. Salva a organização.
3. Cria e salva a unidade principal com join code.
4. Chama sincronamente o `user_api` para promover o usuário criador e gravar os IDs de organização/unidade.
5. Retorna o DTO mapeado.

O método está em uma transação PostgreSQL local, mas a chamada HTTP não participa dela. Se o `user_api` confirmar e a transação local falhar depois, o usuário pode apontar para uma organização que não foi confirmada. Se houver timeout depois do commit remoto, o mesmo risco existe. Não há compensação, saga ou outbox.

As validações de organização verificam somente presença de nome, descrição, logo e status ativo. Não há trim/canonicalização, tamanho explícito, nome duplicado, chave duplicada ou controle de concorrência otimista.

No update, o MapStruct substitui os campos mutáveis e o serviço salva. Portanto, uma futura chave derivada do nome terá de ser recalculada dentro da mesma transação antes do `save`, e não aceita diretamente do cliente.

### 3.4 Controller e autorização

Arquivo: `organization_api/src/main/java/com/br/capoeira/eventos/organization_api/controller/OrganizationController.java`

Endpoints atuais:

| Operação | Endpoint | Autorização |
|---|---|---|
| Consultar organização | `GET /api/v1/organizacao/{id}` | autenticado |
| Consultar unidade | `GET /api/v1/organizacao/unit/{id}` | autenticado |
| Listar unidades | `GET /api/v1/organizacao/unit/all/{organizationId}` | `ADMIN` ou `SUPER_ADMIN` |
| Consultar por join code | `GET /api/v1/organizacao/unit/code/{code}` | autenticado |
| Criar organização e unidade principal | `POST /api/v1/organizacao` | qualquer autenticado |
| Criar unidade | `POST /api/v1/organizacao/unit` | `SUPER_ADMIN` |
| Atualizar organização | `PUT /api/v1/organizacao` | `SUPER_ADMIN` |
| Atualizar unidade | `PUT /api/v1/organizacao/unit` | `ADMIN` ou `SUPER_ADMIN` |

Não existem endpoints de delete, deactivate/reactivate explícitos ou consulta por chave.

As verificações são apenas por role. Não há comparação entre a organização do JWT e o ID sendo lido/alterado. Um `SUPER_ADMIN` pode atualizar qualquer organização e um `ADMIN` pode atualizar qualquer unidade se souber o ID. Isso é um risco crítico para isolamento multi-organização e precisa ser corrigido antes de expor operações de acervo RAG.

### 3.5 DTOs e mapper

Arquivos principais:

- `OrganizationCreateRequestDto`
- `OrganizationUpdateDto`
- `OrganizationResponseDto`
- `MainUnitDto`
- `OrganizationUnitDto`
- `OrganizationUnitUpdateDto`
- `OrganizationUnitResponseDto`
- `OrganizationUnitSummaryDto`
- `OrganizationMapper`

Constatações:

- Create recebe nome, descrição, logo, ativo, `userId` e unidade principal.
- Update recebe ID, nome, descrição, logo e ativo.
- Responses retornam ID e nome, mas não uma chave lógica.
- O cliente não deve informar a futura `organization_key` nos DTOs de create/update. Ela deve ser exclusivamente server-derived.
- `OrganizationUnitResponseDto` já inclui `organizationId` e dados desnormalizados da organização; é um ponto natural para expor também a chave em leitura e nas integrações.
- MapStruct ignora ID e unidades no update, mas precisará ignorar a chave recebida ou não ter esse campo nos requests.

### 3.6 Banco e migrations

Arquivo: `organization_api/src/main/resources/application.properties`

- PostgreSQL configurado por `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME` e `DB_PASSWORD`.
- Default de banco: `organization_api`.
- `spring.jpa.hibernate.ddl-auto=update`.
- Não há Flyway/Liquibase no `pom.xml`.
- `docker/postgres/init/01-init-databases.sql` cria somente `eventos_api`, `organization_api` e `user_api`.

Para o Lote 2, o schema precisa deixar de depender de `ddl-auto=update`. A recomendação é introduzir Flyway e mover para `validate` após uma baseline controlada. Adicionar o campo à entidade antes da migration seria arriscado porque o Hibernate poderia alterar o schema automaticamente sem executar o preflight e o backfill planejados.

### 3.7 Exclusão e dependências com unidades

- Não há remoção física exposta.
- `active` permite desativação por update, mas sem regra explícita.
- A FK da unidade é obrigatória e não há cascade; uma exclusão física de organização com unidades deve falhar no banco.
- Não há cascata de `active`: desativar organização não desativa unidades.
- Leituras por ID e join code não filtram organização/unidade ativa.
- O join code de uma unidade inativa continua resolvível.
- Serviços externos não têm FK, então não participam da proteção referencial.

A estratégia segura para RAG é formalizar exclusão como desativação permanente/reversível e não remover a linha. Uma chave de organização inativa deve continuar reservada. Exclusão física, se alguma vez necessária por LGPD/operação, deve ser um processo administrativo separado, com tombstone de identidade e confirmação de todos os consumidores.

### 3.8 Usuários e permissões

O `organization_api` não mantém relação JPA com usuários. Ele envia `userId`, `organizationId` e `organizationUnitId` ao endpoint interno do `user_api`.

No `user_api`:

- `User` guarda `organizationId` e `organizationUnitId` como `Long`, sem FK.
- O login emite JWT com `organizationId`, mas não `organizationUnitId` nem `organizationKey`.
- O usuário entra em uma unidade por join code e copia os dois IDs retornados pelo `organization_api`.
- Não há fluxo de saída/troca de organização nem sincronização de desativação/renomeação.
- O token pode ficar válido por 24 horas com claims antigas.

### 3.9 Upload S3 e normalização já existente

Arquivo: `organization_api/src/main/java/com/br/capoeira/eventos/organization_api/service/aws/S3Service.java`

`normalizeToSlug` hoje executa, em essência:

1. Unicode NFD.
2. Remoção de marcas combinantes.
3. `toLowerCase(Locale.ROOT)`.
4. Substituição de tudo que não seja `[a-z0-9]` por `-`.
5. Remoção de hífens das pontas.

O slug é usado em `organizations/{slug}/{slug}.{extensão}`. Riscos:

- organizações com nomes normalizados iguais escrevem no mesmo caminho de logo;
- mudança de nome altera o caminho de novos uploads, mas não move o objeto antigo;
- fallback aleatório para nome vazio é aceitável para arquivo, mas proibido para identidade;
- a lógica é privada e pode divergir da futura lógica de chave.

O Lote 2 deve criar um componente único e testado de normalização. O S3 pode usá-lo, mas o ideal para arquivos é formar o caminho com o ID imutável da organização, deixando a chave apenas como parte legível opcional.

## 4. Respostas às análises obrigatórias

### 4.1 Já existe campo equivalente a `organization_key`?

Não. O campo mais próximo é o slug temporário do `S3Service`, mas ele não é uma identidade de domínio. Nenhuma entidade, repository, DTO, endpoint, claim JWT ou mensagem RabbitMQ possui `organization_key`/`organizationKey`.

### 4.2 Como gerar a chave normalizada sem conflitos?

Proposta de contrato determinístico, a ser aprovado e congelado:

1. Rejeitar `null`, vazio e texto que resulte em chave vazia.
2. Aplicar `trim` e colapsar espaços/separadores.
3. Aplicar Unicode NFKD (ou manter NFD para compatibilidade com o slug atual; escolher uma única forma).
4. Remover marcas diacríticas.
5. Converter com `Locale.ROOT` para minúsculas.
6. Substituir qualquer sequência fora de `[a-z0-9]` por um único `-`.
7. Remover hífens nas extremidades.
8. Validar tamanho máximo definido no schema.
9. Não adicionar cidade, ID ou sufixo numérico para “resolver” colisão.

Exemplos esperados:

| Nome | Chave |
|---|---|
| `Bonfim Capoeira` | `bonfim-capoeira` |
| ` Bônfim   Capoeira ` | `bonfim-capoeira` |
| `BONFIM—CAPOEIRA` | `bonfim-capoeira` |
| `Ação & Cultura` | `acao-cultura` |

Se duas organizações gerarem a mesma chave, a segunda criação ou alteração deve ser rejeitada. A checagem programática melhora a mensagem, mas a garantia real deve ser uma constraint/índice único no PostgreSQL. Operações concorrentes devem capturar a violação do banco e retornar um erro de conflito estável, preferencialmente HTTP 409.

A regra deve ter testes de caracterização e uma versão explícita (`normalizationVersion` no contrato/evento ou, no mínimo, constante/documentação). Uma mudança futura de algoritmo não pode renomear chaves silenciosamente durante startup.

### 4.3 Como adicionar unicidade com segurança aos dados existentes?

Rollout recomendado em fases:

1. Introduzir Flyway e baselinear o schema atual.
2. Executar preflight somente leitura em cada ambiente, calculando a chave com exatamente o mesmo componente/algoritmo do código.
3. Gerar relatório de:
   - nomes que resultam em chave vazia;
   - chaves duplicadas;
   - espaços/caracteres anômalos;
   - organizações inativas;
   - candidatos Bonfim.
4. Resolver cada colisão por decisão de negócio: consolidar registros, renomear a organização correta ou manter uma organização desativada com identidade reservada. Não criar sufixos automáticos.
5. Migration A: adicionar `organization_key` nullable, sem unique.
6. Backfill controlado e auditável.
7. Verificar `NULL`, duplicidade e correspondência nome/chave.
8. Migration B: tornar a coluna `NOT NULL` e criar índice único. Para tabela grande, usar índice concorrente em migration apropriada.
9. Só então implantar entidade/service/DTO e mudar Hibernate para `validate`.

Uma migration SQL não deve inventar uma normalização diferente via `lower/regexp` ou depender de `unaccent` sem garantir equivalência byte a byte com Java. Uma opção segura é gerar previamente uma tabela/mapeamento auditável por ID e aplicá-lo na migration.

Como os dados reais não foram acessados neste lote, não é possível afirmar que o índice único pode ser criado hoje.

### 4.4 Como implementar o seed idempotente do Bonfim em todos os ambientes?

O seed deve ser individual, transacional e independente da contagem total de organizações.

Proposta:

- adicionar um identificador estável de seed, por exemplo `systemCode = BONFIM_CAPOEIRA`, único e imutável, na própria organização ou em uma tabela de registry de seeds;
- rodar um `ApplicationRunner` habilitado por propriedade em todos os ambientes;
- procurar primeiro por `systemCode`, não pelo nome mutável;
- se já existir, validar/reconciliar os campos autorizados sem criar outro registro;
- se não existir, procurar apenas candidatos definidos por uma migração/manifesto aprovado;
- se não houver candidato, criar uma única vez;
- usar transação e constraint única para suportar múltiplas instâncias simultâneas;
- falhar de forma explícita em caso de ambiguidade, em vez de escolher pelo primeiro nome parecido;
- registrar métricas/logs: `created`, `already_present`, `reconciled`, `ambiguous`, `failed`.

Não é recomendável chamar `createWithMainUnit`, porque ele exige `userId` e promove um usuário remotamente. O seed precisa de um caminho de domínio próprio. Antes de implementá-lo, é necessário decidir se o Bonfim deve nascer com unidade principal, join code e administrador, ou apenas como organização de sistema.

O seed de categorias do `event_api` fornece exemplos úteis de runner, properties, validação estrita, concorrência e testes. Sua condição `count() > 0` não deve ser reutilizada.

### 4.5 Como lidar com Bonfim já cadastrado com outro nome ou chave?

Não fazer fuzzy match automático. Os fixtures de teste já usam pelo menos dois nomes diferentes — `Grupo Bonfim` e `Grupo de Capoeira Nosso Senhor do Bonfim` — que não gerariam a mesma chave de `Bonfim Capoeira`.

Procedimento seguro:

1. O preflight lista candidatos por ID, nome atual, chave calculada, unidades, usuários e eventos relacionados.
2. Um responsável aprova, por ambiente, qual ID representa o Bonfim institucional.
3. Uma migration/manifesto associa esse ID ao `systemCode` estável.
4. Decide-se se o display name será preservado ou alterado para o nome canônico.
5. A chave é recalculada conforme a decisão e a troca é publicada como evento, se já houver consumidores.
6. Se houver mais de um candidato real, executar uma consolidação de dados antes de ativar a unicidade; jamais fundir automaticamente.

### 4.6 Como atualizar a chave sem quebrar integrações?

Nome e chave devem mudar na mesma transação do `organization_api`. A mudança deve criar no mesmo commit um evento de outbox com:

- `eventId` único;
- tipo e versão do contrato;
- `organizationId`;
- `oldOrganizationKey`;
- `organizationKey` novo;
- `aggregateVersion` monotônica;
- `active`;
- `occurredAt`.

Um publisher envia o evento ao RabbitMQ com retry e confirmação. Consumidores usam inbox/idempotência e ignoram versões antigas.

No `ai_agent_api`, o consumer localiza a base por procedência estável (`sourceOrganizationId`), atualiza apenas `knowledge_base.organization_key` e mantém o mesmo `knowledge_base.id`. Documentos e embeddings continuam apontando para esse ID interno, portanto não são copiados nem reindexados apenas por renomeação.

Durante a transição, os IDs atuais devem continuar nos contratos. `organization_key` deve ser aditivo e versionado. Remover ou reinterpretar `organizationId` quebraria usuários, eventos, processor, notificações e tópicos Firebase.

### 4.7 Como impedir apropriação após exclusão e recriação?

Somente uma unique constraint na chave atual não resolve todos os cenários. A política recomendada combina:

1. exclusão lógica de organização, preservando ID e chave;
2. chave de organização inativa continua reservada;
3. reativação do mesmo registro em vez de recriação;
4. tombstone/histórico para qualquer exclusão física excepcional;
5. `knowledge_base` guarda `source_organization_id` e uma versão/geração de ciclo de vida, além de `organization_key`;
6. evento de criação com chave já conhecida, mas outro `organizationId`, nunca assume a base antiga;
7. base excluída fica `RETIRED/DELETED`, sem ser removida automaticamente;
8. reassociação de acervo só ocorre por comando administrativo explícito, auditado e com IDs de origem/destino.

Também é recomendável uma registry de chaves históricas (`organization_key_history` ou equivalente), associando cada chave ao dono original. Deve ser decidido se uma chave antiga de uma organização renomeada pode algum dia ser reutilizada por outra. A opção mais segura é não reutilizar.

### 4.8 Alterações necessárias nos outros serviços

#### `user_api`

Obrigatórias para ciclo de vida seguro:

- consumir eventos de desativação/reativação e definir o que acontece com usuários vinculados;
- manter `organizationId` como identidade de associação durante a migração;
- impedir autorização baseada apenas em role sem validar tenant;
- definir como obter a chave atual para chamadas RAG.

Duas opções para a chave no JWT:

1. Materializar `organizationKey` no `user_api`, sincronizá-la por eventos e adicioná-la ao token. É rápida em runtime, mas tokens ficam stale até expirar/renovar.
2. Manter apenas `organizationId` no token e o `ai_agent_api` resolver a chave pela projeção local de eventos de organização. É mais robusta a renomeações e evita duplicar a chave em usuários.

A segunda opção é a recomendada. Se o frontend precisar da chave, ela pode ser retornada como dado informativo, sem virar autoridade de acesso.

O DTO local usado no join code pode aceitar `organizationKey` de forma aditiva, mas o vínculo do usuário deve continuar pelo ID. O endpoint interno de promoção também deve permanecer compatível.

#### `event_api`

- continuar persistindo `organizationId` e `organizationUnitId`;
- estender o DTO de validação de unidade para ler a chave atual, se ela for necessária na publicação;
- nunca aceitar uma chave arbitrária enviada pelo cliente como autoridade;
- para ingestão RAG de eventos, publicar `organizationId` e, opcionalmente, a chave como snapshot; o consumer deve resolver a base por procedência/ID;
- se a chave for materializada no Mongo, tratá-la como snapshot e não fazer dela a identidade primária do evento;
- adicionar contract tests para responses enriquecidos e compatibilidade com mensagens antigas.

O `event_api` hoje valida se uma unidade pertence à organização, mas não verifica se o usuário autenticado pertence à organização informada. Isso precisa ser tratado antes de usar eventos como fonte automática de documentos RAG.

#### `processor_api` e `notification_api`

Embora não tenham sido citados como alvos principais, recebem as mensagens do `event_api` e materializam `organizationId`/`organizationUnitId`. O `notification_api` constrói tópicos como `org_<organizationId>`. Esses contratos não devem ser trocados pela chave neste rollout. Se `organizationKey` for acrescentada à mensagem, os consumidores devem tolerá-la de forma aditiva.

#### `ai_agent_api`

Estado atual:

- somente endpoints stateless de chat, descrição, resumo e revisão;
- `EventSummaryService` chama apenas `AiProvider`;
- sem banco, migrations, entidades, repositories, vector store ou embeddings;
- sem RabbitMQ;
- sem OAuth2/JWT e endpoints atualmente públicos;
- sem Dockerfile/Compose e fora das matrizes de build/coverage do GitHub Actions.

Necessário:

- banco próprio e migrations próprias;
- tabela `knowledge_base` com ID próprio;
- `organization_key` como vínculo lógico, sem FK cross-service;
- coluna de procedência `source_organization_id` e versão/geração para impedir apropriação;
- status da base (`ACTIVE`, `INACTIVE`, `RETIRED` ou equivalente);
- documentos e embeddings vinculados por `knowledge_base_id`;
- índices/uniqueness para chave atual e procedência;
- consumer idempotente de eventos de organização;
- inbox/event version para deduplicação e ordenação;
- segurança JWT e autorização por organização;
- filtro obrigatório por `knowledge_base_id` em toda busca vetorial;
- Compose, health checks, métricas e CI.

### 4.9 Existe RabbitMQ reutilizável?

Sim, no nível de infraestrutura e padrões locais:

- broker no Compose local/homolog/prod;
- `spring-boot-starter-amqp` em `event_api` e `user_api`;
- `RabbitTemplate`, Jackson converter, exchanges/queues e `RabbitAdmin`;
- consumers com `@RabbitListener`;
- testes com `RabbitMQContainer` e `spring-rabbit-test`.

Não existe:

- infraestrutura AMQP dentro do `organization_api` ou `ai_agent_api`;
- módulo compartilhado de contratos;
- schema registry;
- outbox/inbox genérico;
- evento de organização;
- DLQ/retry padronizado para esse novo fluxo.

Recomendação: reutilizar broker, variáveis, Jackson e estratégia de Testcontainers, mas criar contrato versionado de organização e transactional outbox. Copiar o padrão atual de “salvar e chamar `convertAndSend`” não oferece a garantia necessária para renomear uma base RAG sem duplicar acervo.

## 5. Configuração por ambiente

### Local

O Compose base configura PostgreSQL, RabbitMQ e os serviços. O overlay `docker-compose.local.yml` corrige `USER_API_URL_BASE` do `organization-api` para `http://user-api:8082` e adiciona S3 local.

### Homologação e produção

Os arquivos `docker-compose.homolog.yml` e `docker-compose.prod.yml` configuram DB e JWT via env file, porém o serviço `organization-api` não define `USER_API_URL_BASE`. Assim, ele usa o default `http://localhost:8082`; dentro do container, isso aponta para o próprio container, não para `user-api`. A criação de organização com promoção de usuário tende a falhar nesses ambientes, salvo configuração externa não versionada.

### Lacunas comuns

- nenhum profile Spring específico por ambiente no `organization_api`;
- mesmo `ddl-auto=update` em todos os ambientes;
- seed do Bonfim inexistente;
- `organization-api` sem RabbitMQ e sem dependência do broker no Compose;
- `ai_agent_api` ausente do Compose e do init de bancos;
- CI atual cobre cinco serviços Java 17, mas não `ai_agent_api`, que usa Java 21.

## 6. Riscos encontrados

| Prioridade | Risco | Impacto |
|---|---|---|
| Crítica | Sem unicidade de nome/chave | Organizações diferentes podem convergir para a mesma base RAG |
| Crítica | Chave usada sem procedência estável | Recriação pode assumir acervo antigo |
| Crítica | Sem isolamento por tenant nas autorizações atuais | Usuário administrativo pode alterar dados de outra organização |
| Crítica | `ddl-auto=update` sem migrations | Mudança de schema não auditável e backfill inseguro |
| Alta | Dados reais não pré-auditados | Unique index pode falhar durante deploy |
| Alta | Chamada ao `user_api` dentro de transação local | Estado distribuído inconsistente |
| Alta | Sem outbox/idempotência/ordenação | Evento de rename pode ser perdido, duplicado ou aplicado fora de ordem |
| Alta | Desativação sem cascata de regras | Usuários, unidades, eventos e RAG permanecem utilizáveis |
| Alta | Join code resolve unidade inativa | Usuário pode entrar em organização desativada |
| Alta | Seed por nome seria instável | Rename do Bonfim poderia criar duplicata no restart |
| Alta | `ai_agent_api` sem segurança | Futuro acesso RAG poderia vazar dados entre organizações |
| Média | Slug S3 colide por nome | Logos podem sobrescrever caminhos e divergir após rename |
| Média | JWT de 24h | Claims organizacionais ficam stale após mudanças |
| Média | Contratos duplicados entre serviços | Evolução de DTO pode divergir silenciosamente |
| Média | AI fora do CI/Compose | Alterações RAG não são validadas no pipeline atual |

## 7. Proposta de implementação — Lotes 2 a 5

### Lote 2 — Identidade, migrations e seed no `organization_api`

Objetivo: tornar a organização uma fonte de verdade segura para chave e ciclo de vida.

Escopo proposto:

- adotar Flyway e baseline;
- executar e aprovar preflight de dados por ambiente;
- criar componente de normalização versionado;
- adicionar/backfillar `organization_key` em duas fases;
- adicionar unique index e `NOT NULL`;
- adicionar lookup por chave e conflito HTTP estável;
- recalcular a chave em create/update, sem aceitar valor externo;
- expor a chave apenas nos responses necessários;
- formalizar deactivate/reactivate e bloquear delete físico comum;
- criar history/tombstone de chaves, conforme decisão aprovada;
- implementar `systemCode`/registry e seed idempotente do Bonfim;
- corrigir configuração `USER_API_URL_BASE` em homolog/prod;
- mudar JPA para `validate` após as migrations.

Não incluir ainda RAG nem consumer AI.

### Lote 3 — Eventos de organização e compatibilidade entre serviços

Objetivo: propagar mudanças sem romper contratos existentes.

Escopo proposto:

- transactional outbox no `organization_api`;
- exchange versionada e envelope de evento;
- eventos create, key-changed, deactivated e reactivated;
- publisher com retry, confirmação e observabilidade;
- inbox/idempotência nos consumidores;
- estratégia de tenant no `user_api` e autorização nos serviços;
- enriquecimento aditivo de DTOs/contratos no `user_api` e `event_api`;
- manter IDs atuais em event/processor/notification;
- definir comportamento de usuários e eventos quando organização é desativada;
- contract tests de compatibilidade.

### Lote 4 — Persistência RAG no `ai_agent_api`

Objetivo: criar bases independentes e seguras por organização.

Escopo proposto:

- banco próprio do `ai_agent_api` e Flyway;
- `knowledge_base` com ID interno, chave, procedência e status;
- tabelas de documento/chunk/embedding ou integração de vector store com `knowledge_base_id` obrigatório;
- consumer dos eventos de organização;
- rename atualiza a mesma base;
- desativação bloqueia consulta/ingestão sem apagar acervo;
- recriação nunca reassocia automaticamente;
- APIs de ingestão/consulta protegidas por JWT e tenant;
- filtro de busca vetorial por ID da base;
- integração no Compose, observabilidade e CI Java 21.

### Lote 5 — Rollout, reconciliação e hardening E2E

Objetivo: validar a operação completa e preparar produção.

Escopo proposto:

- ensaio das migrations com cópias anonimizadas de cada ambiente;
- rollout na ordem: organization schema/app → consumers compatíveis → AI schema/app → ativação de publishers;
- job de reconciliação `organization_api` × `ai_agent_api`;
- replay seguro de outbox/DLQ;
- testes de concorrência, falhas, duplicidade e mensagens fora de ordem;
- testes de isolamento entre organizações;
- métricas e alertas de lag, DLQ, conflito de chave, seed e base sem organização;
- runbooks de rename, deactivate/reactivate, restauração e merge administrativo;
- remoção de compatibilidade antiga somente após janela aprovada.

## 8. Critérios de aceitação e testes recomendados

### Lote 2

Critérios:

- toda organização tem chave não nula e única;
- chave é sempre derivada pelo mesmo normalizador;
- mesma chave normalizada é rejeitada independentemente das unidades/cidades;
- rename atualiza nome e chave atomicamente;
- conflito concorrente não cria duplicata;
- Bonfim existe exatamente uma vez após qualquer quantidade de startups;
- organização inativa não libera a chave;
- Hibernate não altera schema automaticamente.

Testes:

- unitários tabulares do normalizador: acentos, caixa, espaços, pontuação, dash Unicode, caracteres somente inválidos e limite;
- service tests para create/update, chave inalterada quando normalização não muda e conflito quando muda;
- repository/Testcontainers para unique constraint e concorrência;
- migration tests desde baseline, incluindo dataset com duplicatas que deve falhar no preflight;
- seed em banco vazio, banco com outras organizações, seed repetido e duas instâncias concorrentes;
- Bonfim exato, alias aprovado, candidato ambíguo e candidato conflitante;
- controller/contract tests para response com chave e HTTP 409;
- testes de deactivate/reactivate e bloqueio de join code inativo.

### Lote 3

Critérios:

- toda alteração relevante gera exatamente um registro de outbox no mesmo commit;
- falha do broker não perde alteração;
- consumidores são idempotentes;
- evento antigo não desfaz rename novo;
- contratos antigos continuam funcionando durante a janela de migração;
- acesso administrativo respeita organização do usuário.

Testes:

- transação rollback não deixa outbox órfã;
- retry e publisher confirm;
- duplicidade e ordem invertida por `aggregateVersion`;
- RabbitMQ Testcontainers com filas separadas por consumer;
- contract tests entre organization/user/event/AI;
- token antigo após rename conforme estratégia aprovada;
- desativação propagada para usuários e validação de eventos;
- teste negativo de usuário de organização A operando sobre B.

### Lote 4

Critérios:

- cada organização ativa resolve uma única `knowledge_base`;
- documentos e embeddings usam somente `knowledge_base_id` como ownership interno;
- rename preserva o ID da base e o número de documentos/embeddings;
- delete/deactivate não transfere acervo;
- nova organização com outro ID nunca assume base antiga, mesmo com mensagem repetida ou chave reapresentada;
- nenhuma consulta retorna chunks de outra organização;
- endpoints RAG exigem autenticação e tenant válido.

Testes:

- repository/migration com PostgreSQL/pgvector Testcontainers;
- consumer create/rename/deactivate/reactivate, duplicado e fora de ordem;
- rename com documentos existentes sem cópia/reembedding;
- tentativa de apropriação com `sourceOrganizationId` divergente;
- busca vetorial com dados semanticamente similares em duas bases e assert de isolamento;
- autorização cruzada A→B negada;
- recuperação de restart e replay de inbox;
- integração do `EventSummaryService` permanece independente do RAG, salvo decisão explícita de produto.

### Lote 5

Critérios:

- migrations aprovadas executam em dados representativos;
- Bonfim aparece uma vez em local, homologação e produção;
- rename E2E converge em todos os serviços sem duplicar acervo;
- falha temporária do RabbitMQ converge após recuperação;
- dashboards detectam backlog, DLQ, divergência e conflito;
- runbooks foram ensaiados.

Testes:

- E2E multi-serviço com PostgreSQL, MongoDB e RabbitMQ;
- chaos test desligando broker durante rename;
- reconciliação após evento deliberadamente perdido;
- carga/índice para lookup de chave e filtro vetorial;
- smoke tests de cada Compose/ambiente;
- rollback compatível de aplicação sem rollback destrutivo de schema;
- validação da matriz CI incluindo Java 21 para `ai_agent_api`.

## 9. Estrutura de testes existente

Contagem estática aproximada por `@Test`:

| Serviço | Arquivos de teste Java | Métodos `@Test` |
|---|---:|---:|
| `organization_api` | 8 | 41 |
| `user_api` | 6 | 67 |
| `event_api` | 21 | 169 |
| `ai_agent_api` | 11 | 50 |

No `organization_api` há:

- `OrganizationServiceTest` com Mockito;
- slice MVC do controller com MockMvc e segurança;
- repositories com PostgreSQL Testcontainers e `create-drop`;
- testes do S3.

Lacunas relevantes:

- não há teste de `UserClient` no `organization_api`;
- não há teste da transação distribuída/rollback após promoção remota;
- não há teste de nome duplicado, normalização ou concorrência de organização;
- não há seed nem testes de startup;
- não há RabbitMQ no serviço;
- não há teste de deactivate/delete/recreate;
- não há teste de autorização por tenant;
- os testes chamados “IntegrationTest” do controller são `@WebMvcTest`, não integração com banco/HTTP real.

O `event_api` já oferece referências úteis de RabbitMQ Testcontainers e testes de seed. O `ai_agent_api` possui boa cobertura unitária dos endpoints stateless atuais, mas nenhum teste de banco, segurança, RAG ou organização.

## 10. Arquivos e classes envolvidos

### `organization_api`

- `model/Organization.java`
- `model/OrganizationUnit.java`
- `repository/OrganizationRepository.java`
- `repository/OrganizationUnitRepository.java`
- `service/OrganizationService.java`
- `service/InternalServiceTokenProvider.java`
- `service/aws/S3Service.java`
- `controller/OrganizationController.java`
- `mapper/OrganizationMapper.java`
- `restClient/UserClient.java`
- todos os DTOs `Organization*`, `MainUnitDto`, `PromoteToSuperAdminDtoRequest` e `UserResponseDto`
- `config/SecurityConfig.java`
- `config/RestClientConfig.java`
- `config/exception/GlobalHandlerException.java`
- `resources/application.properties`
- `pom.xml`
- testes de service/controller/repositories/S3 e `utils/MockUtils.java`

### `user_api`

- `model/User.java`
- `service/UserService.java`
- `service/JwtService.java`
- `repository/UserRepository.java`
- `restClient/OrganizationClient.java`
- `dto/OrganizationResponseDto.java`
- `dto/UserResponseDto.java`
- `dto/PromoteToSuperAdminDtoRequest.java`
- `controller/InternalUserController.java`
- `config/RabbitMqConfig.java`
- `resources/application.properties`

### `event_api`

- `model/Event.java`
- `service/EventService.java`
- `restClient/OrganizationClient.java`
- `dto/OrganizationResponseDto.java`
- DTOs `EventCreateRequestDto`, `EventUpdateRequestDto`, `EventResponseDto`
- `config/RabbitMqConfig.java`
- `producer/EventProducer.java`
- seed de categorias e seus testes como referência
- `resources/application.properties`

### Outros

- `processor_api`: entidade/DTOs/serviço de processamento de evento com IDs organizacionais.
- `notification_api`: DTOs e `NotificationEventService`, que usa tópicos `org_<organizationId>`.
- `ai_agent_api`: `pom.xml`, `application.yml`, services/controllers/providers e testes; atualmente sem RAG.
- `docker-compose.yml`, `docker-compose.local.yml`, `docker-compose.homolog.yml`, `docker-compose.prod.yml`.
- `docker/postgres/init/01-init-databases.sql`.
- `.github/workflows/build.yml` e `.github/workflows/coverage.yml`.

## 11. Decisões que precisam de aprovação

1. Especificação exata do normalizador: NFD ou NFKD, charset permitido e tamanho máximo.
2. Nome e chave canônicos do Bonfim. `Bonfim Capoeira`, `Grupo Bonfim` e `Grupo de Capoeira Nosso Senhor do Bonfim` produzem chaves distintas.
3. ID candidato do Bonfim em cada ambiente e política para múltiplos candidatos.
4. Campos obrigatórios do seed: organização apenas ou também unidade principal, join code e administrador.
5. Uso de `systemCode` na organização ou de uma tabela separada de seed registry.
6. Política de colisões legadas: merge, rename ou desativação; nunca sufixo automático.
7. Semântica HTTP para chave duplicada, recomendada como 409.
8. Exclusão: confirmar soft delete como regra padrão e se chaves históricas nunca poderão ser reutilizadas.
9. Aprovar `source_organization_id` e geração/versão no `knowledge_base` como proteção adicional à chave lógica.
10. Estratégia de chave no JWT: materializada no `user_api` ou resolvida no `ai_agent_api`; recomendação por resolução/projeção local.
11. Contrato e versionamento dos eventos de organização e adoção de outbox/inbox.
12. Comportamento de unidades, usuários, eventos e RAG ao desativar/reativar uma organização.
13. Escopo de autorização: `SUPER_ADMIN` global versus administrador restrito ao tenant.
14. Banco/vector store do `ai_agent_api`, recomendando PostgreSQL próprio com pgvector se compatível com a operação desejada.
15. Estratégia de paths S3 após rename, recomendando ownership por ID imutável.
16. Ordem de rollout e janela de compatibilidade entre contratos antigos e novos.

## 12. Recomendação final

Não avançar diretamente para entidade + unique column em produção. O próximo lote deve primeiro aprovar as decisões acima, introduzir migrations versionadas e executar o preflight dos dados reais.

A regra central recomendada é:

> `organization_key` é um endereço lógico mutável e único; `organization.id` identifica a organização de origem; `knowledge_base.id` identifica permanentemente o acervo. Mudanças de chave alteram o endereço da mesma base, nunca o ID da base, e nenhuma chave isolada autoriza transferência de ownership.

O Lote 2 só deve começar após autorização explícita.
