package TFTP;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.InputMismatchException;
import java.util.Scanner;


public class tftp {
	
	//Variable globales correspondant au code/mode/taille
    final static int MODE_NETASCII = 0;
    final static int MODE_BINARY = 1;
    final static int OP_RRQ = 1;
    final static int OP_WRQ = 2;
    final static byte OP_DATAPACKET = 3;
    final static byte OP_ACK = 4;
    final static byte OP_ERROR = 5;
    final static int PACKET_SIZE = 516;
    
	static byte[] data = new byte[PACKET_SIZE];
	static DatagramPacket outBound = null;
	final static String host = "127.0.0.1";
	static int port;
	static FileOutputStream fichierRead = null;
	static FileInputStream fichierWrite = null;
	static String fileName = "";
	static DatagramSocket socket = null;
	static InetAddress tftpServer = null;
	static int numeroBloc = 1;
	static Scanner sc = new Scanner(System.in);
	
	//Converti un string en byte et renvoie la position suivant le dernier caractère, Exemple : convertirStringEnBytes("test") renvoit 4
	public static int convertirStringEnBytes(byte[] table, int pos, String n) {
		for (int i = 0 ; i < n.length() ; i++){
		    table[pos] = (byte) n.charAt(i) ;
	            pos++;
	        }
		table[pos++] = 0 ;
		return pos ;
	    }
	
	//Vérifie que la saisie est bien une chaine de caractères et renvoie celui-ci
	public static String saisieString(String affiche){
		String string = "";
		boolean ok = false;
		
		while (!ok){
			System.out.println(affiche);
			try{
				sc = new Scanner(System.in);
				string= sc.nextLine();
				ok = true;
			}
			catch(InputMismatchException e){
				System.out.println("Saisie d'un string incorrect.");
			}
		}
		return string;
	}
	

	//Méthode établissant la connexion avec le serveur dans le cas d'un READ
	public static void connexionRead() throws IOException{
		String nomFichier = "";
		String affiche = "Veuillez saisir le nom du fichier qui sera sauvegard� : ";
		nomFichier = saisieString(affiche);
		fichierRead = new FileOutputStream(nomFichier);

		affiche = "Quel fichier voulez-vous t�l�charger sur le serveur : ";
		fileName = saisieString(affiche);
		
		socket = new DatagramSocket();
		tftpServer = InetAddress.getByName(host);
		
		data = compositionTrameRW(1);
		envoyerTrame(data);
	}
	
	//Méthode établissant la connexion avec le serveur dans le cas d'un WRITE (retourne 1 si connexion OK, sinon -1) 
	public static int connexionPut() throws IOException{
		String affiche = "Veuillez saisir le nom du fichier � envoyer sur le serveur : ";
		fileName = saisieString(affiche);
		try{
			fichierWrite = new FileInputStream(fileName);
		}
		catch(FileNotFoundException e){
			System.out.println("Le fichier '" + fileName + "' n'existe pas.");
			return -1;
		}
		socket = new DatagramSocket();
		tftpServer = InetAddress.getByName(host);
		data = compositionTrameRW(2);
		envoyerTrame(data);
		return 1;
	}
	
	//Création de la première trame en fonction du paramètre (1 : READ, 2 : WRITE)
	public static byte[] compositionTrameRW(int codeOperation){
		byte[] dataPaquet = new byte[PACKET_SIZE];
		if(codeOperation == OP_RRQ){
			dataPaquet[0] = 0;
			dataPaquet[1] = 1;
		}
		else if(codeOperation == OP_WRQ){
			dataPaquet[0] = 0;
			dataPaquet[1] = 2;
		}
		int pos = convertirStringEnBytes(dataPaquet,2,fileName);
	    convertirStringEnBytes(dataPaquet,pos,"octet");
	    dataPaquet[pos] = 0;
	    
		return dataPaquet;
		
	}
	
	//Création de la trame ACK en fonction du numéro de block
	public static byte[] compositionTrameACK(int num){
		byte[] donneeACK = new byte[4];
		donneeACK[0] = 0;
		donneeACK[1] = 4;		
		donneeACK[2] = (byte) (num / 512);
		donneeACK[3] = (byte) (num % 512);
		
		return donneeACK;
	}
	
	//Création de la trame DATA en fonction du numéro de bloc et des données
	public static byte[] compositionTrameDATA(int num, byte[] donnees, int taille){
		byte[] trameData = new byte[taille];
		trameData[0] = 0;
		trameData[1] = 3;		
		trameData[2] = (byte) (num / 512);
		trameData[3] = (byte) (num % 512);
		
		for(int i=4;i<taille;i++){
			trameData[i] = donnees[i-4];
		}
		return trameData;
	}
		
	//Analyse du type de trame reçu et renvoie un entier correspondant au type
	public static int analyseTypeTrame(byte[] b){
		int valRetour = 0;
		if (b.length < 2){
			valRetour = -1;
		}
		else{
			if(b[1] == 1){
				valRetour = 1; //Demande de lecture
			}
			else if(b[1] == 2){
				valRetour = 2; //Demande d'écriture
			}
			else if(b[1] == 3){
				valRetour = 3; //Trame de données
			}
			else if(b[1] == 4){
				valRetour = 4; //Accusé de reception
			}
			else if(b[1] == 5){
				valRetour = 5; //Erreur
				if(b[3] == 0){
					System.out.println("Erreur non d�fini");
				}
				else if(b[3] == 1){
					System.out.println("ERREUR : Fichier non trouv�");
				}
				else if(b[3] == 2){
					System.out.println("ERREUR : Violation de l'acc�s");
				}
				else if(b[3] == 3){
					System.out.println("ERREUR : Disque plein");
				}
				else if(b[3] == 4){
					System.out.println("ERREUR : Op�ration TFTP ill�gale");
				}
				else if(b[3] == 5){
					System.out.println("ERREUR : Transfert ID inconnu");
				}
				else if(b[3] == 6){
					System.out.println("ERREUR : Le fichier existe d�j�");
				}
				else if(b[3] == 7){
					System.out.println("ERREUR : Utilisateur inconnu");
				}
			}
		}
		return valRetour;
	}
	
	//Envoyer une trame
	public static void envoyerTrame(byte[] donnees) throws IOException{
		outBound = new DatagramPacket(donnees, donnees.length, tftpServer, port);
        socket.send(outBound);
	}
	
	//Recevoir une trame
	public static void recevoirTrame() throws IOException{
		data = new byte[PACKET_SIZE];
    	outBound = new DatagramPacket(data, data.length);
    	socket.receive(outBound);		
	}
	
	//Méthode permettant de récupérer des fichiers
	public static void recupererFichier() throws IOException{
        boolean erreur = false;
		connexionRead();

		int numeroBloc = 1;
		recevoirTrame();		
        while(outBound.getLength() == 516 && erreur == false){
        	if(analyseTypeTrame(data)==5){
        		socket.close();
        		fichierRead.close();
        		return;
        	}
        	if(data[3] == numeroBloc){
	        	fichierRead.write(data, 4 ,data.length - 4);
	        	envoyerTrame(compositionTrameACK(numeroBloc));
	    		numeroBloc++;
	    		recevoirTrame();	
        	}
        	else {
        		envoyerTrame(compositionTrameACK(numeroBloc-1));
	    		recevoirTrame();
        	}

        }
        fichierRead.write(data, 4 ,data.length - 4);
        envoyerTrame(compositionTrameACK(numeroBloc)); 
        fichierRead.close();
        socket.close();
	}
	
	
	//Méthode permettant d'envoyer des fichiers sur le serveur
	public static void envoyerFichier() throws IOException{
		byte[] bufferFichier = new byte[512];
		int numeroBloc = 0;
		int tailleBuffer = 0;
		
		//On vérifie que la connexion se passe correctement
		if(connexionPut()==-1){
			return;
		}
		
			
		//On lit le fichier par paquet de longueur de trame DATA qu'on envoit sur le serveur une fois formaté en trame
		byte[] dataRenvoi = null;
		while((tailleBuffer = fichierWrite.read(bufferFichier)) >= 0){
			recevoirTrame();
			System.out.println(numeroBloc);
			if(analyseTypeTrame(data)!= 4){
				System.out.println("Le serveur n'a pas re�u la trame de composition.");
				return;
			}
			else{
				if(data[3]==numeroBloc){
					System.out.println(numeroBloc + " recu par le serveur");
					numeroBloc++;
					System.out.println("envoi au serveur " + numeroBloc);
					data = compositionTrameDATA(numeroBloc, bufferFichier, tailleBuffer+4);
					dataRenvoi = data;
					envoyerTrame(data); 
				}
				else{
					System.out.println("renvoi " + numeroBloc);
					while(data[3]!=numeroBloc){
						envoyerTrame(dataRenvoi);
						recevoirTrame();
					}
					System.out.println(numeroBloc + " recu par le serveur apres renvoi");
					numeroBloc++;
					data = compositionTrameDATA(numeroBloc, bufferFichier, tailleBuffer+4);
					dataRenvoi=data;
					envoyerTrame(data);
				}
			}
		}
		
		//On récupère le dernier ACK du serveur
		recevoirTrame();
		if(data[3]!=numeroBloc){
			while(data[3]!=numeroBloc){
				System.out.println("renvoi " + numeroBloc + " data " + data[3]);
				envoyerTrame(dataRenvoi);
				recevoirTrame();
			}
		}
		System.out.println("dernier paquet " + numeroBloc + " recu par le serveur");
	}
	
	
	public static void main(String[] args) throws IOException {
		
		boolean quitte = false;
		int choix = 0;
		
		//Demande du port de connexion pour le serveur
		while(port<1024 || port>65535){
			System.out.println("Veuillez saisir le port de connexion : ");
			try{
				sc = new Scanner(System.in);
				port = sc.nextInt();
				if(port<1024 || port>65535){
					System.out.println("Le num�ro de port doit �tre compris entre 1024 et 65 535 inclus.");
				}
			}
			catch (InputMismatchException e){
				System.out.println("Erreur. Vous n'avez pas saisie un entier.");
			}
		}

		//menu
		while (!quitte){
			System.out.println("\n===============================================");
			System.out.println("                 PROJET RESEAU                 ");
			System.out.println("===============================================");
			System.out.println();
			System.out.println("Que voulez-vous faire ?");
			System.out.println("1. Envoyer un fichier sur le serveur");
			System.out.println("2. Recup�rer un fichier sur le serveur");
			System.out.println("3. Quitter");
			System.out.println();
			try {
				sc = new Scanner(System.in);
				choix = sc.nextInt();
				switch(choix){
				case 1 : 
					envoyerFichier();
					break;
				case 2 : 
					recupererFichier();
					break;
				case 3: 
					System.out.println("Au revoir");
					quitte = true;
					break;
				}
			}
			catch (InputMismatchException e){
				System.out.println("Erreur. Les choix possibles vont de 1 �3.\n");
			}
		}
		sc.close();
				
		
   
		//Récupération des fichiers
		//Demande le choix à l'utilisateur à la fin (à implémenter)
		
	    
        /*
         *  - Connection entre les deux machines
         *  - Transfert commence en envoyant un paquet (WRQ pour écrire un fichier ou RRQ pour lire un fichier)
         *  - Le client et le serveur génère aléatoirement un TID (Transfert ID)
         *  
         *  - Perte d'un paquet, détectable par un temps d'attente trop long (timeout)
         *  - Envoie d'un paquet d'erreur (error packet) entraine l'arret de l'envoi
         *   
         */
        
		
	

	}

}



