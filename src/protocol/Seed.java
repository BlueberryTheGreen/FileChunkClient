package com.agiac.filechunk.protocol;

import com.agiac.filechunk.network.Client;
import com.agiac.filechunk.UtilityFunctions;
import com.agiac.filechunk.peer.PeerListController;
import com.agiac.filechunk.peer.PeerListener;

import java.io.File;
import java.util.HashMap;
import java.util.Random;
import javax.swing.JFileChooser;

/**
 * This class creates a Tracker and then creates a client with the complete file
 * chunked that proceeds to connect to the tracker and host the file.  The two
 * are linked together so both must stay active.
 * 
 * @author Adam Giacobbe
 */
public class Seed implements Runnable {
	
	private PeerListController pController;
	
	public Seed(){
		
	}
	
	public Seed(PeerListController pController){
		this.pController = pController;
	}

    public void run() {
        Random r = new Random();
        r.setSeed(System.nanoTime());

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

        jf = new JFileChooser();
        jf.showOpenDialog(jf);
        File p2pFile = jf.getSelectedFile();
        if (p2pFile == null) {
            return;
        }

        String user = "username-" + r.nextInt();

        Client c = new Client(user, 0, file, p2pFile, new PeerListener(pController),true);
    }
    
}
