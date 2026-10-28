import { useState } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { Campo } from '../components/Formulario';
import { PAGINA_INICIAL } from '../formatar';

export default function Login() {
  const { usuario, entrar } = useAuth();
  const navegar = useNavigate();
  const local = useLocation();
  const [login, setLogin] = useState('');
  const [senha, setSenha] = useState('');
  const [erro, setErro] = useState(null);
  const [entrando, setEntrando] = useState(false);

  if (usuario) return <Navigate to={PAGINA_INICIAL[usuario.perfil]} replace />;

  async function enviar(evento) {
    evento.preventDefault();
    if (!login.trim() || !senha) {
      setErro('Informe login e senha.');
      return;
    }
    setEntrando(true);
    setErro(null);
    try {
      const logado = await entrar(login.trim(), senha);
      const destino = local.state?.de?.startsWith(`/${logado.perfil.toLowerCase()}`) ? local.state.de : PAGINA_INICIAL[logado.perfil];
      navegar(destino, { replace: true });
    } catch (e) {
      setErro(e.message);
      setEntrando(false);
    }
  }

  return (
    <div className="pagina-acesso">
      <div className="acesso">
        <div className="acesso-marca">
          <img src="/moeda.svg" alt="" width="40" height="40" />
          <div>
            <h1>Moeda Estudantil</h1>
            <p>Reconhecimento do mérito estudantil por meio de moedas virtuais.</p>
          </div>
        </div>

        <section className="cartao">
          <h2>Entrar</h2>
          {local.state?.mensagem && <div className="mensagem sucesso">{local.state.mensagem}</div>}
          {erro && <div className="mensagem erro" role="alert">{erro}</div>}
          <form onSubmit={enviar} noValidate>
            <div className="pilha">
              <Campo rotulo="Login" name="login" value={login} onChange={(e) => setLogin(e.target.value)}
                     autoComplete="username" autoFocus />
              <Campo rotulo="Senha" name="senha" type="password" value={senha} onChange={(e) => setSenha(e.target.value)}
                     autoComplete="current-password" />
            </div>
            <div className="acoes">
              <button type="submit" className="btn-primario largo" disabled={entrando}>
                {entrando ? 'Entrando...' : 'Entrar'}
              </button>
            </div>
          </form>
        </section>

        <section className="cartao cadastros">
          <p>Ainda não tem conta?</p>
          <div className="acoes">
            <Link className="btn-secundario" to="/cadastro/aluno">Sou aluno</Link>
            <Link className="btn-secundario" to="/cadastro/empresa">Sou empresa parceira</Link>
          </div>
          <p className="dica">
            Professores já são cadastrados pela instituição. Na demonstração: <code>prof.ana</code> / <code>123456</code>.
          </p>
        </section>
      </div>
    </div>
  );
}
