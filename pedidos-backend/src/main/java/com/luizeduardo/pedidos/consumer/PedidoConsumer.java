package com.luizeduardo.pedidos.consumer;

import com.luizeduardo.pedidos.exception.ExcecaoDeProcessamento;
import com.luizeduardo.pedidos.model.Pedido;
import com.luizeduardo.pedidos.model.StatusPedido;
import com.luizeduardo.pedidos.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.logging.Logger;

import static com.luizeduardo.pedidos.config.RabbitConfig.*;

@Component
public class PedidoConsumer {
    private static final Logger log = Logger.getLogger(PedidoConsumer.class.getName());
    private final Random random = new Random();
    private final RabbitTemplate rabbitTemplate;
    private final PedidoService pedidoService;

    @Autowired
    public PedidoConsumer(RabbitTemplate rabbitTemplate, PedidoService pedidoService) {
        this.rabbitTemplate = rabbitTemplate;
        this.pedidoService = pedidoService;
    }

    @RabbitListener(queues = QUEUE_PEDIDOS)
    public void processarPedido(Pedido pedido) throws InterruptedException {
        try {
            log.info(String.format("Iniciando processamento do pedido: %s", pedido.getId()));
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
            log.info(String.format("Pedido %s processado com sucesso", pedido.getId()));

        } catch (Exception e) {
            log.severe(String.format("Erro ao processar pedido %s: %s", pedido.getId(), e.getMessage()));
            pedidoService.atualizarStatus(pedido.getId(), StatusPedido.FALHA);
            rabbitTemplate.convertAndSend(QUEUE_STATUS_FALHA, pedido);
            throw e; // Rejeita a mensagem, enviando para DLQ
        }
    }
}
