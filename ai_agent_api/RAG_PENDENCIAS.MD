# Cultura Conecta — Pendências do RAG

**Microserviço:** `ai_agent_api`  
**Stack:** Java 21, Spring Boot, Spring AI, Ollama, PostgreSQL e pgvector  
**Modelo de embeddings:** `nomic-embed-text` (768 dimensões)

## 1. Estado atual

- [x] Entidades `KnowledgeBase`, `KnowledgeDocument` e `KnowledgeChunk`
- [x] Repositories JPA e consultas vetoriais
- [x] `KnowledgeEmbeddingService`
- [x] `KnowledgeSearchService`
- [x] `KnowledgeIngestionService`
- [x] Hash SHA-256 na ingestão
- [x] Endpoint `POST /api/v1/knowledge/ask`
- [ ] Endpoint HTTP de ingestão de texto
- [ ] Validar ingestão completa com PostgreSQL e Ollama

## 2. Ingestão de texto — prioridade imediata

- [ ] Confirmar/criar `KnowledgeTextIngestionRequest`
- [ ] Criar `POST /api/v1/knowledge/documents/text`
- [ ] Retornar HTTP 201 com `documentId`
- [ ] Testar ingestão pelo Swagger
- [ ] Verificar registros nas três tabelas
- [ ] Validar geração e armazenamento dos embeddings
- [ ] Repetir a pergunta sobre o berimbau
- [ ] Confirmar resposta com fontes

## 3. Melhorias da ingestão

- [ ] Impedir documentos duplicados usando `contentHash`
- [ ] Normalizar `organizationKey` e `title`
- [ ] Revisar o chunking em `KnowledgeProcessingService`
- [ ] Validar rollback em falhas do Ollama
- [ ] Tratar concorrência na criação da base
- [ ] Confirmar configuração das datas automáticas
- [ ] Validar vetores com valores inválidos (NaN/Infinito)
- [ ] Adicionar testes unitários e de integração

## 4. Ingestão de arquivos

- [ ] Implementar upload de PDF
- [ ] Extrair texto preservando páginas e referências
- [ ] Suportar PDFs digitalizados com OCR posteriormente
- [ ] Implementar ingestão de Excel (XLSX)
- [ ] Preservar abas, cabeçalhos e estrutura das planilhas
- [ ] Adicionar DOCX, TXT e Markdown
- [ ] Definir limites de tamanho e formatos permitidos
- [ ] Reutilizar o processamento central de documentos

## 5. Gestão do conhecimento

- [ ] Listar documentos por organização
- [ ] Atualizar documentos
- [ ] Excluir documentos e embeddings associados
- [ ] Reindexar documentos
- [ ] Tratar versões e duplicidades
- [ ] Melhorar metadados e rastreabilidade das fontes

## 6. Visibilidade e isolamento

**Decisão atual:** manter o comportamento existente de `PUBLIC`, `SHARED` e `PRIVATE`.

- [ ] Documentar formalmente as regras de visibilidade
- [ ] Revisar o compartilhamento entre organizações
- [ ] Validar o isolamento de informações privadas
- [ ] Avaliar uma base global de conhecimento cultural

## 7. Segurança — etapa final, antes da publicação

**Decisão:** somente `SUPER_ADMIN` poderá realizar ingestão inicialmente.

- [ ] Configurar autenticação JWT
- [ ] Restringir endpoints de ingestão ao `SUPER_ADMIN`
- [ ] Validar autorização de leitura por organização
- [ ] Implementar permissões mais granulares futuramente
- [ ] Proteger uploads contra arquivos maliciosos
- [ ] Validar os cenários HTTP 401 e 403
- [ ] Impedir exposição pública dos endpoints antes da proteção

## 8. Critério de conclusão do primeiro marco

O primeiro marco estará concluído quando:

1. Um texto for enviado pelo Swagger.
2. A base, o documento e seus chunks forem persistidos.
3. Os embeddings forem gerados pelo Ollama.
4. A pergunta sobre o berimbau recuperar o documento.
5. O RAG produzir uma resposta fundamentada e identificar a fonte.

**Prioridade atual:** concluir a ingestão de texto antes de implementar PDF, Excel ou novas funcionalidades de agentes.