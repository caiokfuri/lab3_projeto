package br.pucminas.moedaestudantil.dao;

import br.pucminas.moedaestudantil.model.InstituicaoEnsino;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstituicaoDAO extends JpaRepository<InstituicaoEnsino, Long> {

    List<InstituicaoEnsino> findAllByOrderByNomeAsc();
}
