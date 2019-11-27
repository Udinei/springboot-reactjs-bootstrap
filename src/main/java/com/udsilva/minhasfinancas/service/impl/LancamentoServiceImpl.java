package com.udsilva.minhasfinancas.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.udsilva.minhasfinancas.model.entity.Lancamento;
import com.udsilva.minhasfinancas.model.entity.enums.StatusLancamento;
import com.udsilva.minhasfinancas.model.entity.enums.TipoLancamento;
import com.udsilva.minhasfinancas.model.repository.LancamentoRepository;
import com.udsilva.minhasfinancas.service.LancamentoService;
import com.udsilva.minhasfinancas.service.exceptions.RegraNegocioException;


@Service
public class LancamentoServiceImpl implements LancamentoService {

	private LancamentoRepository repository;
	
	
	public LancamentoServiceImpl(LancamentoRepository repository) {
	  this.repository = repository;
	}
	
	
	@Override 
	@Transactional
	public Lancamento salvar(Lancamento lancamento) {
		validar(lancamento);
		lancamento.setStatus(StatusLancamento.PENDENTE);
		return repository.save(lancamento);
	}

	@Override
	@Transactional
	public Lancamento atualizar(Lancamento lancamento) {
		Objects.requireNonNull(lancamento.getId());
		validar(lancamento);
		return repository.save(lancamento);
	}

	@Override
	@Transactional
	public void deletar(Lancamento lancamento) {
		Objects.requireNonNull(lancamento.getId());
		repository.delete(lancamento);
		
	}

	@Override
	@Transactional(readOnly = true)
	public List<Lancamento> buscar(Lancamento lancamentoFiltro) {
	// Example - Recebe um objeto como filtro e faz a busca pelos atributos preenchidos do objeto
	// iguinora caixa alta e que contenha qualquer string passada como parametro nos atributos	
       Example example = Example.of( lancamentoFiltro,
    		             ExampleMatcher.matching()
    		             .withIgnoreCase()
    		             .withStringMatcher(StringMatcher.CONTAINING));
		return repository.findAll(example);
	}




	@Override
	public void validar(Lancamento lancamento) {
		
		// Todo lancamento deve ter uma descricao
		if(lancamento.getDescricao() == null || lancamento.getDescricao().trim().equals("")){
			throw new RegraNegocioException("Informe uma descricao válida.");
		}
		
		// o mes informado nao deve ser menor que 1 e tem que estar entre 1 (janeiro) e 12 (dezembro) 
		if(lancamento.getMes() == null || lancamento.getMes() < 1 || lancamento.getMes() > 12){
			throw new RegraNegocioException("Informe um mês válido.");
		}
		
		//  o ano informado nao pode ser diferente de 4 caracteres
		if(lancamento.getAno() == null || lancamento.getAno().toString().length() != 4){
			throw new RegraNegocioException("Informe uma descricao válida.");
		}
		
		// o usuario deve estar cadastrado para fazer lancamentos
		if(lancamento.getUsuario() == null || lancamento.getUsuario().getId() == null){
			throw new RegraNegocioException("Informe um usuário.");
		}
		
		//  nao deve ter valor de lancamento negativo ou zero  
		if(lancamento.getValor() == null || lancamento.getValor().compareTo(BigDecimal.ZERO) < 1){
			throw new RegraNegocioException("Informe um valor válido.");
		}
		
		if(lancamento.getTipo() == null ){
			throw new RegraNegocioException("Informe um tipo de lancamento.");
		}
		
	}


	

	@Override
	public void atualizarStatus(Lancamento lancamento, StatusLancamento status) {
        lancamento.setStatus(status);
        atualizar(lancamento);
		
	}


	@Override
	public Optional<Lancamento> obterPorId(Long id) {
		return repository.findById(id);
	}


	@Override
	@Transactional(readOnly=true)
	public BigDecimal obterSaldoPorUsuario(Long id) {
       BigDecimal receitas = repository.obterSaldoPorTipoLancamentoEUsuarioEStatus(id, TipoLancamento.RECEITA);
       BigDecimal despesas = repository.obterSaldoPorTipoLancamentoEUsuarioEStatus(id, TipoLancamento.DESPESA);
        
       if(receitas == null){
    	   receitas = BigDecimal.ZERO;
       }
       
       if(despesas == null){
    	   despesas = BigDecimal.ZERO;
       }
       
       return receitas.subtract(despesas);
	}
	
	
	
	
	

}
