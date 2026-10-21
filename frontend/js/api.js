// Comunicação com o back-end (API REST) e utilitários compartilhados pelas telas.

// Servido pelo Spring (porta 8080): mesma origem. Aberto por outro servidor local: aponta para a API na 8080.
const API_URL = location.port === '8080' ? '' : 'http://localhost:8080';

class ApiError extends Error {
    constructor(status, mensagem, campos) {
        super(mensagem);
        this.status = status;
        this.campos = campos || {};
    }
}

async function requisicao(metodo, url, corpo) {
    const opcoes = { method: metodo, headers: {} };
    if (corpo !== undefined) {
        opcoes.headers['Content-Type'] = 'application/json';
        opcoes.body = JSON.stringify(corpo);
    }

    let resposta;
    try {
        resposta = await fetch(API_URL + url, opcoes);
    } catch (e) {
        throw new ApiError(0, 'Não foi possível conectar ao servidor');
    }

    if (resposta.status === 204) return null;
    const dados = await resposta.json().catch(() => null);
    if (!resposta.ok) {
        throw new ApiError(resposta.status, dados?.mensagem || `Erro ${resposta.status}`, dados?.campos);
    }
    return dados;
}

const api = {
    listar: (recurso) => requisicao('GET', `/api/${recurso}`),
    buscar: (recurso, id) => requisicao('GET', `/api/${recurso}/${id}`),
    criar: (recurso, dados) => requisicao('POST', `/api/${recurso}`, dados),
    atualizar: (recurso, id, dados) => requisicao('PUT', `/api/${recurso}/${id}`, dados),
    remover: (recurso, id) => requisicao('DELETE', `/api/${recurso}/${id}`),
};

function escapeHtml(valor) {
    return String(valor ?? '').replace(/[&<>"']/g, (c) =>
        ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

function formatarCpf(cpf) {
    return (cpf || '').replace(/^(\d{3})(\d{3})(\d{3})(\d{2})$/, '$1.$2.$3-$4');
}

function formatarCnpj(cnpj) {
    return (cnpj || '').replace(/^(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})$/, '$1.$2.$3/$4-$5');
}

function mostrarMensagem(texto, tipo) {
    const el = document.getElementById('mensagem');
    el.textContent = texto;
    el.className = `mensagem ${tipo}`;
    clearTimeout(mostrarMensagem.timer);
    mostrarMensagem.timer = setTimeout(() => { el.className = 'mensagem'; }, 5000);
}

function limparErros(form) {
    form.querySelectorAll('.campo').forEach((c) => c.classList.remove('invalido'));
    form.querySelectorAll('.erro-campo').forEach((e) => { e.textContent = ''; });
}

function mostrarErros(form, erro) {
    limparErros(form);
    Object.entries(erro.campos || {}).forEach(([campo, msg]) => {
        const input = form.elements[campo];
        if (!input) return;
        const container = input.closest('.campo');
        container.classList.add('invalido');
        container.querySelector('.erro-campo').textContent = msg;
    });
    mostrarMensagem(erro.message, 'erro');
}

function dadosDoFormulario(form) {
    const dados = {};
    new FormData(form).forEach((valor, chave) => { dados[chave] = String(valor).trim(); });
    return dados;
}
