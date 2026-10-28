import { useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../api';
import { useAuth } from '../auth/AuthContext';
import EmpresaForm from '../components/EmpresaForm';
import { Mensagem, useMensagem } from '../components/Formulario';

export default function CadastroEmpresa() {
  const { entrar } = useAuth();
  const navegar = useNavigate();
  const [mensagem, mostrar] = useMensagem();
  const aoErro = useCallback((e) => mostrar(e.message, 'erro'), [mostrar]);

  async function cadastrar(dados) {
    await api.post('/api/empresas', dados);
    await entrar(dados.login, dados.senha);
    navegar('/empresa/vantagens', { replace: true });
  }

  return (
    <div className="pagina-acesso">
      <div className="acesso largo">
        <Link to="/login" className="voltar">← Voltar para o login</Link>
        <h1>Cadastro de empresa parceira</h1>
        <p className="subtitulo">Ofereça vantagens aos alunos em troca de moedas estudantis.</p>
        <Mensagem mensagem={mensagem} />
        <section className="cartao">
          <EmpresaForm textoBotao="Cadastrar empresa" aoSalvar={cadastrar} aoErro={aoErro} />
        </section>
      </div>
    </div>
  );
}
