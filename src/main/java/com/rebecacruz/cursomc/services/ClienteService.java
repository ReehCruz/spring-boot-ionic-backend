package com.rebecacruz.cursomc.services;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rebecacruz.cursomc.domain.Cidade;
import com.rebecacruz.cursomc.domain.Cliente;
import com.rebecacruz.cursomc.domain.Endereco;
import com.rebecacruz.cursomc.domain.enums.TipoCliente;
import com.rebecacruz.cursomc.dto.ClienteDTO;
import com.rebecacruz.cursomc.dto.ClienteNewDTO;
import com.rebecacruz.cursomc.repositories.ClienteRepository;
import com.rebecacruz.cursomc.repositories.EnderecoRepository;
import com.rebecacruz.cursomc.services.exceptions.DataIntegrityException;
import com.rebecacruz.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class ClienteService {	
	
	@Autowired
	private ClienteRepository repo;
	
	@Autowired
	private EnderecoRepository enderecoRepository;
	
	public Cliente find(Integer id) {
		Optional<Cliente> obj = repo.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! Id: " + id + ", Tipo: " + Cliente.class.getName()));
		}
	
	@Transactional
	public Cliente insert(Cliente obj) {
		obj.setId(null);
		
		verificaDocumentoExistente(obj);
		
		obj = repo.save(obj);
		enderecoRepository.saveAll(obj.getEnderecos());
		return obj;
	}

	private void verificaDocumentoExistente(Cliente obj) {
		if (Objects.isNull(obj.getCpfOucnpj())) {
			List<Cliente> clientes = this.repo.findByCpfOucnpj(obj.getCpfOucnpj());
			
			if(clientes.size() > 0) {
				throw new DataIntegrityException("ja existe um cpf/ cnpj cadastrado na nossa base");
			}
		}
	}
	
	public Cliente update(Cliente obj) {
		Cliente newObj = find(obj.getId());
		updateData(newObj, obj);
		return repo.save(newObj);
	}
		
	public void delete(Integer id) {
		find(id);
		try {
			repo.deleteById(id);
		}
		catch (DataIntegrityViolationException e) {
			throw new DataIntegrityException("Não é possivel excluir porque pedidos relacionados");
		}
	}
	
	public List<Cliente> findAll(){
		return repo.findAll();
	}
	
	public Page<Cliente> findPage(Integer page, Integer linesPerPage, String orderBy, String direction) {
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return repo.findAll(pageRequest);
	}
	
	public Cliente fromDTO(ClienteDTO objDto) {
		return new Cliente(objDto.getId(),objDto.getNome(), objDto.getEmail(), null, null);
	}
	
	public Cliente fromDTO(ClienteNewDTO objDto) {
		Cliente cli = new Cliente(null, objDto.getNome(), objDto.getEmail(), objDto.getCpfOucnpj(), TipoCliente.toEnum(objDto.getTipo()));
		Cidade cid = new Cidade(objDto.getCidadeId(), null, null);
		Endereco end = new Endereco(null, objDto.getLogradouro(), objDto.getNumero(), objDto.getComplemento(), objDto.getBairro(), objDto.getCep(), cli, cid);
		cli.getEnderecos().add(end);
		cli.getTelefones().add(objDto.getTelefone1());
		if (objDto.getTelefone2()!=null) {
			cli.getTelefones().add(objDto.getTelefone2());
		}
		if (objDto.getTelefone3()!=null) {
			cli.getTelefones().add(objDto.getTelefone3());
		}
		return cli;
	}
	
	private void updateData(Cliente newObj, Cliente obj) {
		newObj.setNome(obj.getNome());
		newObj.setEmail(obj.getEmail());
	}
}
