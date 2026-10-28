import { Link } from 'react-router-dom';
import Saldo from '../../components/Saldo';
import useExtrato from '../../components/useExtrato';
import { formatarData, formatarMoedas } from '../../formatar';

export default function AlunoExtrato() {
  const { extrato, erro } = useExtrato();
  const transacoes = extrato?.transacoes ?? [];

  return (
    <>
      <h1>Extrato</h1>
      <p className="subtitulo">Moedas recebidas dos professores e trocas por vantagens.</p>

      <Saldo valor={extrato?.saldoMoedas}>
        <Link className="btn-primario" to="/aluno/vantagens">Trocar moedas</Link>
      </Saldo>

      <section className="cartao">
        <h2>Transações</h2>
        <div className="tabela-container">
          <table>
            <thead>
              <tr><th>Data</th><th>Tipo</th><th>Descrição</th><th>Cupom</th><th className="num">Valor</th></tr>
            </thead>
            <tbody>
              {erro && <tr><td colSpan={5} className="vazio">{erro}</td></tr>}
              {!erro && !extrato && <tr><td colSpan={5} className="vazio">Carregando...</td></tr>}
              {extrato && transacoes.length === 0 && (
                <tr><td colSpan={5} className="vazio">Nenhuma transação ainda. Participe das aulas para ganhar moedas!</td></tr>
              )}
              {transacoes.map((t) => (
                <tr key={t.id}>
                  <td>{formatarData(t.data)}</td>
                  <td>
                    <span className={`selo ${t.tipo === 'ENVIO' ? 'selo-entrada' : 'selo-saida'}`}>
                      {t.tipo === 'ENVIO' ? 'Recebimento' : 'Troca'}
                    </span>
                  </td>
                  <td className="quebra">
                    {t.tipo === 'ENVIO'
                      ? <><strong>Prof. {t.professorNome}</strong><br /><span className="texto-suave">“{t.motivo}”</span></>
                      : <><strong>{t.vantagemNome}</strong><br /><span className="texto-suave">{t.empresaNome}</span></>}
                  </td>
                  <td>
                    {t.cupomCodigo && (
                      <>
                        <code className="cupom">{t.cupomCodigo}</code>{' '}
                        <span className={`selo ${t.cupomUtilizado ? 'selo-neutro' : 'selo-alerta'}`}>
                          {t.cupomUtilizado ? 'Utilizado' : 'Disponível'}
                        </span>
                      </>
                    )}
                  </td>
                  <td className={`num ${t.tipo === 'ENVIO' ? 'positivo' : 'negativo'}`}>
                    {t.tipo === 'ENVIO' ? '+' : '−'}{formatarMoedas(t.valor)}
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
