# aws-s3-assurance

Spring Boot API focused on S3 file operations with AWS SDK for Java v2 and support for console login credentials via `aws login`.

## Features

- health endpoints for local and cloud checks
- list folders and files from an S3 bucket
- upload files to S3 with an optional prefix
- download files from S3
- inspect object metadata
- CRUD de usuarios em PostgreSQL/RDS com propriedades `id` e `nome`
- envio basico de mensagens para SQS
- fluxo assíncrono de CEP com RDS + SQS
- configuration through environment variables instead of hardcoded account values

## Stack

- Java 21
- Spring Boot 3.5
- Maven Wrapper
- AWS SDK v2 (`s3` and `signin`)

## Configuration

Set these environment variables before running:

```powershell
$env:AWS_REGION="us-east-2"
$env:AWS_S3_BUCKET="your-bucket-name"
$env:AWS_SQS_QUEUE_URL="https://sqs.us-east-2.amazonaws.com/123456789012/your-queue"
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://database-1.cja44802coa4.us-east-2.rds.amazonaws.com:5432/postgres"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="your-password"
```

Authentication can come from:

- `aws login`
- `aws configure sso`
- standard AWS environment variables

## Running locally

```powershell
./mvnw spring-boot:run
```

Default URL:

```text
http://localhost:8080
```

## Endpoints

- `GET /health`
- `GET /actuator/health`
- `GET /s3/list?prefix=folder/`
- `POST /s3/upload?prefix=folder/`
- `GET /s3/file?key=folder/file.txt`
- `GET /s3/metadata?key=folder/file.txt`
- `POST /sqs/messages`
- `GET /ceps`
- `GET /ceps/{procedureId}`
- `POST /ceps`
- `GET /users`
- `GET /users/{id}`
- `POST /users`
- `PUT /users/{id}`
- `DELETE /users/{id}`

## Example requests

```powershell
Invoke-WebRequest http://localhost:8080/health
Invoke-WebRequest "http://localhost:8080/s3/list?prefix="
```

Upload:

```powershell
curl.exe -X POST "http://localhost:8080/s3/upload?prefix=docs/" -F "file=@C:\temp\example.txt"
```

Create user:

```powershell
curl.exe -X POST "http://localhost:8080/users" -H "Content-Type: application/json" -d "{\"id\":\"1\",\"nome\":\"Lincoln\"}"
```

Send SQS message:

```powershell
curl.exe -X POST "http://localhost:8080/sqs/messages" -H "Content-Type: application/json" -d "{\"messageBody\":\"{\\\"procedureId\\\":\\\"123\\\",\\\"cep\\\":\\\"30140071\\\"}\"}"
```

Create CEP request:

```powershell
curl.exe -X POST "http://localhost:8080/ceps" -H "Content-Type: application/json" -d "{\"cep\":\"30140-071\"}"
```

## Deployment notes

- use `http://` unless you explicitly configure TLS in Spring Boot or behind a reverse proxy
- open the application port in the EC2 security group when deploying to AWS
- prefer environment variables or Parameter Store for production configuration
