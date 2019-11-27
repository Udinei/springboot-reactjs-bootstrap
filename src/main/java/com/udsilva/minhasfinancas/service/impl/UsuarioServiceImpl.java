package com.udsilva.minhasfinancas.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.udsilva.minhasfinancas.model.entity.Usuario;
import com.udsilva.minhasfinancas.model.repository.UsuarioRepository;
import com.udsilva.minhasfinancas.service.UsuarioService;
import com.udsilva.minhasfinancas.service.exceptions.ErroAutenticacao;
import com.udsilva.minhasfinancas.service.exceptions.RegraNegocioException;


@Service
public class UsuarioServiceImpl implements UsuarioService {

	private UsuarioRepository repository;
	
	/** Esse metodo sera necessário para uso do mockito nos testes unitarios
	 *  na simulação do repository */	
	public UsuarioServiceImpl(UsuarioRepository repository) {
		super();
		this.repository = repository;
	}
	

	@Override
	public Usuario autenticar(String email, String senha) {
      Optional<Usuario> usuario = repository.findByEmail(email);
      
      if(!usuario.isPresent()){
    	  throw new ErroAutenticacao("Usuário não encontrado para o email informado.");
      }
      
      if(!usuario.get().getSenha().equals(senha)){
    	  throw new ErroAutenticacao("Senha inválida.");
      }
      
      return usuario.get();
	}

	@Override
	@Transactional // abre a transação, salva o usuario, comita e fecha a transacao 
	public Usuario salvarUsuario(Usuario usuario) {
        validarEmail(usuario.getEmail());
		return repository.save(usuario);
	}

	@Override
	public void validarEmail(String email) {
        boolean existe = repository.existsByEmail(email);
        if(existe) {
        	throw new RegraNegocioException("Já existe um usuário cadastrado com este email.");
        }
		
	}


	@Override
	public Optional<Usuario> obterPorId(Long id) {
		return repository.findById(id);
	}

}
