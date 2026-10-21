package br.pucminas.moedaestudantil.dao;

import br.pucminas.moedaestudantil.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransacaoDAO extends JpaRepository<Transacao, Long> {

    List<Transacao> findByAlunoIdOrderByDataDesc(Long alunoId);

    boolean existsByAlunoId(Long alunoId);

    @Query("select count(t) > 0 from TransacaoResgate t where t.vantagem.empresa.id = :empresaId")
    boolean existsResgateDaEmpresa(@Param("empresaId") Long empresaId);
}
