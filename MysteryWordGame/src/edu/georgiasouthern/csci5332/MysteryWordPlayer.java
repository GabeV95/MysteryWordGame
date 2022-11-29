package edu.georgiasouthern.csci5332;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;


public class MysteryWordPlayer extends JFrame {	
	
	//init primitives 
	private int width, height, playerId, otherPlayer, maxTurns,
	turnsMade, myPoints, enemyPoints;
	private String mysteryWord;
	private boolean buttonsEnabled;
	
	//init arrs
	private int[] values;
	private String[] words = new String[4];
	
	//init client conn obj
	private ClientSideConnection csc;
	
	//init jfx vars
	private JButton[] btnsArr = new JButton[words.length];
	private Container contentPane;
	private JButton b1, b2, b3, b4;
	private ArrayList<JButton> buttons = new ArrayList<>();
	private JTextArea message;
	private JTextArea window;	
	
	
	//client method
	public MysteryWordPlayer(int w, int h) {
		
		width = w;
		height = h;
		contentPane = this.getContentPane();
		message = new JTextArea();
		
		b1 = new JButton("1");
		b2 = new JButton("2");
		b3 = new JButton("3");
		b4 = new JButton("4");
		
		buttons.add(b1);
		buttons.add(b2);
		buttons.add(b3);
		buttons.add(b4);
		
		values = new int[4];
		turnsMade = 0;
		myPoints = 0;
		enemyPoints = 0;
		
	}
	
	
	public void setUpGUI() {
		
		this.setSize(width, height);
		this.setTitle("Player #" + playerId);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		
		contentPane.setLayout(new GridLayout(1,5));
		contentPane.add(message);
		
		message.setText("Creating a simple turn-based game in java");
		message.setWrapStyleWord(true);
		message.setLineWrap(true);
		message.setEditable(false);
		
//		contentPane.add(b1);
//		contentPane.add(b2);
//		contentPane.add(b3);
//		contentPane.add(b4);
		
		//adding buttons to content pane
		for(int i = 0; i < btnsArr.length; i++) {
			
			contentPane.add(btnsArr[i]);
		}
		
		this.setVisible(true);
		
		//checks correct player and enables/disables buttons based on turn 
		//then creates a new thread if not user 1 to run the update turn method
		if(playerId == 1) {
			
			message.setText("You are player #1, go first");
			buttonsEnabled = true;
			otherPlayer = 2;
			
		}
		else {
			
			message.setText("You are player #2, wait for your turn.");
			otherPlayer = 1;
			buttonsEnabled = false;
			
			Thread t = new Thread(new Runnable() {
				
				public void run() {
					
					updateTurn();
				}
			});
			
			t.start();
		}
		
		toggleButtons();
	}
	
	//server conn
	public void connectToServer() {
		
		csc = new ClientSideConnection();
		
	}
	
	//adds listener and activates based on press.
	public void setUpButtons() {
		
		ActionListener al = new ActionListener() {
			
		public void actionPerformed(ActionEvent ae) {
			
			JButton b = (JButton)ae.getSource();
			
			//gets the word of the pressed button and adds/finishes the turn
			String bWord = b.getText();
			message.setText("You clicked on the word: " + bWord + ", now wait for player #" + otherPlayer);
			turnsMade++;
			
			System.out.printf("Turns made: %d%n", turnsMade);
			buttonsEnabled = false;
			toggleButtons();
			
			//gives player points if correct button is pushed
			if(bWord.equals(mysteryWord)) {
				
				myPoints++;
				
			}
			
			System.out.printf("My points: %d%n", myPoints);
			
			//sends pressed button to server
			csc.sendBtnWord(bWord);
			
			//win condition
			if (playerId == 2 && turnsMade == maxTurns) {
				
				checkWinner();
				
			}
			else {
				
				Thread t = new Thread(new Runnable() {
					
					public void run() {
						
						updateTurn();
					}
				});
				
				t.start();
			}
		}
	};
	
	//adding action listener to array of buttons
	for (int i = 0; i < btnsArr.length; i++) {
		
		btnsArr[i].addActionListener(al);
		
	}
	
}
	
	public void toggleButtons() {
		
		for (int i = 0; i < btnsArr.length; i++) {
			
			btnsArr[i].setEnabled(buttonsEnabled);
		}
	}
	
	//recieves selected word from server and updates turn based on results
	public void updateTurn() {
		
		String word = csc.receiveBtnWord();
		message.setText("Your enemy clicked button " + word + ". Your turn");
		
		//gets word of button pressed by enemy player and adds points for match
		if (word.equals(mysteryWord)) {
			
			enemyPoints++;
			
		}
		
		System.out.printf("Your enemy has %d points%n", enemyPoints);
		
		if (playerId == 1 && turnsMade == maxTurns) {
			
			checkWinner();
			
		}
		else {
			
			buttonsEnabled = true;
			
		}
		
		toggleButtons();
	}
	
	
	private void checkWinner() {
		
		buttonsEnabled = false;
		
		if (myPoints > enemyPoints) {
			
			message.setText("You won!\nYou: " + myPoints + "\nEnemy: " + enemyPoints);
		}
		else if (myPoints < enemyPoints) {
			
			message.setText("You lost!\nYou: " + myPoints + "\nEnemy: " + enemyPoints);
		}
		else {
			
			message.setText("It's a tie. You both got: " + myPoints + " points");
		}
		
		csc.closeConnection();
	}
	
	//Client Connection
	private class ClientSideConnection{
		
		private Socket socket;
		private DataInputStream dataIn;
		private DataOutputStream dataOut;
		
		public ClientSideConnection() {
			
			
			System.out.println("Client");
			
			try {
				
				socket = new Socket("localhost", 51736);
				dataIn = new DataInputStream(socket.getInputStream());
				dataOut = new DataOutputStream(socket.getOutputStream());
				
				playerId = dataIn.readInt();
				System.out.printf("Connected to server as Player #%d%n", playerId);
				maxTurns = dataIn.readInt()/2;
				
				mysteryWord = dataIn.readUTF();
				
				words[0] = dataIn.readUTF();
				words[1] = dataIn.readUTF();
				words[2] = dataIn.readUTF();
				words[3] = dataIn.readUTF();
				
				System.out.printf("maxTurns: %s%n", maxTurns);
				
				System.out.printf("Mystery word: %s%n", mysteryWord);
				
				for (int i = 0; i < words.length; i++) { 
					
					System.out.printf("Word #%s is %s%n", i, words[i]);
					
				}
				
				for (int i = 0; i < btnsArr.length; i++) {
					
					btnsArr[i] = new JButton(words[i]);
					System.out.println(btnsArr[i].getText());
					
				}
				

			} catch (IOException ex) {
				
				System.out.println("IOException from CSC constructor");
				
			}
		}
		
		
		public void sendBtnWord(String word) {
			
			try {
				
				dataOut.writeUTF(word);
				dataOut.flush();
				
			} catch (IOException ex) {
				
				System.out.println("IOException from send button");
				
			}
		}
		
		
		public String receiveBtnWord() {
			
			String word = "";
			
			try {
				
				word = dataIn.readUTF();
				System.out.printf("Player #%d clicked word %s%n", otherPlayer, word);
				
			} catch (IOException ex) {
				
				System.out.println("IOException at receiveBtnNum CSC");
				
			}
			
			return word;
		}

		public void closeConnection() {
			
			try {
				
				socket.close();
				System.out.println("Connection closed");
				
			}
			catch (IOException ex) {
				
				System.out.println("IOException in closing the connection");
				
			}
		}
	}
	
	//Main Method
	public static void main(String[] args) {
		
		MysteryWordPlayer p = new MysteryWordPlayer(500, 100);
		p.connectToServer();
		p.setUpGUI();
		p.setUpButtons();
		
	}
}