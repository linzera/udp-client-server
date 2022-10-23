package br.edu.ucsal;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Sender {

    public static void main(String[] args) {

        if(args.length < 2) {
            System.out.println("Usage: Sender <host> <port>");
            System.exit(1);
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        start(host, port);
    }

    public static void start(String host, int port) {
        System.out.print("Caminho do arquivo para ser enviado: ");

        Scanner sc = new Scanner(System.in);
        String path = sc.nextLine();

        // Para teste: /Users/monarchlin/Desktop/scteste.png

        File file = new File(path);

        if(!file.exists()) {
            System.out.println("Arquivo não encontrado!");
            System.exit(1);
        }

        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(host);

            String fileName = file.getName();
            byte[] fileNameBytes = fileName.getBytes();
            // Envia o nome do arquivo
            socket.send(new DatagramPacket(fileNameBytes, fileNameBytes.length, address, port));

            //Envia o arquivo
            sendFile(file, socket, address, port);

        } catch (SocketException e) {
            System.out.println("Erro ao criar socket: " + e.getMessage());
        } catch (UnknownHostException e) {
            System.out.println("Host desconhecido: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Erro ao enviar arquivo: " + e.getMessage());
        }
    }

    private static void sendFile(File file, DatagramSocket socket, InetAddress address, int port) throws IOException {
        System.out.println("Enviando arquivo...");

        byte[] fileBytes = new byte[(int) file.length()];
        int fileSize = fileBytes.length;
        boolean isLastPacket = false;

        // Loop que intera tamanho do arquivo e envia ele em pacotes de 100 bytes
        for(int i = 0; i < fileSize; i += Helpers.FILE_BUFFER_SIZE) {
            // Verifica se é o último pacote ao comprar o tamanho do arquivo com o tamanho do buffer
            isLastPacket = i + Helpers.FILE_BUFFER_SIZE >= fileSize;

            // Cria o pacote com o tamanho do buffer ou o tamanho do arquivo
            byte[] message = new byte[Helpers.FILE_BUFFER_SIZE + 1];
            // Adiciona o tamanho do arquivo no primeiro byte do pacote
            byte[] fileBuffer = new byte[Helpers.FILE_BUFFER_SIZE];

            if(isLastPacket) {
                fileBuffer = new byte[fileSize - i];
            }

            // Copia o arquivo para o buffer
            System.arraycopy(fileBytes, i, fileBuffer, 0, fileBuffer.length);
            // Adiciona o tamanho do arquivo no primeiro byte do pacote para ser enviado
            message[0] = (byte) fileBuffer.length;
            // Copia o buffer para o pacote
            System.arraycopy(fileBuffer, 0, message, 1, fileBuffer.length);

            System.out.println("Enviando pacote " + (i / Helpers.FILE_BUFFER_SIZE + 1) + " de " + (fileSize / Helpers.FILE_BUFFER_SIZE + 1));

            socket.send(new DatagramPacket(message, message.length, address, port));

            // Caso seja o ultimo pacote, espera a confirmação do servidor
            if(isLastPacket){
                receiveAck(socket);
            }
        }

    }

    private static void receiveAck(DatagramSocket socket) throws IOException {
        byte[] ack = new byte[1];
        DatagramPacket ackPacket = new DatagramPacket(ack, ack.length);
        socket.receive(ackPacket);
        System.out.println("ACK recebido!");
    }

}
