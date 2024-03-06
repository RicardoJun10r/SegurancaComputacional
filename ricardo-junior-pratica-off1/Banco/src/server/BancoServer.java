package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.List;

import model.ContaCorrente;
import util.ClientSocket;
import util.HashTable.Table;

public class BancoServer {

    public final int PORTA = 1025;

    private ServerSocket serverSocket;

    private final List<ClientSocket> USUARIOS = new LinkedList<>();

    private Table<ContaCorrente, Integer> tabela;

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORTA);
        System.out.println("Iniciando servidor na porta = " + PORTA);
        this.tabela = new Table<>();
        clientConnectionLoop();
    }

    private void clientConnectionLoop() throws IOException {
        while (true) {
            ClientSocket clientSocket = new ClientSocket(this.serverSocket.accept());
            USUARIOS.add(clientSocket);
            new Thread(() -> {
                try {
                    clientMessageLoop(clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void adicionarConta(String mensagem) {
        String[] conta_corrente = mensagem.split(";");
        this.tabela.Adicionar(
                new ContaCorrente(conta_corrente[1], conta_corrente[0], conta_corrente[2], conta_corrente[3],
                        conta_corrente[4]),
                Integer.parseInt(conta_corrente[0]));
    }

    private void clientMessageLoop(ClientSocket clientSocket) throws IOException {
        String mensagem;
        try {
            while ((mensagem = clientSocket.getMessage()) != null) {
                switch (mensagem.split(";")[0]) {
                    case "sair": {
                        // SAIR
                        System.out.println(
                                "[sair] Mensagem de " + clientSocket.getSocketAddress() + ": " + mensagem);
                        break;
                    }
                    case "1": {
                        // AUTENTICAR
                        System.out.println(
                                "[1] Mensagem de " + clientSocket.getSocketAddress() + ": " + mensagem);
                        unicast(clientSocket, "Mundo");
                        break;
                    }
                    case "2": {
                        // CRIAR CONTA
                        System.out.println(
                                "[2] Mensagem de " + clientSocket.getSocketAddress() + ": " + mensagem);
                        String[] msg = mensagem.split(";");
                        try {
                            this.tabela.Adicionar(new ContaCorrente(msg[2], msg[1], msg[3], msg[4], msg[5]),
                                    Integer.parseInt(msg[1]));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        unicast(clientSocket, "status true");
                        break;
                    }
                    case "3": {
                        // SAQUE
                        System.out.println(
                                "[3] Mensagem de " + clientSocket.getSocketAddress() + ": " + mensagem);
                        ContaCorrente contaCorrente = this.tabela.BuscarCF(Integer.parseInt(mensagem.split(";")[1]))
                                .getValor();
                        contaCorrente.saque(Double.valueOf(mensagem.split(";")[2]));
                        try {
                            this.tabela.Atualizar(contaCorrente, Integer.parseInt(mensagem.split(";")[1]));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        unicast(clientSocket, "Quantia Retirada");
                        break;
                    }
                    case "4": {
                        // DEPÓSITO
                        System.out.println(
                                "[4] Mensagem de " + clientSocket.getSocketAddress() + ": " + mensagem);
                        ContaCorrente contaCorrente = this.tabela.BuscarCF(Integer.parseInt(mensagem.split(";")[1]))
                                .getValor();
                        contaCorrente.deposito(Double.valueOf(mensagem.split(";")[2]));
                        try {
                            this.tabela.Atualizar(contaCorrente, Integer.parseInt(mensagem.split(";")[1]));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        unicast(clientSocket, "Quantia Adicionada");
                        break;
                    }
                    case "5": {
                        // TRANSFERÊNCIA
                        System.out.println(
                                "[5] Mensagem de " + clientSocket.getSocketAddress() + ": " + mensagem);
                        ContaCorrente contaCorrenteEmissor = this.tabela
                                .BuscarCF(Integer.parseInt(mensagem.split(";")[1])).getValor();
                        contaCorrenteEmissor.saque(Double.valueOf(mensagem.split(";")[3]));
                        try {
                            this.tabela.Atualizar(contaCorrenteEmissor, Integer.parseInt(mensagem.split(";")[1]));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        ContaCorrente contaCorrenteDestino = this.tabela
                                .BuscarCF(Integer.parseInt(mensagem.split(";")[2])).getValor();
                        contaCorrenteDestino.deposito(Double.valueOf(mensagem.split(";")[3]));
                        try {
                            this.tabela.Atualizar(contaCorrenteDestino, Integer.parseInt(mensagem.split(";")[2]));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    case "6": {
                        // SALDO
                        System.out.println(
                                "[6] Mensagem de " + clientSocket.getSocketAddress() + ": " + mensagem);
                        ContaCorrente contaCorrente = this.tabela.BuscarCF(Integer.parseInt(mensagem.split(";")[1]))
                                .getValor();
                        unicast(clientSocket, "Saldo = R$ [ " + contaCorrente.getSaldo() + " ]");
                        break;
                    }
                    case "7": {
                        // INVESTIMENTOS
                        System.out.println(
                                "[7] Mensagem de " + clientSocket.getSocketAddress() + ": " + mensagem);
                        unicast(clientSocket, "Mundo");
                        break;
                    }
                    default:
                        System.out.println(
                                "Mensagem de " + clientSocket.getSocketAddress() + ": " + mensagem);
                        break;
                }
            }
        } finally {
            clientSocket.close();
        }
    }

    private void unicast(ClientSocket destinario, String mensagem) {
        ClientSocket emissor = this.USUARIOS.stream()
                .filter(user -> user.getSocketAddress().equals(destinario.getSocketAddress()))
                .findFirst().get();
        emissor.sendMessage(mensagem);
    }

    private void broadcast(ClientSocket emissor, String mensagem) {
        this.USUARIOS.stream()
                .filter(user -> !user.getSocketAddress().equals(emissor.getSocketAddress()))
                .forEach(user -> user.sendMessage(mensagem));
    }

}
