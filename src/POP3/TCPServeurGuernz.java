package POP3;

import java.net.*;
import java.util.Scanner;
import java.io.*;

public class TCPServeurGuernz {
	
	public static void main( String [ ] args ) throws IOException {
		ServerSocket srv = new ServerSocket (5001);
		// Attente connexion
		Socket socket = srv.accept();
		boolean auRevoir = false;
		while(!auRevoir){
			//serveur envoi ensuite un message
			BufferedWriter wr = new BufferedWriter(new OutputStreamWriter (socket.getOutputStream()));
			Scanner sc = new Scanner(System.in);
			System.out.println("Quel message voulez vous envoyé au client ?");
			String message=sc.next();
			wr.write(message);
			wr.flush (); 
			if(message.equals("Au revoir")){
				auRevoir=true;
			}
			//serveur recoit d'abord un message
			BufferedReader rd = new BufferedReader (new InputStreamReader ( socket . getInputStream ( ) ) ) ; 
			System.out.println("Message reçu: "+rd.readLine());
			if(((String) rd.readLine()).equals("Au revoir")){
				auRevoir = true;
			}
		}
		socket.close();	
	}
}