package TFTP;

import java.io.*;
import java.net.*;

/**
 * RemCat - affichage d’un fichier distant (commande cat du DOS) à l’aide 
 * du protocole TFTP. Inspiré de l’exercice "rcat" du cours 363 de Learning
 * Tree, <I>UNIX Network Programming</I>, par Pr. Chris Brown.
 *
 * Notez que le serveur TFTP n’est PAS "internationalisé" ; le nom et le mode
 * dans le protocole sont donnés en ASCII, pas en UniCode.
 */
public class Exemple2 {
    /** Numéro de port UDP. */
    public final static int TFTP_PORT = 69;
    /** Mode à utiliser - toujours "octet". */
    protected final String MODE = "octet";

    /** Déplacement pour le code et la réponse (un octet). */
    protected final int OFFSET_REQUEST = 1;
    /** Déplacement pour le numéro du paquet (un octet). */
    protected final int OFFSET_PACKETNUM = 3;

    /** Indicateur de débogage. */
    protected static boolean debug = false;

    /** Code d’opération TFTP pour une demande de lecture. */
    public final int OP_RRQ = 1,
        /** Code d’opération TFTP pour une demande de lecture. */
        OP_WRQ = 2,
        /** Code d’opération TFTP pour une demande d’écriture. */
        OP_DATA     = 3,
        /** Code d’opération TFTP pour une demande de données. */
        OP_ACK     = 4,
        /** Code d’opération TFTP pour une demande d’acquittement. */
        OP_ERROR = 5;
    protected final static int PACKET = 516;    // == 2 + 2 + 512
    protected String host;
    protected InetAddress servAddr;
    protected DatagramSocket sock;
    protected byte buffer[];
    protected DatagramPacket inp, outp;

    /** Programme principal pilotant ce client réseau.
     * @param argv[0] Nom du serveur TFTP.
     * @param argv[1..n] Nom(s) de fichier, au moins un.
     */
    public static void main(String[] argv) throws IOException {
        if (argv.length < 2) {
            System.err.println("utilisation : "+        
                               "java RemCat machine fichier[...]");
            System.exit(1);
        }
        if (debug)
            System.err.println("Java RemCat - lancement");
        Exemple2 rc = new Exemple2(argv[0]);
        for (int i = 1; i<argv.length; i++) {
            if (debug)
                System.err.println("-- Fichier de démarrage " + 
                    argv[0] + " : " + argv[i] + " ---");
            rc.readFile(argv[i]);
        }
    }

    Exemple2(String host) throws IOException {
        super();
        this.host = host;
        servAddr = InetAddress.getByName(host);
        sock = new DatagramSocket();
        buffer = new byte[PACKET];
        inp = new DatagramPacket(buffer, PACKET);
        outp = new DatagramPacket(buffer, PACKET, servAddr, TFTP_PORT);
    }

    void readFile(String path) throws IOException {
        /* Construire un paquet de demande de lecture tftp. Ce n’est pas 
         * très propre car les champs ont une taille variable. Les numéros
         * doivent être dans l’ordre réseau ; heureusement Java semble assez
         * naturellement intelligent :-) pour utiliser cet ordre.
         */
        buffer[0] = 0;
        buffer[OFFSET_REQUEST] = OP_RRQ;        // Demande de lecture.
        int p = 2;            // Nombre de caractères dans le tampon.

        // Convertir la chaîne contenant le nom de fichier en octets dans le
        // tampon, en utilisant "p" comme indicateur de déplacement pour que
        // tous les bits de cette requête soient au bon endroit.
        path.getBytes(0, path.length(), buffer, p); // Nom de fichier.
        p += path.length();
        buffer[p++] = 0;        // Octet nul pour terminer la chaîne.

        // Convertir de même le MODE ("octet") en octets dans le tampon.
        MODE.getBytes(0, MODE.length(), buffer, p);
        p += MODE.length();
        buffer[p++] = 0;        // Terminé par null.

        /* Envoyer la demande de lecture au serveur TFTP. */
        outp.setLength(p);
        sock.send(outp);

        /* Boucle de lecture des paquets de données provenant du serveur. Se
         * termine quand un paquet court arrive ; il indique la fin du fichier.
         */
        int len = 0;
        do {
            sock.receive(inp);
            if (debug)
                System.err.println(
                    "Paquet # " + Byte.toString(buffer[OFFSET_PACKETNUM]) + 
                    "CODE DE RÉPONSE " +
                    Byte.toString(buffer[OFFSET_REQUEST]));
            if (buffer[OFFSET_REQUEST] == OP_ERROR) {
                System.err.println("ERREUR remcat : " +
                    new String(buffer, 4, inp.getLength()-4));
                return;
            }
            if (debug)
                System.err.println("Reçu un paquet de taille " +
                    inp.getLength());

            /* Afficher les données du paquet. */
            System.out.write(buffer, 4, inp.getLength()-4);

            /* Accuser réception du paquet. Le numéro de bloc que nous
             * voulons valider se trouve déjà dans le tampon, nous changeons
             * donc uniquement le code d’opération. L’accusé est envoyé au
             * numéro de port sur lequel le serveur a envoyé les données, PAS
             * le port TFTP_PORT.
             */
            buffer[OFFSET_REQUEST] = OP_ACK;
            outp.setLength(4);
            outp.setPort(inp.getPort());
            sock.send(outp);
        } while (inp.getLength() == PACKET);

        if (debug)
            System.err.println("** TERMINÉ ** On quitte la boucle, "+
                               "dernière taille " + inp.getLength());
    }
}