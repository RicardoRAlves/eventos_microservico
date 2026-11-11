# Autenticação e autorização

```yaml
document_id: QAK-API-002
service: all
document_type: authentication
related_endpoints: [EP-USR-001, EP-USR-014, EP-EVT-002, EP-ORG-005]
related_business_rules: [BR-USR-002, BR-USR-003, BR-USR-007]
source_files: [user_api/src/main/java/com/br/capoeira/eventos/user_api/config/SecurityConfig.java, user_api/src/main/java/com/br/capoeira/eventos/user_api/config/filter/JwtAuthenticationFilter.java, user_api/src/main/java/com/br/capoeira/eventos/user_api/service/JwtService.java, event_api/src/main/java/com/br/capoeira/eventos/event_api/config/SecurityConfig.java, organization_api/src/main/java/com/br/capoeira/eventos/organization_api/config/SecurityConfig.java]
last_analyzed: 2026-11-09
confidence: high
```

`user_api` autentica email/senha pelo `AuthenticationManager` e emite JWT HMAC. Claims: `sub=email`, `roles=[role]`, `userId`, `organizationId` quando presente, `iat`, `exp`. Expiração configurada: 86.400.000 ms. `User.isEnabled()` retorna `active`, portanto o login de conta inativa é rejeitado pelo provider de autenticação; o status/formato HTTP exato não é customizado.

O filtro aceita token SERVICE sem consultar banco; para usuários comuns carrega o usuário por email e valida assinatura, expiração e subject. `JwtService.isTokenValid` não consulta `isEnabled`, logo um JWT emitido antes da desativação continua autenticando até expirar. Exceções do filtro são engolidas e o contexto é limpo, levando a resposta de não autenticado pela cadeia.

`event_api` e `organization_api` usam Resource Server com a mesma família de segredo HMAC e convertem `roles` para `ROLE_*`. A compatibilidade efetiva depende de todos os ambientes receberem o mesmo `JWT_SECRET`.

Matriz de acesso:

| Área | Público | Autenticado | ADMIN | SUPER_ADMIN | SERVICE |
|---|---|---|---|---|---|
| login/cadastro/upload avatar | sim | sim | sim | sim | não necessário |
| categorias | não | não | sim | sim | não |
| eventos create/update/delete/upload | não | não | sim | sim | não |
| item de venda | não | sim | sim | sim | tecnicamente sim se JWT aceito |
| organização create/read/join lookup/upload | não | sim | sim | sim | tecnicamente autenticado |
| criar unidade/editar organização | não | não | não | sim | não |
| editar unidade | não | não | sim | sim | não |
| favoritos/reservas/perfil | não | sim | sim | sim | tecnicamente sim |
| promoção interna | não | não | não | não | sim |

Riscos: ausência de checagem de ownership em vários endpoints; ADMIN não é limitado por código à própria unidade/organização; token SERVICE é aceito com base apenas no claim e assinatura compartilhada; CORS permite somente localhost 4200/5173 e sem credentials.

