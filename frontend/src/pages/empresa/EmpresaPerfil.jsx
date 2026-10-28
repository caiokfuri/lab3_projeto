import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../../api';
import { useAuth } from '../../auth/AuthContext';
import EmpresaForm from '../../components/EmpresaForm';
import { Mensagem, useMensagem } from '../../components/Formulario';

export default function EmpresaPerfil() {
  const { usuario, atualizarUsuario, encerrarSessaoLocal } = useAuth();
  const navegar = useNavigate();
  const [empresa, setEmpresa] = useState(null);
  const [mensagem, mostrar] = useMensagem();
  const aoErro = useCallback((e) => mostrar(e.message, 'erro'), [mostrar]);

  useEffect(() => {
    api.get(`/api/empresas/${usuario.id}`).then(setEmpresa).catch(aoErro);
  }, [usuario.id, aoErro]);

  async function salvar(dados) {
    const atualizada = await api.put(`/api/empresas/${usuario.id}`, dados);
    setEmpresa(atualizada);
    atualizarUsuario({ ...usuario, nome: atualizada.nome, email: atualizada.email, login: atualizada.login });
    mostrar('Dados atualizados com sucesso.');
  }

  async function removerConta() {
    if (!window.confirm('Remover a conta da empresa e todas as suas vantagens? Esta ação não pode ser desfeita.')) return;
    try {
      await api.delete(`/api/empresas/${usuario.id}`);
      encerrarSessaoLocal();
      navegar('/login', { replace: true, state: { mensagem: 'A conta da empresa foi removida.' } });
    } catch (e) {
      aoErro(e);
    }
  }

  return (
    <>
      <h1>Meu perfil</h1>
      <p className="subtitulo">
        Dados da empresa parceira{empresa && ` · ${empresa.quantidadeVantagens} vantagem(ns) cadastrada(s)`}.
      </p>
      <Mensagem mensagem={mensagem} />
      <section className="cartao">
        {empresa ? (
          <EmpresaForm inicial={empresa} textoBotao="Salvar alterações" aoSalvar={salvar} aoErro={aoErro}>
            <button type="button" className="btn-perigo empurra-direita" onClick={removerConta}>Remover conta</button>
          </EmpresaForm>
        ) : <p className="vazio">Carregando...</p>}
      </section>
    </>
  );
}
