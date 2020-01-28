package com.rebecacruz.cursomc.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rebecacruz.cursomc.domain.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

	List<Cliente> findByCpfOucnpj(String cpfOucnpj);
	
	
}
