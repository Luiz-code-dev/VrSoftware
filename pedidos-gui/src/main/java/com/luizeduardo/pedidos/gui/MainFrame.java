package com.luizeduardo.pedidos.gui;

import com.luizeduardo.pedidos.gui.client.PedidoClient;
import com.luizeduardo.pedidos.gui.worker.PollingWorker;
import lombok.extern.java.Log;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.UUID;
import java.util.logging.Level;

@Log
public class MainFrame extends JFrame {
    private final PedidoClient client;
    private final JTextField produtoField;
    private final JSpinner quantidadeSpinner;
    private final JTable statusTable;
    private final DefaultTableModel tableModel;

    public MainFrame() {
        client = new PedidoClient();
        
        // Configuração da janela
        setTitle("Sistema de Pedidos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        // Painel de entrada
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Campos de entrada
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Produto:"), gbc);
        
        gbc.gridx = 1;
        produtoField = new JTextField(20);
        inputPanel.add(produtoField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Quantidade:"), gbc);
        
        gbc.gridx = 1;
        quantidadeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        inputPanel.add(quantidadeSpinner, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        JButton enviarButton = new JButton("Enviar Pedido");
        enviarButton.addActionListener(e -> enviarPedido());
        inputPanel.add(enviarButton, gbc);

        // Tabela de status
        String[] colunas = {"ID", "Produto", "Status"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        statusTable = new JTable(tableModel);
        statusTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        statusTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        statusTable.getColumnModel().getColumn(2).setPreferredWidth(100);

        // Layout principal
        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(statusTable), BorderLayout.CENTER);
    }

    private void enviarPedido() {
        String produto = produtoField.getText().trim();
        int quantidade = (Integer) quantidadeSpinner.getValue();

        // Validação do produto
        if (produto.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "O produto não pode estar vazio",
                "Erro de Validação",
                JOptionPane.ERROR_MESSAGE);
            produtoField.requestFocus();
            return;
        }
        
        if (produto.length() < 3) {
            JOptionPane.showMessageDialog(this,
                "O nome do produto deve ter pelo menos 3 caracteres",
                "Erro de Validação",
                JOptionPane.ERROR_MESSAGE);
            produtoField.requestFocus();
            return;
        }

        // Validação da quantidade
        if (quantidade <= 0) {
            JOptionPane.showMessageDialog(this,
                "A quantidade deve ser maior que zero",
                "Erro de Validação",
                JOptionPane.ERROR_MESSAGE);
            quantidadeSpinner.requestFocus();
            return;
        }

        try {
            UUID id = client.enviarPedido(produto, quantidade);
            int row = tableModel.getRowCount();
            tableModel.addRow(new Object[]{id.toString(), produto, "ENVIADO"});
            
            // Inicia polling de status
            new PollingWorker(client, id, statusTable, row).execute();
            
            // Limpa campos
            produtoField.setText("");
            quantidadeSpinner.setValue(1);
            
        } catch (IOException e) {
            log.log(Level.SEVERE, "Erro de comunicação ao enviar pedido", e);
            String mensagem = "Erro ao comunicar com o servidor. Verifique se o backend está em execução.\n" +
                            "Detalhes: " + e.getMessage();
            JOptionPane.showMessageDialog(this,
                mensagem,
                "Erro de Comunicação",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Erro inesperado ao enviar pedido", e);
            String mensagem = "Ocorreu um erro inesperado ao processar seu pedido.\n" +
                            "Por favor, tente novamente ou contate o suporte se o erro persistir.\n" +
                            "Detalhes: " + e.getMessage();
            JOptionPane.showMessageDialog(this,
                mensagem,
                "Erro Inesperado",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                log.log(Level.WARNING, "Erro ao definir Look and Feel", e);
            }
            new MainFrame().setVisible(true);
        });
    }
}
