package com.agiac.filechunk.network;

import com.agiac.filechunk.MessageServer;
import com.agiac.filechunk.UtilityFunctions;
import com.agiac.filechunk.chunk.ChunkDownloadPool;
import com.agiac.filechunk.chunk.ChunkUploadPool;
import com.agiac.filechunk.chunk.ConstructFile;
import com.agiac.filechunk.peer.P2PFileMetadata;
import com.agiac.filechunk.peer.Peer;
import com.agiac.filechunk.peer.PeerListener;
import com.agiac.filechunk.protocol.Query;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class accepts connections from the tracker on (clientPort-1)
 * in order to remain "alive" and get new list updates
 * 
 * @author Adam Giacobbe
 * 
 */
class AcceptTrackerPing implements Runnable {
    final int port;
    final List<Peer> ips;
    final Object stateLock;

    AcceptTrackerPing(int port, List<Peer> ips, Object stateLock) {
        this.port = port;
        this.ips = ips;
        this.stateLock = stateLock;
    }

    @Override
    public void run() {

        ServerSocket ss = null;
        try {
            ss = new ServerSocket(port);
            System.out.println("Accepting pings on port " + port);
            while(true) {
                Socket tracker = ss.accept();
                try {
                    System.out.println("### Asking for new Seed List");
                    UtilityFunctions.receiveNewList(stateLock, tracker, ips, port+1);
                    System.out.println("### Received updated Seed List");
                } finally {
                    tracker.close();
                }
            }
        } catch (IOException ex) {
            System.out.println("ServerSocket for accepting pings died!");
            if(ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }
}

/**
 * This class handles the main client thread of a p2p client.
 * Everything starts here.
 * 
 * @author Adam Giacobbe
 * 
 */
public class Client {
    final static int NUM_DOWNLOAD_THREADS = 4;

    ServerSocket ss;
    Socket tracker;

    final HashMap<Integer, byte[]> chunkMap;
    final LinkedList<Integer> chunks;
    final int clientPort;
    final LinkedBlockingQueue<Integer> chunkRequests;
    final LinkedBlockingQueue<Query> cxnPool;
    final List<Peer> ips;
    final PeerListener pListener;

    final Object stateLock = new Object();

    public Client(String user, int clientPort, HashMap<Integer, byte[]> chunkMap, File p2pFile, PeerListener pListener, boolean isSeed) {
        P2PFileMetadata p2pFileMetadata = UtilityFunctions.readP2PFromFile(p2pFile);

        this.clientPort = clientPort;
        this.chunkMap = chunkMap;

        chunks = new LinkedList<Integer>();
        chunkRequests = new LinkedBlockingQueue<Integer>();
        cxnPool = new LinkedBlockingQueue<Query>();
        ips = new LinkedList<Peer>();
        this.pListener = pListener;

        try {
            tracker = new Socket(p2pFileMetadata.getTrackerIP(), p2pFileMetadata.getTrackerPort());
        } catch (IOException ioe) {
            System.out.println("### Cannot connect to tracker!");
            System.exit(-1);
        }

        try {
            ss = new ServerSocket(clientPort);
            if(clientPort == 0) {
                clientPort = ss.getLocalPort();
            }

            //Message Server
            MessageServer m = new MessageServer(clientPort + 1);
            Thread mst = new Thread(m);
            mst.start();

            //Tracker Ping
            AcceptTrackerPing atp = new AcceptTrackerPing(clientPort-1,ips, stateLock);
            Thread acceptPings = new Thread(atp);
            acceptPings.start();

            InetAddress hostAdr = InetAddress.getLocalHost();
            System.out.println("### Client listen socket established on: " + hostAdr.getHostName() + "/" + hostAdr.getHostAddress() + " : " + clientPort);

        } catch (IOException ex) {
            System.out.println("Cannot start server on clientPort, port is not available");
            System.exit(-1);
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("### Asking for List / Sending Address");
        UtilityFunctions.receivedList(stateLock, tracker, ips, clientPort);
        System.out.println("### Received list");

        try {
            tracker.close();
        } catch (IOException ioe) {
        }

        for (int i = 0; i < p2pFileMetadata.getNumChunks(); i++) {
            chunks.add(i);
        }

        //Get random chunk order
        while (chunks.size() != 0) {
            int randChunk = ((new Random(System.nanoTime())).nextInt()) % chunks.size();
            if (randChunk < 0) {
                randChunk *= -1;
            }
            int c = chunks.remove(randChunk);

            if (chunkMap.get(c) == null) {
                chunkRequests.add(c);
                System.out.println("Added chunk " + c + " to request list");
            }
        }

        CountDownLatch cdl = new CountDownLatch(NUM_DOWNLOAD_THREADS);

        ConstructFile constructFileThread = new ConstructFile(cdl, user, p2pFileMetadata.getName(), p2pFileMetadata.getFileSize(), chunkMap);
        constructFileThread.start();

        if (isSeed == false) {
            ChunkDownloadPool chunkDownloadPool = new ChunkDownloadPool(user, p2pFileMetadata, chunkMap, chunkRequests, ips, clientPort, cdl, pListener, stateLock);
            chunkDownloadPool.download();
            System.out.println("### Download Threads Created");
        }

        ChunkUploadPool chunkUploadPool = new ChunkUploadPool(cxnPool, chunkMap, p2pFileMetadata.getNumChunks());
        chunkUploadPool.seedUpload();
        System.out.println("### Upload threads created");

        while (true) {
            try {
                Socket nextCxn = ss.accept();
                System.out.println("Connection receieved from " + nextCxn.getInetAddress().toString());
                nextCxn.setSoTimeout(15000);

                DataInputStream dis = new DataInputStream(new BufferedInputStream(nextCxn.getInputStream()));
                int port = dis.readInt();
                String key = nextCxn.getInetAddress().toString().substring(1);

                synchronized (stateLock) {
                    System.out.println("Adding, " + key + " on port " + port);
                    Peer peer = new Peer(key, port);
                    pListener.addPeer(peer);
                    ips.add(peer);

                    stateLock.notifyAll();
                }

                int chunkNum = dis.readInt();
                Query nextQuery = new Query(nextCxn, chunkNum);

                try {
                    cxnPool.put(nextQuery);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }

            } catch (IOException ioe) {
            }

        }
    }
}
