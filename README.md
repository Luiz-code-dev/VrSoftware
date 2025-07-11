# Sistema de Pedidos Desktop Assíncrono

Sistema distribuído composto por uma aplicação desktop Java Swing que se comunica com um backend Spring Boot através de REST, utilizando RabbitMQ para processamento assíncrono de pedidos.

## Arquitetura

```
[GUI Java Swing] -> HTTP -> [Spring Boot] -> RabbitMQ -> [Consumidor]
                                                     -> [DLQ]
                                                     -> [Status]
```

## Pré-requisitos

- Java 17+
- Maven 3.8+

## RabbitMQ

O sistema utiliza um servidor RabbitMQ já configurado e disponível para o teste prático. A configuração está definida no arquivo `application.yml` do backend.

> Nota: Um docker-compose.yml está incluído apenas para referência de desenvolvimento local, mas não é necessário para executar o sistema.

## Build e Execução

### Backend (Spring Boot)

```bash
cd pedidos-backend
mvn clean package
java -jar target/pedidos-backend-1.0.0.jar
```

O backend estará disponível em http://localhost:8080

### GUI (Java Swing)

```bash
cd pedidos-gui
mvn clean package
java -jar target/pedidos-gui-1.0.0.jar
```

## Variáveis de Ambiente (Backend)

O backend pode ser configurado através das seguintes variáveis de ambiente:

```bash
RABBITMQ_HOST=137.131.204.96
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=vrsoftware
RABBITMQ_PASSWORD=6uOVxR6dy1H7Zqfz
RABBITMQ_VIRTUAL_HOST=/
```

## Filas RabbitMQ

- `pedidos.entrada.luiz-eduardo`: Fila principal de pedidos
- `pedidos.entrada.luiz-eduardo.dlq`: Dead Letter Queue
- `pedidos.status.sucesso.luiz-eduardo`: Status de sucesso
- `pedidos.status.falha.luiz-eduardo`: Status de falha

## Endpoints REST

### POST /api/pedidos
Cria um novo pedido
```json
{
  "id": "UUID",
  "produto": "string",
  "quantidade": 1,
  "dataCriacao": "2025-07-11T18:00:00"
}
```

### GET /api/pedidos/status/{id}
Consulta o status de um pedido

## Funcionalidades

### Backend
- Validação de pedidos
- Processamento assíncrono
- Simulação de falhas (20% chance)
- DLQ para mensagens com erro
- Status tracking em memória

### GUI
- Interface desktop intuitiva
- Polling assíncrono de status
- Tratamento de erros
- Atualizações em tempo real

## RabbitMQ Local (Opcional)

Se desejar executar o RabbitMQ localmente:

```bash
docker-compose up -d
```

## Testes

Execute os testes unitários com:

```bash
cd pedidos-backend
mvn test
```

## Checklist de Testes Manuais

1. Backend:
   - [ ] Verificar se o servidor iniciou (GET http://localhost:8080/actuator/health)
   - [ ] Testar POST /api/pedidos com payload válido
   - [ ] Testar validações (quantidade <= 0, produto vazio)
   - [ ] Verificar criação das filas no RabbitMQ Management

2. GUI:
   - [ ] Verificar se a interface abre corretamente
   - [ ] Testar envio de pedido
   - [ ] Verificar atualização de status
   - [ ] Testar tratamento de erros (backend offline)

## Melhorias Futuras

1. Backend:
   - Persistência de dados
   - Métricas e monitoramento
   - Documentação OpenAPI/Swagger

2. GUI:
   - Filtros e busca na tabela de status
   - Exportação de dados
   - Temas visuais
