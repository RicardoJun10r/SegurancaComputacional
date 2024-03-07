package server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.SecretKey;

import util.ClientSocket;

public class ServidorChave {

    private int porta;

    private ServerSocket serverSocket;

    private final List<ClientSocket> USUARIOS = new LinkedList<>();

    private SecretKey chave;

    private ObjectOutputStream outputStream;

    public ServidorChave(int porta, SecretKey chave) {
        this.porta = porta;
        this.chave = chave;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(porta);
        System.out.println("Iniciando servidor na porta = " + porta);
        clientConnectionLoop();
        serverSocket.close();
    }

    private void clientConnectionLoop() throws IOException {
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

    private void clientMessageLoop(ClientSocket clientSocket) throws IOException {
        String mensagem;
        try {
            while ((mensagem = clientSocket.getMessage()) != null) {
                continue;
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

}
