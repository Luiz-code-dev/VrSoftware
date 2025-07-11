package com.luizeduardo.pedidos.consumer;

import com.luizeduardo.pedidos.exception.ExcecaoDeProcessamento;
import com.luizeduardo.pedidos.model.Pedido;
import com.luizeduardo.pedidos.model.StatusPedido;
import com.luizeduardo.pedidos.service.PedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Random;

import static com.luizeduardo.pedidos.config.RabbitConfig.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class PedidoConsumer {
    private final Random random = new Random();
    private final RabbitTemplate rabbitTemplate;
    private final PedidoService pedidoService;

    @RabbitListener(queues = QUEUE_PEDIDOS)
    public void processarPedido(Pedido pedido) throws InterruptedException {
        try {
            log.info("Iniciando processamento do pedido: {}", pedido.getId());
            pedidoService.atualizarStatus(pedido.getId(), StatusPedido.PROCESSANDO);

            // Simula processamento de 1-3 segundos
            Thread.sleep(1000 + random.nextInt(2000));

            // 20% de chance de falha
            if (random.nextDouble() < 0.2) {
                throw new ExcecaoDeProcessamento("Falha simulada no processamento");
            }

            // Sucesso
            pedidoService.atualizarStatus(pedido.getId(), StatusPedido.SUCESSO);
            rabbitTemplate.convertAndSend(QUEUE_STATUS_SUCESSO, pedido);
            log.info("Pedido {} processado com sucesso", pedido.getId());

        } catch (Exception e) {
            log.error("Erro ao processar pedido {}: {}", pedido.getId(), e.getMessage());
            pedidoService.atualizarStatus(pedido.getId(), StatusPedido.FALHA);
            rabbitTemplate.convertAndSend(QUEUE_STATUS_FALHA, pedido);
            throw e; // Rejeita a mensagem, enviando para DLQ
        }
    }
}
