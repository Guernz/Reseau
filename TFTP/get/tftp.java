
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

public class tftp {

	static byte[] data = new byte[516];
	
	public static int convertirStringEnBytes(byte[] table, int pos, String n) {
		for (int i = 0 ; i < n.length() ; i++){
		    table[pos] = (byte) n.charAt(i) ;
	            pos++;
	        }
		table[pos++] = 0 ;
		return pos ;
	    }
	
	public static void main(String[] args) throws IOException {
		
		DatagramSocket socket = null;
        InetAddress tftp_server = null;
        String host = "127.0.0.1";
        int port = 1024;
        
        //Connexion au serveur
		try {
            tftp_server = InetAddress.getByName(host);
            socket = new DatagramSocket();
        }
        catch (IOException ex) {
            System.err.println(ex);
            System.exit(1);
        }
		
		//System.out.println("Connexion au serveur réussi");
		
		//Récupération des fichiers
		String request = "";
		String fileName = "rfc1350.txt";
		DatagramPacket outBound;
	    DatagramPacket inBound;
		
	    
	    //Mode Read
	    data[0] = '0';
	    data[1] = '1';
	    int pos = convertirStringEnBytes(data,2,fileName);
	    convertirStringEnBytes(data,pos,"octet");
	    
        outBound = new DatagramPacket(data, data.length, tftp_server, port);
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



