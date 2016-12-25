/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fileserver;

import client.ChatClient;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.Client;
import util.FileOperations;

/**
 *
 * @author navega
 */
public class FileReceiverThread extends Thread {

    private Client mClient;

    public FileReceiverThread(Client client) {
        this.mClient = client;
    }

    @Override
    public void run() {
        try {
            //Address
            String multiCastAddress = "224.0.0.1";
            final int multiCastPort = mClient.getSocket().getPort();
            final int bufferSize = 1024 * 4; //Maximum size of transfer object

            //Create Socket
            InetAddress group = InetAddress.getByName(multiCastAddress);
            MulticastSocket s = new MulticastSocket(multiCastPort);
            s.joinGroup(group);

            //Receive data
            while (true) {
                System.out.println(mClient.getUserName() + " espera receber um ficheiro na porta " + multiCastPort);

                //Create buffer
                byte[] buffer = new byte[bufferSize];
                s.receive(new DatagramPacket(buffer, bufferSize, group, multiCastPort));

                //Deserialze object
                ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
                ObjectInputStream ois = new ObjectInputStream(bais);
                try {
                    Object readObject = ois.readObject();
                    if (readObject instanceof File) {

                        File file = (File) readObject;

                        File atualFile = new File(Client.PATH + mClient.getUserName() + "/" + file.getName());
                        FileOperations.copyFile(file, atualFile);

                        System.out.println(mClient.getUserName() + " recebeu ->" + atualFile.getName());
                    }
                } catch (Exception e) {
                    //mOut.print("No object could be read from the received UDP datagram.");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ListReceiverThread.class.getName()).log(Level.SEVERE, null, ex);

        }
    }
}
