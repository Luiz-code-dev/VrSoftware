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
        log.info("Processando pedido: {}", pedido.getId());
        statusPedidos.put(pedido.getId(), StatusPedido.RECEBIDO.name());
        rabbitTemplate.convertAndSend(QUEUE_PEDIDOS, pedido);
    }

    public String getStatusPedido(UUID id) {
        return statusPedidos.getOrDefault(id, "PEDIDO_NAO_ENCONTRADO");
    }

    public void atualizarStatus(UUID id, StatusPedido status) {
        statusPedidos.put(id, status.name());
        log.info("Status do pedido {} atualizado para: {}", id, status);
    }
}
