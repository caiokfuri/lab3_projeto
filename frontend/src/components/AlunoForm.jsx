import { useEffect, useState } from 'react';
import { api, ApiError } from '../api';
import { formatarCpf } from '../formatar';
import { Campo, useFormulario } from './Formulario';

const VAZIO = {
  nome: '', email: '', cpf: '', rg: '', endereco: '', instituicaoId: '', curso: '', login: '', senha: '',
};

export default function AlunoForm({ inicial, textoBotao, aoSalvar, aoErro, children }) {
  const edicao = Boolean(inicial);
  const { valores, setValores, erros, setErros, alterar } = useFormulario(VAZIO);
  const [instituicoes, setInstituicoes] = useState([]);
  const [salvando, setSalvando] = useState(false);

  useEffect(() => {
    api.get('/api/instituicoes').then(setInstituicoes).catch((e) => aoErro?.(e));
  }, [aoErro]);

  useEffect(() => {
    if (inicial) {
      setValores({ ...VAZIO, ...inicial, cpf: formatarCpf(inicial.cpf), instituicaoId: String(inicial.instituicaoId), senha: '' });
    }
  }, [inicial, setValores]);

  async function enviar(evento) {
    evento.preventDefault();
    const dados = Object.fromEntries(Object.entries(valores).map(([k, v]) => [k, String(v).trim()]));
    dados.instituicaoId = dados.instituicaoId ? Number(dados.instituicaoId) : null;

    setSalvando(true);
    try {
      await aoSalvar(dados);
      setErros({});
    } catch (e) {
      if (e instanceof ApiError) setErros(e.campos);
      aoErro?.(e);
    } finally {
      setSalvando(false);
    }
  }

  return (
    <form onSubmit={enviar} noValidate>
      <div className="grade">
        <Campo rotulo="Nome completo" name="nome" largo value={valores.nome} onChange={alterar} erro={erros.nome} maxLength={255} />
        <Campo rotulo="E-mail" name="email" type="email" value={valores.email} onChange={alterar} erro={erros.email}
               dica="Você será notificado por e-mail ao receber moedas." />
        <Campo rotulo="CPF" name="cpf" value={valores.cpf} onChange={alterar} erro={erros.cpf} placeholder="000.000.000-00" maxLength={14} />
        <Campo rotulo="RG" name="rg" value={valores.rg} onChange={alterar} erro={erros.rg} maxLength={20} />
        <Campo rotulo="Endereço" name="endereco" largo value={valores.endereco} onChange={alterar} erro={erros.endereco} maxLength={255} />
        <Campo rotulo="Instituição de ensino" name="instituicaoId" erro={erros.instituicaoId}>
          <select id="instituicaoId" name="instituicaoId" value={valores.instituicaoId} onChange={alterar}>
            <option value="">Selecione...</option>
            {instituicoes.map((i) => <option key={i.id} value={i.id}>{i.nome}</option>)}
          </select>
        </Campo>
        <Campo rotulo="Curso" name="curso" value={valores.curso} onChange={alterar} erro={erros.curso} maxLength={255} />
        <Campo rotulo="Login" name="login" value={valores.login} onChange={alterar} erro={erros.login}
               autoComplete="username" maxLength={50} />
        <Campo rotulo="Senha" name="senha" type="password" value={valores.senha} onChange={alterar} erro={erros.senha}
               autoComplete="new-password"
               dica={edicao ? 'Deixe em branco para manter a senha atual.' : 'Mínimo de 6 caracteres.'} />
      </div>
      <div className="acoes">
        <button type="submit" className="btn-primario" disabled={salvando}>
          {salvando ? 'Salvando...' : textoBotao}
        </button>
        {children}
      </div>
    </form>
  );
}
