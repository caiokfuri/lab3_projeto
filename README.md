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
| [Modelo ER e estratégia de persistência](docs/ModeloER.md) | Lab03 S02 / S03 · Lab04 S02 | Tabelas, relacionamentos e acesso ao banco (JPA/Hibernate + DAO) |
| [Arquitetura e modelos v3](docs/Arquitetura.md) | Lab03 S03 · Lab04 S02 | Diagramas de classes e componentes atualizados, histórico de mudanças, autenticação e API REST |
| [Diagrama de Implantação e deploy na nuvem](docs/Implantacao.md) | Lab04 S02 | Vercel (front-end) + Render (API e PostgreSQL), passo a passo e variáveis de ambiente |

## Tecnologias

- **Back-end:** Java 25 LTS + Spring Boot 3.5 (Web, Data JPA, Validation, Mail)
- **ORM:** JPA/Hibernate, com DAOs (Spring Data `JpaRepository`)
- **Banco:** H2 em arquivo (`backend/data/`) localmente; PostgreSQL na nuvem
- **Front-end:** React 18 + React Router + Vite (`frontend/`)
- **Autenticação:** login/senha (hash BCrypt) com token de sessão no cabeçalho `Authorization: Bearer`
- **E-mail:** API HTTP da Brevo (nuvem) ou SMTP (local); sem nenhum configurado, os e-mails são simulados no log
- **Nuvem:** Vercel (front-end) e Render (API em Docker + PostgreSQL), com deploy automático a cada push na `main`

## Arquitetura (MVC)

```
frontend/   View — React (telas do aluno, professor, empresa e login)
backend/    Controller (REST) → Model: Serviços → Model: Persistência (DAO/ORM) → H2
docs/       diagramas, histórias de usuário e modelo ER
```

| Pacote (`backend/src/main/java/br/pucminas/moedaestudantil`) | Camada |
|---|---|
| `controller` | Controller — API REST |
| `config` | Interceptor de autenticação, web (SPA/CORS), dados iniciais, conversão da `DATABASE_URL` |
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

Pré-requisitos: JDK 25+ e Node.js 18+ (o Maven vem pelo wrapper `mvnw`).

```bash
# 1. gerar o front-end
cd frontend
npm install
npm run build          # gera frontend/dist, servido pelo Spring

# 2. subir o back-end
cd ../backend
./mvnw spring-boot:run # no Windows (cmd/PowerShell): mvnw.cmd spring-boot:run
```

Acesse http://localhost:8080. O app deve ser iniciado de dentro de `backend/` (os caminhos `../frontend/dist`
e `./data` são relativos a essa pasta).

**Desenvolvimento do front-end:** com o back-end rodando, `npm run dev` em `frontend/` abre o Vite em
http://localhost:5173 com recarga automática; as chamadas a `/api` são repassadas para a 8080.

## Implantação na nuvem

| Parte | Serviço | Configuração |
|---|---|---|
| Front-end (React) | Vercel | Root Directory `frontend`, variável `VITE_API_URL` ([vercel.json](frontend/vercel.json)) |
| API (Spring Boot) | Render, Web Service Docker | [render.yaml](render.yaml) e [backend/Dockerfile](backend/Dockerfile) |
| Banco | Render PostgreSQL | Criado pelo mesmo Blueprint; chega à API pela variável `DATABASE_URL` |

Passo a passo, variáveis de ambiente, limitações do plano gratuito e o Diagrama de Implantação estão em
[docs/Implantacao.md](docs/Implantacao.md).

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
do back-end como `[e-mail simulado]`. Para envio real localmente, defina antes de iniciar:

```bash
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=seu-email@gmail.com
SPRING_MAIL_PASSWORD=senha-de-app
```

Na nuvem o SMTP é bloqueado pela Render; o envio usa a API da Brevo (`APP_EMAIL_BREVO_API_KEY` e
`APP_EMAIL_REMETENTE`), conforme [docs/Implantacao.md](docs/Implantacao.md#3-e-mails-reais-opcional).

## Funcionalidades

| Perfil | Telas |
|---|---|
| Aluno | Cadastro; extrato (saldo, recebimentos, trocas e cupons); catálogo de vantagens com troca; perfil (editar/remover conta) |
| Professor | Enviar moedas (aluno da instituição + motivo obrigatório); extrato de envios; perfil com a instituição |
| Empresa parceira | Cadastro; CRUD de vantagens com foto; resgates com conferência do código do cupom; perfil (editar/remover conta) |

## Testes

```bash
cd backend
./mvnw test
```

Cobrem os CRUDs de aluno e empresa com autenticação/autorização, o fluxo completo (envio → cadastro de vantagem
com foto → troca → cupom → conferência), as fotos guardadas no banco, as regras de saldo/motivo/instituição,
o crédito semestral e a conversão da `DATABASE_URL`.

A API completa está em [docs/Arquitetura.md](docs/Arquitetura.md#api-rest).
