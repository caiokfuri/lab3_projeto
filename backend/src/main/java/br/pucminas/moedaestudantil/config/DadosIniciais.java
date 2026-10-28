package br.pucminas.moedaestudantil.config;

import br.pucminas.moedaestudantil.dao.InstituicaoDAO;
import br.pucminas.moedaestudantil.dao.ProfessorDAO;
import br.pucminas.moedaestudantil.model.InstituicaoEnsino;
import br.pucminas.moedaestudantil.model.Professor;
import br.pucminas.moedaestudantil.service.ProfessorService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Instituições e professores são pré-cadastrados (a instituição envia a lista de professores no momento
 * da parceria). Na inicialização também é lançado o crédito semestral pendente dos professores.
 */
@Component
public class DadosIniciais implements CommandLineRunner {

    /** Senha inicial dos professores pré-cadastrados (ambiente de demonstração). */
    public static final String SENHA_PROFESSORES = "123456";

    private final InstituicaoDAO instituicaoDAO;
    private final ProfessorDAO professorDAO;
    private final ProfessorService professorService;
    private final PasswordEncoder passwordEncoder;

    public DadosIniciais(InstituicaoDAO instituicaoDAO, ProfessorDAO professorDAO,
                         ProfessorService professorService, PasswordEncoder passwordEncoder) {
        this.instituicaoDAO = instituicaoDAO;
        this.professorDAO = professorDAO;
        this.professorService = professorService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (instituicaoDAO.count() == 0) {
            instituicaoDAO.saveAll(List.of(
                    new InstituicaoEnsino("PUC Minas"),
                    new InstituicaoEnsino("UFMG"),
                    new InstituicaoEnsino("CEFET-MG")));
        }

        if (professorDAO.count() == 0) {
            professorDAO.saveAll(List.of(
                    professor("Ana Paula Ribeiro", "prof.ana", "ana.ribeiro@pucminas.br", "52998224725",
                            "Engenharia de Software", "PUC Minas"),
                    professor("Carlos Henrique Souza", "prof.carlos", "carlos.souza@pucminas.br", "39053344705",
                            "Ciência da Computação", "PUC Minas"),
                    professor("Beatriz Lima Costa", "prof.beatriz", "beatriz.costa@ufmg.br", "11144477735",
                            "Sistemas de Informação", "UFMG"),
                    professor("Diego Martins Alves", "prof.diego", "diego.alves@cefetmg.br", "16899535009",
                            "Engenharia de Computação", "CEFET-MG")));
        }

        professorService.creditarSemestreAtual();
    }

    private Professor professor(String nome, String login, String email, String cpf, String departamento,
                                String instituicao) {
        Professor p = new Professor();
        p.setNome(nome);
        p.setLogin(login);
        p.setEmail(email);
        p.setSenha(passwordEncoder.encode(SENHA_PROFESSORES));
        p.setCpf(cpf);
        p.setDepartamento(departamento);
        p.setInstituicao(instituicaoDAO.findByNome(instituicao)
                .orElseThrow(() -> new IllegalStateException("Instituição não cadastrada: " + instituicao)));
        return p;
    }
}
