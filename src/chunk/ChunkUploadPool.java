package com.agiac.filechunk.chunk;

import com.agiac.filechunk.protocol.Query;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * This class is spawned by the Client in order to appropriately upload
 * the chunks of the requested file. 
 * 
 * @author Adam Giacobbe
 * 
 */
public class ChunkUploadPool {
    private static final int DEFAULT_THREAD_POOL_SIZE = 4;

    private final Executor threadPool = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);

    final LinkedBlockingQueue<Query> cxn;
    final HashMap<Integer, byte[]> chunks;
    final int chunkSize;

    public ChunkUploadPool(LinkedBlockingQueue<Query> cxn, HashMap<Integer, byte[]> chunks, int chunkSize) {
        this.chunks = chunks;
        this.cxn = cxn;
        this.chunkSize = chunkSize;
        if (cxn == null) {
            System.out.println("### Null fail");
            System.exit(-1);
        }
    }

    public void seedUpload() {
        for(int i=0;i<DEFAULT_THREAD_POOL_SIZE;i++) {
            threadPool.execute(uploadRunnable);
        }
    }


    Runnable uploadRunnable = new Runnable() {
        @Override
        public void run() {
            byte[] b;
            while (!Thread.currentThread().isInterrupted()) {
                Query next = new Query(null, -1);
                try {
                    System.out.println("Waiting for connection");
                    next = cxn.take();

                    System.out.println("Handling connection from " + next.socket.getInetAddress().toString());

                    int findChunk = next.chunkDesired;

                    b = chunks.get(findChunk);

                    DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(next.socket.getOutputStream()));

                    if (b == null) {
                        //Doesn't have chunk....handle accordingly
                        dos.writeBoolean(false);
                        dos.flush();
                    } else {
                        try {
                            dos.writeBoolean(true);
                            dos.flush();
                            dos.write(b);
                            dos.flush();
                        } catch (IOException ioe) {
                        }
                    }

                } catch (IOException ioe) {
                } catch (InterruptedException ie) {
                    System.out.println("### " + Thread.currentThread().getName() + " shutdown receieved");
                } finally {
                    try {
                        if (next.socket != null) {
                            if (!next.socket.isClosed()) {
                                next.socket.close();
                            }
                        }
                    } catch (IOException ioe) {
                    }
                }
            }
        }
    };
}
