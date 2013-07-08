package com.agiac.filechunk.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Adam Giacobbe
 * 
 * Tracker for p2p network
 */
class TrackerQuery {

    int query;
    int[] downloaded;
    final Socket connection;

    TrackerQuery(Socket CONNECTION, int chunkSize) {
        connection = CONNECTION;
    }
}

/**
 * This class pings clients periodically in order to determine if they are still
 * "alive".  If they are, it sends them an updated list.
 * 
 * @author Adam Giacobbe
 */
class PingClients implements Runnable {

    final LinkedList<String> peerList;
    HashMap<String, Integer> peerMap;

    PingClients(LinkedList<String> pLIST, HashMap<String, Integer> MAP) {
        peerList = pLIST;
        peerMap = MAP;
    }

    @Override
    public void run() {
        LinkedList<String> removeList = new LinkedList<String>();

        while (true) {
            removeList.clear();
            try {
                System.out.println("Sleeping...");
                Thread.sleep(5000);
                System.out.println("Pinging...");
                for (String temp : peerList) {
                    try {
                        System.out.println("Connecting to..." + temp.substring(0, temp.indexOf("#")) + " : " + (peerMap.get(temp) - 1));
                        Socket ping = new Socket(temp.substring(0, temp.indexOf("#")), peerMap.get(temp) - 1);
                        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(ping.getOutputStream()));

                        String listString = peerMap.toString();
                        System.out.println("listString = " + listString);
                        listString = listString + '\0';
                        dos.writeChars(listString);
                        dos.flush();
                        ping.close();
                    } catch (UnknownHostException ex) {
                        System.out.println("Failed! Removing...");
                        removeList.add(temp);
                    } catch (IOException ex) {
                        System.out.println("Failed! Removing...");
                        removeList.add(temp);
                    }
                }

                for (String temp : removeList) {
                    peerList.remove(temp);
                    peerMap.remove(temp);
                }

            } catch (InterruptedException ex) {
                Logger.getLogger(PingClients.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


    }
}

/**
 * This class handles the initial connection of a new peer to the tracker
 * and sends them an initial list to start with.
 * @author Adam Giacobbe
 */
class ThreadedConnection extends Thread {

    final HashMap<String, Integer> list;
    final LinkedBlockingQueue<Socket> queue;
    final LinkedList<String> peerList;
    DataOutputStream dos;
    DataInputStream dis;

   
    ThreadedConnection(LinkedBlockingQueue<Socket> QUEUE, HashMap<String, Integer> LIST, LinkedList<String> pLIST) {
        list = LIST;
        queue = QUEUE;
        peerList = pLIST;
    }

    @Override
    public void run() {
        Socket cxn = null;
        while (true) {
            try {

                cxn = queue.take();

                dis = new DataInputStream(new BufferedInputStream(cxn.getInputStream()));
                dos = new DataOutputStream(new BufferedOutputStream(cxn.getOutputStream()));

                int port = dis.readInt();

                String listString = list.toString();
                System.out.println("listString = " + listString);
                listString = listString + '\0';
                dos.writeChars(listString);
                dos.flush();

                char temp = dis.readChar();

                String key = cxn.getInetAddress().toString().substring(1) + "#" + port;


                synchronized (System.out) {
                    peerList.add(key);
                    list.put(key, port);
                }

                try {
                    if (!cxn.isClosed()) {
                        cxn.close();
                    }
                } catch (IOException ioe) {
                }

            } catch (InterruptedException ie) {
            } catch (IOException ioe) {
            }
        }
    }
}

/**
 * Synchronized Tracker list on System.out
 * 
 * @author Adam Giacobbe
 */
public class Tracker implements Runnable {

    private int port;
    ServerSocket tracker = null;

    public Tracker(int port) {
        this.port = port;
        try {
            tracker = new ServerSocket(port);
            if(port == 0) {
                this.port = tracker.getLocalPort();
            }
        } catch (IOException ioe) {
        }
    }

    public int getPort() {
        return port;
    }

    @Override
    public void run() {
        HashMap<String, Integer> clientMap = new HashMap<String, Integer>();
        LinkedList<String> clientList = new LinkedList<String>();

        final LinkedBlockingQueue<Socket> queuedConnections = new LinkedBlockingQueue<Socket>();

        Socket connection = null;
        try {
            InetAddress hostAdr = InetAddress.getLocalHost();
            System.out.println("###Tracker Established On: " + hostAdr.getHostName() + "/" + hostAdr.getHostAddress() + " : " + port);
        } catch (IOException ioe) {
        }

        ThreadedConnection clientTalk = new ThreadedConnection(queuedConnections, clientMap, clientList);
        Thread t = new Thread(clientTalk);
        t.setName("Client Talk");
        t.start();

        PingClients clientPing = new PingClients(clientList, clientMap);
        t = new Thread(clientPing);
        t.setName("Client Ping");
        t.start();

        while (true) {
            try {
                connection = tracker.accept();
                connection.setSoTimeout(15000);

                System.out.println("### Cxn received from " + connection.getLocalAddress().getHostAddress());
                synchronized (queuedConnections) {
                    try {
                        queuedConnections.put(connection);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }

            } catch (IOException ioe) {
            }
        }

    }
}
