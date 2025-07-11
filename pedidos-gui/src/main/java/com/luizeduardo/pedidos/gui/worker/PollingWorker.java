package com.luizeduardo.pedidos.gui.worker;

import com.luizeduardo.pedidos.gui.client.PedidoClient;
import lombok.extern.java.Log;

import javax.swing.*;
import java.util.UUID;
import java.util.logging.Level;

@Log
public class PollingWorker extends SwingWorker<Void, String> {
    private final PedidoClient client;
    private final UUID pedidoId;
    private final JTable statusTable;
    private final int row;
    private boolean running = true;

    public PollingWorker(PedidoClient client, UUID pedidoId, JTable statusTable, int row) {
        this.client = client;
        this.pedidoId = pedidoId;
        this.statusTable = statusTable;
        this.row = row;
    }

    @Override
    protected Void doInBackground() throws Exception {
        while (running) {
            try {
                String status = client.consultarStatus(pedidoId);
                publish(status);
                
                if (status.equals("SUCESSO") || status.equals("FALHA")) {
                    running = false;
                }
                
                Thread.sleep(3000); // Polling a cada 3 segundos
            } catch (Exception e) {
                log.log(Level.SEVERE, "Erro ao consultar status", e);
            }
        }
        return null;
    }

    @Override
    protected void process(java.util.List<String> statusList) {
        String status = statusList.get(statusList.size() - 1);
        SwingUtilities.invokeLater(() -> 
            statusTable.setValueAt(status, row, 2)
        );
    }
}
