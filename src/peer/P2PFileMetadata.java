package com.agiac.filechunk.peer;

/**
 * Created with IntelliJ IDEA.
 * User: Adam
 * Date: 7/7/13
 * Time: 9:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class P2PFileMetadata {
    private String name, trackerIP;
    private int numChunks, chunkSize, trackerPort;
    private long fileSize;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTrackerIP() {
        return trackerIP;
    }

    public void setTrackerIP(String trackerIP) {
        this.trackerIP = trackerIP;
    }

    public int getNumChunks() {
        return numChunks;
    }

    public void setNumChunks(int numChunks) {
        this.numChunks = numChunks;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getTrackerPort() {
        return trackerPort;
    }

    public void setTrackerPort(int trackerPort) {
        this.trackerPort = trackerPort;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
