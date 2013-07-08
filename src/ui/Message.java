package com.agiac.filechunk.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * This is the main class for sending messages to other clients
 *
 */
public class Message extends JFrame implements Runnable{
	
	Socket connection;
	JPanel topPanel, bottomPanel;
	JTextArea textArea;
	JTextField textfield;
	JScrollPane scrollPane;
	JButton send;
	DataOutputStream dos;
	DataInputStream dis;
	String alltext = "";
	
	public Message(){ }
	
	public Message(String ip, int portNum){	//handles messages that start as outgoing
		setTitle("Conversation with "+ip);
		try{
			connection = new Socket(ip, portNum);
			System.out.println("Starting conversation with: "+ip);
			display();
			
		}catch(IOException e){
			System.out.println("Error in connecting to peer's message server. IP:"+ip+" port "+portNum);
		}
	}
        
	
	public Message(Socket connection){	//handles incoming message connections (starts from MessageServer)
		setTitle("Conversation with "+connection.getInetAddress());
		this.connection = connection;
		display();
		
	}

	public void run() {
		try{
			dos = new DataOutputStream(new BufferedOutputStream(connection.getOutputStream()));
			dis = new DataInputStream(new BufferedInputStream(connection.getInputStream()));
			while(this.isShowing()){
				String line = dis.readUTF();
				receiveText(line);
			}
		}catch(IOException e){
			
		}
		
	}
	
	public void display(){
		setSize(370,190);
		topPanel = new JPanel();
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridLayout(1,2));
		send = new JButton("Send");
		textArea = new JTextArea(8,30);
		textArea.setText(alltext);
		textArea.setEditable(false);
		scrollPane = new JScrollPane(textArea);
		textfield = new JTextField(30);
		
		send.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				sendText();
			}
		});
		
		topPanel.add(scrollPane);
		bottomPanel.add(textfield);
		bottomPanel.add(send);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(topPanel);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		setVisible(true);
	}
	
	public void sendText(){
		try{
			String line = textfield.getText();
			if(line.equals("")){}
			else{
				dos.writeUTF(line);
				dos.flush();
				textfield.setText("");
				alltext = alltext+"YOU: "+line+"\n";
				textArea.setText(alltext);
				textArea.revalidate();
				textArea.repaint();
			}
		}catch(IOException ioe){ }
	}
	
	public void receiveText(String line){
		alltext = alltext+"THEM: "+line+"\n";
		textArea.setText(alltext);
		textArea.revalidate();
		textArea.repaint();
	}

}
