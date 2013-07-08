package com.agiac.filechunk;

import com.agiac.filechunk.peer.P2PFileMetadata;
import com.agiac.filechunk.peer.Peer;
import com.google.gson.Gson;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Adam Giacobbe
 */
public class UtilityFunctions {
	
	/**
	 * This function returns the MAC address 
	 *
	 */
    private static String getMAC() {
        // Gets the MAC address
        String macAddress = "";
        try {
            NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            if (ni != null) {
                byte[] mac = new byte[8];
                Random rand = new Random();
                rand.setSeed(System.nanoTime());
                rand.nextBytes(mac);
                if (mac != null) {
                    for (int i = 0; i < mac.length; i++) {
                        macAddress += String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : "");
                    }
                } else {
                    System.out.println("Address doesn't exist or not accessible");
                }
            } else {
                System.out.println("Network interface for specified address not found");
            }
        } catch (SocketException e) {
            System.out.println("Unknown host " + e);
        } catch (UnknownHostException e) {
            System.out.println("Unknown host " + e);
        }
        return macAddress;
    }

    /**
     * This function takes in the input string and adds each ip/port to the hashMap and ip list
     * 
     * @author Adam Giacobbe
     * 
     * @param stateLock
     * @param input
     * @param ips
     * @param clientPort
     */
    public static void addString(Object stateLock, String input, List<Peer> ips, int clientPort) {

        System.out.println("### input = " + input);
        input = input.substring(1, input.length() - 1);
        if (input.equals("")) {
            return;
        }
        input = input + ",@";
        System.out.println("### input = " + input);

        String combinedValue = "";

        String localIp = null;
        try {
            localIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            Logger.getLogger(UtilityFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }

        synchronized (stateLock) {
            ips.clear();

            for (int i = 0; i < input.length();) {
                if (input.charAt(i) == '@') {
                    return;
                }

                int end = input.indexOf(',', i);
                if (end < 0 || end >= input.length()) {
                    return;
                }

                combinedValue = input.substring(i, end);
                System.out.println("### combinedValue = " + combinedValue);

                String ip = combinedValue.substring(0, combinedValue.indexOf("="));
                int port = Integer.parseInt(combinedValue.substring(combinedValue.indexOf("=") + 1));
                Peer peer = new Peer(ip.substring(0, ip.indexOf("#")), port);

                //Add Key+value to HashMap

                if ((peer.getIp().equals(localIp)) && peer.getPort() == clientPort) {
                    System.out.println("Skipping Myself");
                } else {
                    ips.add(peer);
                    System.out.println("### Key = " + ip);
                    System.out.println("### Value = " + port);
                }
                i = end + 1;
            }

            stateLock.notifyAll();
        }
    }

    /**
     * This function receives an updated peer list from the tracker
     * 
     * @author Adam Giacobbe
     *
     * @param cxn
     * @param ips
     * @param clientPort
     */
    public static void receiveNewList(Object stateLock, Socket cxn, List<Peer> ips, int clientPort) {
        try {
            DataInputStream dis = new DataInputStream(new BufferedInputStream(cxn.getInputStream()));

            String stringList = "";

            char temp = dis.readChar();

            while (temp != '\0') {
                stringList += temp;
                temp = dis.readChar();
            }

            addString(stateLock, stringList, ips, clientPort);

        } catch (IOException ioe) {
        }
    }

    /**
     * This function receives the first list from the tracker to start downloading from peers
     * 
     * @author Adam Giacobbe
     * @param stateLock
     * @param cxn
     * @param ips
     * @param clientPort
     */
    public static void receivedList(Object stateLock, Socket cxn, List<Peer> ips, int clientPort) {
        try {
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(cxn.getOutputStream()));
            DataInputStream dis = new DataInputStream(new BufferedInputStream(cxn.getInputStream()));

            dos.writeInt(clientPort);
            dos.flush();

            String mac = getMAC();
            mac = mac + '\0';
            dos.writeChars(mac);
            dos.flush();

            String stringList = "";

            char temp = dis.readChar();

            while (temp != '\0') {
                stringList += temp;
                temp = dis.readChar();
            }

            addString(stateLock, stringList, ips, clientPort);

        } catch (IOException ioe) {
        }
    }

    /**
     *	This function takes in a File and the size of each chunk.  It then breaks apart the file into
     *   chunks and stores them inside the HashMap giving each one an ID in order so that it can be
     *   put back together.
     *   
     *  @author Adam Giacobbe
     *  @return Returns a HashMap<Integer,byte[]> which consists of a map of chunk number to chunk data
     */
    public static HashMap<Integer, byte[]> chunkFile(File f, int chunkSize, int fileSize) {
        HashMap<Integer, byte[]> file = new HashMap<Integer, byte[]>();

        System.out.println("f.length() = " + f.length());

        FileInputStream fis;
        try {
            fis = new FileInputStream(f);
            BufferedInputStream bfis = new BufferedInputStream(fis);

            byte[] chunk;
            int chunkID;
            int bytesRead = 0;

            byte[] buf = new byte[1];

            int numChunks = ((fileSize / chunkSize) + 1);
            System.out.println("numChunks = " + numChunks);
            for (chunkID = 0; chunkID < numChunks; chunkID++) {
                chunk = new byte[chunkSize];
                for (int i = 0; i < chunkSize; i++) {
                    int result = bfis.read(buf);
                    if (result == -1) {
                        System.out.println("end of stream reached");
                        System.out.println("bytesRead = " + bytesRead);
                        file.put(chunkID, chunk);
                        return file;
                    }
                    chunk[i] = buf[0];
                    bytesRead += 1;
                    fileSize--;
                }
                file.put(chunkID, chunk);
                if (fileSize == 0) {
                    System.out.println("bytesRead = " + bytesRead);
                    return file;
                }
            }

            System.out.println("bytesRead = " + bytesRead);
        } catch (FileNotFoundException e) {
        } catch (IOException ioe) {
        }
        return file;
    }

    /**
     * This function creates a starting file for downloading this file on our p2p network
     * similar to a .torrent file.  It consists of the following:
     * 
     * @author Adam Giacobbe
     * 
     * @param name
     * @param numChunks
     * @param chunkSize
     * @param fileSize
     * @param trackerIP
     * @param trackerPort
     * @return Returns the File that is created
     */
    
    public static File createP2PFile(String name, String path, int numChunks, int chunkSize, long fileSize, String trackerIP, int trackerPort) {
        P2PFileMetadata p2pMetadata = new P2PFileMetadata();
        p2pMetadata.setChunkSize(chunkSize);
        p2pMetadata.setName(name);
        p2pMetadata.setNumChunks(numChunks);
        p2pMetadata.setFileSize(fileSize);
        p2pMetadata.setTrackerIP(trackerIP);
        p2pMetadata.setTrackerPort(trackerPort);

        return writeP2PToFile(p2pMetadata, path);
    }

    public static File writeP2PToFile(P2PFileMetadata p2pFileMetadata, String path) {
        Gson gson = new Gson();
        FileWriter fw = null;

        try {
            File p2pFile = new File(new File(path), p2pFileMetadata.getName() + ".p2p");
            System.out.println("Path: " + p2pFile.getAbsolutePath());
            fw = new FileWriter(p2pFile);
            gson.toJson(p2pFileMetadata, fw);
            return p2pFile;
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            try {
                if(fw != null) {
                    fw.close();
                }
            } catch(Exception e) {

            }
        }
        return null; //Failed to write file
    }

    public static P2PFileMetadata readP2PFromFile(File p2pFile) {
        Gson gson = new Gson();
        FileReader fr = null;
        try {
            fr = new FileReader(p2pFile);
            return gson.fromJson(fr, P2PFileMetadata.class);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            try {
                if(fr != null) {
                    fr.close();
                }
            } catch(Exception e) {

            }
        }
        return null; //Failed to write file
    }

}
