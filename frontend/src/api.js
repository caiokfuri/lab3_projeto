// Comunicação com o back-end (API REST). O token de sessão vai no cabeçalho Authorization.

const CHAVE_TOKEN = 'moeda-estudantil.token';

export class ApiError extends Error {
  constructor(status, mensagem, campos) {
    super(mensagem);
    this.status = status;
    this.campos = campos || {};
  }
}

export const token = {
  ler() {
    try { return localStorage.getItem(CHAVE_TOKEN); } catch { return null; }
  },
  salvar(valor) {
    try { localStorage.setItem(CHAVE_TOKEN, valor); } catch { /* sem storage: sessão só nesta aba */ }
  },
  limpar() {
    try { localStorage.removeItem(CHAVE_TOKEN); } catch { /* ignora */ }
  },
};

let aoExpirarSessao = () => {};

/** Chamado quando a API responde 401 para uma requisição autenticada (token expirado/inválido). */
export function definirAoExpirarSessao(callback) {
  aoExpirarSessao = callback;
}

async function requisicao(metodo, url, corpo) {
  const headers = {};
  const tokenAtual = token.ler();
  if (tokenAtual) headers.Authorization = `Bearer ${tokenAtual}`;

  let body;
  if (corpo instanceof FormData) {
    body = corpo; // o navegador define o multipart boundary
  } else if (corpo !== undefined) {
    headers['Content-Type'] = 'application/json';
    body = JSON.stringify(corpo);
  }

  let resposta;
  try {
    resposta = await fetch(url, { method: metodo, headers, body });
  } catch {
    throw new ApiError(0, 'Não foi possível conectar ao servidor');
  }

  if (resposta.status === 204) return null;
  const dados = await resposta.json().catch(() => null);
  if (!resposta.ok) {
    if (resposta.status === 401 && tokenAtual) aoExpirarSessao();
    throw new ApiError(resposta.status, dados?.mensagem || `Erro ${resposta.status}`, dados?.campos);
  }
  return dados;
}

export const api = {
  get: (url) => requisicao('GET', url),
  post: (url, corpo) => requisicao('POST', url, corpo),
  put: (url, corpo) => requisicao('PUT', url, corpo),
  delete: (url) => requisicao('DELETE', url),
};
