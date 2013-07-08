/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.agiac.filechunk.protocol;

import com.agiac.filechunk.network.Client;
import com.agiac.filechunk.network.Tracker;
import com.agiac.filechunk.UtilityFunctions;
import com.agiac.filechunk.peer.PeerListController;
import com.agiac.filechunk.peer.PeerListener;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

/**
 * This class creates a Tracker and then creates a client with the complete file
 * chunked that proceeds to connect to the tracker and host the file.  The two
 * are linked together so both must stay active.
 * 
 * @author Adam Giacobbe
 */
public class SeedAndTrack implements Runnable {

	private PeerListController pController;
	
	public SeedAndTrack(){
		//default empty constructor
	}
	
	public SeedAndTrack(PeerListController pController){
		this.pController = pController;
	}
        
    /**
     */
    public void run() {
        HashMap<Integer, byte[]> file;

        JFileChooser jf = new JFileChooser();

        jf.showOpenDialog(jf);
        File openFile = jf.getSelectedFile();
        if (openFile == null) {
            return;
        }

        int chunkSize = 256000;

        long fsize = openFile.length();

        file = UtilityFunctions.chunkFile(openFile, chunkSize, (int) fsize);
        long temp = (openFile.length() / chunkSize) + 1;
        int numChunks = (int) temp;

        System.out.println("numChunks = " + numChunks);

        File p2pFile = null;

        Tracker tracker = new Tracker(0);

        try {
            System.out.println(InetAddress.getLocalHost().getHostAddress());
            p2pFile = UtilityFunctions.createP2PFile(openFile.getName(), openFile.getParent(), numChunks, chunkSize, openFile.length(), InetAddress.getLocalHost().getHostAddress(), tracker.getPort());
        } catch (UnknownHostException ex) {
            Logger.getLogger(SeedAndTrack.class.getName()).log(Level.SEVERE, null, ex);
        }

        String user = null;
        try {
            user = "seed - "+ InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        Thread t = new Thread(tracker);
        t.setName("Tracker");
        t.start();

        Client c = new Client(user, 0, file, p2pFile, new PeerListener(pController),true);

    }

}
