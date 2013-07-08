package com.agiac.filechunk.peer;

import com.agiac.filechunk.ui.GUI;

/**
 *
 */
public class PeerListController{
	private GUI g;

	public PeerListController(GUI g){
		this.g = g;
	}
	
	public void addPeerToList(Peer peer){
		g.addPeer(peer);
	}
	
	public boolean exists(Peer peer){
		return g.exists(peer);
	}
}


