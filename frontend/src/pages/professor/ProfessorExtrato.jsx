import { Link } from 'react-router-dom';
import Saldo from '../../components/Saldo';
import useExtrato from '../../components/useExtrato';
import { formatarData, formatarMoedas } from '../../formatar';

export default function ProfessorExtrato() {
  const { extrato, erro } = useExtrato();
  const envios = extrato?.transacoes ?? [];
  const totalEnviado = envios.reduce((soma, t) => soma + t.valor, 0);

  return (
    <>
      <h1>Extrato</h1>
      <p className="subtitulo">Moedas enviadas aos seus alunos.</p>

      <Saldo valor={extrato?.saldoMoedas}>
        <div className="saldo-extra">
          <span className="saldo-rotulo">Total enviado</span>
          <span>{formatarMoedas(totalEnviado)}</span>
        </div>
        <Link className="btn-primario" to="/professor/enviar">Enviar moedas</Link>
      </Saldo>

      <section className="cartao">
        <h2>Envios</h2>
        <div className="tabela-container">
          <table>
            <thead>
              <tr><th>Data</th><th>Aluno</th><th>Motivo</th><th className="num">Valor</th></tr>
            </thead>
            <tbody>
              {erro && <tr><td colSpan={4} className="vazio">{erro}</td></tr>}
              {!erro && !extrato && <tr><td colSpan={4} className="vazio">Carregando...</td></tr>}
              {extrato && envios.length === 0 && <tr><td colSpan={4} className="vazio">Nenhum envio realizado.</td></tr>}
              {envios.map((t) => (
                <tr key={t.id}>
                  <td>{formatarData(t.data)}</td>
                  <td>{t.alunoNome}</td>
                  <td className="quebra">{t.motivo}</td>
                  <td className="num negativo">−{formatarMoedas(t.valor)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}
