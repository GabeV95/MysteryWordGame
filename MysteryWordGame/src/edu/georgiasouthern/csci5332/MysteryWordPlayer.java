package edu.georgiasouthern.csci5332;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
public class MysteryWordPlayer extends JFrame {
	private int width, height, playerId, otherPlayer, maxTurns,
	turnsMade, myPoints, enemyPoints;
	private Container contentPane;
	private JTextArea message;
	private JButton b1, b2, b3, b4;
	private ClientSideConnection csc;	
	private int[] values;
	private String mysteryWord;
	private String[] words = new String[4];
	private ArrayList<JButton>buttons = new ArrayList<>();
	private JButton[] wordsArr = new JButton[words.length];
	private boolean buttonsEnabled;
	private JTextArea window;	
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
		this.setTitle("Player #"+playerId);
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
		for(int i = 0;i<wordsArr.length;i++) {
			contentPane.add(wordsArr[i]);
		}
		this.setVisible(true);
		
		if(playerId == 1) {
			message.setText("You are player #1, go first");
			buttonsEnabled = true;
			otherPlayer = 2;
		}
		else {
			message.setText("You are player #2, wait for your turn");
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
	public void connectToServer() {
		csc = new ClientSideConnection();
	}
	public void setUpButtons() {
		ActionListener al = new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
			JButton b = (JButton)ae.getSource();
			//int bNum = Integer.parseInt(b.getText());
			String bWord = b.getText();
			message.setText("You clicked button number: " + bWord + ", now wait for player #" + otherPlayer);
			turnsMade++;
			System.out.printf("Turns made: %d%n", turnsMade);
			buttonsEnabled = false;
			toggleButtons();
			//myPoints+=values[bNum - 1];
			if(bWord.equals(mysteryWord)) {
				myPoints++;
			}
			System.out.printf("My points: %d%n", myPoints);
			csc.sendBtnWord(bWord);
			
			if(playerId==2 && turnsMade==maxTurns) {
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
	for(int i = 0;i<wordsArr.length;i++) {
		wordsArr[i].addActionListener(al);
	}
//	b1.addActionListener(al);
//	b2.addActionListener(al);
//	b3.addActionListener(al);
//	b4.addActionListener(al);
		}
	public void toggleButtons() {
		for(int i = 0;i<wordsArr.length;i++) {
			wordsArr[i].setEnabled(buttonsEnabled);
		}
	}
//	public void startReceivingBtnNums() {
//		Thread t = new Thread(new Runnable() {
//			public void run() {
//				while(true) {
//					csc.receiveBtnNum();
//				}
//			}
//		});
//		t.start();
//	}
	public void updateTurn() {
		String word = csc.receiveBtnWord();
		message.setText("Your enemy clicked button #" + word + ". Your turn");;
		//enemyPoints += values[num - 1];
		if(word.equals(mysteryWord)) {
			enemyPoints++;
		}
		System.out.printf("Your enemy has %d points%n", enemyPoints);
		
		if(playerId == 1 && turnsMade == maxTurns) {
			checkWinner();
		}
		else {
			buttonsEnabled = true;
		}
		toggleButtons();
	}
	private void checkWinner() {
		buttonsEnabled = false;
		if(myPoints > enemyPoints) {
			message.setText("You won!\nYou: " + myPoints + "\nEnemy: " + enemyPoints);
		}
		else if(myPoints<enemyPoints) {
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
				
				for(int i = 0; i < words.length; i++) {
					words[i] = dataIn.readUTF();
				}
				System.out.printf("maxTurns: %s%n", maxTurns);
				System.out.printf("Mystery word: %s%n", mysteryWord);
				for(int i = 0; i < words.length; i++) {
					System.out.printf("Word #%s is %s%n", i, words[i]);
				}
				for(int i = 0;i<wordsArr.length;i++) {
					wordsArr[i] = new JButton(words[i]);
					System.out.println(wordsArr[i].getText());
				}
				
//				values[0] = dataIn.readInt();
//				values[1] = dataIn.readInt();
//				values[2] = dataIn.readInt();
//				values[3] = dataIn.readInt();
				
				
//				for(int i : values) {
//					values[i] = dataIn.readInt();
//				}
//				System.out.printf("maxTurns: %d%n", maxTurns);
				
//				for(int i : values) {
//					System.out.printf("Value #%d is %d",i, values[i]);
//				}
				
			}catch(IOException ex) {
				System.out.println("IOException from CSC constructor");
			}
		}
		
		public void sendBtnNum(int num) {
			try {
				dataOut.writeInt(num);
				dataOut.flush();
			}catch(IOException ex) {
				System.out.println("IOException from send button");
			}
		}
		public void sendBtnWord(String word) {
			try {
				dataOut.writeUTF(word);
				dataOut.flush();
			}catch(IOException ex) {
				System.out.println("IOException from send button");
			}
		}
		public int receiveBtnNum() {
			int num = -1;
			try {
				num = dataIn.readInt();
				System.out.printf("Player #%d clicked button #%d%n", otherPlayer, num);
			}catch(IOException ex) {
				System.out.println("IOException at receiveBtnNum CSC");
			}
			return num;
		}
		public String receiveBtnWord() {
			//int num = -1;
			String word = "";
			try {
				word = dataIn.readUTF();
				System.out.printf("Player #%d clicked word %s%n", otherPlayer, word);
			}catch(IOException ex) {
				System.out.println("IOException at receiveBtnNum CSC");
			}
			return word;
		}

		public void closeConnection() {
			try {
				socket.close();
				System.out.println("Connection closed");
			}catch(IOException ex) {
				System.out.println("IOException in closing the connection");
			}
		}
	}
	public static void main(String[] args) {
		MysteryWordPlayer p = new MysteryWordPlayer(500, 100);
		p.connectToServer();
		p.setUpGUI();
		p.setUpButtons();
		
	}
}
