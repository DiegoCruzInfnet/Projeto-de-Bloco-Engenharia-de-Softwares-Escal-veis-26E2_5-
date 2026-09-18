# Projeto de Bloco — Sistema de Biblioteca (Microsserviços)

Sistema de biblioteca com empréstimos e avaliações, construído com Spring Boot 3.3.5 / Spring Cloud (Java 21). Este repositório inclui a implantação em contêineres, orquestração com Kubernetes, monitoramento e pipeline de CI/CD (TP5).

## Arquitetura

| Serviço | Função | Porta (compose) |
|---|---|---|
| `eureka-server` | Descoberta de serviços | 8761 |
| `biblioteca` | Livros e usuários | 8080 |
| `avaliacao-service` | Avaliações de livros | 8081 |
| `loan-service` | Empréstimos (regras de limite e disponibilidade) | 8082 |
| `notification-service` | Consome eventos de empréstimo (RabbitMQ) | 8083 |
| `frontend` | Interface web (nginx) | 3000 |
| `postgres` | Banco de dados (bancos `biblioteca`, `loan`, `avaliacao`) | 5432 |
| `rabbitmq` | Mensageria (fila `loan.created`) | 5672 / 15672 |

Comunicação:
- Os serviços se registram no Eureka.
- `loan-service` chama `biblioteca` via OpenFeign para validar livro e usuário.
- Ao criar um empréstimo, `loan-service` publica um evento na fila `loan.created`, consumido pelo `notification-service`.
- As configurações externas ficam no repositório `biblioteca-config` (Config Server).

## Regras de negócio do empréstimo

- Um usuário pode ter no máximo **2 empréstimos ativos**; o 3º é recusado.
- Um livro já emprestado (com devolução futura) não pode ser emprestado de novo.
- Livro ou usuário inexistente retorna 404.

## 1. Executando com Docker Compose

Pré-requisitos: Docker Desktop.

Crie um arquivo `.env` na raiz com a senha do banco:

```
DB_PASSWORD=sua_senha
```

Suba tudo:

```bash
docker compose up -d --build
```

Endereços:
- Frontend: http://localhost:3000
- Eureka: http://localhost:8761
- RabbitMQ (painel): http://localhost:15672
- Grafana: http://localhost:3001
- Prometheus: http://localhost:9090

Para derrubar: `docker compose down`.

## 2. Executando no Kubernetes

Pré-requisitos: Docker Desktop com Kubernetes habilitado e `kubectl`.

O Docker Desktop cria o cluster com `kind`, que **não compartilha o cache de imagens** do Docker. Por isso, depois de buildar as imagens, é preciso carregá-las no cluster. Os manifests usam `imagePullPolicy: Never`.

```bash
# 1. Buildar as imagens
docker compose build

# 2. Carregar no cluster (repita para cada imagem projeto_de_bloco-<serviço>)
kind load docker-image projeto_de_bloco-eureka-server:latest --name desktop
kind load docker-image projeto_de_bloco-biblioteca:latest --name desktop
kind load docker-image projeto_de_bloco-loan-service:latest --name desktop
kind load docker-image projeto_de_bloco-avaliacao-service:latest --name desktop
kind load docker-image projeto_de_bloco-notification-service:latest --name desktop
kind load docker-image projeto_de_bloco-frontend:latest --name desktop

# 3. Aplicar os manifests
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/
kubectl get pods -n biblioteca
```

Acessando o frontend (o NodePort não é alcançável pelo Windows por causa do `kind`):

```bash
kubectl port-forward -n biblioteca svc/frontend 3000:80
```

### Escalabilidade

O `biblioteca` roda com 3 réplicas (`k8s/biblioteca.yaml`). Para escalar manualmente:

```bash
kubectl scale deployment biblioteca -n biblioteca --replicas=5
kubectl get pods -n biblioteca -l app=biblioteca
```

Cada instância se registra no Eureka pelo IP (`eureka.instance.prefer-ip-address=true`), o que permite ao `loan-service` balancear as chamadas entre as réplicas.

Para comprovar o balanceamento, chame o Service de dentro do cluster (o `port-forward` não balanceia, ele fixa em um pod):

```bash
kubectl run curl -n biblioteca --rm -it --image=curlimages/curl -- sh
# dentro do pod: repita a chamada e observe pods diferentes nos logs
```

## 3. Monitoramento e rastreamento

Stack de observabilidade (manifests em `k8s/`, configs para compose em `monitoring/`):

| Ferramenta | Função |
|---|---|
| **Prometheus** | Métricas dos serviços (`/actuator/prometheus`) |
| **Loki + Promtail** | Agregação centralizada de logs |
| **Tempo** | Rastreamento distribuído (recebe spans no formato Zipkin, porta 9411) |
| **Grafana** | Interface única, com os três datasources já provisionados |

Instrumentação nos serviços: Spring Boot Actuator, Micrometer Tracing (Brave), exportação Zipkin para o Tempo e `feign-micrometer` para propagar o trace entre `loan-service` e `biblioteca`.

Acessos:
- Compose: Grafana em http://localhost:3001
- Kubernetes: `kubectl port-forward -n biblioteca svc/grafana 3001:3000`

Como usar no Grafana (menu **Explore**):
- **Logs:** datasource Loki, filtro por `app`, por exemplo `{app="loan-service"}`.
- **Traces:** datasource Tempo; uma requisição `POST /loan` aparece como um trace único com as chamadas ao `biblioteca`.
- **Métricas:** datasource Prometheus.

> O Grafana está com acesso anônimo habilitado apenas para demonstração. Não use essa configuração em produção.

## 4. CI/CD (GitHub Actions)

Workflow: `.github/workflows/tp5.yml`, disparado em push e pull request para `tp5` e `main`.

**Job `build-and-test`**
- Sobe PostgreSQL e RabbitMQ como `services` do job.
- Roda `mvn test` em Biblioteca, Loan Service e Avaliacao Service.
- Faz o build do frontend (`npm ci && npm run build`).

**Job `docker`** (só executa se os testes passarem)
- Faz login no GHCR com o `GITHUB_TOKEN`.
- Builda e publica as 6 imagens em `ghcr.io/<owner em minúsculo>/<serviço>:latest`.

## 5. Testes

- **Repositório** (`@SpringBootTest`, exigem PostgreSQL): `BookRepositoryTest`, `UserRepositoryTest`, `LoanRepositoryTest`, `AvaliacaoRepositoryTest`.
- **Regras de negócio** (unitários com Mockito, sem banco): `LoanServiceTest` cobre salvar e publicar evento, livro já emprestado, limite de 2 empréstimos (inclusive o limite exato), livro/usuário inexistente e devolução.

Rodar os testes que dependem de banco localmente:

```powershell
docker run --rm -d --name pg-teste -e POSTGRES_PASSWORD=postgres -p 5433:5432 postgres:15
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5433/postgres"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:DB_PASSWORD="postgres"
$env:SPRING_JPA_HIBERNATE_DDL_AUTO="update"
mvn -f Loan_Service/loan_service/pom.xml test
docker stop pg-teste
```

Rodar só os testes unitários (sem banco):

```bash
mvn -f Loan_Service/loan_service/pom.xml test -Dtest=LoanServiceTest
```

Exemplos de chamadas à API estão em `testes-api.http`.

## Estrutura do repositório

```
Biblioteca/            serviço de livros e usuários (+ frontend/)
Loan_Service/          serviço de empréstimos
Avaliacao_Service/     serviço de avaliações
notification-service/  consumidor de eventos
Eureka_Sever/          descoberta de serviços
Config_Sever/          servidor de configuração
k8s/                   manifests Kubernetes
monitoring/            configs de Prometheus, Loki, Promtail, Tempo e Grafana
docker-compose.yml     ambiente local completo
.github/workflows/     pipeline de CI/CD
```