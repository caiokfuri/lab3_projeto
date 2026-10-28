# lab3_projeto — Sistema de Moeda Estudantil

Sistema para reconhecimento do mérito estudantil por meio de uma moeda virtual: professores distribuem moedas
aos alunos, que as trocam por vantagens oferecidas por empresas parceiras.

## Documentação

| Documento | Sprint | Descrição |
|-----------|--------|-----------|
| [Diagrama de Casos de Uso](docs/CasosDeUso.pdf) | S01 | Casos de uso do sistema |
| [Histórias de Usuário](docs/HistoriasDeUsuario.md) | S01 | Histórias com critérios de aceitação |
| [Diagrama de Classes v1](docs/Classes.pdf) | S01 | Estrutura de classes (versão inicial) |
| [Diagrama de Componentes v1](docs/Componentes.pdf) | S01 | Componentes e dependências (versão inicial) |
| [Modelo ER e estratégia de persistência](docs/ModeloER.md) | S02 / S03 | Tabelas, relacionamentos e acesso ao banco (JPA/Hibernate + DAO) |
| [Arquitetura e modelos v2](docs/Arquitetura.md) | S03 | Diagramas de classes e componentes atualizados, autenticação e API REST |

## Tecnologias

- **Back-end:** Java 25 LTS + Spring Boot 3.5 (Web, Data JPA, Validation, Mail)
- **ORM:** JPA/Hibernate, com DAOs (Spring Data `JpaRepository`)
- **Banco:** H2 em arquivo (`backend/data/`)
- **Front-end:** React 18 + React Router + Vite (`frontend/`)
- **Autenticação:** login/senha (hash BCrypt) com token de sessão no cabeçalho `Authorization: Bearer`
- **E-mail:** JavaMail (SMTP configurável; sem SMTP os e-mails são simulados no log)

## Arquitetura (MVC)

```
frontend/   View — React (telas do aluno, professor, empresa e login)
backend/    Controller (REST) → Model: Serviços → Model: Persistência (DAO/ORM) → H2
docs/       diagramas, histórias de usuário e modelo ER
```

| Pacote (`backend/src/main/java/br/pucminas/moedaestudantil`) | Camada |
|---|---|
| `controller` | Controller — API REST |
| `config` | Interceptor de autenticação, web (SPA/uploads/CORS), dados iniciais |
| `service` | Model – Serviços: regras de negócio, transações, e-mail, crédito semestral |
| `dao` | Model – Persistência: DAOs sobre o ORM |
| `model` | Entidades JPA do diagrama de classes (com as regras de domínio) |
| `dto` | Objetos de entrada/saída da API |

| Pasta (`frontend/src`) | Conteúdo |
|---|---|
| `pages/` | Login e cadastros públicos; `aluno/`, `professor/`, `empresa/` com as telas de cada perfil |
| `components/` | Layout, rota protegida, formulários de aluno/empresa, campos e modal |
| `auth/AuthContext.jsx` | Sessão do usuário logado |
| `api.js` | Chamadas à API REST |

## Como executar

Pré-requisitos: JDK 25+, Maven 3.9+ e Node.js 18+.

```bash
# 1. gerar o front-end
cd frontend
npm install
npm run build          # gera frontend/dist, servido pelo Spring

# 2. subir o back-end
cd ../backend
mvn spring-boot:run
```

Acesse http://localhost:8080. O app deve ser iniciado de dentro de `backend/` (os caminhos `../frontend/dist`,
`./data` e `./uploads` são relativos a essa pasta).

**Desenvolvimento do front-end:** com o back-end rodando, `npm run dev` em `frontend/` abre o Vite em
http://localhost:5173 com recarga automática; as chamadas a `/api` e `/uploads` são repassadas para a 8080.

Console do banco: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./data/moeda-estudantil`, usuário `sa`, sem senha).

### Usuários de demonstração

Professores são pré-cadastrados (a instituição envia a lista na parceria) e recebem 1.000 moedas por semestre,
acumuladas. Senha de todos: `123456`.

| Login | Professor | Instituição |
|---|---|---|
| `prof.ana` | Ana Paula Ribeiro | PUC Minas |
| `prof.carlos` | Carlos Henrique Souza | PUC Minas |
| `prof.beatriz` | Beatriz Lima Costa | UFMG |
| `prof.diego` | Diego Martins Alves | CEFET-MG |

Alunos e empresas parceiras se cadastram pela tela de login ("Sou aluno" / "Sou empresa parceira").
O professor só envia moedas a alunos da **mesma instituição**.

### E-mails

Sem configuração, cada e-mail (moedas recebidas, cupom do aluno, conferência do parceiro) aparece no console
do back-end como `[e-mail simulado]`. Para envio real, defina antes de iniciar:

```bash
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=seu-email@gmail.com
SPRING_MAIL_PASSWORD=senha-de-app
```

## Funcionalidades

| Perfil | Telas |
|---|---|
| Aluno | Cadastro; extrato (saldo, recebimentos, trocas e cupons); catálogo de vantagens com troca; perfil (editar/remover conta) |
| Professor | Enviar moedas (aluno da instituição + motivo obrigatório); extrato de envios; perfil com a instituição |
| Empresa parceira | Cadastro; CRUD de vantagens com foto; resgates com conferência do código do cupom; perfil (editar/remover conta) |

## Testes

```bash
cd backend
mvn test
```

Cobrem os CRUDs de aluno e empresa com autenticação/autorização, o fluxo completo (envio → cadastro de vantagem
com foto → troca → cupom → conferência), as regras de saldo/motivo/instituição e o crédito semestral.

A API completa está em [docs/Arquitetura.md](docs/Arquitetura.md#api-rest).
