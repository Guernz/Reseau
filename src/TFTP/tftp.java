package TFTP;

import java.util.*;

public class tftp {

	public static void main(String[] args) {
		
		//Déclaration variables
		Scanner sc = new Scanner(System.in);
		int perteDonnees, port;
		boolean avecPerteDonnees, ok;
		
		//Demande a l'utilisateur si avec ou sans perte de donnees
		ok = false;
		System.out.println("Veuillez saisir \"1\" pour activer la gestion de pertes de données, \"0\" sinon :");
		while(!ok){
			try{
				perteDonnees = sc.nextInt();
				if(perteDonnees == 0){
					avecPerteDonnees =  false;
					System.out.println("OK, sans gestion de pertes de données");
					ok = true;
				}
				if(perteDonnees == 1){
					avecPerteDonnees =  true;
					System.out.println("OK, avec gestion de pertes de données.");
					ok = true;
				}
				if(perteDonnees != 0 && perteDonnees != 1){
					System.out.println("Erreur, veuillez saisir \"1\" pour la gestion de pertes de données, \"0\" sinon.");
				}
			}
			catch (InputMismatchException e){
				System.out.println("Erreur, il faut saisir un entier \"1\" pour la gestion de pertes de données, \"0\" sinon.");
				System.exit(0);
			}
		}
		System.out.println(" ");
		
		//Demande a l'utilisateur le port de connexion
		ok = false;
		while(!ok){
			try{
				port = sc.nextInt();
				if (port<0){
					System.out.println("Veuillez saisir un port positif.");
				}
				else{
					ok = true;
					System.out.println("OK, port de connexion " + port);
				}	
			}
			catch (InputMismatchException e){
				System.out.println("Erreur, il faut saisir un entier \"1\" pour la gestion de pertes de données, \"0\" sinon.");
				System.exit(0);
			}
		}
		
		//Sans la gestion des pertes de données
		if(!avecPerteDonnees){
			
		}
		
		//Avec la gestion des pertes de données
		if(avecPerteDonnees);

	}
	
	sc.close();

}
