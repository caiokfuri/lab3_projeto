package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.Cupom;
import br.pucminas.moedaestudantil.model.Transacao;
import br.pucminas.moedaestudantil.model.TransacaoEnvio;
import br.pucminas.moedaestudantil.model.TransacaoResgate;
import br.pucminas.moedaestudantil.model.Vantagem;

import java.time.LocalDateTime;

/** Linha do extrato. Campos que não se aplicam ao tipo da transação vêm nulos. */
public record TransacaoResponse(
        Long id,
        String tipo,
        LocalDateTime data,
        int valor,
        String alunoNome,
        // ENVIO
        String motivo,
        String professorNome,
        // RESGATE
        String vantagemNome,
        String empresaNome,
        String cupomCodigo,
        Boolean cupomUtilizado
) {

    public static TransacaoResponse from(Transacao t) {
        if (t instanceof TransacaoEnvio e) {
            return new TransacaoResponse(e.getId(), "ENVIO", e.getData(), e.getValor(), e.getAluno().getNome(),
                    e.getMotivo(), e.getProfessor().getNome(), null, null, null, null);
        }
        TransacaoResgate r = (TransacaoResgate) t;
        Vantagem v = r.getVantagem();
        Cupom c = r.getCupom();
        return new TransacaoResponse(r.getId(), "RESGATE", r.getData(), r.getValor(), r.getAluno().getNome(),
                null, null, v.getNome(), v.getEmpresa().getNome(),
                c != null ? c.getCodigo() : null, c != null ? c.isUtilizado() : null);
    }
}
