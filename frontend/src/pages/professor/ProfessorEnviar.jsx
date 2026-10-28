import { useEffect, useMemo, useState } from 'react';
import { api, ApiError } from '../../api';
import { Campo, Mensagem, useFormulario, useMensagem } from '../../components/Formulario';
import Saldo from '../../components/Saldo';
import useExtrato from '../../components/useExtrato';
import { formatarMoedas } from '../../formatar';

const VAZIO = { alunoId: '', valor: '', motivo: '' };

export default function ProfessorEnviar() {
  const { extrato, recarregar } = useExtrato();
  const [alunos, setAlunos] = useState(null);
  const [filtro, setFiltro] = useState('');
  const [enviando, setEnviando] = useState(false);
  const { valores, setValores, erros, setErros, alterar } = useFormulario(VAZIO);
  const [mensagem, mostrar] = useMensagem();

  useEffect(() => {
    api.get('/api/alunos').then(setAlunos).catch((e) => mostrar(e.message, 'erro'));
  }, [mostrar]);

  const alunosFiltrados = useMemo(() => {
    const termo = filtro.trim().toLowerCase();
    if (!alunos || !termo) return alunos ?? [];
    return alunos.filter((a) => `${a.nome} ${a.curso} ${a.email}`.toLowerCase().includes(termo)
      || String(a.id) === valores.alunoId);
  }, [alunos, filtro, valores.alunoId]);

  async function enviar(evento) {
    evento.preventDefault();
    const dados = {
      alunoId: valores.alunoId ? Number(valores.alunoId) : null,
      valor: valores.valor === '' ? null : Number(valores.valor),
      motivo: valores.motivo.trim(),
    };
    setEnviando(true);
    try {
      const envio = await api.post('/api/transacoes/envios', dados);
      mostrar(`${formatarMoedas(envio.valor)} enviada(s) para ${envio.alunoNome}. O aluno foi notificado por e-mail.`);
      setValores(VAZIO);
      setErros({});
      recarregar();
    } catch (e) {
      if (e instanceof ApiError) setErros(e.campos);
      mostrar(e.message, 'erro');
    } finally {
      setEnviando(false);
    }
  }

  return (
    <>
      <h1>Enviar moedas</h1>
      <p className="subtitulo">
        Reconheça o mérito dos seus alunos. A cada semestre você recebe 1.000 moedas, que se acumulam ao saldo.
      </p>

      <Saldo valor={extrato?.saldoMoedas} />
      <Mensagem mensagem={mensagem} />

      <section className="cartao">
        <h2>Novo reconhecimento</h2>
        <form onSubmit={enviar} noValidate>
          <div className="grade">
            <Campo rotulo="Aluno" name="alunoId" largo erro={erros.alunoId}
                   dica={alunos && `${alunos.length} aluno(s) da sua instituição.`}>
              <div className="seletor-aluno">
                <input type="search" placeholder="Filtrar por nome, curso ou e-mail..." value={filtro}
                       onChange={(e) => setFiltro(e.target.value)} aria-label="Filtrar alunos" />
                <select id="alunoId" name="alunoId" value={valores.alunoId} onChange={alterar}>
                  <option value="">{alunos ? 'Selecione o aluno...' : 'Carregando...'}</option>
                  {alunosFiltrados.map((a) => (
                    <option key={a.id} value={a.id}>{a.nome} — {a.curso}</option>
                  ))}
                </select>
              </div>
            </Campo>
            <Campo rotulo="Quantidade de moedas" name="valor" type="number" min="1" step="1"
                   max={extrato?.saldoMoedas || undefined} value={valores.valor} onChange={alterar} erro={erros.valor} />
            <Campo rotulo="Motivo do reconhecimento" name="motivo" largo erro={erros.motivo}
                   dica="Obrigatório. O aluno verá esta mensagem no e-mail e no extrato.">
              <textarea id="motivo" name="motivo" rows={3} maxLength={500} value={valores.motivo} onChange={alterar}
                        placeholder="Ex.: Excelente participação na aula de hoje." />
            </Campo>
          </div>
          <div className="acoes">
            <button type="submit" className="btn-primario" disabled={enviando}>
              {enviando ? 'Enviando...' : 'Enviar moedas'}
            </button>
          </div>
        </form>
      </section>
    </>
  );
}
