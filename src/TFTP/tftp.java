package TFTP;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

public class tftp {

	public static void main(String[] args) {
	/*	
		//DÈclaration variables
		Scanner sc = new Scanner(System.in);
		int perteDonnees, port;
		boolean avecPerteDonnees = true, ok = false;
	
		//Demande a l'utilisateur si avec ou sans perte de donnees
		System.out.println("Veuillez saisir \"1\" pour activer la gestion de pertes de donnÈes, \"0\" sinon :");
		while(!ok){
			try{
				perteDonnees = sc.nextInt();
				if(perteDonnees == 0){
					avecPerteDonnees =  false;
					System.out.println("OK, sans gestion de pertes de donnÈes.");
					ok = true;
				}
				if(perteDonnees == 1){
					avecPerteDonnees =  true;
					System.out.println("OK, avec gestion de pertes de donnÈes.");
					ok = true;
				}
				if(perteDonnees != 0 && perteDonnees != 1){
					System.out.println("Erreur, veuillez saisir \"1\" pour la gestion de pertes de donnÈes, \"0\" sinon :");
				}
			}
			catch (InputMismatchException e){
				System.out.println("Erreur, il faut saisir un entier \"1\" pour la gestion de pertes de donnÈes, \"0\" sinon.");
				System.exit(0);
			}
		}
		System.out.println(" ");
		
		//Demande a l'utilisateur le port de connexion
		ok = false;
		System.out.println("Veuillez saisir le port de connexion :");
		while(!ok){
			try{
				port = sc.nextInt();
				if (port < 0){
					System.out.println("Veuillez saisir un port positif :");
				}
				if (port > 65536){
					System.out.println("Veuillez saisir un port infÈrieur ‡ 65536 :");
				}
				if (port >= 0 && port <= 65535){
					ok = true;
					System.out.println("OK, port de connexion " + port + ".");
				}	
			}
			catch (InputMismatchException e){
				System.out.println("Erreur, il faut saisir un entier pour indiquer le port de connexion.");
				System.exit(0);
			}
		}
		
		//Sans la gestion des pertes de donnÈes
		if(!avecPerteDonnees){
			
		}
		
		//Avec la gestion des pertes de donnÈes
		if(avecPerteDonnees){
			
		}
	*/
		
		
		DatagramSocket socket = null;
        InetAddress tftp_server = null;
        String host = "1024";
        
        //Connexion au serveur
		try {
            tftp_server = InetAddress.getByName(host);
            socket = new DatagramSocket();
        }
        catch (IOException ex) {
            System.err.println(ex);
            System.exit(1);
        }
		System.out.println("Connexion au serveur réussi");
		
		//Récupération des fichiers
		String request = "";
		String fileName = "special_read";
		DatagramPacket outBound;
	    DatagramPacket inBound;
		
		request = createData(1, fileName, 1).toByteArray();
        outBound = new DatagramPacket(request, request.length(), tftp_server, 69);
        socket.send(outBound);

		
		
		//sc.close();

	}

}



