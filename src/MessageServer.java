package com.agiac.filechunk;

import com.agiac.filechunk.ui.Message;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class acts as the Message Server for the Chat
 *
 */
public class MessageServer extends Thread{
	ServerSocket ms;
	
	public MessageServer(){
		
	}
        
	
	public MessageServer(int portNum){
		try{
			ms = new ServerSocket(portNum);
			System.out.println("MessageServer started on port "+portNum+" IPAddress: "+ InetAddress.getLocalHost().getHostAddress());
		}catch(IOException ex) {
            System.out.println("Cannot Start MessageServer on port "+portNum+". Port is not available");
            System.exit(-1);
            Logger.getLogger(MessageServer.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
	
	public void run(){
		while(true){
			try{
				Socket connection = ms.accept();
				Message m = new Message(connection);
				System.out.println("Starting conversation with: "+connection.getInetAddress());
				Thread t = new Thread(m);
				t.start();
			}catch(IOException ex){
			}
		}
	}

}
