import { useEffect, useState } from 'react';
import { ApiError } from '../api';
import { formatarCnpj } from '../formatar';
import { Campo, useFormulario } from './Formulario';

const VAZIO = { nome: '', email: '', cnpj: '', login: '', senha: '' };

export default function EmpresaForm({ inicial, textoBotao, aoSalvar, aoErro, children }) {
  const edicao = Boolean(inicial);
  const { valores, setValores, erros, setErros, alterar } = useFormulario(VAZIO);
  const [salvando, setSalvando] = useState(false);

  useEffect(() => {
    if (inicial) setValores({ ...VAZIO, ...inicial, cnpj: formatarCnpj(inicial.cnpj), senha: '' });
  }, [inicial, setValores]);

  async function enviar(evento) {
    evento.preventDefault();
    const dados = Object.fromEntries(Object.entries(valores).map(([k, v]) => [k, String(v).trim()]));

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
        <Campo rotulo="Nome da empresa" name="nome" largo value={valores.nome} onChange={alterar} erro={erros.nome} maxLength={255} />
        <Campo rotulo="E-mail" name="email" type="email" value={valores.email} onChange={alterar} erro={erros.email}
               dica="Recebe os e-mails de conferência dos cupons." />
        <Campo rotulo="CNPJ" name="cnpj" value={valores.cnpj} onChange={alterar} erro={erros.cnpj}
               placeholder="00.000.000/0000-00" maxLength={18} />
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
