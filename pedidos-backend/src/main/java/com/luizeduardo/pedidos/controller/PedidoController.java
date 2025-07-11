package com.luizeduardo.pedidos.controller;

import com.luizeduardo.pedidos.model.Pedido;
import com.luizeduardo.pedidos.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Slf4j
public class PedidoController {
    private final PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<UUID> criarPedido(@Valid @RequestBody Pedido pedido) {
        log.info("Recebendo pedido: {}", pedido);
        pedidoService.processarPedido(pedido);
        return ResponseEntity.accepted().body(pedido.getId());
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<String> getStatus(@PathVariable UUID id) {
        String status = pedidoService.getStatusPedido(id);
        return ResponseEntity.ok(status);
    }
}
