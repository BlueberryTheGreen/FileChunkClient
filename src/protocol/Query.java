package com.agiac.filechunk.protocol;

import java.net.Socket;

/**
 * This class is used to place into a queue in order to store information
 * about a clients connection and what chunk they are looking for.
 * 
 * @author Adam Giacobbe
 */
public class Query {

    public final Socket socket;
    public final int chunkDesired;

    public Query(Socket SOCKET, int CHUNK) {
        socket = SOCKET;
        chunkDesired = CHUNK;
    }
}

