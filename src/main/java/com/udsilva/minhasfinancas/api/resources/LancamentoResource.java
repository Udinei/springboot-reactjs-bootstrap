package com.udsilva.minhasfinancas.api.resources;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.udsilva.minhasfinancas.api.dto.AtualizaStatusDTO;
import com.udsilva.minhasfinancas.api.dto.LancamentoDTO;
import com.udsilva.minhasfinancas.model.entity.Lancamento;
import com.udsilva.minhasfinancas.model.entity.Usuario;
import com.udsilva.minhasfinancas.model.entity.enums.StatusLancamento;
import com.udsilva.minhasfinancas.model.entity.enums.TipoLancamento;
import com.udsilva.minhasfinancas.service.LancamentoService;
import com.udsilva.minhasfinancas.service.UsuarioService;
import com.udsilva.minhasfinancas.service.exceptions.RegraNegocioException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/lancamentos")
@RequiredArgsConstructor
public class LancamentoResource {
	
	private final LancamentoService service;
	private final UsuarioService usuarioService;
	
	
	@GetMapping
	public ResponseEntity buscar(
			@RequestParam(value = "descricao", required = false) String descricao,
			@RequestParam(value = "mes", required = false) Integer mes,
			@RequestParam(value = "ano", required = false) Integer ano,
			@RequestParam("usuario") Long idUsuario // somente usuario nao é opcional, é obrigatorio
			){
		
		Lancamento lancamentoFiltro = new Lancamento();
		lancamentoFiltro.setDescricao(descricao);
		lancamentoFiltro.setMes(mes);
		lancamentoFiltro.setAno(ano);
	
		Optional<Usuario> usuario = usuarioService.obterPorId(idUsuario);
		if(!usuario.isPresent()){
			return ResponseEntity.badRequest().body("Não foi possível realizar a consulta. Usuário não encontrado para o id informado.");
		}else{
			lancamentoFiltro.setUsuario(usuario.get());
		}
		
		List<Lancamento> lancamentos = service.buscar(lancamentoFiltro);
		return ResponseEntity.ok(lancamentos);
	}
		
	
	@PostMapping
	public ResponseEntity salvar(@RequestBody LancamentoDTO dto){
		
		try {
			Lancamento entidade = converter(dto);
			entidade = service.salvar(entidade);
			return new ResponseEntity(entidade, HttpStatus.CREATED); // retorna 202
			
		} catch (RegraNegocioException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
			
		}
	}
	
	
	@PutMapping("{id}")
	public ResponseEntity atualizar(@PathVariable("id") Long id, @RequestBody LancamentoDTO dto){
		// entity - recebe o lancamento retornando pelo metodo obterPorId
		return service.obterPorId(id).map(entity -> {
			try {
				Lancamento lancamento = converter(dto);
				lancamento.setId(entity.getId());
				service.atualizar(lancamento);
				
				return ResponseEntity.ok(lancamento);	
				
			} catch (RegraNegocioException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
				
			}
			
		}).orElseGet(() -> 
		              new ResponseEntity("Lancamento não encontrado na base de dados.", HttpStatus.BAD_REQUEST));  
	}
	
	@PutMapping("{id}/atualiza-status")
	public ResponseEntity atualizarStatus(@PathVariable("id") Long id, @RequestBody AtualizaStatusDTO dto ){
		return service.obterPorId(id).map( entity -> {
			StatusLancamento statusSelecionando= StatusLancamento.valueOf(dto.getStatus());
			
			if(statusSelecionando == null){
				return ResponseEntity.badRequest().body("Não foi possível atualizar o status do lancamento, envie um status válido.");
			}
			try {
				entity.setStatus(statusSelecionando);
				service.atualizar(entity);
				return ResponseEntity.ok(entity);
				
			}catch (RegraNegocioException e) {
					return ResponseEntity.badRequest().body(e.getMessage());
					
			}			
		}).orElseGet(() -> 
        new ResponseEntity("Lancamento não encontrado na base de dados.", HttpStatus.BAD_REQUEST));
	}
	
	
	@DeleteMapping("{id}")
	public ResponseEntity deletar(@PathVariable("id") Long id){
		return service.obterPorId(id).map( entidade -> {
			   service.deletar(entidade);
			   
			   return new ResponseEntity(HttpStatus.NO_CONTENT);
			
		}).orElseGet(() ->
				new ResponseEntity("Lancamento não encontrado na base de dados.", HttpStatus.BAD_REQUEST));
	}
	
	
	
	private Lancamento converter(LancamentoDTO dto){
		Lancamento lancamento = new Lancamento();
		lancamento.setId(dto.getId());
		lancamento.setDescricao(dto.getDescricao());
		lancamento.setAno(dto.getAno());
		lancamento.setMes(dto.getMes());
		lancamento.setValor(dto.getValor());
		
		Usuario usuario = usuarioService
				            .obterPorId(dto.getUsuario())
				            .orElseThrow(() -> new RegraNegocioException("Usuário não encotrado  para o Id informado.") );
				            		
		
		lancamento.setUsuario(usuario);
		
		if(dto.getTipo() != null){
			lancamento.setTipo(TipoLancamento.valueOf(dto.getTipo()));	
		}
		
		if(dto.getStatus() != null){
		   lancamento.setStatus(StatusLancamento.valueOf(dto.getStatus()));
		}
		
		return lancamento;
		
	}
}

