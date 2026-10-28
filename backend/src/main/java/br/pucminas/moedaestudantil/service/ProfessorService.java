package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.dao.ProfessorDAO;
import br.pucminas.moedaestudantil.dto.ProfessorResponse;
import br.pucminas.moedaestudantil.exception.RecursoNaoEncontradoException;
import br.pucminas.moedaestudantil.model.Professor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ProfessorService {

    private static final Logger log = LoggerFactory.getLogger(ProfessorService.class);

    private final ProfessorDAO professorDAO;

    public ProfessorService(ProfessorDAO professorDAO) {
        this.professorDAO = professorDAO;
    }

    @Transactional(readOnly = true)
    public ProfessorResponse buscar(Long id) {
        return ProfessorResponse.from(professorDAO.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Professor", id)));
    }

    /**
     * Credita as 1.000 moedas do semestre corrente a quem ainda não as recebeu. Roda na inicialização e
     * diariamente à 01h; como cada professor guarda o último semestre creditado, nunca credita duas vezes.
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void creditarSemestreAtual() {
        String semestre = semestre(LocalDate.now());
        long creditados = professorDAO.findAll().stream().filter(p -> p.creditarSemestre(semestre)).count();
        if (creditados > 0) {
            log.info("Crédito do semestre {}: {} moedas lançadas para {} professor(es)",
                    semestre, Professor.CREDITO_SEMESTRAL, creditados);
        }
    }

    static String semestre(LocalDate data) {
        return data.getYear() + "/" + (data.getMonthValue() <= 6 ? 1 : 2);
    }
}
