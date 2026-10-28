package br.pucminas.moedaestudantil.dao;

import br.pucminas.moedaestudantil.model.InstituicaoEnsino;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InstituicaoDAO extends JpaRepository<InstituicaoEnsino, Long> {

    List<InstituicaoEnsino> findAllByOrderByNomeAsc();

    Optional<InstituicaoEnsino> findByNome(String nome);
}
