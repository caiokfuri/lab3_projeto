package br.pucminas.moedaestudantil.config;

import br.pucminas.moedaestudantil.dao.InstituicaoDAO;
import br.pucminas.moedaestudantil.model.InstituicaoEnsino;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/** As instituições de ensino são pré-cadastradas; o aluno escolhe uma delas no cadastro. */
@Component
public class DadosIniciais implements CommandLineRunner {

    private final InstituicaoDAO instituicaoDAO;

    public DadosIniciais(InstituicaoDAO instituicaoDAO) {
        this.instituicaoDAO = instituicaoDAO;
    }

    @Override
    public void run(String... args) {
        if (instituicaoDAO.count() == 0) {
            instituicaoDAO.saveAll(List.of(
                    new InstituicaoEnsino("PUC Minas"),
                    new InstituicaoEnsino("UFMG"),
                    new InstituicaoEnsino("CEFET-MG")));
        }
    }
}
