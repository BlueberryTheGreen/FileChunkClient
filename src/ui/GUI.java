package com.agiac.filechunk.ui;

import com.agiac.filechunk.*;
import com.agiac.filechunk.peer.Peer;
import com.agiac.filechunk.peer.PeerListController;
import com.agiac.filechunk.protocol.Seed;
import com.agiac.filechunk.protocol.SeedAndTrack;
import com.agiac.filechunk.protocol.Track;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This is the main GUI window of our program.  It owns the peerMap and updates the list
 * 
 * @author Adam Giacobbe
 * 
 */
public class GUI extends JFrame{

    private Set<Peer> peerSet = new HashSet<Peer>();
    private DefaultListModel entries;
    private JList displayList;
    private JPanel topPanel, bottomPanel, middlePanel, midEastPanel;
    private JScrollPane scrollPane;
    private JMenuBar menu;
    private JMenu file;
    private JMenuItem download, seed, host, track, exit;
    private JLabel instructions, buddyList, welcome, names, course;
    private JPanel midWestPanel, midCenterPanel, botWestPanel, botCenterPanel;
    private JButton message;
    private JTextArea howto;
    
    public static void main(String[] args) {
        GUI g = new GUI();
        g.setVisible(true);
    }
    public GUI(){
    	
       	final PeerListController pController = new PeerListController(this);
    	setTitle("File Chunk Client");
    	setSize(680,400);
        setResizable(false);

        //ImagePanel icon = new ImagePanel("icon.jpg");
        
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
    	topPanel = new JPanel();
    	bottomPanel = new JPanel();
    	middlePanel = new JPanel();
    	
    	menu = new JMenuBar();
    	file = new JMenu("File");
    	download = new JMenuItem("Download");
    	download.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		StartClient m = new StartClient(pController);
        		Thread t = new Thread(m);
        		t.start();
        	}
        });
    	seed = new JMenuItem("Seed");
    	seed.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		Seed s = new Seed(pController);
        		Thread t = new Thread(s);
        		t.start();
        		
        	}
        });
        host = new JMenuItem("Host");
    	host.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		SeedAndTrack st = new SeedAndTrack(pController);
        		Thread t = new Thread(st);
        		t.start();
        	}
        });

        track = new JMenuItem("Create Tracker");
    	track.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		Track tr = new Track(pController);
        		Thread t = new Thread(tr);
        		t.start();
        	}
        });
    	
    	exit = new JMenuItem("Exit");
        exit.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		System.exit(-1);
        	}
        });
        
        message = new JButton("Message");
        message.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		if(displayList.getSelectedIndex() >= 0){
        			Peer peer = (Peer) entries.elementAt(displayList.getSelectedIndex());
        			Message m = new Message(peer.getIp(), peer.getPort()+1);
        			Thread t = new Thread(m);
        			t.start();
        		}
        	}
        });
       
        
        file.add(download);
        file.add(seed);
        file.add(host);
        file.add(track);
        file.add(exit);
        
        menu.add(file);
        menu.setVisible(true);
        file.setVisible(true);
        menu.setSize(312, 25);
        topPanel.setLayout(new GridLayout(2, 1));
        topPanel.add(menu);
        welcome = new JLabel("Welcome!");
        welcome.setForeground(Color.WHITE);
        welcome.setHorizontalAlignment(JLabel.CENTER);
        topPanel.add(welcome);
        
        instructions = new JLabel("Instructions");
    	instructions.setForeground(Color.WHITE);
        
    	entries = new DefaultListModel();

    	displayList = new JList(entries);
        displayList.setBackground(Color.BLACK);
        displayList.setForeground(Color.WHITE);
    	displayList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	
    	Iterator i = peerSet.iterator();
    	while(i.hasNext()){
    		entries.addElement(i.next().toString());
    	}
    	buddyList = new JLabel("Known Peers");
        buddyList.setForeground(Color.WHITE);
    	scrollPane = new JScrollPane();
    	scrollPane.getViewport().add(displayList);
    	midEastPanel = new JPanel();
    	midEastPanel.setLayout(new BorderLayout());
    	midEastPanel.add(buddyList, BorderLayout.NORTH);
    	midEastPanel.add(scrollPane);
    	
        midWestPanel = new JPanel();
    	howto = new JTextArea("Select your task from the File menu.\n\n" +
    			"To Host a file, select Host and the file you would like to host. A .p2p file will be generated for those who would like" +
    			" to download it from you in the future. \n\nTo Seed a file, select the file you would like to seed. If you " +
    			"have an existing .p2p file, select Download and the file to download it from the host and other peers. \n\n" +
    			" Known peers with whom you are interacting with are shown in the right column." +
    			" Feel free to message them regarding your file.");
        howto.setForeground(Color.WHITE);
        howto.setBackground(Color.BLACK);
    	howto.setEditable(false);
        howto.setLineWrap(true);
    	howto.setSize(220, 250);
    	//howto.setBackground(midWestPanel.getBackground());
    	midWestPanel.add(howto);
    	
        midCenterPanel = new JPanel();
        JPanel blankSpace = new JPanel();
        JPanel blankSpace2 = new JPanel();
        JPanel blankSpace3 = new JPanel();
        blankSpace.setBackground(Color.BLACK);
        blankSpace2.setBackground(Color.BLACK);
        blankSpace3.setBackground(Color.BLACK);
        midCenterPanel.add(blankSpace);
        midCenterPanel.add(blankSpace2);
        midCenterPanel.add(blankSpace3);
        //midCenterPanel.add(icon);
    	middlePanel.setLayout(new GridLayout(1,3));
    	middlePanel.add(midWestPanel);
    	middlePanel.add(midCenterPanel);
    	middlePanel.add(midEastPanel);
       
        names = new JLabel("AzureCloud");
        course = new JLabel("---");
        names.setForeground(Color.WHITE);
        course.setForeground(Color.WHITE);
        botWestPanel = new JPanel();
        botWestPanel.add(course);
        botCenterPanel = new JPanel();
        botCenterPanel.add(names);
        bottomPanel.setLayout(new GridLayout(1, 3));
        bottomPanel.add(botWestPanel);
        bottomPanel.add(botCenterPanel);
        bottomPanel.add(message);

        topPanel.setBackground(Color.BLACK);
        bottomPanel.setBackground(Color.BLACK);
        middlePanel.setBackground(Color.BLACK);
        botWestPanel.setBackground(Color.BLACK);
        botCenterPanel.setBackground(Color.BLACK);
        midCenterPanel.setBackground(Color.BLACK);
        midWestPanel.setBackground(Color.BLACK);
        midEastPanel.setBackground(Color.BLACK);
        getContentPane().setLayout(new BorderLayout());
    	getContentPane().add(topPanel, BorderLayout.NORTH);
    	getContentPane().add(bottomPanel, BorderLayout.SOUTH);
    	getContentPane().add(middlePanel);
    }
    
    public void addPeer(Peer peer){
        peerSet.add(peer);
    	entries.clear();
    	Iterator i = peerSet.iterator();
    	while(i.hasNext()){
    		entries.addElement(i.next());
    	}
    	
    	displayList = new JList(entries);
    	scrollPane.getViewport().add(displayList);
    	scrollPane.revalidate();
    	scrollPane.repaint();
    	
    }
    public boolean exists(Peer peer){
    	if(peerSet.contains(peer)) {
            return true;
        }
    	return false;
    }
}


