# Agente QA local com Ollama

Este agente lê diretamente `../docs/qa-agent-knowledge` e confere cada rota selecionada com os controllers Java. O catálogo atual contém 47 endpoints HTTP, 19 contratos RabbitMQ e 41 cenários. `processor_api` e `notification_api` não expõem endpoints de negócio no catálogo; suas verificações funcionais precisam de evidências das projeções e integrações em ambiente isolado.

## Implementação incremental

1. **Disponível agora:** validar contratos e Markdown, inventariar os cinco serviços, pedir propostas ao Ollama com IDs reais, executar apenas três GETs de categorias sem JWT e produzir Excel/PDF. O status 401 desses casos é hipótese da cadeia de segurança; divergência é `INCONCLUSIVO / DOCUMENTACAO`, não falha comprovada da API.
2. **Disponível para os GETs aprovados:** sondagens assíncronas com JSON Pointer e valor esperado. A sondagem usa um dos três pares de ID e caminho já aprovados, sem credenciais.
3. **Próximo incremento:** observadores de PostgreSQL, MongoDB, RabbitMQ e Firestore com credenciais somente leitura, correlação por `transactionId`, prazo por etapa e comprovante de projeto Firebase de teste. Depois, liberar cenários mutantes em containers descartáveis com dados de teste e limpeza por namespace. Mensagens RabbitMQ/FCM não são consideradas verificadas apenas porque uma fila ficou vazia.

## Instalação e comandos

Crie um ambiente Python dentro desta pasta e instale `requirements.txt`. O comando `validate` não precisa dessas dependências e não acessa a rede.

```powershell
python -m venv .venv
.venv\Scripts\python.exe -m pip install -r requirements.txt
.venv\Scripts\python.exe qa_agent.py validate
.venv\Scripts\python.exe qa_agent.py inventory
.venv\Scripts\python.exe qa_agent.py propose
.venv\Scripts\python.exe qa_agent.py report
.venv\Scripts\python.exe qa_agent.py run
```

`propose` usa `qwen2.5-coder:3b` em `http://127.0.0.1:11434`. Aceita `--model` e `--ollama-url`, sempre no loopback. O Ollama recebe um inventário compacto e um JSON Schema com os IDs reais. Se o servidor rejeitar explicitamente o schema, o agente tenta o modo JSON com a mesma validação local.

O agente preserva propostas válidas e faz até duas chamadas adicionais para corrigir as demais. Os diagnósticos vão para stderr. O arquivo `proposals.json` contém até dez propostas, todas com `REVIEW_REQUIRED`, e os hashes da documentação. Se a geração terminar sem propostas válidas, o arquivo contém uma lista vazia e o comando sai com código 2. Somente os casos de `approved-tests.json` alimentam `run`.

`report` lê o `proposals.json` existente e cria `reports/qa-report.pdf`. O documento apresenta as propostas como material para revisão humana: `REVIEW_REQUIRED` significa aguardando revisão, não teste aprovado. O relatório também deixa explícito que `propose` não executa testes HTTP e que as evidências listadas são expectativas, não resultados observados. Modelo e métricas de geração ausentes do JSON são exibidos como `Não disponível`.

`run` é o fluxo autônomo completo. Ele valida a documentação, inventaria os cinco serviços, consulta o Ollama para gerar propostas, carrega separadamente os três casos autorizados, executa somente esses GETs, solicita ao Ollama um diagnóstico consultivo e cria `reports/qa-run-report.pdf`. As propostas permanecem `REVIEW_REQUIRED` e nunca alimentam o executor HTTP. O diagnóstico da IA também não pode alterar status, latência, evidências ou resultados calculados pelo agente.

Se `environment.json` não existir, `run` usa automaticamente `environment.example.json`, que declara somente URLs HTTP loopback e `isolated: true`. Um arquivo informado por `--environment` continua sujeito à mesma validação. Falhas de ambiente deixam os testes como bloqueados e ainda produzem o PDF. Se o Ollama estiver indisponível, os testes determinísticos continuam e o relatório registra a indisponibilidade.

```powershell
.venv\Scripts\python.exe qa_agent.py run
```

`observe` exige um `observations.json` com um dos pares `endpoint_id` e `path` já aprovados, além de `json_pointer`, `equals` e `timeout_seconds`. Execute `python qa_agent.py observe --environment environment.json`. O mesmo requisito de ambiente isolado vale para a sondagem.

O fluxo autônomo grava status HTTP, latência, resposta observada com campos sensíveis removidos, evidência real e classificação de cada caso. Seu código de saída é `0` quando testes e etapas do Ollama terminam sem falhas, e `1` quando há reprovação, bloqueio, resultado inconclusivo ou indisponibilidade do Ollama; o PDF é gerado nos dois casos. Erros que impedem até a validação inicial usam código `2`. O comando `observe` mantém os relatórios legados Excel/PDF por execução.

Não execute `GET /api/v1/evento/all` como leitura comum: o contrato registra publicação de mensagem e sincronização. Nenhum POST, PUT, PATCH ou DELETE é implementado no executor atual. Os testes simulados em `tests/` não acessam serviços.
