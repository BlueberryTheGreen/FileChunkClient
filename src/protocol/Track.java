package com.agiac.filechunk.protocol;

import com.agiac.filechunk.network.Tracker;
import com.agiac.filechunk.UtilityFunctions;
import com.agiac.filechunk.peer.PeerListController;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

/**
 * This class is called to start up a new tracker for a file to be hosted
 * 
 * @author Adam Giacobbe
 * 
 */
public class Track implements Runnable {

	private PeerListController pController;

	public Track(){

	}

	public Track(PeerListController pController){
		this.pController = pController;
	}
	
    /**
     *
     */
    public void run() {
        JFileChooser jf = new JFileChooser();

        jf.showOpenDialog(jf);
        File openFile = jf.getSelectedFile();
        if (openFile == null) {
            return;
        }

        int chunkSize = 256000;
        long fsize = openFile.length();
        //HashMap<Integer, byte[]> fileChunkMap = UtilityFunctions.chunkFile(openFile, chunkSize, (int) fsize);

        long temp = (openFile.length() / chunkSize) + 1;
        int numChunks = (int) temp;

        System.out.println("numChunks = " + numChunks);

        Tracker tracker = new Tracker(0);

        try {
            System.out.println(InetAddress.getLocalHost().getHostAddress());
            File p2pFile = UtilityFunctions.createP2PFile(openFile.getName(), openFile.getParent(), numChunks, chunkSize, openFile.length(), InetAddress.getLocalHost().getHostAddress(), tracker.getPort());
        } catch (UnknownHostException ex) {
            Logger.getLogger(SeedAndTrack.class.getName()).log(Level.SEVERE, null, ex);
        }

        Thread t = new Thread(tracker);
        t.setName("Tracker");
        t.start();
    }

}

