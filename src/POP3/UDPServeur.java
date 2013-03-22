package POP3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServeur {

		public static final int port = 1310;
		public static final int MAX_DATA = 256;
		   
		public static void main( String [ ] args ) throws IOException {
			File file = new File ("test.png");
			FileOutputStream stream = new FileOutputStream (file);
			byte[] fileData; //Il sera empacter dans le datagrasocket
			DatagramSocket socket = new DatagramSocket(port);
			DatagramPacket receivePacket;
			fileData = new byte[MAX_DATA];
			receivePacket = new DatagramPacket(fileData, fileData.length);			
			socket.receive(receivePacket); //Je regarde les paquets que je reçoit
			int i=0;
			while(receivePacket.getLength() == MAX_DATA){
				stream.write(receivePacket.getData());
				fileData = new byte[MAX_DATA];
				receivePacket = new DatagramPacket(fileData, fileData.length);
				socket.receive(receivePacket);
				i++;
			}
			stream.write(receivePacket.getData(), 0, receivePacket.getLength());
			i++;
			System.out.println("recu "+i);
			stream.close();
			socket.close();
		}
}