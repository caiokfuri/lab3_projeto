package br.pucminas.moedaestudantil.dao;

import br.pucminas.moedaestudantil.model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorDAO extends JpaRepository<Professor, Long> {

    boolean existsByCpf(String cpf);
}
