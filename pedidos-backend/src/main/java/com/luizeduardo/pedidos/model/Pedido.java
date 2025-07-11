package com.luizeduardo.pedidos.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Pedido {
    @NotNull
    private UUID id;

    @NotBlank(message = "Produto não pode estar vazio")
    private String produto;

    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    private int quantidade;

    @NotNull
    private LocalDateTime dataCriacao;
}
