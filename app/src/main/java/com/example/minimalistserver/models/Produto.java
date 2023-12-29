package com.example.minimalistserver.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class Produto {

    private final int id;
    private final String nome;
    private final BigDecimal preco;

    @JsonCreator
    public Produto(
            @JsonProperty("id") int id,
            @JsonProperty("nome") String nome,
            @JsonProperty("preco") BigDecimal preco) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getPreco() {
        return preco;
    }
}
