# Histórias de Usuário

Cada história segue o formato *Como [ator], quero [ação], para [benefício]* e traz critérios de aceitação
verificáveis. Elas detalham os casos de uso do [diagrama de casos de uso](CasosDeUso.pdf).

## Aluno

**HU01 — Cadastrar-me no sistema**
Como aluno, quero me cadastrar informando nome, e-mail, CPF, RG, endereço, instituição de ensino e curso,
para participar do sistema de mérito.
- A instituição é escolhida entre as pré-cadastradas.
- Login, e-mail e CPF não podem estar em uso; a senha tem no mínimo 6 caracteres.
- Após o cadastro, o aluno já entra logado com saldo 0.

**HU02 — Manter meus dados**
Como aluno, quero consultar, editar e remover meu cadastro, para manter minhas informações corretas.
- Só o próprio aluno acessa e altera seus dados completos.
- Senha em branco na edição mantém a atual.
- Um aluno com transações registradas não pode ser removido (preserva o histórico).

**HU03 — Ser notificado ao receber moedas**
Como aluno, quero receber um e-mail quando um professor me enviar moedas, para saber que fui reconhecido.
- O e-mail informa professor, quantidade, motivo e saldo atual.

**HU04 — Consultar meu extrato**
Como aluno, quero ver meu saldo e minhas transações (recebimentos e trocas), para acompanhar minhas moedas.
- Recebimentos mostram professor e motivo; trocas mostram vantagem, empresa e código do cupom.

**HU05 — Trocar moedas por uma vantagem**
Como aluno, quero escolher uma vantagem cadastrada e trocá-la pelas minhas moedas, para usufruir do benefício.
- Não é possível trocar sem saldo suficiente.
- O custo é descontado do saldo e é gerado um cupom com código único.
- Recebo por e-mail o cupom com o código para usar na troca presencial.

## Professor

**HU06 — Receber moedas semestrais**
Como professor, quero receber 1.000 moedas a cada semestre, acumuladas ao saldo não distribuído,
para ter moedas para reconhecer meus alunos.
- O crédito é lançado uma única vez por semestre.

**HU07 — Enviar moedas a um aluno**
Como professor, quero enviar moedas a um aluno da minha instituição informando o motivo, para reconhecer
seu bom desempenho.
- O motivo é obrigatório; a quantidade deve ser positiva e não pode exceder meu saldo.
- Só aparecem/são aceitos alunos da instituição à qual estou vinculado.
- O aluno é notificado por e-mail (HU03).

**HU08 — Consultar meu extrato**
Como professor, quero ver meu saldo e as moedas que enviei, para controlar a distribuição.

## Empresa parceira

**HU09 — Cadastrar minha empresa**
Como empresa parceira, quero me cadastrar (nome, e-mail, CNPJ, login e senha), para oferecer vantagens aos alunos.
- Login, e-mail e CNPJ não podem estar em uso.

**HU10 — Manter os dados da empresa**
Como empresa parceira, quero consultar, editar e remover meu cadastro.
- Empresas com vantagens já resgatadas não podem ser removidas.

**HU11 — Cadastrar vantagens**
Como empresa parceira, quero cadastrar, editar e remover vantagens com nome, descrição, foto e custo em moedas,
para que os alunos possam resgatá-las.
- A foto é obrigatória no cadastro (JPG, PNG, GIF ou WEBP, até 5 MB).
- Só a empresa dona altera ou remove a vantagem; vantagens já resgatadas não podem ser removidas.

**HU12 — Conferir trocas**
Como empresa parceira, quero receber um e-mail a cada resgate com o código do cupom e poder registrar seu uso,
para validar a troca presencial.
- O código do e-mail é o mesmo enviado ao aluno.
- Um cupom só pode ser confirmado uma vez e apenas pela empresa dona da vantagem.

## Todos os usuários

**HU13 — Autenticar-me**
Como aluno, professor ou empresa parceira, quero entrar com login e senha, para acessar as funções do meu perfil.
- Todas as operações (exceto login e auto-cadastro) exigem autenticação.
- Cada perfil só acessa as próprias telas e operações.
