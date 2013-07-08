package com.agiac.filechunk.peer;

/**
 *
 */
public class PeerListener {
	
	private PeerListController pController;
	
	public PeerListener(){
		
	}
	
	public PeerListener(PeerListController pController){
		this.pController = pController;
	}
	
	public void addPeer(Peer peer){
		pController.addPeerToList(peer);
	}
	
	public boolean exists(Peer peer){
		return pController.exists(peer);
	}
	
}

