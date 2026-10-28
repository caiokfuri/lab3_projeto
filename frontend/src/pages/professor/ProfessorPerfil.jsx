import { useEffect, useState } from 'react';
import { api } from '../../api';
import { useAuth } from '../../auth/AuthContext';
import { formatarCpf, formatarMoedas } from '../../formatar';

export default function ProfessorPerfil() {
  const { usuario } = useAuth();
  const [professor, setProfessor] = useState(null);
  const [erro, setErro] = useState(null);

  useEffect(() => {
    api.get(`/api/professores/${usuario.id}`).then(setProfessor).catch((e) => setErro(e.message));
  }, [usuario.id]);

  return (
    <>
      <h1>Meu perfil</h1>
      <p className="subtitulo">
        Professores são cadastrados pela instituição de ensino parceira. Para alterar seus dados, procure a secretaria.
      </p>
      {erro && <div className="mensagem erro">{erro}</div>}
      <section className="cartao">
        {professor ? (
          <dl className="dados">
            <div><dt>Nome</dt><dd>{professor.nome}</dd></div>
            <div><dt>Instituição de ensino</dt><dd>{professor.instituicaoNome}</dd></div>
            <div><dt>Departamento</dt><dd>{professor.departamento}</dd></div>
            <div><dt>CPF</dt><dd>{formatarCpf(professor.cpf)}</dd></div>
            <div><dt>E-mail</dt><dd>{professor.email}</dd></div>
            <div><dt>Login</dt><dd>{professor.login}</dd></div>
            <div><dt>Saldo</dt><dd>{formatarMoedas(professor.saldoMoedas)}</dd></div>
          </dl>
        ) : !erro && <p className="vazio">Carregando...</p>}
      </section>
    </>
  );
}
