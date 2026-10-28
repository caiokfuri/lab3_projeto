export function formatarCpf(cpf) {
  return (cpf || '').replace(/^(\d{3})(\d{3})(\d{3})(\d{2})$/, '$1.$2.$3-$4');
}

export function formatarCnpj(cnpj) {
  return (cnpj || '').replace(/^(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})$/, '$1.$2.$3/$4-$5');
}

const FORMATO_DATA = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' });

export function formatarData(iso) {
  return iso ? FORMATO_DATA.format(new Date(iso)) : '';
}

const FORMATO_NUMERO = new Intl.NumberFormat('pt-BR');

export function formatarMoedas(valor) {
  return `${FORMATO_NUMERO.format(valor ?? 0)} ${Math.abs(valor) === 1 ? 'moeda' : 'moedas'}`;
}

export const PAGINA_INICIAL = {
  ALUNO: '/aluno/extrato',
  PROFESSOR: '/professor/enviar',
  EMPRESA: '/empresa/vantagens',
};
