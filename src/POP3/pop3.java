package POP3;

import java.io.*;
import java.util.*;
import java.net.*;

public class pop3 {

	//On renseigne le port de connexion
	public static final int port = 1100;
	public static Socket socket;
	static ArrayList<String> reponses;
	static String commandeEnvoi;
		
	//void envoyer(String message)
	//envoie le message placé en argument au serveur
	public static void envoyer(String message)throws IOException{
		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		wr.write(message);
		wr.flush (); 
	}
	
	//ArrayList recevoir()
	//recoit des chaines de caractère du serveur et les ajoute dans une arraylist
	//retourne l'arraylist
	public static ArrayList<String> recevoir() throws IOException{
		ArrayList<String> liste = new ArrayList<String>();
		BufferedReader rd = new BufferedReader (new InputStreamReader(socket.getInputStream ())) ;
		liste.add(rd.readLine().toString());
		
		while(rd.ready()){
			liste.add(rd.readLine().toString());
		}
		
		return liste;
	}
	
	//void quitter()
	//envoi la commande "QUIT" au serveur et quitte le programme
	public static void quitter() throws IOException{
		commandeEnvoi = "QUIT" + System.getProperty("line.separator") ;
		envoyer(commandeEnvoi);
		reponses = new ArrayList<String>();
		reponses = recevoir();
		System.out.println(reponses.get(0).substring(3));
		System.exit(0);
	}
		
	public static void main(String[] args) throws IOException {
		
		//declaration variables
		Scanner sc = new Scanner(System.in);
		String 	login, 
				mdp;
		boolean loginValide = false, 
				mdpValide = false, 
				loginCorrect = false, 
				mdpCorrect = false, 
				quitte = false;
		int 	nbErreurLogin = 4, 
				nbErreurMdp = 4, 
				choix;
		//loginValide et mdpValide verifie que la chaine de caractère entrée est une chaine de caractère
		//loginCorrect et mdpCorrect verifie que  l'utilisateur existe et que le mdp est associé au login
				
		//On renseigne l'adresse IP de connexion
		InetAddress address = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
		//Connexion au serveur
		System.out.println("Connexion au serveur");
		socket = new Socket(address,port);
		
		//recoit message de bienvenue du serveur
		reponses = new ArrayList<String>();
		reponses=recevoir();
		//si erreur
		if(!reponses.get(0).contains("+OK")){
			System.out.println("Erreur de connexion");
			socket.close();
			System.exit(0);
		}
		//sinon
		else{
			System.out.println(reponses.get(0).substring(3));
		}
		
		//Identification de l'utilisateur sur le serveur
		System.out.println("");
		while(!loginValide){
			System.out.println("Entrez votre login :");
			while(!loginCorrect){
				try {
					//saisie clavier du login
					login = sc.nextLine();
					//preparation de la ligne de commande a envoyer au serveur, envoi et reponse
					commandeEnvoi = "USER " + login + System.getProperty("line.separator") ;
					envoyer(commandeEnvoi);
					reponses = new ArrayList<String>();
					reponses = recevoir();
					//si reponse erreur
					if(!reponses.get(0).contains("+OK")){
						nbErreurLogin--;
						//quitte si trop d'erreur
						if(nbErreurLogin==0){
							System.out.println("Trop de tentatives incorrectes, au revoir.");
							quitter();
						}
						System.out.println("L'utilisateur " + login + " n'existe pas, il vous reste " + nbErreurLogin + " tentative(s) veuillez ressaisir votre login :");
					}
					//si reponse ok
					else
					{
						System.out.println(reponses.get(0).substring(3));
						loginCorrect = true;
						loginValide = true;
					}
				}
				//exception si mauvaise saisie clavier
				catch (InputMismatchException e){
					System.out.println("Erreur.");
					quitter();
				}
			}
		}
	
		//Mot de passe
		System.out.println("");
		while(!mdpValide){
			System.out.println("Entrez votre mot de passe :");
			while(!mdpCorrect){
				try {
					//saisie clavier du mot de passe
					mdp = sc.nextLine();
					//preparation ligne de commande a envoyer au serveur, envoi et reponse
					commandeEnvoi = "PASS " + mdp + System.getProperty("line.separator") ;
					envoyer(commandeEnvoi);
					reponses = new ArrayList<String>();
					reponses = recevoir();
					//si reponse erreur
					if(!reponses.get(0).contains("+OK")){
						nbErreurMdp--;
						//quitte si trop d'erreur
						if(nbErreurMdp==0){
							System.out.println("Trop de tentatives incorrectes, au revoir.");
							quitter();
						}
						System.out.println("Votre mot de passe est incorrect, il vous reste " + nbErreurMdp + " tentative(s) veuillez ressaisir votre mot de passe :");
					}
					//si reponse ok
					else
					{
						System.out.println(reponses.get(0).substring(3));
						mdpCorrect = true;
						mdpValide = true;
					}
				}
				//exception si mauvaise saisie clavier
				catch (InputMismatchException e){
					System.out.println("Erreur.");
					quitter();
				}
			}
		}
		
		//tant que l'on ne quitte pas
		while(!quitte) {
			//menu
			System.out.println("");
			System.out.println("Menu de la messagerie, faites votre choix :");
			System.out.println("1 : Consulter vos messages.");
			System.out.println("2 : Quitter.");
			try{
				//saisie clavier du choix du menu
				choix=sc.nextInt();
				switch(choix){
				//consulter les messages
				case 1 : 
					//envoi de la commande list dans le but de récupérer le nombre de message
					commandeEnvoi = "LIST" + System.getProperty("line.separator") ;
					envoyer(commandeEnvoi);
					reponses = new ArrayList<String>();
					reponses = recevoir();
					//si serveur renvoi erreur
					if(!reponses.get(0).contains("+OK")){
						System.out.println("Erreur.");
					}
					//si serveur renvoi ok
					else {
						int nbMessage, i = 1;
						//calcul nb message (- 1ere ligne "ok" - derniere ligne ".")
						nbMessage = reponses.size()-2;
						System.out.println("");
						System.out.println("Mail(s) sur le serveur : " + nbMessage);
						//pour chaque message
						while (i<=nbMessage){
							//recupere tout le contenu du message dans arraylist message
							commandeEnvoi = "RETR " + i + System.getProperty("line.separator");
							envoyer(commandeEnvoi);
							ArrayList<String> message = new ArrayList<String>();
							message = recevoir();
							//si message non trouvé
							if(!reponses.get(0).contains("+OK")){
								System.out.println("Erreur. Le message d'ID " + i + " n'existe pas(plus).");
							}
							//sinon message trouvé
							else {
								//recupere l'expediteur et le sujet
								int j = 1;
								String expediteur = null , sujet = null;
								while(j<message.size()){
									if(message.get(j).startsWith("From: ")){
										expediteur = (String) message.get(j).substring(5);
									}
									if(message.get(j).startsWith("Subject: ")){
										sujet = message.get(j).substring(8);
									}
									j++;
								}
								//affiche les infos
								System.out.println(i + " " + expediteur + " -- " + sujet);
							}
							i++;
						}
					}					
					break;
				//quitter
				case 2 :
					quitter();
					break;
				}
			}
			//exception si mauvaise saisie clavier
			catch (InputMismatchException e){
				System.out.println("Erreur.");
				quitter();
			}
			System.out.println("");
		}
		//fin du programme, on ferme les flux
		sc.close();
		socket.close();
	}

}
