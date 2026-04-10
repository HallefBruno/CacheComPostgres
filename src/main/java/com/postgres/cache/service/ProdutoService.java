package com.postgres.cache.service;

import com.postgres.cache.model.ListaProdutosDTO;
import com.postgres.cache.model.Produto;
import com.postgres.cache.repository.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    @CachePut(value = "produtos", key = "#result.id")
    public Produto salvar(Produto produto) {
        return produtoRepository.save(produto);
    }

    @Cacheable(value = "produtos", key = "#id")
    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + id));
    }
	
	@Cacheable(value = "produtos", key = "'todos'")
	public ListaProdutosDTO listarTodos() {
		return new ListaProdutosDTO(produtoRepository.findAll());
	}

}
