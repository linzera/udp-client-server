package br.edu.ucsal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;

public class Receiver {

    public static void main(String[] args) {
        if(args.length < 2) {
            System.out.println("Usage: Receiver <port> <pathToSave>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        String pathToSaveFiles = args[1];

        createFileReceiver(port, pathToSaveFiles);
    }

    private static void createFileReceiver(int port, String pathToSaveFiles) {
        try {
            DatagramSocket socket = new DatagramSocket(port);

            byte[] fileNameBytes = new byte[1024];
            DatagramPacket fileNamePacket = new DatagramPacket(fileNameBytes, fileNameBytes.length);
            socket.receive(fileNamePacket);

            String fileName = new String(fileNamePacket.getData(), 0, fileNamePacket.getLength());
            System.out.println("Recebendo arquivo: " + fileName);

            File file = new File("./" + fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            while(true) {

                byte[] message = new byte[101];
                byte[] fileBuffer = new byte[Helpers.FILE_BUFFER_SIZE];
                boolean isLastPacket = false;

                DatagramPacket receivedPacket = new DatagramPacket(message, message.length);
                socket.receive(receivedPacket);

                message = receivedPacket.getData();
                int fileSize = (int) message[0];

                isLastPacket = fileSize < 100;

                if(!isLastPacket){
                    // Copia o conteúdo do buffer para o arquivo, como nao é o ultimo pacote copia com o tamanho fixo de 100 bytes
                    System.arraycopy(message, 1, fileBuffer, 0, Helpers.FILE_BUFFER_SIZE);
                    fileOutputStream.write(fileBuffer, 0, fileSize);
                }else{
                    // Copia o conteúdo do buffer para o arquivo, como é o ultimo pacote copia com o tamanho do arquivo faltante
                    System.arraycopy(message, 1, fileBuffer, 0, fileSize);
                    fileOutputStream.write(fileBuffer, 0, fileSize);
                    fileOutputStream.close();
                    sendAck(socket, receivedPacket.getAddress(), receivedPacket.getPort());
                    break;
                }
            }


            System.out.println("Arquivo salvo com sucesso!");

        } catch (SocketException e) {
            System.out.println("Erro ao criar socket: " + e.getMessage());
        } catch (UnknownHostException e) {
            System.out.println("Host desconhecido: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Erro ao receber arquivo: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private static void sendAck(DatagramSocket socket, InetAddress address, int port) throws IOException {
        byte[] ackBytes = new byte[1];
        DatagramPacket ackPacket = new DatagramPacket(ackBytes, ackBytes.length, address, port);
        socket.send(ackPacket);
    }
}
