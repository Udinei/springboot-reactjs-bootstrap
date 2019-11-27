package com.udsilva.minhasfinancas.serviceTest;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.udsilva.minhasfinancas.model.entity.Usuario;
import com.udsilva.minhasfinancas.model.repository.UsuarioRepository;
import com.udsilva.minhasfinancas.service.exceptions.ErroAutenticacao;
import com.udsilva.minhasfinancas.service.exceptions.RegraNegocioException;
import com.udsilva.minhasfinancas.service.impl.UsuarioServiceImpl;

/** Classe de testes unitários da entidade UsuarioService.
 *  A utilização do mockito em testes unitários evita a duplicação dos codigos de teste de integração, 
 *  e permite a criação de metodos de testes unitários mais simples e pequenos  */

//@SpringBootTest // não será necessaria essa anotação - Carrega todo o contexto do springframework ao realizar os testes
@RunWith(SpringRunner.class) //essa anotação é necessario para mocar os objetos usando o context do spring 
@ActiveProfiles("test")
public class UsuarioServiceTest {
	
	@SpyBean // cria uma instancia real de UsuarioServiceImpl acessada no contexto do spring
	UsuarioServiceImpl service;
		
	@MockBean // cria uma instancia real de UsuarioRepository acessada no contexto do spring
	UsuarioRepository repository;
		
	@Test(expected = Test.None.class)
	public void deveSalvarUmUsuario(){
		// cenario de sucesso - sem erros
		// nao faz nada quando o metodo validar email for chamado com qualquer valor
		Mockito.doNothing().when(service).validarEmail(Mockito.anyString());
		Usuario usuario = Usuario.builder()
				.id(1l)
				.nome("nome")
				.email("email@email.com")
				.senha("senha")
				.build();
		
		Mockito.when( repository.save(Mockito.any(Usuario.class) )).thenReturn(usuario);
		
		//acao
		Usuario usuarioSalvo = service.salvarUsuario(new Usuario());
		
		//verificacao
		Assertions.assertThat(usuarioSalvo).isNotNull();
		Assertions.assertThat(usuarioSalvo.getId()).isEqualTo(1l);
		Assertions.assertThat(usuarioSalvo.getNome()).isEqualTo("nome");
		Assertions.assertThat(usuarioSalvo.getEmail()).isEqualTo("email@email.com");
		Assertions.assertThat(usuarioSalvo.getSenha()).isEqualTo("senha");
		
						
	}
	
	@Test(expected = RegraNegocioException.class)
	public void naoDeveSalvarUmUsuarioComEmailJaCadastrado(){
		// cenario
		String email = "email@email.com";
		Usuario usuario = Usuario.builder().email(email).build();
		Mockito.doThrow(RegraNegocioException.class).when(service).validarEmail(email);

		//acao
		service.salvarUsuario(usuario);
		
		//verificacao
		Mockito.verify(repository, Mockito.never() ).save(usuario); // verifica se ele nunca executou o metodo save com o parametro usuario 
	}
	
	/*
	 Esse trecho de codigo foi substituido pelas duas linhas de codigo acima,
	 com uso de @MockBean e @SpyBean
	 // criando um UsuarioRepository fake com mokito
	  @Before
	public void setUp(){
	
		repository = Mockito.mock(UsuarioRepository.class);
		
		service = new UsuarioServiceImpl(repository);
	}
	*/
		
	
	/* O metodo service.autenticar(email, senha) faz a chamada ao banco para obter o usuario, 
	 * mas o mockito vai excutar o metodo findByEmail de forma fake retornando o usuario criado 
     * nesse metodo em vez de recuperar de fato do banco, na classe UsuarioService
     */
	@Test(expected = Test.None.class) // nao deve retornar uma exception para o metodo passar
	public void deveAutenticarUmUsuarioComSucesso(){
		// cenario
		String email = "email@email.com";
		String senha = "senha";
		
		// Esse usuario sera usado como se fosse o usuario recuperdo do BD
		Usuario usuario = Usuario.builder().email(email).senha(senha).id(1l).build();
		
		// Quando o metodo findByEmail de UsuarioRepository for executado retorna o usuario criado acima 
		Mockito.when( repository.findByEmail(email) ).thenReturn(Optional.of(usuario));
		
		// acao 
		Usuario result = service.autenticar(email, senha);
		
		// verificacao
		Assertions.assertThat(result).isNotNull();
	}
	
	@Test
	public void deveLancarErroQuandoNaoEncontrarUsuarioCadastradoComOEmailInformado(){
		// cenario
		Mockito.when( repository.findByEmail(Mockito.anyString()) ).thenReturn(Optional.empty());
		
		// acao
		//service.autenticar("email@eamil.com", "senha");
		 Throwable exception = Assertions.catchThrowable( () ->  service.autenticar("email@eamil.com", "senha") );
		 
		 // 
		 Assertions.assertThat(exception).isInstanceOf(ErroAutenticacao.class).hasMessage("Usuário não encontrado para o email informado.");
		
		
	}
	
		
	@Test   // Se usar Assertions.catchThrowable nao usar -> (expected = ErroAutenticacao.class) // se o metodo passar é porque essa exception foi lancada 
	public void deveLancarErroQuandoSenhaNaoBater(){
		//cenario
		String senha = "senha";
		Usuario usuario = Usuario.builder().email("email@email.com").senha(senha).build();
		Mockito.when( repository.findByEmail(Mockito.anyString()) ).thenReturn(Optional.of(usuario));
		
		// acao
		// usar  Assertions.catchThrowable na forma abaixo quando uma mesma exception é usada em varios testes, com retorno de msgs diferentes.
		// para saber qual metodo de teste que deu erro ou sucesso.
		 Throwable exception = Assertions.catchThrowable( () ->  service.autenticar("email@email.com", "123") );
		
		 // verificacao
		 Assertions.assertThat(exception).isInstanceOf(ErroAutenticacao.class).hasMessage("Senha inválida.");
		
	}
	
	/** @Test(expected = Test.None.class) - Espera-se que nenhuma exception seja lançada 
	 *  sinalizando que o teste passou */
	@Test(expected = Test.None.class)
	public void deveValidarEmail(){
		// cenario - nao existe nenhum email cadastrado no BD
		Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(false);
		
		// acao - informa um email pra validar se já existe no BD
		service.validarEmail("email@email.com");
		
		//verificar
		/** O metodo passa, caso nenhuma excessão for lançada */
	}
	
	
	/** @Test(expected = RegraNegociocException.class) - Espera-se que a exception RegraNegociocException
	 * seja lançada sinalizando que o teste passou */
	@Test(expected = RegraNegocioException.class)
	public void deveLancarErroQuandoExistirEmailCadastrado(){
		// cenario - Já existe um usuario com o email informado cadastrado
		Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(true);
		
		// acao
		service.validarEmail("email@email.com");
		
		//verificar
		/** O metodo passa, caso a exception RegraNegociocException seja lançada */
		
	}
	

}
