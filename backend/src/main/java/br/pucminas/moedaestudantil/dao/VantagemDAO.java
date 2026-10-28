package br.pucminas.moedaestudantil.dao;

import br.pucminas.moedaestudantil.model.Vantagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VantagemDAO extends JpaRepository<Vantagem, Long> {

    List<Vantagem> findAllByOrderByCustoMoedasAsc();

    List<Vantagem> findByEmpresaIdOrderByNomeAsc(Long empresaId);
}
