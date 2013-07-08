package com.agiac.filechunk.chunk;

import com.agiac.filechunk.peer.P2PFileMetadata;
import com.agiac.filechunk.peer.Peer;
import com.agiac.filechunk.peer.PeerListener;
import com.agiac.filechunk.peer.ReputationSet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

/**
 *
 */

public class ChunkDownloadPool {
    private static final int DEFAULT_THREAD_POOL_SIZE = 4;

    Executor threadPool = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);

    //Shared State
    final LinkedBlockingQueue<Integer> chunkQueue;
    final HashMap<Integer, byte[]> chunks;
    final LinkedBlockingQueue<Integer> activeChunks;
    final List<Peer> ips;

    final int uploadPort;
    final String user;
    final P2PFileMetadata p2pFileMetadata;
    final CountDownLatch cdl;

    final PeerListener pListener;

    final Object stateLock;

    public ChunkDownloadPool(String username, P2PFileMetadata p2pFileMetadata, HashMap<Integer, byte[]> chunks,
                             LinkedBlockingQueue<Integer> chunkIdQueue, List<Peer> ips,
                             int myPort, CountDownLatch CDL, PeerListener pListener, Object stateLock) {
        this.chunkQueue = chunkIdQueue;
        this.stateLock = stateLock;
        this.ips = ips;
        this.chunks = chunks;
        this.uploadPort = myPort;
        this.p2pFileMetadata = p2pFileMetadata;
        this.user = username;
        this.activeChunks = new LinkedBlockingQueue<Integer>();
        this.cdl = CDL;
        this.pListener = pListener;
    }

    public void download() {
        for(int i=0;i<DEFAULT_THREAD_POOL_SIZE;i++) {
            threadPool.execute(DownloadRunnable);
        }
    }

    Runnable DownloadRunnable = new Runnable() {
        @Override
        public void run() {

        Peer peer = null;

        int next = -1;

        ReputationSet reputationSet = new ReputationSet();

        while (true) {
            synchronized (stateLock) {
                if (chunkQueue.isEmpty()) {
                    System.out.println("Queue empty returning");
                    synchronized(cdl) {  //TODO
                    	cdl.countDown();
                    }
                    return;
                }
            }
            try {
                next = -1;
                while (next == -1) {
                    Thread.sleep(200);
                    synchronized (stateLock) {
                        if (chunkQueue.isEmpty()) {
                            System.out.println("Queue empty returning");
                            synchronized(cdl) {
                            	cdl.countDown();
                            }
                            return;
                        } else {
                            next = chunkQueue.take();
                            activeChunks.add(next);
                        }
                    }
                }

                synchronized (stateLock) {
                    while (ips.size() == 0) {
                        stateLock.wait();
                    }
                }

                if (reputationSet.hasPeerWithReputation()) {
                    peer = reputationSet.getRandomHighestRepPeer();
                } else {
                    peer = reputationSet.populateRepSetAndGetRandomIp(ips);
                }

                Socket s = null;

                try {
                    System.out.println("Connection Attempt " + " to : " + peer);
                    s = new Socket(peer.getIp(), peer.getPort());
                    if(!pListener.exists(peer)){
                    	pListener.addPeer(peer);
                    }
                } catch (IOException ex) {
                    System.out.println("### Connection Failed on : " + peer);
                    reputationSet.downgrade(peer);
                }
                Thread.sleep(1000);

                if (s == null) {
                    synchronized (chunkQueue) {
                        chunkQueue.add(next);
                        activeChunks.remove(next);
                    }
                    continue;
                }

                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
                DataInputStream dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));

                dos.writeInt(uploadPort);
                dos.flush();

                dos.writeInt(next);
                dos.flush();

                System.out.println("Requesting " + next + " ...");

                if (dis.readBoolean() == false) {
                    System.out.println("Chunk was not found");

                    synchronized (stateLock) {
                        chunkQueue.add(next);
                        activeChunks.remove(next);
                    }

                    reputationSet.changeGrade(peer, ReputationSet.Reputation.MEDIUM);

                    dos.close();
                    dis.close();
                    s.close();
                    continue;
                } else {
                    System.out.println("Found the bytes, receiving");
                    byte[] b = new byte[p2pFileMetadata.getChunkSize()];
                    byte[] buf = new byte[1];

                    int bytesRead = 0;

                    int index = 0;
                    while (dis.read(buf) != -1) {
                        b[index] = buf[0];
                        index++;
                        bytesRead++;
                    }

                    System.out.println("bytes Read = " + bytesRead);
                    System.out.println(b.length + " Bytes Received");
                    System.out.println("this is Chunk " + next);
                    System.out.println(b.length + " Bytes Received");

                    synchronized (stateLock) {
                        chunks.put(next, b);
                        activeChunks.remove(next);
                    }

                    reputationSet.upgrade(peer);

                    dos.close();
                    dis.close();
                    s.close();
                }

            } catch (InterruptedException ie) {
                System.out.println("### " + Thread.currentThread().getName() + " shutdown received");
            } catch (UnknownHostException ex) {
                synchronized (stateLock) {
                    chunkQueue.add(next);
                    activeChunks.remove(next);
                }
                System.out.println("### Connection refused on : " + peer);
                reputationSet.downgrade(peer);
            } catch (IOException ex) {
                synchronized (stateLock) {
                    chunkQueue.add(next);
                    activeChunks.remove(next);
                }
                System.out.println("### Connection Refused on : " + peer);
                reputationSet.downgrade(peer);
            } catch (NullPointerException npe) {
            	// Skip
            }

        }

    }
    };

}