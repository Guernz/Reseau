package TFTP;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
	static int port = 2000;
	static FileOutputStream fichierRead = null;
	static FileInputStream fichierWrite = null;
	static String fileName = "";
	static DatagramSocket socket = null;
	static InetAddress tftpServer = null;
	static int numeroBloc = 1;
	
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
	public static String saisieString(){
		String string = "";
		Scanner scanString = new Scanner(System.in);
		boolean ok = false;
		
		while (ok == false){
			try{
				string= scanString.nextLine();
				ok = true;
			}
			catch(Exception e){
				System.out.println("Saisie d'un string incorrect. Veuillez réessayer : ");
				scanString.nextLine();
				ok = false;
			}
		}
		
		return string;
	}
	
	//Vérifie que la saisie est bien un entier et renvoie celui-ci
	public static int saisieInt(){
		int entier = 0;
		Scanner scanString = new Scanner(System.in);
		boolean ok = false;
		
		while (ok == false){
			try{
				entier = scanString.nextInt();
				ok = true;
			}
			catch(Exception e){
				System.out.println("Saisie d'un entier incorrecte. Veuillez réessayer : ");
				scanString.nextLine();
				ok = false;
			}
		}
		return entier;
	}
	
	//Conversion un tableau de 2 byte en un entier  
	public static int convertirByteEnEntier2(byte b[]){
		int valRetour = 0;
		
		valRetour = b[1] << 8 + b[0];
		valRetour = valRetour / 256;

		return valRetour;
	}
	
	//Permet le choix de port de connexion et vérifie sa validité
	public static void choixPort(){
		System.out.println("Veuillez saisir le port de connexion : ");
		while(port<1024 || port>65535){
			port = saisieInt();
			if(port<1024 || port>65535){
				System.out.println("Le numéro de port doit être compris entre 1024 et 65 535 inclus");
			}
		}	
	}
	
	//Méthode établissant la connexion avec le serveur dans le cas d'un READ
	public static void connexionRead() throws IOException{
		String nomFichier = "";
		System.out.println("Veuillez saisir le nom du fichier qui sera sauvegarder : ");
		nomFichier = saisieString();
		fichierRead = new FileOutputStream(nomFichier);

		System.out.println("Quelle fichier voulez-vous télécharger sur le serveur : ");
		fileName = saisieString();
		
		socket = new DatagramSocket();
		tftpServer = InetAddress.getByName(host);
		
		data = compositionTrameRW(1);
		envoyerTrame(data);
	}
	
	//Méthode établissant la connexion avec le serveur dans le cas d'un WRITE (retourne 1 si connexion OK, sinon -1) 
	public static int connexionPut() throws IOException{
		System.out.println("Veuillez saisir le nom du fichier à envoyer sur le serveur : ");
		fileName = saisieString();
		try{
			fichierWrite = new FileInputStream(fileName);
		}
		catch(FileNotFoundException e){
			System.out.println("Le fichier '" + fileName + "' n'existe pas");
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
					System.out.println("Erreur non défini");
				}
				else if(b[3] == 1){
					System.out.println("ERREUR : Fichier non trouvé");
				}
				else if(b[3] == 2){
					System.out.println("ERREUR : Violation de l'accès");
				}
				else if(b[3] == 3){
					System.out.println("ERREUR : Disque plein");
				}
				else if(b[3] == 4){
					System.out.println("ERREUR : Opération TFTP illégale");
				}
				else if(b[3] == 5){
					System.out.println("ERREUR : Transfert ID inconnu");
				}
				else if(b[3] == 6){
					System.out.println("ERREUR : Le fichier existe déjà");
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
        	fichierRead.write(data, 4 ,data.length - 4);
        	envoyerTrame(compositionTrameACK(numeroBloc));
    		numeroBloc++;
    		recevoirTrame();
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
		System.out.println("test");
		//On lit le fichier par paquet de longueur de trame DATA qu'on envoit sur le serveur une fois formaté en trame
		while((tailleBuffer = fichierWrite.read(bufferFichier)) >= 0){
			recevoirTrame();
			System.out.println(numeroBloc);
			if(analyseTypeTrame(data)!= 4){
				System.out.println("Le serveur n'a pas reçu la trame de composition ");
			}
			else{
				numeroBloc++;
				data = compositionTrameDATA(numeroBloc, bufferFichier, tailleBuffer+4);
				envoyerTrame(data);
			}
		}
		
		//On récupère le dernier ACK du serveur
		recevoirTrame();
		numeroBloc++;
	}
	
	//Ancienne méthode (à supprimer) sans la factorisation avec les méthodes pour "mieux comprendre"
	public static void recupererFichierAvecErreur() throws IOException{
		String fileName = "rfc1350.txt";
		FileOutputStream fichier = new FileOutputStream("recu");
		DatagramSocket socket = new DatagramSocket();
		
		InetAddress tftp_server = InetAddress.getByName(host);
        
		
		//Composition de la première trame pour demander l'envoie de paquet
	    data[0] = 0;
	    data[1] = 1;
	   
	    int pos = convertirStringEnBytes(data,2,fileName);
	    convertirStringEnBytes(data,pos,"octet");
	    
	    //Envoie de la première trame
        outBound = new DatagramPacket(data, data.length, tftp_server, port);
        socket.send(outBound);

        //Récupération du premier block
		int numeroBlock = 1;
		int numeroBlockSuivant = numeroBlock + 1;
		data = new byte[516];
    	outBound = new DatagramPacket(data, data.length);
    	socket.receive(outBound);
		
    	byte codeOp[];
    	byte numBlock[];
    	//Tant qu'on n'atteint pas le dernier paquet (poids de celui-ci < 256)
        while(outBound.getLength() == 516){
        	//Récupération du code opération de la trame reçue
        	codeOp = new byte[2];
        	codeOp[1] = outBound.getData()[1];
        	
        	//Récupération du numero du block de la trame reçue
        	numBlock = new byte[2];
        	numBlock[0] = outBound.getData()[2];
        	numBlock[1] = outBound.getData()[3];
        	int result = convertirByteEnEntier2(numBlock);
        			
        	//Analyse de l'en-tête du paquet
        	if ((int) codeOp[1] == 5){ 
        		System.out.println("Erreur.");
        		fichier.close();
        		System.exit(0);
        	}
        	else if ((int) codeOp[1] == 3){
        		
        	
	        	//On écrit dans le fichier la données sans les 4 premiers octets (Code bloc et num bloc)
	        	fichier.write(data, 4 ,data.length - 4);
	    		
	        	//Composition de la trame ACK à envoyer
	        	data = new byte[4];
	    		data[0] = 0;
	    		data[1] = 4;		
				data[2] = (byte) (numeroBlock / 512); //512 car octets
	    		data[3] = (byte) (numeroBlock % 512);
	        	
	    		//Envoie de la trame qui confirme la réception du paquet 
	    		outBound = new DatagramPacket(data, 4, tftp_server, port); 
	    		socket.send(outBound);
	    		numeroBlock++;
	    		
	    		data = new byte[516];
	        	outBound = new DatagramPacket(data, data.length);
	        	socket.receive(outBound);
        	}
	    }
        
        codeOp = new byte[2];
    	codeOp[1] = outBound.getData()[1];
        if ((int) codeOp[1] == 5){ 
    		
    	}
    	else if ((int) codeOp[1] == 3){
	        
	        fichier.write(data, 4 ,data.length - 4);
	        
	        data = new byte[4];
			data[0] = 0;
			data[1] = 4;	
			data[2] = (byte) (numeroBlock / 512);
			data[3] = (byte) (numeroBlock % 512);
			outBound = new DatagramPacket(data, 4, tftp_server, port); 
			socket.send(outBound);
    	}
	}
	
	//Méthode affichant l'interface principal de l'application
	public static void afficherInterface(){
		System.out.println("===============================================");
		System.out.println("                 PROJET RESEAU                 ");
		System.out.println("===============================================");
		System.out.println();
		System.out.println("Que voulez-vous faire ?");
		System.out.println("1. Utiliser le client POP3");
		System.out.println("2. Envoyer un fichier sur le serveur");
		System.out.println("3. Recupérer un fichier sur le serveur");
		System.out.println("4. Quitter");
		System.out.println();
	}

	
	public static void main(String[] args) throws IOException {
		int choix = 0;

		while (choix != 4){
			afficherInterface();
			choix = saisieInt();
			switch(choix){
			case 1 : 
				
				break;
			case 2 : 
				envoyerFichier();
				break;
			case 3 : 
				recupererFichier();
				break;
			case 4 : 
				System.out.println("Au revoir");
				break;
			default : 
				System.out.println("Les choix possibles vont de 1 à 3.\n");
				break;
			}
		}
				
		
   
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



