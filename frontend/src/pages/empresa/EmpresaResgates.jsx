import { useCallback, useEffect, useState } from 'react';
import { api } from '../../api';
import { Mensagem, useMensagem } from '../../components/Formulario';
import { formatarData, formatarMoedas } from '../../formatar';

export default function EmpresaResgates() {
  const [resgates, setResgates] = useState(null);
  const [codigo, setCodigo] = useState('');
  const [confirmando, setConfirmando] = useState(false);
  const [mensagem, mostrar] = useMensagem();

  const carregar = useCallback(async () => {
    try {
      setResgates(await api.get('/api/transacoes/resgates'));
    } catch (e) {
      mostrar(e.message, 'erro');
    }
  }, [mostrar]);

  useEffect(() => { carregar(); }, [carregar]);

  async function confirmar(codigoCupom) {
    setConfirmando(true);
    try {
      const resgate = await api.post(`/api/transacoes/cupons/${encodeURIComponent(codigoCupom.trim())}/confirmacao`);
      mostrar(`Cupom ${resgate.cupomCodigo} confirmado: ${resgate.vantagemNome} para ${resgate.alunoNome}.`);
      setCodigo('');
      await carregar();
    } catch (e) {
      mostrar(e.message, 'erro');
    } finally {
      setConfirmando(false);
    }
  }

  function aoEnviar(evento) {
    evento.preventDefault();
    if (codigo.trim()) confirmar(codigo);
  }

  const pendentes = resgates?.filter((r) => !r.cupomUtilizado).length ?? 0;

  return (
    <>
      <h1>Resgates</h1>
      <p className="subtitulo">
        Confira o código apresentado pelo aluno com o recebido por e-mail e registre o uso do cupom.
      </p>
      <Mensagem mensagem={mensagem} />

      <section className="cartao">
        <h2>Conferir cupom</h2>
        <form className="linha-form" onSubmit={aoEnviar}>
          <input value={codigo} onChange={(e) => setCodigo(e.target.value.toUpperCase())} placeholder="XXXX-XXXX"
                 aria-label="Código do cupom" maxLength={20} className="entrada-cupom" />
          <button type="submit" className="btn-primario" disabled={confirmando || !codigo.trim()}>Confirmar uso</button>
        </form>
      </section>

      <section className="cartao">
        <h2>Histórico {resgates && <span className="texto-suave">({pendentes} pendente{pendentes === 1 ? '' : 's'})</span>}</h2>
        <div className="tabela-container">
          <table>
            <thead>
              <tr><th>Data</th><th>Aluno</th><th>Vantagem</th><th className="num">Valor</th><th>Código</th><th>Situação</th></tr>
            </thead>
            <tbody>
              {!resgates && <tr><td colSpan={6} className="vazio">Carregando...</td></tr>}
              {resgates?.length === 0 && <tr><td colSpan={6} className="vazio">Nenhum resgate ainda.</td></tr>}
              {resgates?.map((r) => (
                <tr key={r.id}>
                  <td>{formatarData(r.data)}</td>
                  <td>{r.alunoNome}</td>
                  <td>{r.vantagemNome}</td>
                  <td className="num">{formatarMoedas(r.valor)}</td>
                  <td><code className="cupom">{r.cupomCodigo}</code></td>
                  <td>
                    {r.cupomUtilizado
                      ? <span className="selo selo-neutro">Utilizado</span>
                      : (
                        <button type="button" className="btn-secundario btn-pequeno" disabled={confirmando}
                                onClick={() => confirmar(r.cupomCodigo)}>
                          Confirmar uso
                        </button>
                      )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}
