package br.pucminas.moedaestudantil.dao;

import br.pucminas.moedaestudantil.model.Foto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FotoDAO extends JpaRepository<Foto, Long> {
}
