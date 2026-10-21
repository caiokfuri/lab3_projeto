const RECURSO = 'alunos';
const form = document.getElementById('form-aluno');
const tabela = document.getElementById('tabela-alunos');
const btnSalvar = document.getElementById('btn-salvar');
const btnCancelar = document.getElementById('btn-cancelar');
const tituloForm = document.getElementById('titulo-form');
const dicaSenha = document.getElementById('dica-senha');

let alunos = [];

async function carregarInstituicoes() {
    const select = form.elements.instituicaoId;
    try {
        const instituicoes = await api.listar('instituicoes');
        instituicoes.forEach((i) => select.add(new Option(i.nome, i.id)));
    } catch (e) {
        mostrarMensagem(`Erro ao carregar instituições: ${e.message}`, 'erro');
    }
}

async function carregarAlunos() {
    try {
        alunos = await api.listar(RECURSO);
        renderizarTabela();
    } catch (e) {
        tabela.innerHTML = `<tr><td colspan="7" class="vazio">${escapeHtml(e.message)}</td></tr>`;
    }
}

function renderizarTabela() {
    if (alunos.length === 0) {
        tabela.innerHTML = '<tr><td colspan="7" class="vazio">Nenhum aluno cadastrado.</td></tr>';
        return;
    }
    tabela.innerHTML = alunos.map((a) => `
        <tr>
            <td>${escapeHtml(a.nome)}</td>
            <td>${escapeHtml(formatarCpf(a.cpf))}</td>
            <td>${escapeHtml(a.email)}</td>
            <td>${escapeHtml(a.instituicaoNome)}</td>
            <td>${escapeHtml(a.curso)}</td>
            <td>${a.saldoMoedas}</td>
            <td class="acoes-linha">
                <button class="btn-secundario btn-pequeno" data-acao="editar" data-id="${a.id}">Editar</button>
                <button class="btn-perigo btn-pequeno" data-acao="remover" data-id="${a.id}">Remover</button>
            </td>
        </tr>`).join('');
}

function modoCadastro() {
    form.reset();
    form.elements.id.value = '';
    limparErros(form);
    tituloForm.textContent = 'Novo aluno';
    btnSalvar.textContent = 'Cadastrar';
    btnCancelar.hidden = true;
    dicaSenha.textContent = 'Mínimo de 6 caracteres.';
}

function modoEdicao(aluno) {
    limparErros(form);
    ['id', 'nome', 'email', 'rg', 'endereco', 'curso', 'login', 'instituicaoId']
        .forEach((campo) => { form.elements[campo].value = aluno[campo]; });
    form.elements.cpf.value = formatarCpf(aluno.cpf);
    form.elements.senha.value = '';
    tituloForm.textContent = `Editando: ${aluno.nome}`;
    btnSalvar.textContent = 'Salvar alterações';
    btnCancelar.hidden = false;
    dicaSenha.textContent = 'Deixe em branco para manter a senha atual.';
    form.scrollIntoView({ behavior: 'smooth' });
}

form.addEventListener('submit', async (evento) => {
    evento.preventDefault();
    const { id, ...dados } = dadosDoFormulario(form);
    dados.instituicaoId = dados.instituicaoId ? Number(dados.instituicaoId) : null;

    btnSalvar.disabled = true;
    try {
        if (id) {
            await api.atualizar(RECURSO, id, dados);
            mostrarMensagem('Aluno atualizado com sucesso.', 'sucesso');
        } else {
            await api.criar(RECURSO, dados);
            mostrarMensagem('Aluno cadastrado com sucesso.', 'sucesso');
        }
        modoCadastro();
        await carregarAlunos();
    } catch (e) {
        mostrarErros(form, e);
    } finally {
        btnSalvar.disabled = false;
    }
});

btnCancelar.addEventListener('click', modoCadastro);

tabela.addEventListener('click', async (evento) => {
    const botao = evento.target.closest('button[data-acao]');
    if (!botao) return;
    const aluno = alunos.find((a) => a.id === Number(botao.dataset.id));
    if (!aluno) return;

    if (botao.dataset.acao === 'editar') {
        modoEdicao(aluno);
        return;
    }
    if (!confirm(`Remover o aluno "${aluno.nome}"?`)) return;
    try {
        await api.remover(RECURSO, aluno.id);
        if (form.elements.id.value === String(aluno.id)) modoCadastro();
        mostrarMensagem('Aluno removido.', 'sucesso');
        await carregarAlunos();
    } catch (e) {
        mostrarMensagem(e.message, 'erro');
    }
});

carregarInstituicoes();
carregarAlunos();
