package com.luizeduardo.pedidos.gui.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.java.Log;
import okhttp3.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Level;

@Log
public class PedidoClient {
    private static final String BASE_URL = "http://localhost:8080/api/pedidos";
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public PedidoClient() {
        this.client = new OkHttpClient();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    public UUID enviarPedido(String produto, int quantidade) throws IOException {
        PedidoRequest pedido = new PedidoRequest(
            UUID.randomUUID(),
            produto,
            quantidade,
            LocalDateTime.now()
        );

        String json = objectMapper.writeValueAsString(pedido);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request request = new Request.Builder()
                .url(BASE_URL)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erro ao enviar pedido: " + response.code());
            }
            return objectMapper.readValue(response.body().string(), UUID.class);
        }
    }

    public String consultarStatus(UUID id) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/status/" + id)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.log(Level.WARNING, "Erro ao consultar status: " + response.code());
                return "ERRO";
            }
            return response.body().string();
        }
    }

    public record PedidoRequest(UUID id, String produto, int quantidade, LocalDateTime dataCriacao) {}
}
