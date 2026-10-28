import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../../api';
import { useAuth } from '../../auth/AuthContext';
import AlunoForm from '../../components/AlunoForm';
import { Mensagem, useMensagem } from '../../components/Formulario';

export default function AlunoPerfil() {
  const { usuario, atualizarUsuario, encerrarSessaoLocal } = useAuth();
  const navegar = useNavigate();
  const [aluno, setAluno] = useState(null);
  const [mensagem, mostrar] = useMensagem();
  const aoErro = useCallback((e) => mostrar(e.message, 'erro'), [mostrar]);

  useEffect(() => {
    api.get(`/api/alunos/${usuario.id}`).then(setAluno).catch(aoErro);
  }, [usuario.id, aoErro]);

  async function salvar(dados) {
    const atualizado = await api.put(`/api/alunos/${usuario.id}`, dados);
    setAluno(atualizado);
    atualizarUsuario({ ...usuario, nome: atualizado.nome, email: atualizado.email, login: atualizado.login });
    mostrar('Dados atualizados com sucesso.');
  }

  async function removerConta() {
    if (!window.confirm('Remover sua conta? Esta ação não pode ser desfeita.')) return;
    try {
      await api.delete(`/api/alunos/${usuario.id}`);
      encerrarSessaoLocal();
      navegar('/login', { replace: true, state: { mensagem: 'Sua conta foi removida.' } });
    } catch (e) {
      aoErro(e);
    }
  }

  return (
    <>
      <h1>Meu perfil</h1>
      <p className="subtitulo">Consulte e atualize seus dados cadastrais.</p>
      <Mensagem mensagem={mensagem} />
      <section className="cartao">
        {aluno ? (
          <AlunoForm inicial={aluno} textoBotao="Salvar alterações" aoSalvar={salvar} aoErro={aoErro}>
            <button type="button" className="btn-perigo empurra-direita" onClick={removerConta}>Remover minha conta</button>
          </AlunoForm>
        ) : <p className="vazio">Carregando...</p>}
      </section>
    </>
  );
}
