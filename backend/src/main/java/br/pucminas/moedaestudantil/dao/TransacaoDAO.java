package br.pucminas.moedaestudantil.dao;

import br.pucminas.moedaestudantil.model.Cupom;
import br.pucminas.moedaestudantil.model.Transacao;
import br.pucminas.moedaestudantil.model.TransacaoEnvio;
import br.pucminas.moedaestudantil.model.TransacaoResgate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransacaoDAO extends JpaRepository<Transacao, Long> {

    /** Extrato do aluno: moedas recebidas e trocas realizadas. */
    List<Transacao> findByAlunoIdOrderByDataDesc(Long alunoId);

    /** Extrato do professor: moedas enviadas. */
    @Query("select t from TransacaoEnvio t where t.professor.id = :professorId order by t.data desc")
    List<TransacaoEnvio> findEnviosDoProfessor(@Param("professorId") Long professorId);

    /** Resgates das vantagens de uma empresa, para conferência dos cupons. */
    @Query("select t from TransacaoResgate t where t.vantagem.empresa.id = :empresaId order by t.data desc")
    List<TransacaoResgate> findResgatesDaEmpresa(@Param("empresaId") Long empresaId);

    @Query("select c from Cupom c where c.codigo = :codigo")
    Optional<Cupom> findCupomPorCodigo(@Param("codigo") String codigo);

    boolean existsByAlunoId(Long alunoId);

    @Query("select count(t) > 0 from TransacaoResgate t where t.vantagem.empresa.id = :empresaId")
    boolean existsResgateDaEmpresa(@Param("empresaId") Long empresaId);

    @Query("select count(t) > 0 from TransacaoResgate t where t.vantagem.id = :vantagemId")
    boolean existsResgateDaVantagem(@Param("vantagemId") Long vantagemId);
}
