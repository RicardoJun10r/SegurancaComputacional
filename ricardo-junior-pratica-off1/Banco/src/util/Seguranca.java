package util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Seguranca {

    public static final String ALG = "HmacSHA256";

    private KeyGenerator geradorDeChaves;

    public static SecretKey chave;

    private String mensagem;

    private String mensagemCifrada;

    public Seguranca(int num){
        try {
            gerarChave(num);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public Seguranca(){}

    public void gerarChave(int t) throws NoSuchAlgorithmException {
        geradorDeChaves = KeyGenerator.getInstance("AES");
        geradorDeChaves.init(t);
        chave = geradorDeChaves.generateKey();
        System.out.println(Arrays.toString(chave.getEncoded()));
    }

    public String cifrar(String textoAberto, SecretKey chave) {

        System.out.println("CIFRANDO");

        byte[] bytesMensagemCifrada = null;
        Cipher cifrador = null;
        // Encriptar mensagem

        System.out.println("CHAVE: " + chave);

        mensagem = textoAberto;
        try {
            cifrador = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cifrador.init(Cipher.ENCRYPT_MODE, chave);
            bytesMensagemCifrada = cifrador.doFinal(mensagem.getBytes());
            mensagemCifrada = codificar(bytesMensagemCifrada);
            System.out.println(
                    ">> Mensagem cifrada = " + mensagemCifrada);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException
                | InvalidKeyException e) {
            e.printStackTrace();
        }
        return mensagemCifrada;
    }

    private String codificar(byte[] bytesCifrados) {
        String mensagemCodificada = Base64
                .getEncoder()
                .encodeToString(bytesCifrados);
        return mensagemCodificada;
    }

    private byte[] decodificar(String mensagemCodificada) {
        mensagemCodificada = mensagemCodificada.replaceAll("\\s", "");
        byte[] bytesCifrados = Base64
                .getDecoder()
                .decode(mensagemCodificada);
        return bytesCifrados;
    }

    public String decifrar(String textoCifrado, SecretKey chave) {

        // byte[] chaveBytes = chave.getBytes(StandardCharsets.UTF_8);

        // // Criar uma instância de SecretKeySpec

        // Decriptação
        byte[] bytesMensagemCifrada = decodificar(textoCifrado);
        Cipher decriptador = null;
        String mensagemDecifrada;

        try {
            decriptador = Cipher.getInstance("AES/CBC/PKCS5Padding");
            decriptador.init(Cipher.DECRYPT_MODE, chave);
            byte[] bytesMensagemDecifrada = decriptador.doFinal(bytesMensagemCifrada);
            mensagemDecifrada = new String(bytesMensagemDecifrada);
            mensagem = mensagemDecifrada;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException
                | InvalidKeyException e) {
            e.printStackTrace();
        }
        return mensagem;
    }

    public String hMac(String mensagem) {
        byte[] bytesHMAC = null;
        try {
            Mac shaHMAC = Mac.getInstance(ALG);
            shaHMAC.init(chave);
            bytesHMAC = shaHMAC
                    .doFinal(mensagem.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException
                | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return codificar(bytesHMAC);
    }

}
