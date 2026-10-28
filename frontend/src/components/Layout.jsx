import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { PAGINA_INICIAL } from '../formatar';

const MENUS = {
  ALUNO: [
    ['/aluno/extrato', 'Extrato'],
    ['/aluno/vantagens', 'Vantagens'],
    ['/aluno/perfil', 'Meu perfil'],
  ],
  PROFESSOR: [
    ['/professor/enviar', 'Enviar moedas'],
    ['/professor/extrato', 'Extrato'],
    ['/professor/perfil', 'Meu perfil'],
  ],
  EMPRESA: [
    ['/empresa/vantagens', 'Minhas vantagens'],
    ['/empresa/resgates', 'Resgates'],
    ['/empresa/perfil', 'Meu perfil'],
  ],
};

const NOME_PERFIL = { ALUNO: 'Aluno', PROFESSOR: 'Professor', EMPRESA: 'Empresa parceira' };

export default function Layout({ children }) {
  const { usuario, sair } = useAuth();
  const navegar = useNavigate();

  async function aoSair() {
    await sair();
    navegar('/login', { replace: true });
  }

  return (
    <>
      <header className="topo">
        <div className="conteudo topo-interno">
          <Link className="marca" to={PAGINA_INICIAL[usuario.perfil]}>
            <img src="/moeda.svg" alt="" width="24" height="24" /> Moeda Estudantil
          </Link>
          <nav>
            {MENUS[usuario.perfil].map(([para, rotulo]) => (
              <NavLink key={para} to={para}>{rotulo}</NavLink>
            ))}
          </nav>
          <div className="usuario-topo">
            <span>
              <strong>{usuario.nome}</strong>
              <small>{NOME_PERFIL[usuario.perfil]}</small>
            </span>
            <button type="button" className="btn-sair" onClick={aoSair}>Sair</button>
          </div>
        </div>
      </header>
      <main className="conteudo">{children}</main>
    </>
  );
}
