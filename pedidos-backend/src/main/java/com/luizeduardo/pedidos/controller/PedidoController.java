package com.luizeduardo.pedidos.controller;

import com.luizeduardo.pedidos.model.Pedido;
import com.luizeduardo.pedidos.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

import java.util.UUID;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    private static final Logger log = Logger.getLogger(PedidoController.class.getName());
    private final PedidoService pedidoService;
    
    @Autowired
    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<UUID> criarPedido(@Valid @RequestBody Pedido pedido) {
        log.info(String.format("Recebendo pedido: %s", pedido));
        pedidoService.processarPedido(pedido);
        return ResponseEntity.accepted().body(pedido.getId());
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<String> getStatus(@PathVariable UUID id) {
        String status = pedidoService.getStatusPedido(id);
        return ResponseEntity.ok(status);
    }
}
