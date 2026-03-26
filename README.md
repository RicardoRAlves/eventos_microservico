# eventos_microservico
Projeto para gerenciar o micro serviço de eventos da capoeira composto por uma 
Rest API que recebe a solicitação para adicionar novos eventos, uma api para transação com a base de dados e guardar todos os eventos ja registrados e um microserviço para notificar o android sobre eventos novos

Evento Pedido API:
- Recebe uma solicitação via endpoint para adicionar no AWS S3
- Faz a solicitação para adicionar um novo event
- Realização uma solicitação para enviar todos eventos para o android/client

Evento Banco Api:
- Atraves de uma fila do rabbitMQ faz a adição na base de dados de um novo event
- Atraves de uma fila faz o envio dos eventos para os clients

Evento Notificacao API:
- Essa API recebe um pedido de um ou mais eventos como payload e se comunica com os clients/android para notifica-los

# Visando geral do projeto
![diagram.png](diagram.png)
