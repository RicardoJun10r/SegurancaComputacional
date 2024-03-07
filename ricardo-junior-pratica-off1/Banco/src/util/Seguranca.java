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

    private static Seguranca instance;

    public static final String ALG = "HmacSHA256";

    private KeyGenerator geradorDeChaves;

    private SecretKey chave;

    private String mensagem;

    private String mensagemCifrada;

    private Seguranca() {
        try {
            gerarChave(192);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static Seguranca getInstance() {
        if (instance == null) {
            synchronized (Seguranca.class) {
                if (instance == null) {
                    System.out.println("INSTANCIANDO");
                    instance = new Seguranca();
                }
            }
        }
        return instance;
    }

    public void setChave(SecretKey chave) {
        this.chave = chave;
    }

    public SecretKey getChave() {
        return this.chave;
    }

    public void gerarChave(int t) throws NoSuchAlgorithmException {
        geradorDeChaves = KeyGenerator.getInstance("AES");
        geradorDeChaves.init(t);
        chave = geradorDeChaves.generateKey();
        System.out.println(Arrays.toString(chave.getEncoded()));
    }

    public String cifrar(String textoAberto, SecretKey chave) {
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

    public String decifrar(String textoCifrado, String chave) {

        // byte[] chaveBytes = chave.getBytes(StandardCharsets.UTF_8);

        // // Criar uma instância de SecretKeySpec

        System.out.println("CHAVE CHEGOU: " + chave);

        byte[] decodedKey = decodificar(Base64.getEncoder().encodeToString(chave));

        // rebuild key using SecretKeySpec
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALG); 

        System.out.println("Chave gerada: " + originalKey.toString());

        // Decriptação
        byte[] bytesMensagemCifrada = decodificar(textoCifrado);
        Cipher decriptador = null;
        String mensagemDecifrada;

        try {
            decriptador = Cipher.getInstance("AES/CBC/PKCS5Padding");
            decriptador.init(Cipher.DECRYPT_MODE, originalKey);
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
