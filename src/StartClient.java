package com.agiac.filechunk;

import com.agiac.filechunk.network.Client;
import com.agiac.filechunk.peer.PeerListController;
import com.agiac.filechunk.peer.PeerListener;

import java.io.File;
import java.util.HashMap;
import java.util.Random;
import javax.swing.JFileChooser;

/**
 * This class handles the creation of a new client that wants to download a file
 * User will select a p2pfile to start
 * @author Adam Giacobbe
 */
public class StartClient implements Runnable {

	private PeerListController pController;
	
	public StartClient(){
		
	}
	
	public StartClient(PeerListController pController){
		this.pController = pController;
	}

    public void run() {

        Random r = new Random();
        r.setSeed(System.nanoTime());
        int random = r.nextInt(Integer.MAX_VALUE);

        HashMap<Integer, byte[]> file = new HashMap<Integer, byte[]>();

        JFileChooser jf = new JFileChooser();
        jf.showOpenDialog(jf);
        File p2pFile = jf.getSelectedFile();
        
        if (p2pFile == null) {
            return;
        }
        
        String user = "username" + r.nextInt();
        Client c = new Client(user, 50000 + (random % 9999), file, p2pFile, new PeerListener(pController),false);
    }
}

