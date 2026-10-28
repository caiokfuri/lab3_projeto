import { useCallback, useEffect, useRef, useState } from 'react';
import { api, ApiError } from '../../api';
import { useAuth } from '../../auth/AuthContext';
import { Campo, Mensagem, useFormulario, useMensagem } from '../../components/Formulario';
import { formatarMoedas } from '../../formatar';

const VAZIO = { nome: '', descricao: '', custoMoedas: '' };

export default function EmpresaVantagens() {
  const { usuario } = useAuth();
  const [vantagens, setVantagens] = useState(null);
  const [editando, setEditando] = useState(null);
  const [foto, setFoto] = useState(null);
  const [previa, setPrevia] = useState(null);
  const [salvando, setSalvando] = useState(false);
  const { valores, setValores, erros, setErros, alterar } = useFormulario(VAZIO);
  const [mensagem, mostrar] = useMensagem();
  const inputFoto = useRef();
  const formulario = useRef();

  const carregar = useCallback(async () => {
    try {
      setVantagens(await api.get(`/api/vantagens?empresaId=${usuario.id}`));
    } catch (e) {
      mostrar(e.message, 'erro');
    }
  }, [usuario.id, mostrar]);

  useEffect(() => { carregar(); }, [carregar]);

  // Libera a URL temporária da prévia da foto escolhida.
  useEffect(() => () => { if (previa?.startsWith('blob:')) URL.revokeObjectURL(previa); }, [previa]);

  function escolherFoto(evento) {
    const arquivo = evento.target.files[0] ?? null;
    setFoto(arquivo);
    setPrevia(arquivo ? URL.createObjectURL(arquivo) : editando?.fotoUrl ?? null);
    setErros((e) => ({ ...e, foto: undefined }));
  }

  function modoCadastro() {
    setEditando(null);
    setValores(VAZIO);
    setErros({});
    setFoto(null);
    setPrevia(null);
    if (inputFoto.current) inputFoto.current.value = '';
  }

  function modoEdicao(vantagem) {
    setEditando(vantagem);
    setValores({ nome: vantagem.nome, descricao: vantagem.descricao, custoMoedas: String(vantagem.custoMoedas) });
    setErros({});
    setFoto(null);
    setPrevia(vantagem.fotoUrl);
    if (inputFoto.current) inputFoto.current.value = '';
    formulario.current?.scrollIntoView({ behavior: 'smooth' });
  }

  async function salvar(evento) {
    evento.preventDefault();
    const dados = new FormData();
    dados.append('nome', valores.nome.trim());
    dados.append('descricao', valores.descricao.trim());
    dados.append('custoMoedas', valores.custoMoedas);
    if (foto) dados.append('foto', foto);

    setSalvando(true);
    try {
      if (editando) {
        await api.put(`/api/vantagens/${editando.id}`, dados);
        mostrar('Vantagem atualizada.');
      } else {
        await api.post('/api/vantagens', dados);
        mostrar('Vantagem cadastrada. Ela já aparece para os alunos.');
      }
      modoCadastro();
      await carregar();
    } catch (e) {
      if (e instanceof ApiError) setErros(e.campos);
      mostrar(e.message, 'erro');
    } finally {
      setSalvando(false);
    }
  }

  async function remover(vantagem) {
    if (!window.confirm(`Remover a vantagem "${vantagem.nome}"?`)) return;
    try {
      await api.delete(`/api/vantagens/${vantagem.id}`);
      if (editando?.id === vantagem.id) modoCadastro();
      mostrar('Vantagem removida.');
      await carregar();
    } catch (e) {
      mostrar(e.message, 'erro');
    }
  }

  return (
    <>
      <h1>Minhas vantagens</h1>
      <p className="subtitulo">Produtos e descontos que os alunos podem obter trocando moedas.</p>
      <Mensagem mensagem={mensagem} />

      <section className="cartao" ref={formulario}>
        <h2>{editando ? `Editando: ${editando.nome}` : 'Nova vantagem'}</h2>
        <form onSubmit={salvar} noValidate>
          <div className="form-vantagem">
            <div className="grade">
              <Campo rotulo="Nome" name="nome" largo value={valores.nome} onChange={alterar} erro={erros.nome}
                     maxLength={100} placeholder="Ex.: 20% de desconto no almoço" />
              <Campo rotulo="Descrição" name="descricao" largo erro={erros.descricao}>
                <textarea id="descricao" name="descricao" rows={3} maxLength={500} value={valores.descricao} onChange={alterar} />
              </Campo>
              <Campo rotulo="Custo (em moedas)" name="custoMoedas" type="number" min="1" step="1"
                     value={valores.custoMoedas} onChange={alterar} erro={erros.custoMoedas} />
              <Campo rotulo="Foto do produto" name="foto" erro={erros.foto}
                     dica={editando ? 'Envie outra imagem só se quiser trocar a atual.' : 'JPG, PNG, GIF ou WEBP, até 5 MB.'}>
                <input id="foto" name="foto" type="file" accept="image/png,image/jpeg,image/gif,image/webp"
                       ref={inputFoto} onChange={escolherFoto} />
              </Campo>
            </div>
            <div className="previa">
              {previa ? <img src={previa} alt="Prévia da foto" /> : <span>Prévia da foto</span>}
            </div>
          </div>
          <div className="acoes">
            <button type="submit" className="btn-primario" disabled={salvando}>
              {salvando ? 'Salvando...' : editando ? 'Salvar alterações' : 'Cadastrar vantagem'}
            </button>
            {editando && <button type="button" className="btn-secundario" onClick={modoCadastro}>Cancelar edição</button>}
          </div>
        </form>
      </section>

      <section className="cartao">
        <h2>Vantagens cadastradas</h2>
        {!vantagens && <p className="vazio">Carregando...</p>}
        {vantagens?.length === 0 && <p className="vazio">Nenhuma vantagem cadastrada.</p>}
        <div className="grade-vantagens">
          {vantagens?.map((v) => (
            <article key={v.id} className="vantagem">
              <img src={v.fotoUrl} alt={v.nome} loading="lazy" />
              <div className="vantagem-corpo">
                <h3>{v.nome}</h3>
                <p>{v.descricao}</p>
                <div className="vantagem-rodape">
                  <span className="preco">{formatarMoedas(v.custoMoedas)}</span>
                  <span className="acoes-linha">
                    <button type="button" className="btn-secundario btn-pequeno" onClick={() => modoEdicao(v)}>Editar</button>
                    <button type="button" className="btn-perigo btn-pequeno" onClick={() => remover(v)}>Remover</button>
                  </span>
                </div>
              </div>
            </article>
          ))}
        </div>
      </section>
    </>
  );
}
