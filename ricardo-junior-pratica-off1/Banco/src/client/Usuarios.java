package client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import util.ClientSocket;

public class Usuarios implements Runnable {

    private final String ENDERECO_SERVER = "localhost";

    private ClientSocket clientSocket;

    private Scanner scan;

    private Boolean logado;

    public Usuarios() {
        this.scan = new Scanner(System.in);
        this.logado = false;
    }

    @Override
    public void run() {
        String mensagem;
        while ((mensagem = this.clientSocket.getMessage()) != null) {
            if (mensagem.split(" ")[0].equals("status")) {
                logado = Boolean.parseBoolean(mensagem.split(" ")[1]);
            } else {
                System.out.println(
                        "Resposta do banco: " + mensagem);
            }
        }
    }

    private void autenticar() {
        System.out.println("> 1 Entrar\n> 2 Registrar-se");
        String op = scan.next();
        if (op.equals("1")) {
            System.out.println("> CPF");
            System.out.print("> ");
            String login = scan.nextLine();
            System.out.println("> Senha");
            System.out.print("> ");
            String senha = scan.next();
            enviar("1 " + login + " " + senha);
        } else if (op.equals("2")) {
            String senha;
            String confirmar_senha;
            String nova_conta = "";
            System.out.println("Registrando\n> CPF");
            System.out.print("> ");
            nova_conta += scan.next() + ";";
            System.out.println("> Nome");
            System.out.print("> ");
            scan.nextLine();
            nova_conta += scan.nextLine() + ";";
            System.out.println("> Endereço");
            System.out.print("> ");
            nova_conta += scan.nextLine() + ";";
            System.out.println("> Telefone");
            System.out.print("> ");
            nova_conta += scan.nextLine() + ";";
            // do {
            // System.out.println("> Senha");
            // System.out.print("> ");
            // senha = scan.next();
            // System.out.println("> Confirmar Senha");
            // System.out.print("> ");
            // confirmar_senha = scan.next();
            // } while (validarSenha(senha, confirmar_senha));
            System.out.println("> Senha");
            System.out.print("> ");
            senha = scan.next();
            nova_conta += senha;
            enviar("2;" + nova_conta);
        }
    }

    private void enviar(String mensagem) {
        this.clientSocket.sendMessage(mensagem);
    }

    private Boolean validarSenha(String senha, String confirmar) {
        if (senha.equals(confirmar) && senha.length() >= 3)
            return true;
        else
            return false;
    }

    private void menu() {
        System.out.println(
                "> 3 [ SAQUE ]\n> 4 [ DEPÓSITO ]\n> 5 [ Transferência ]\n> 6 [ SALDO ]\n> 7 [ INVESTIMENTOS ]\n> sair");
    }

    private void messageLoop() {
        String mensagem = "";
        try {
            do {
                Thread.sleep(300);
                if (!logado) {
                    autenticar();
                } else {
                    System.out.println("> LOGADO");
                    menu();
                    System.out.print("> ");
                    mensagem = scan.next();
                    processOption(mensagem);
                }
            } while (!mensagem.equalsIgnoreCase("sair"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void processOption(String option) {
        String msg;
        switch (option) {
            case "3":
                System.out.println("Escreva:");
                System.out.print("> ");
                msg = this.scan.nextLine();
                enviar("executar " + msg);
                break;
            case "4":
                System.out.println("> CPF");
                System.out.print("> ");
                msg = this.scan.next() + ";";
                System.out.println("> Quantia");
                System.out.print("> ");
                msg += this.scan.next();
                enviar("4;" + msg);
                break;
            case "5":
                System.out.println("> Seu CPF");
                System.out.print("> ");
                msg = this.scan.next() + ";";
                System.out.println("> Enviar para qual CPF ?");
                System.out.print("> ");
                msg += this.scan.next() + ";";
                System.out.println("> Quantia");
                System.out.print("> ");
                msg += this.scan.next();
                enviar("5;" + msg);
                break;
            case "6":
                System.out.println("> CPF");
                System.out.print("> ");
                msg = this.scan.next() + ";";
                enviar("6;" + msg);
                break;
            case "7":
                break;
            case "sair":
                System.out.println("Saindo");
                break;
            default:
                System.out.println("comando não achado");
                break;
        }
    }

    public void start() throws IOException, UnknownHostException {
        try {
            clientSocket = new ClientSocket(
                    new Socket(ENDERECO_SERVER, 1025));
            System.out
                    .println("Cliente conectado ao servidor de endereço = " + ENDERECO_SERVER + " na porta = " + 1025);
            new Thread(this).start();
            messageLoop();
        } finally {
            clientSocket.close();
        }
    }

}
