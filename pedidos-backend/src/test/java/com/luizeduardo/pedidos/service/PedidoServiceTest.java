package com.luizeduardo.pedidos.service;

import com.luizeduardo.pedidos.model.Pedido;
import com.luizeduardo.pedidos.model.StatusPedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.luizeduardo.pedidos.config.RabbitConfig.QUEUE_PEDIDOS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {
    @Mock
    private RabbitTemplate rabbitTemplate;

    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoService(rabbitTemplate);
    }

    @Test
    void devePublicarPedidoNaFila() {
        // Arrange
        UUID id = UUID.randomUUID();
        Pedido pedido = new Pedido();
        pedido.setId(id);
        pedido.setProduto("Produto Teste");
        pedido.setQuantidade(1);
        pedido.setDataCriacao(LocalDateTime.now());

        // Act
        pedidoService.processarPedido(pedido);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(eq(QUEUE_PEDIDOS), eq(pedido));
        assertEquals(StatusPedido.RECEBIDO.name(), pedidoService.getStatusPedido(id));
    }

    @Test
    void deveAtualizarStatusDoPedido() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        pedidoService.atualizarStatus(id, StatusPedido.PROCESSANDO);

        // Assert
        assertEquals(StatusPedido.PROCESSANDO.name(), pedidoService.getStatusPedido(id));
    }

    @Test
    void deveRetornarPedidoNaoEncontradoParaIdInexistente() {
        // Act & Assert
        assertEquals("PEDIDO_NAO_ENCONTRADO", pedidoService.getStatusPedido(UUID.randomUUID()));
    }
}
