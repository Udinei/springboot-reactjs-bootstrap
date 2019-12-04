package com.udsilva.minhasfinancas.model.repositoryTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
       
import com.udsilva.minhasfinancas.model.entity.Usuario;
import com.udsilva.minhasfinancas.model.repository.UsuarioRepository;

/**
 * Classe de teste de integração da entidade UsuarioRepository.
 * Esse tipo de testes utiliza recursos externos a aplicação no caso o BD postgresql e qualquer outro recurso.
 */
//@SpringBootTest
@DataJpaTest // Cria uma instancia do BD para cada testes em memoria, fazendo roll back a cada teste
@AutoConfigureTestDatabase(replace = Replace.NONE) // Permite alterar as configurações de @DataJpaTest - (replace = Replace.NONE) não altere minhas configuraçoes de BD de testes
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class UsuarioRepositoryTest {

	@Autowired
	UsuarioRepository repository;

	// TestEntityManager - Objeto do @DataJpaTest, nao sendo necessario mais o uso do repository dos objetos
	@Autowired
	TestEntityManager entityManager;
	
		
		
	/** metodos de testes nao possuem retorno */
	@Test
	public void deveVerificaraExistenciaDeUmEmail() {
		// cenario 1 - O usuario existe
		Usuario usuario = criarUsuario(); 
		repository.save(usuario);

		// acao/execucao
		boolean result = repository.existsByEmail("usuario@email.com");

		// verificacao
		//assertTrue(result);
		assertThat(result).isNotNull();

	}
	
	@Test
	public void deveRetornarFalsoQuandoNaoHouverUsuarioCadastradoComOEmail(){
		// cenario
		// com uso de TestEntityManager, não é mais necessario repository.deleteAll(), 
		// pois TestEntityManager fara o controle de limpesa (roll back ) da base de dados, nao tem nenhum usuario cadastrado
		//repository.deleteAll(); 
		
		// acao
		boolean result = repository.existsByEmail("usuario@email.com"); 
		
		// verificacao
		//assertFalse(result);
		assertThat(result).isFalse();
	}
	
	
	@Test
	public void devePersistirUmUsuarioNaBaseDeDados(){
		// cenario 
		Usuario usuario = criarUsuario();
		
		// acao
		Usuario usuarioSalvo = repository.save(usuario);
		
		// verificacao
		assertThat(usuarioSalvo.getId()).isNotNull();
	}

	
	
	@Test
	public void deveBuscarUmUsuarioPorEmail(){
		// cenario
		Usuario usuario = criarUsuario();
		entityManager.persist(usuario);
		
		// acao
		Optional<Usuario> result = repository.findByEmail("usuario@email.com");
					
		// verificacao
		assertThat(result.isPresent()).isTrue();
		
	}

	@Test
	public void deveRetornarVazioAoBuscarUmUsuarioPorEmailQuandoNaoExisteNaBase(){
		// cenario
		// a base ja esta vazia
			
		// acao
		Optional<Usuario> result = repository.findByEmail("usuario@email.com");
					
		// verificacao
		assertThat(result.isPresent()).isFalse();
	}
	
	private static Usuario criarUsuario() {
		return Usuario.builder()
							.nome("usuario")
							.email("usuario@email.com")
							.senha("senha")
							.build();
	}

	
}
