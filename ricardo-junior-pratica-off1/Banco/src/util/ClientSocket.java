package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketAddress;

public class ClientSocket {
    
    private String id;

    private final Socket socket;

    private final BufferedReader leitor;

    private final PrintWriter escritor;

    public ClientSocket(Socket socket) throws IOException{
        this.socket = socket;
        System.out.println("Cliente = " + socket.getRemoteSocketAddress() + " conectado!");
        this.id = socket.getRemoteSocketAddress().toString().split(":")[1];
        this.leitor = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.escritor = new PrintWriter(socket.getOutputStream(), true);
    }

    public SocketAddress getSocketAddress(){
        return this.socket.getRemoteSocketAddress();
    }

    public void setId(String id){
        this.id = id;
    }

    public String getId(){
        return this.id;
    }

    public void close(){
        try {
            this.leitor.close();
            this.escritor.close();
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMessage(){
        try {
            return this.leitor.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "\nERRO [ NA MENSAGEM ]: " + e.getMessage();
        }
    }

    public boolean sendMessage(String mensagem){
        this.escritor.println(mensagem);
        return !this.escritor.checkError();
    }
    
}
