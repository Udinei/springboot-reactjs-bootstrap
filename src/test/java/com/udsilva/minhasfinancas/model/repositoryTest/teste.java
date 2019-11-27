package com.udsilva.minhasfinancas.model.repositoryTest;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.udsilva.minhasfinancas.model.entity.Usuario;
import com.udsilva.minhasfinancas.model.repository.UsuarioRepository;

@SpringBootTest
@RunWith(SpringRunner.class)
public class teste {

	@Autowired
	UsuarioRepository repository;
	
	@Test
	public void deveVerificaraExistenciaDeUmEmail(){
	// cenario 1 - O usuario existe 
		Usuario usuario = Usuario.builder().nome("usuario").email("usuario@email.com").build();
		repository.save(usuario);
		
	// acao/execucao
		boolean result = repository.existsByEmail("usuario@email.com");
		
	// verificacao
	assertTrue(result);
	
	}
}
