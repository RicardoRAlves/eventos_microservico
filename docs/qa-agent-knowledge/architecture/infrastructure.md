# Infraestrutura e ambientes

```yaml
document_id: QAK-ARCH-003
service: all
document_type: infrastructure
related_endpoints: []
related_business_rules: [BR-MSG-001, BR-CAT-006]
source_files: [docker-compose.yml, docker-compose.local.yml, docker-compose.homolog.yml, docker-compose.prod.yml, docker/postgres/init/01-init-databases.sql]
last_analyzed: 2026-11-09
confidence: high
```

O Compose base declara MongoDB, RabbitMQ 3.10.5-management, PostgreSQL 15.7 e os cinco serviços. O SQL de inicialização cria `eventos_api`, `organization_api` e `user_api`. `event_api` usa MongoDB; os outros bancos relacionais pertencem a processor, organization e user, respectivamente.

`docker-compose.local.yml` adiciona replica set Mongo (necessário às transações do seed) e S3 compatível RustFS em `localhost:4566`; também fixa o Firebase esperado em projeto de homologação. `docker-compose.homolog.yml` igualmente usa Firebase esperado de homologação. `docker-compose.prod.yml` fixa um projeto Firebase distinto de produção. A validação de project ID no `FirebaseCredentialResolver` reduz, mas não elimina, o risco de credencial cruzada.

Todos os serviços JPA usam `spring.jpa.hibernate.ddl-auto=update`: o esquema pode ser alterado automaticamente na inicialização. Isso impede classificar testes que iniciem serviços contra bancos não descartáveis como seguros/READY.

Riscos de ambiente:

- arquivos `.env` e um CSV de access key existem no repositório; não foram reproduzidos;
- Compose publica Mongo, PostgreSQL e RabbitMQ management no host;
- Compose base/prod usa imagens `mongo:latest`, reduzindo reprodutibilidade;
- nomes `homolog`/`prod` não isolam por si só S3, PostgreSQL ou RabbitMQ; isolamento depende dos valores secretos externos;
- homologação configura explicitamente o Firebase esperado, mas a correspondência real de S3/bancos/filas não pode ser confirmada sem revelar/testar credenciais;
- credenciais default locais aparecem em propriedades/Compose e não são adequadas a ambientes compartilhados.

Pré-requisitos de QA: Docker, portas 8080-8084/27017/5433/5672/15672/4566 livres conforme perfil, bancos descartáveis, RabbitMQ isolado, bucket local e um projeto/emulador Firebase não produtivo. Não há Firebase Emulator configurado.

