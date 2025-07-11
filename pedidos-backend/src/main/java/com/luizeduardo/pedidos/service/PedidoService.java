package com.luizeduardo.pedidos.service;

import com.luizeduardo.pedidos.model.Pedido;
import com.luizeduardo.pedidos.model.StatusPedido;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.luizeduardo.pedidos.config.RabbitConfig.QUEUE_PEDIDOS;

@Service
public class PedidoService {
    private static final Logger log = Logger.getLogger(PedidoService.class.getName());
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public PedidoService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    private final Map<UUID, String> statusPedidos = new ConcurrentHashMap<>();

    public void processarPedido(Pedido pedido) {
        log.info(String.format("Iniciando processamento do pedido - ID: %s, Produto: %s, Quantidade: %d", 
            pedido.getId(), pedido.getProduto(), pedido.getQuantidade()));
        
        statusPedidos.put(pedido.getId(), StatusPedido.RECEBIDO.name());
        log.fine(String.format("Status do pedido %s atualizado para RECEBIDO", pedido.getId()));
        
        try {
            rabbitTemplate.convertAndSend(QUEUE_PEDIDOS, pedido);
            log.info(String.format("Pedido %s enviado com sucesso para a fila %s", pedido.getId(), QUEUE_PEDIDOS));
        } catch (Exception e) {
            log.severe(String.format("Erro ao enviar pedido %s para a fila: %s", pedido.getId(), e.getMessage()));
            statusPedidos.put(pedido.getId(), "ERRO_ENVIO");
            throw e;
        }
    }

    public String getStatusPedido(UUID id) {
        String status = statusPedidos.getOrDefault(id, "PEDIDO_NAO_ENCONTRADO");
        log.fine(String.format("Consultando status do pedido %s - Status atual: %s", id, status));
        return status;
    }

    public void atualizarStatus(UUID id, StatusPedido status) {
        String statusAntigo = statusPedidos.get(id);
        statusPedidos.put(id, status.name());
        log.info(String.format("Status do pedido %s atualizado: %s -> %s", id, 
            statusAntigo != null ? statusAntigo : "NENHUM", status.name()));
    }
}
