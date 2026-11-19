# Implantação na nuvem (Lab04S02)

O front-end React fica na **Vercel** e a API Spring Boot e o banco PostgreSQL ficam na **Render**, todos em
planos gratuitos. Cada push na branch `main` gera um novo deploy nos dois serviços.

## Diagrama de Implantação

![Diagrama de Implantação](lab4-sprint2/DiagramaImplantacao.svg)

Arquivos: [SVG](lab4-sprint2/DiagramaImplantacao.svg) · [PNG](lab4-sprint2/DiagramaImplantacao.png) ·
[fonte PlantUML](lab4-sprint2/DiagramaImplantacao.puml).

| Elemento | Tipo UML | O que é |
|---|---|---|
| Computador ou celular do usuário › Navegador web | «device» › «execution environment» | Onde a SPA React roda; guarda o token de sessão no `localStorage` |
| Vercel — Edge Network | «node» | CDN que serve o build estático do React (`frontend/dist`) |
| Render — Web Service | «node» | Instância gratuita (512 MB) que executa o contêiner Docker com a JVM e o `moeda-estudantil.jar` |
| Render — PostgreSQL | «node» com «database» | Banco gerenciado com as tabelas do [Modelo ER](ModeloER.md) |
| Brevo — API de e-mail | «node» (externo, opcional) | Envia os e-mails de moedas recebidas, cupom e conferência |
| GitHub | «node» | Repositório; as setas «deploy» indicam o build automático a cada push |

| Comunicação | Protocolo | Observação |
|---|---|---|
| Navegador → Vercel | HTTPS | Baixa `index.html` e os assets; qualquer rota do React devolve o `index.html` (`vercel.json`) |
| Navegador → Render | HTTPS, REST/JSON | Cabeçalho `Authorization: Bearer <token>`; CORS liberado para `https://*.vercel.app` |
| API → PostgreSQL | TCP/JDBC | Rede interna da Render, endereço vindo de `DATABASE_URL` |
| API → Brevo | HTTPS | `POST https://api.brevo.com/v3/smtp/email` |

## O que mudou no código para rodar na nuvem

| Mudança | Motivo |
|---|---|
| `backend/Dockerfile` e `render.yaml` (Blueprint) | A Render não tem runtime Java nativo; a API roda como contêiner Docker |
| `DATABASE_URL` → PostgreSQL (`config/DatabaseUrlEnvironmentPostProcessor`) | A Render entrega o banco como `postgresql://usuario:senha@host/banco`, formato que o JDBC não aceita. Localmente continua o H2 |
| Fotos das vantagens guardadas no banco (entidade `Foto`, rota pública `GET /api/fotos/{id}`) | O disco da Render é apagado a cada deploy, reinício ou repouso; arquivos em `uploads/` sumiriam |
| E-mail pela API HTTP da Brevo (`service/BrevoEmailClient`) | O plano gratuito da Render bloqueia as portas SMTP 25, 465 e 587 |
| `VITE_API_URL` no front-end (`src/api.js`) e CORS configurável (`app.cors.origens`) | Front e API ficam em domínios diferentes |
| `server.port=${PORT:8080}` e perfil `prod` | A Render define a porta pela variável `PORT`; o perfil desliga o console do H2 |
| Aviso "servidor iniciando" no login e no carregamento | A instância gratuita entra em repouso após 15 min sem acesso e leva cerca de 1 min para voltar |

## Passo a passo

O repositório precisa estar acessível pela conta da Render e da Vercel. Como ele está em
`github.com/caiokfuri/lab3_projeto`, o dono do repositório faz os passos abaixo, ou adiciona as contas do
grupo como colaboradoras.

### 1. API e banco na Render

1. Em https://dashboard.render.com, **New › Blueprint** e selecione o repositório `lab3_projeto`.
2. A Render lê o [`render.yaml`](../render.yaml) e propõe dois recursos: o banco `moeda-estudantil-db` e o
   serviço `moeda-estudantil-api`. Ela pede os valores das variáveis de e-mail; podem ficar em branco (veja o passo 3).
3. Clique em **Apply**. O primeiro build da imagem leva alguns minutos.
4. Quando o serviço ficar *Live*, copie a URL (ex.: `https://moeda-estudantil-api.onrender.com`) e teste
   `https://<url>/api/instituicoes`, que deve listar PUC Minas, UFMG e CEFET-MG.

### 2. Front-end na Vercel

1. Em https://vercel.com/new, importe o repositório `lab3_projeto`.
2. Em **Root Directory**, escolha `frontend`. O preset **Vite** é detectado sozinho
   (build `npm run build`, saída `dist`).
3. Em **Environment Variables**, adicione `VITE_API_URL` com a URL da Render, sem barra no final.
4. Clique em **Deploy** e acesse o endereço gerado (ex.: `https://moeda-estudantil.vercel.app`).

Se a variável for alterada depois, faça **Redeploy**: o Vite grava `VITE_API_URL` no momento do build.

### 3. E-mails reais (opcional)

Sem configuração, os e-mails aparecem nos **Logs** do serviço na Render como `[e-mail simulado]`.
Para enviá-los de verdade:

1. Crie uma conta gratuita em https://www.brevo.com (300 e-mails/dia).
2. Em **Senders, Domains & Dedicated IPs › Senders**, cadastre e confirme o e-mail remetente do grupo.
3. Em **SMTP & API › API Keys**, gere uma chave.
4. Na Render, em **moeda-estudantil-api › Environment**, preencha `APP_EMAIL_BREVO_API_KEY` com a chave e
   `APP_EMAIL_REMETENTE` com o e-mail confirmado. Salvar reinicia o serviço.

## Variáveis de ambiente

| Onde | Variável | Valor |
|---|---|---|
| Render | `SPRING_PROFILES_ACTIVE` | `prod` (já definida no Blueprint) |
| Render | `DATABASE_URL` | Ligada automaticamente ao banco do Blueprint; aceita também uma URL do Neon/Supabase |
| Render | `APP_EMAIL_BREVO_API_KEY` / `APP_EMAIL_REMETENTE` | Opcionais, para e-mail real |
| Render | `APP_CORS_ORIGENS` | Opcional; padrão `https://*.vercel.app`. Para restringir: `https://moeda-estudantil.vercel.app` |
| Render | `PORT` | Definida pela própria Render |
| Vercel | `VITE_API_URL` | URL do serviço na Render |

## Limitações do plano gratuito

- **Repouso:** após 15 minutos sem acesso a API é desligada; o primeiro acesso seguinte leva cerca de 1 minuto.
  Abra o site alguns minutos antes da apresentação.
- **Sessões em memória:** um reinício ou repouso encerra as sessões, e é preciso entrar de novo.
- **Banco expira em 30 dias:** o PostgreSQL gratuito da Render é apagado 30 dias após a criação (há mais 14 dias
  para fazer upgrade). Alternativa sem prazo: criar um banco no [Neon](https://neon.tech) e colocar a connection
  string dele em `DATABASE_URL`.
- **Dados de demonstração:** os professores pré-cadastrados (senha `123456`) também existem na nuvem.

## Testar a imagem localmente

Reproduz o ambiente da Render (512 MB, PostgreSQL, perfil `prod`) com Docker:

```bash
docker network create moeda
docker run -d --name moeda-db --network moeda -e POSTGRES_USER=moeda -e POSTGRES_PASSWORD=moeda \
  -e POSTGRES_DB=moeda_estudantil postgres:17
docker build -t moeda-estudantil-api ./backend
docker run --rm --name moeda-api --network moeda -p 10000:10000 -m 512m \
  -e PORT=10000 -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL=postgresql://moeda:moeda@moeda-db/moeda_estudantil moeda-estudantil-api
```

Depois, no front-end: `VITE_API_URL=http://localhost:10000 npm run dev`.
