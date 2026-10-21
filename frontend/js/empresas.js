const RECURSO = 'empresas';
const form = document.getElementById('form-empresa');
const tabela = document.getElementById('tabela-empresas');
const btnSalvar = document.getElementById('btn-salvar');
const btnCancelar = document.getElementById('btn-cancelar');
const tituloForm = document.getElementById('titulo-form');
const dicaSenha = document.getElementById('dica-senha');

let empresas = [];

async function carregarEmpresas() {
    try {
        empresas = await api.listar(RECURSO);
        renderizarTabela();
    } catch (e) {
        tabela.innerHTML = `<tr><td colspan="6" class="vazio">${escapeHtml(e.message)}</td></tr>`;
    }
}

function renderizarTabela() {
    if (empresas.length === 0) {
        tabela.innerHTML = '<tr><td colspan="6" class="vazio">Nenhuma empresa cadastrada.</td></tr>';
        return;
    }
    tabela.innerHTML = empresas.map((e) => `
        <tr>
            <td>${escapeHtml(e.nome)}</td>
            <td>${escapeHtml(formatarCnpj(e.cnpj))}</td>
            <td>${escapeHtml(e.email)}</td>
            <td>${escapeHtml(e.login)}</td>
            <td>${e.quantidadeVantagens}</td>
            <td class="acoes-linha">
                <button class="btn-secundario btn-pequeno" data-acao="editar" data-id="${e.id}">Editar</button>
                <button class="btn-perigo btn-pequeno" data-acao="remover" data-id="${e.id}">Remover</button>
            </td>
        </tr>`).join('');
}

function modoCadastro() {
    form.reset();
    form.elements.id.value = '';
    limparErros(form);
    tituloForm.textContent = 'Nova empresa parceira';
    btnSalvar.textContent = 'Cadastrar';
    btnCancelar.hidden = true;
    dicaSenha.textContent = 'Mínimo de 6 caracteres.';
}

function modoEdicao(empresa) {
    limparErros(form);
    ['id', 'nome', 'email', 'login'].forEach((campo) => { form.elements[campo].value = empresa[campo]; });
    form.elements.cnpj.value = formatarCnpj(empresa.cnpj);
    form.elements.senha.value = '';
    tituloForm.textContent = `Editando: ${empresa.nome}`;
    btnSalvar.textContent = 'Salvar alterações';
    btnCancelar.hidden = false;
    dicaSenha.textContent = 'Deixe em branco para manter a senha atual.';
    form.scrollIntoView({ behavior: 'smooth' });
}

form.addEventListener('submit', async (evento) => {
    evento.preventDefault();
    const { id, ...dados } = dadosDoFormulario(form);

    btnSalvar.disabled = true;
    try {
        if (id) {
            await api.atualizar(RECURSO, id, dados);
            mostrarMensagem('Empresa atualizada com sucesso.', 'sucesso');
        } else {
            await api.criar(RECURSO, dados);
            mostrarMensagem('Empresa cadastrada com sucesso.', 'sucesso');
        }
        modoCadastro();
        await carregarEmpresas();
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
    const empresa = empresas.find((e) => e.id === Number(botao.dataset.id));
    if (!empresa) return;

    if (botao.dataset.acao === 'editar') {
        modoEdicao(empresa);
        return;
    }
    if (!confirm(`Remover a empresa "${empresa.nome}" e todas as suas vantagens?`)) return;
    try {
        await api.remover(RECURSO, empresa.id);
        if (form.elements.id.value === String(empresa.id)) modoCadastro();
        mostrarMensagem('Empresa removida.', 'sucesso');
        await carregarEmpresas();
    } catch (e) {
        mostrarMensagem(e.message, 'erro');
    }
});

carregarEmpresas();
