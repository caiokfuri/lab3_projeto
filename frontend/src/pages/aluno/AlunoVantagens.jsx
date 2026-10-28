import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../api';
import { Mensagem, Modal, useMensagem } from '../../components/Formulario';
import Saldo from '../../components/Saldo';
import useExtrato from '../../components/useExtrato';
import { formatarMoedas } from '../../formatar';

export default function AlunoVantagens() {
  const { extrato, recarregar } = useExtrato();
  const [vantagens, setVantagens] = useState(null);
  const [busca, setBusca] = useState('');
  const [escolhida, setEscolhida] = useState(null);
  const [cupom, setCupom] = useState(null);
  const [trocando, setTrocando] = useState(false);
  const [mensagem, mostrar] = useMensagem();
  const saldo = extrato?.saldoMoedas ?? 0;

  useEffect(() => {
    api.get('/api/vantagens').then(setVantagens).catch((e) => mostrar(e.message, 'erro'));
  }, [mostrar]);

  const filtradas = useMemo(() => {
    const termo = busca.trim().toLowerCase();
    if (!vantagens || !termo) return vantagens;
    return vantagens.filter((v) => `${v.nome} ${v.descricao} ${v.empresaNome}`.toLowerCase().includes(termo));
  }, [vantagens, busca]);

  async function confirmarTroca() {
    setTrocando(true);
    try {
      const resgate = await api.post('/api/transacoes/resgates', { vantagemId: escolhida.id });
      setEscolhida(null);
      setCupom(resgate);
      recarregar();
    } catch (e) {
      setEscolhida(null);
      mostrar(e.message, 'erro');
    } finally {
      setTrocando(false);
    }
  }

  return (
    <>
      <h1>Vantagens</h1>
      <p className="subtitulo">Troque suas moedas por produtos e descontos das empresas parceiras.</p>

      <Saldo valor={extrato?.saldoMoedas}>
        <input type="search" className="busca" placeholder="Buscar vantagem ou empresa..." value={busca}
               onChange={(e) => setBusca(e.target.value)} aria-label="Buscar vantagens" />
      </Saldo>
      <Mensagem mensagem={mensagem} />

      {!filtradas && <p className="vazio">Carregando...</p>}
      {filtradas?.length === 0 && (
        <p className="vazio cartao">{busca ? 'Nenhuma vantagem encontrada.' : 'Nenhuma vantagem cadastrada ainda.'}</p>
      )}

      <div className="grade-vantagens">
        {filtradas?.map((v) => {
          const falta = v.custoMoedas - saldo;
          return (
            <article key={v.id} className="vantagem">
              <img src={v.fotoUrl} alt={v.nome} loading="lazy" />
              <div className="vantagem-corpo">
                <span className="texto-suave">{v.empresaNome}</span>
                <h3>{v.nome}</h3>
                <p>{v.descricao}</p>
                <div className="vantagem-rodape">
                  <span className="preco">{formatarMoedas(v.custoMoedas)}</span>
                  <button type="button" className="btn-primario btn-pequeno" disabled={falta > 0}
                          title={falta > 0 ? `Faltam ${formatarMoedas(falta)}` : undefined}
                          onClick={() => setEscolhida(v)}>
                    Trocar
                  </button>
                </div>
              </div>
            </article>
          );
        })}
      </div>

      {escolhida && (
        <Modal titulo="Confirmar troca" aoFechar={() => !trocando && setEscolhida(null)}>
          <p>
            Trocar <strong>{formatarMoedas(escolhida.custoMoedas)}</strong> por <strong>{escolhida.nome}</strong>{' '}
            ({escolhida.empresaNome})?
          </p>
          <p className="texto-suave">Saldo após a troca: {formatarMoedas(saldo - escolhida.custoMoedas)}</p>
          <div className="acoes">
            <button type="button" className="btn-primario" onClick={confirmarTroca} disabled={trocando}>
              {trocando ? 'Trocando...' : 'Confirmar troca'}
            </button>
            <button type="button" className="btn-secundario" onClick={() => setEscolhida(null)} disabled={trocando}>
              Cancelar
            </button>
          </div>
        </Modal>
      )}

      {cupom && (
        <Modal titulo="Troca realizada!" aoFechar={() => setCupom(null)}>
          <p>Apresente este código ao parceiro <strong>{cupom.empresaNome}</strong> para usar <strong>{cupom.vantagemNome}</strong>:</p>
          <p className="cupom-grande">{cupom.cupomCodigo}</p>
          <p className="texto-suave">
            Enviamos o cupom para o seu e-mail, e o parceiro recebeu o mesmo código para conferência.
            Ele também fica disponível no seu <Link to="/aluno/extrato">extrato</Link>.
          </p>
          <div className="acoes">
            <button type="button" className="btn-primario" onClick={() => setCupom(null)}>Fechar</button>
          </div>
        </Modal>
      )}
    </>
  );
}
