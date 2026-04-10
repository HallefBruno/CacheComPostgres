package com.postgres.cache.controller;

import com.postgres.cache.model.ListaProdutosDTO;
import com.postgres.cache.model.Produto;
import com.postgres.cache.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {
	
	@Autowired
	private ProdutoService produtoService;
	
	@PostMapping
	public ResponseEntity<?> salvar(@RequestBody Produto produto) {
		produtoService.salvar(produto);
		return ResponseEntity.ok("Produto salvo com sucesso!");
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Produto> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(produtoService.buscarPorId(id));
	}
	
	@GetMapping("/todos")
	public ResponseEntity<ListaProdutosDTO> todosProdutos() {
		return ResponseEntity.ok(produtoService.listarTodos());
	}
	
}
