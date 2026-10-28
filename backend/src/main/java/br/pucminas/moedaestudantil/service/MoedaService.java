package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.dao.AlunoDAO;
import br.pucminas.moedaestudantil.dao.ProfessorDAO;
import br.pucminas.moedaestudantil.dao.TransacaoDAO;
import br.pucminas.moedaestudantil.dao.VantagemDAO;
import br.pucminas.moedaestudantil.dto.EnvioRequest;
import br.pucminas.moedaestudantil.dto.ExtratoResponse;
import br.pucminas.moedaestudantil.dto.TransacaoResponse;
import br.pucminas.moedaestudantil.dto.UsuarioLogado;
import br.pucminas.moedaestudantil.exception.RecursoNaoEncontradoException;
import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/** Movimentação de moedas: envio professor → aluno, troca por vantagens, extratos e conferência de cupons. */
@Service
public class MoedaService {

    private final ProfessorDAO professorDAO;
    private final AlunoDAO alunoDAO;
    private final VantagemDAO vantagemDAO;
    private final TransacaoDAO transacaoDAO;
    private final EmailService emailService;

    public MoedaService(ProfessorDAO professorDAO, AlunoDAO alunoDAO, VantagemDAO vantagemDAO,
                        TransacaoDAO transacaoDAO, EmailService emailService) {
        this.professorDAO = professorDAO;
        this.alunoDAO = alunoDAO;
        this.vantagemDAO = vantagemDAO;
        this.transacaoDAO = transacaoDAO;
        this.emailService = emailService;
    }

    @Transactional
    public TransacaoResponse enviarMoedas(Long professorId, EnvioRequest dados) {
        Professor professor = professorDAO.findById(professorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Professor", professorId));
        Aluno aluno = alunoDAO.findById(dados.alunoId())
                .orElseThrow(() -> new RegraNegocioException(HttpStatus.BAD_REQUEST, "Aluno não encontrado",
                        Map.of("alunoId", "Aluno não encontrado")));

        TransacaoEnvio envio = professor.enviarMoedas(aluno, dados.valor(), dados.motivo());
        transacaoDAO.save(envio);
        emailService.notificarRecebimento(aluno, envio);
        return TransacaoResponse.from(envio);
    }

    @Transactional
    public TransacaoResponse resgatarVantagem(Long alunoId, Long vantagemId) {
        Aluno aluno = alunoDAO.findById(alunoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno", alunoId));
        Vantagem vantagem = vantagemDAO.findById(vantagemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vantagem", vantagemId));

        Cupom cupom = aluno.resgatarVantagem(vantagem);
        TransacaoResgate resgate = transacaoDAO.save(cupom.getTransacao());
        emailService.enviarCupomAluno(aluno, cupom);
        emailService.enviarConferenciaParceiro(vantagem.getEmpresa(), cupom);
        return TransacaoResponse.from(resgate);
    }

    /** Professor: moedas enviadas. Aluno: moedas recebidas e trocas. */
    @Transactional(readOnly = true)
    public ExtratoResponse consultarExtrato(UsuarioLogado usuario) {
        return switch (usuario.perfil()) {
            case ALUNO -> {
                Aluno aluno = alunoDAO.findById(usuario.id())
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno", usuario.id()));
                yield new ExtratoResponse(aluno.getSaldoMoedas(),
                        paraResposta(transacaoDAO.findByAlunoIdOrderByDataDesc(aluno.getId())));
            }
            case PROFESSOR -> {
                Professor professor = professorDAO.findById(usuario.id())
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Professor", usuario.id()));
                yield new ExtratoResponse(professor.getSaldoMoedas(),
                        paraResposta(transacaoDAO.findEnviosDoProfessor(professor.getId())));
            }
            case EMPRESA -> throw RegraNegocioException.acessoNegado();
        };
    }

    @Transactional(readOnly = true)
    public List<TransacaoResponse> listarResgatesDaEmpresa(Long empresaId) {
        return paraResposta(transacaoDAO.findResgatesDaEmpresa(empresaId));
    }

    /** O parceiro confere o código apresentado pelo aluno e registra o uso do cupom. */
    @Transactional
    public TransacaoResponse confirmarCupom(Long empresaId, String codigo) {
        String normalizado = codigo == null ? "" : codigo.trim().toUpperCase();
        Cupom cupom = transacaoDAO.findCupomPorCodigo(normalizado)
                .filter(c -> c.getTransacao().getVantagem().getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new RegraNegocioException(HttpStatus.NOT_FOUND,
                        "Cupom não encontrado entre os resgates da sua empresa", Map.of("codigo", "Código inválido")));
        if (cupom.isUtilizado()) {
            throw RegraNegocioException.conflito("codigo", "Este cupom já foi utilizado");
        }
        cupom.setUtilizado(true);
        return TransacaoResponse.from(cupom.getTransacao());
    }

    private static List<TransacaoResponse> paraResposta(List<? extends Transacao> transacoes) {
        return transacoes.stream().map(TransacaoResponse::from).toList();
    }
}
