import { useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../api';
import { useAuth } from '../auth/AuthContext';
import AlunoForm from '../components/AlunoForm';
import { Mensagem, useMensagem } from '../components/Formulario';

export default function CadastroAluno() {
  const { entrar } = useAuth();
  const navegar = useNavigate();
  const [mensagem, mostrar] = useMensagem();
  const aoErro = useCallback((e) => mostrar(e.message, 'erro'), [mostrar]);

  async function cadastrar(dados) {
    await api.post('/api/alunos', dados);
    await entrar(dados.login, dados.senha);
    navegar('/aluno/extrato', { replace: true });
  }

  return (
    <div className="pagina-acesso">
      <div className="acesso largo">
        <Link to="/login" className="voltar">← Voltar para o login</Link>
        <h1>Cadastro de aluno</h1>
        <p className="subtitulo">Participe do sistema de mérito e troque suas moedas por vantagens.</p>
        <Mensagem mensagem={mensagem} />
        <section className="cartao">
          <AlunoForm textoBotao="Criar conta" aoSalvar={cadastrar} aoErro={aoErro} />
        </section>
      </div>
    </div>
  );
}
