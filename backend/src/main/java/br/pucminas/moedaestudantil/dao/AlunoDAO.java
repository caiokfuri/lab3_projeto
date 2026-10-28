package br.pucminas.moedaestudantil.dao;

import br.pucminas.moedaestudantil.model.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlunoDAO extends JpaRepository<Aluno, Long> {

    List<Aluno> findAllByOrderByNomeAsc();

    List<Aluno> findByInstituicaoIdOrderByNomeAsc(Long instituicaoId);

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);
}
