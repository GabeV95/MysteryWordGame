package edu.georgiasouthern.csci5332;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;


public class MysteryWordServer  {
	
	private int numPlayers, turnsMade, maxTurns, mysteryNum;
	private String mysteryWord, player1BtnWord, player2BtnWord;
	private String[] guessWords = new String[4];
	private ServerSocket ss;
	private ServerSideConnection player1, player2;
	
	
	public MysteryWordServer() {
		
		System.out.println("Game Server");
		
		numPlayers = 0;
		turnsMade = 0;
		maxTurns = 4;

		//generates random mystery number
		mysteryNum = (int)Math.ceil((Math.random() * 4));
		
		try {
			
			URL path = MysteryWordServer.class.getResource("words.txt");
			File f = new File(path.getFile());
			Scanner scanner = new Scanner(f);
			List<String> words = new ArrayList<>();
			
		      
		      while (scanner.hasNext()) {
		    	  
		        words.add(scanner.nextLine());
		        
		      }

		      scanner.close();
		      
		      Random rand = new Random();
		      mysteryWord = words.get(rand.nextInt(words.size()));
		      
		      for (int i = 0; i < guessWords.length; i++) {
		    	  
		    	  if (mysteryNum == i) {
		    		  
		    		  guessWords[i] = mysteryWord;
		    		  
		    	  }
		    	  else {
		    		  
		    		  guessWords[i] = words.get(rand.nextInt(words.size()));
		    		  
		    	  }
		    	  
		    	  System.out.println(guessWords[i]);
		    	  
		      }
		      
		      System.out.println("Mystery Word: " + mysteryWord);
		      
			  ss = new ServerSocket(51736);
			
		} catch (IOException io) {
			
			System.out.println("IOException");
			
		}
		
	}
	
	
	public void acceptConnections() {
		
		try {
			
			System.out.println("Waiting for connection");
			
			while (numPlayers < 2) {
				
				Socket s = ss.accept();
				numPlayers++;
				System.out.printf("Player #%d has connected%n", numPlayers);
				
				ServerSideConnection ssc = new ServerSideConnection(s, numPlayers);
				
				if (numPlayers == 1) {
					
					player1 = ssc;
					
				}
				else {
					
					player2 = ssc;
					
				}
				
				Thread t = new Thread(ssc);
				
				t.start();
				
			}
			
			System.out.println("We now have two players");
			
		} catch (IOException ex) {
			
			System.out.println("IOException from acceptConnections");
			
		}
	}
	
	
	private class ServerSideConnection implements Runnable {
		
		private Socket socket;
		private DataInputStream dataIn;
		private DataOutputStream dataOut;
		private int playerId;
		
		public ServerSideConnection(Socket s, int id) {
			
			socket = s;
			playerId = id;
			
			try {
				
				dataIn = new DataInputStream(socket.getInputStream());
				dataOut = new DataOutputStream(socket.getOutputStream());
				
			} catch (IOException ex) {
				
				System.out.println("IOException from SSC constructor");
				
			}
		}
		
		public void run() {
			
			try {
				
				dataOut.writeInt(playerId);
				dataOut.writeInt(maxTurns);

				dataOut.writeUTF(mysteryWord);
				dataOut.writeUTF(guessWords[0]);
				dataOut.writeUTF(guessWords[1]);
				dataOut.writeUTF(guessWords[2]);
				dataOut.writeUTF(guessWords[3]);
				
				dataOut.flush();
				
				while (true) {
					
					if (playerId == 1) {
						
						player1BtnWord = dataIn.readUTF();
						System.out.printf("Player 1 clicked word: %s%n", player1BtnWord);
						player2.sendBtnWord(player1BtnWord);
						
					}
					else {
						
						player2BtnWord = dataIn.readUTF();
						System.out.printf("Player 2 clicked word: %s%n", player2BtnWord);
						player1.sendBtnWord(player2BtnWord);
						
					}
					
					turnsMade++;
					
					if (turnsMade == maxTurns) {
						
						System.out.println("Max turns have been reached");
						break;
						
					}
				}
				
				player1.closeConnection();
				player2.closeConnection();
				
			} catch (IOException ex) {
				
				System.out.println("IOException from run() in SS");
				
			}
		}
		
		
		public void sendBtnWord(String word) {
			
			try {
				
				dataOut.writeUTF(word);
				dataOut.flush();
				
			} catch (IOException ex) {
				
				System.out.println("IOException from sendBtnNum SS");
				
			}
		}
		
		public void closeConnection() {
			
			try {
				
				socket.close();
				System.out.println("Connection closed");
				
				
			} catch(IOException ex) {
				
				System.out.println("IOException closing connection");
				
			}
		}
	}
	
	public static void main(String[] args) {
		
		MysteryWordServer gs = new MysteryWordServer();
		gs.acceptConnections();
		
	}
}