package com.luizeduardo.pedidos.service;

import com.luizeduardo.pedidos.model.Pedido;
import com.luizeduardo.pedidos.model.StatusPedido;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.luizeduardo.pedidos.config.RabbitConfig.QUEUE_PEDIDOS;

@Service
@Slf4j
@RequiredArgsConstructor
public class PedidoService {
    private final RabbitTemplate rabbitTemplate;
    private final Map<UUID, String> statusPedidos = new ConcurrentHashMap<>();

    public void processarPedido(Pedido pedido) {
        log.info("Iniciando processamento do pedido - ID: {}, Produto: {}, Quantidade: {}", 
            pedido.getId(), pedido.getProduto(), pedido.getQuantidade());
        
        statusPedidos.put(pedido.getId(), StatusPedido.RECEBIDO.name());
        log.debug("Status do pedido {} atualizado para RECEBIDO", pedido.getId());
        
        try {
            rabbitTemplate.convertAndSend(QUEUE_PEDIDOS, pedido);
            log.info("Pedido {} enviado com sucesso para a fila {}", pedido.getId(), QUEUE_PEDIDOS);
        } catch (Exception e) {
            log.error("Erro ao enviar pedido {} para a fila: {}", pedido.getId(), e.getMessage());
            statusPedidos.put(pedido.getId(), "ERRO_ENVIO");
            throw e;
        }
    }

    public String getStatusPedido(UUID id) {
        String status = statusPedidos.getOrDefault(id, "PEDIDO_NAO_ENCONTRADO");
        log.debug("Consultando status do pedido {} - Status atual: {}", id, status);
        return status;
    }

    public void atualizarStatus(UUID id, StatusPedido status) {
        String statusAntigo = statusPedidos.get(id);
        statusPedidos.put(id, status.name());
        log.info("Status do pedido {} atualizado: {} -> {}", id, 
            statusAntigo != null ? statusAntigo : "NOVO", status);
    }
}
