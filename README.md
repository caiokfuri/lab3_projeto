# lab3_projeto — Sistema de Moeda Estudantil

## Documentação

| Documento | Descrição |
|-----------|-----------|
| [Diagrama de Casos de Uso](docs/CasosDeUso.pdf) | Casos de uso do sistema |
| [Diagrama de Classes](docs/Classes.pdf) | Estrutura de classes do sistema |
| [Diagrama de Componentes](docs/Componentes.pdf) | Componentes e suas dependências |
| [Modelo ER e estratégia de persistência](docs/ModeloER.md) | Tabelas, relacionamentos e acesso ao banco (JPA/Hibernate + DAO) |

## Tecnologias

- Java 17 + Spring Boot 3 (Web, Data JPA, Validation)
- ORM: JPA/Hibernate, com DAOs (Spring Data `JpaRepository`)
- Banco: H2 (arquivo em `backend/data/`)
- Front-end: HTML, CSS e JavaScript puros na pasta `frontend/`, servidos pelo próprio Spring

## Estrutura

```
frontend/   telas (HTML/CSS/JS) — View
backend/    API Spring Boot — Controller, Serviços e Persistência
docs/       diagramas e modelo ER
```

### Front-end (`frontend/`)

| Arquivo | Descrição |
|---|---|
| `index.html` | Página inicial |
| `alunos.html` + `js/alunos.js` | CRUD de alunos |
| `empresas.html` + `js/empresas.js` | CRUD de empresas parceiras |
| `js/api.js` | Chamadas à API REST e utilitários compartilhados |
| `css/style.css` | Estilos |

### Back-end (`backend/src/main/java/br/pucminas/moedaestudantil`)

| Pacote | Camada (diagrama de componentes) |
|---|---|
| `controller` | Controller — API REST (`/api/alunos`, `/api/empresas`, `/api/instituicoes`) |
| `service` | Model – Serviços — regras de negócio e transações |
| `dao` | Model – Persistência — DAOs sobre o ORM |
| `model` | Entidades JPA do diagrama de classes |
| `dto` | Objetos de entrada/saída da API |

## Como executar

Pré-requisitos: JDK 17+ e Maven 3.9+.

```bash
cd backend
mvn spring-boot:run
```

Acesse http://localhost:8080 (telas de Alunos e Empresas parceiras). O Spring serve a pasta
`../frontend`, então o app deve ser iniciado de dentro de `backend/`; alterações nas telas
aparecem só recarregando a página. Também é possível abrir o `frontend/` por outro servidor
local (ex.: Live Server do VS Code) — a API em `localhost:8080` aceita essas origens (CORS).
Console do banco: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./data/moeda-estudantil`, usuário `sa`, sem senha).

Testes:

```bash
cd backend
mvn test
```

## API (Lab03S02)

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/alunos` | Lista alunos |
| GET | `/api/alunos/{id}` | Busca aluno |
| POST | `/api/alunos` | Cadastra aluno |
| PUT | `/api/alunos/{id}` | Atualiza aluno (senha em branco mantém a atual) |
| DELETE | `/api/alunos/{id}` | Remove aluno |
| GET/POST/PUT/DELETE | `/api/empresas[/{id}]` | Mesmo CRUD para empresas parceiras |
| GET | `/api/instituicoes` | Instituições pré-cadastradas |
