package com.postgres.cache.model;

import java.io.Serializable;
import java.util.List;


public record ListaProdutosDTO(List<Produto> produtos) implements Serializable {}
