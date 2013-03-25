package TFTP;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

public class tftp {

	static byte[] data = new byte[516];
	static DatagramPacket outBound = new DatagramPacket(data, data.length);
	
	public static int convertirStringEnBytes(byte[] table, int pos, String n) {
		for (int i = 0 ; i < n.length() ; i++){
		    table[pos] = (byte) n.charAt(i) ;
	            pos++;
	        }
		table[pos++] = 0 ;
		return pos ;
	    }
	
	public static void main(String[] args) throws IOException {
		
		DatagramSocket socket = new DatagramSocket();
		String host = "127.0.0.1";
		int port = 2000;
		InetAddress tftp_server = InetAddress.getByName(host);
        
		FileOutputStream fichier = new FileOutputStream("recu");
        
        
		//Récupération des fichiers
		String fileName = "internet.jpg";
	    
		
	    
	    //Mode Read
	    data[0] = 0;
	    data[1] = 1;
	    int pos = convertirStringEnBytes(data,2,fileName);
	    convertirStringEnBytes(data,pos,"octet");
	    System.out.println("taille du tableau : "+data.length);
	    
        outBound = new DatagramPacket(data, data.length, tftp_server, port);
        socket.send(outBound);

		
		int numeroBlock = 1;
		
		data = new byte[516];
    	outBound = new DatagramPacket(data, data.length);
    	socket.receive(outBound);
		
		
        while(outBound.getLength() == 516){
        	fichier.write(data, 4 ,data.length - 4);
    		data = new byte[4];
    		data[0] = 0;
    		data[1] = 4;
    		
    			data[2] = (byte) (numeroBlock / 512);
        		data[3] = (byte) (numeroBlock % 512);
        		
        	String dataCode = new String().valueOf(data[2] + data[3]);	
    		System.out.println(dataCode);
    		
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
		System.out.println(numeroBlock);
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



