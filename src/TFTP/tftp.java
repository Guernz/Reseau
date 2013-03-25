package TFTP;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

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
	static byte[] data = new byte[516];
	static DatagramPacket outBound = new DatagramPacket(data, data.length);
	
	/*
	 * Converti un string en byte et renvoie la position suivant le dernier caractère
	 * Exemple : convertirStringEnBytes("test") renvoit 4
	 */
	public static int convertirStringEnBytes(byte[] table, int pos, String n) {
		for (int i = 0 ; i < n.length() ; i++){
		    table[pos] = (byte) n.charAt(i) ;
	            pos++;
	        }
		table[pos++] = 0 ;
		return pos ;
	    }
	
	/*
	 * Vérifie que la saisie est bien une chaine de caractères et renvoit la chaine
	 */
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
	
	/*
	 * Méthode GET qui permet de récupérer des fichiers
	 */
	public static void recupererFichier(){
		
	}
	
	
	public static void main(String[] args) throws IOException {
		
		DatagramSocket socket = new DatagramSocket();
		String host = "127.0.0.1";
		int port = 2000;
		InetAddress tftp_server = InetAddress.getByName(host);
        
		FileOutputStream fichier = new FileOutputStream("recu");
        
        
		//Récupération des fichiers
		//Demande le choix à l'utilisateur à la fin (à implémenter)
		String fileName = "rfc1350.txt";
	     
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
		data = new byte[516];
    	outBound = new DatagramPacket(data, data.length);
    	socket.receive(outBound);
		
    	//Tant qu'on n'atteint pas le dernier paquet (poids de celui-ci < 256)
        while(outBound.getLength() == 516){
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
        fichier.write(data, 4 ,data.length - 4);
        
        data = new byte[4];
		data[0] = 0;
		data[1] = 4;
		//String dataCode = new String().valueOf(data[2] + data[3]);	
		data[2] = (byte) (numeroBlock / 512);
		data[3] = (byte) (numeroBlock % 512);
		outBound = new DatagramPacket(data, 4, tftp_server, port); 
		socket.send(outBound);
        
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



