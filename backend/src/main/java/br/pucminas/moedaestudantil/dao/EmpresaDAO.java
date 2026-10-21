package br.pucminas.moedaestudantil.dao;

import br.pucminas.moedaestudantil.model.EmpresaParceira;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmpresaDAO extends JpaRepository<EmpresaParceira, Long> {

    List<EmpresaParceira> findAllByOrderByNomeAsc();

    boolean existsByCnpj(String cnpj);

    boolean existsByCnpjAndIdNot(String cnpj, Long id);
}
