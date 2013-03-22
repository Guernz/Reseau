package POP3;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient {
	public static final int port = 1310;
	public static final int MAX_DATA = 256;
	   
	/*
	 * Pour l'envoie d'un gros fichier, il faut faire une boucle de receive avec pour condition
	 * le poids du fichier / 256 en rajoutant un paquet vide pour �tre sur que le serveur n'attende
	 * pas dans le vide. Utile dans le protocole TCP
	 * 
	 * 
	 */
	
	public static void main( String [ ] args ) throws IOException {
		File file = new File ("Justice4.png");
		FileInputStream stream = new FileInputStream (file);
		byte[] fileData; //Il sera empacter dans le datagrasocket
		DatagramSocket socket = new DatagramSocket();
		InetAddress address = InetAddress.getByName("localhost");
		DatagramPacket datagramFile;
		//Calcul nb de paquet complet a envoyer
		int nbPaquet = (int) file.length()/MAX_DATA;
		//envoi des paquets complets
		int i = 0;
		while(i<=nbPaquet-1) {
			fileData = new byte[MAX_DATA];
			stream.read(fileData);
			datagramFile = new DatagramPacket(fileData, fileData.length, address, port);
			socket.send(datagramFile);
			i++;
			System.out.print("");
			//si pas de System.out.print(), des erreurs de transmission de paquets popent
			//dues à une trop grande puissance de ma machine !
		}
		int tmp = (int) file.length()-MAX_DATA*(nbPaquet);
		System.out.println("bytes restants : "+tmp);
		fileData = new byte[tmp];
		i++;
		System.out.println("envoyé :"+i);
		stream.read(fileData);
		datagramFile = new DatagramPacket(fileData, fileData.length, address, port);
		socket.send(datagramFile);
		stream.close();
		socket.close();
	}
}
