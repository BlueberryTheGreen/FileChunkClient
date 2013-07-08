package com.agiac.filechunk.peer;

/**
 * Created with IntelliJ IDEA.
 * User: Adam
 * Date: 7/7/13
 * Time: 5:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class Peer {
    private final String ip;
    private final int port;
    private final int hashcode;

    public Peer(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.hashcode = this.ip.hashCode()*37 + this.port;
    }

    public int getPort() {
        return port;
    }

    public String getIp() {
        return ip;
    }

    @Override
    public int hashCode() {
        return hashcode;
    }

    @Override
    public boolean equals(Object other) {
        if(other == null) {
            return false;
        } else if(!(other instanceof Peer)) {
            return false;
        } else if(!((Peer) other).getIp().equals(ip)) {
            return false;
        } else if(((Peer) other).getPort() != port) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return ip+":"+port;
    }
}
