package TFTP;

import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;

public class Exemple {

    public final static int DEFAULT_PORT = 69;
    public final static int MODE_NETASCII = 0;
    public final static int MODE_BINARY = 1;
    public final static int OP_RRQ = 1;
    public final static int OP_WRQ = 2;
    public final static byte OP_DATAPACKET = 3;
    public final static byte OP_ACK = 4;
    public final static byte OP_ERROR = 5;
    public final static int PACKET_SIZE = 516;

    public static void main(String[] args) {
        //Checks for correct user input
        if (args.length != 3) {
            System.err.println("Usage: tftp [IP_TALA] [PUT/GET] [FILENAME] ");
            System.exit(1);
        }

        //Sets the PUT/GET to UpperCase in case of LowerCase input from user
        String command = args[1].toUpperCase();
        String host = args[0];
        DatagramSocket socket = null;
        InetAddress tftp_server = null;
        byte[] request;
        byte[] buffer;
        DatagramPacket outBound;
        DatagramPacket inBound;
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

        //Sets the filename to a variable
        String fileName = args[2];

        //If it�s a PUT input from user that is to upload a file to server
        if (command.equals("PUT")) {
            try {
                tftp_server = InetAddress.getByName(host);
                socket = new DatagramSocket();
            }
            catch (IOException ex) {
                System.err.println(ex);
                System.exit(1);
            }

            File file = new File(fileName);

            try {

                FileInputStream input = new FileInputStream(file);

                byte fileContent[] = new byte[(int) file.length()];

                input.read(fileContent);

                //Send WRQ
                request = createData(OP_WRQ, fileName, MODE_NETASCII).toByteArray();
                outBound = new DatagramPacket(request, request.length, tftp_server, DEFAULT_PORT);
                socket.send(outBound);

                //Receive ACK from server
                buffer = new byte[4];
                inBound = new DatagramPacket(buffer, buffer.length, tftp_server, socket.getLocalPort());
                socket.receive(inBound);

                //Write data package
                byte[] data = createData(OP_WRQ, fileName, MODE_NETASCII).toByteArray();

                System.out.println("Size of data: " + data.length);
                System.out.println("Size of fileContent: " + fileContent.length);

                byte[] datatemp = new byte[fileContent.length + data.length];
                System.arraycopy(data, 0, datatemp, 0, data.length);
                System.arraycopy(fileContent, 0, datatemp, data.length, fileContent.length);

                for (int i = 0; i < buffer.length; i++)
                    System.out.println(buffer[i]);

                System.out.println("Size of datatemp: " + datatemp.length);

                byte[] dataSmu = new byte[fileContent.length+4];
                dataSmu[0] = 0;
                dataSmu[1] = 3;
                dataSmu[2] = 0;
                dataSmu[3] = 1;
                System.arraycopy(fileContent,0, dataSmu, 4, fileContent.length);

                for (int i = 0; i < dataSmu.length; i++)
                    System.out.println(dataSmu[i]);
                
                //DatagramPacket packet = new DatagramPacket(data,data.length,tftp_server,DEFAULT_PORT);
                DatagramPacket packet = new DatagramPacket(dataSmu,dataSmu.length,tftp_server,DEFAULT_PORT);
                socket.send(packet);

                socket.close();

                //Getting the first 4 characters from the bufffer
                byte[] opCode = {buffer[0], buffer[1]};

//                if(opCode[1] == OP_ACK){
//                    socket.send(packet);
//                    //System.out.println("inin");
//                    socket.close();
//                }
//                else{
//                    //System.exit(1);
//                    //System.out.println("outout");
//                    socket.close();
//                }
                input.close();

            }
            catch (IOException ex) {
                System.err.println(ex);
            }
        }


        //If it�s a GET input from user that is to download a file from server
        if (command.equals("GET")) {

            try {
                tftp_server = InetAddress.getByName(host);
                socket = new DatagramSocket();
            }
            catch (IOException ex) {
                System.err.println(ex);
                System.exit(1);
            }

            try {

                request = createData(OP_RRQ, fileName, MODE_BINARY).toByteArray();
                outBound = new DatagramPacket(request, request.length, tftp_server, DEFAULT_PORT);
                socket.send(outBound);

                do {
                    buffer = new byte[PACKET_SIZE];
                    inBound = new DatagramPacket(buffer, buffer.length, tftp_server, socket.getLocalPort());
                    socket.receive(inBound);

                    //Getting the first 4 characters from the bufffer
                    byte[] opCode = {buffer[0], buffer[1]};

                    // Check to see if we received a data packet
                    if (opCode[1] == OP_ERROR) {
                        String errorCode = new String(buffer, 3, 1);
                        String errorText = new String(buffer, 4, inBound.getLength() - 4);
                        System.err.println("Error: " + errorCode + " " + errorText);
                    }

                    if (opCode[1] == OP_DATAPACKET) {
                        // Check for the packets block number
                        byte[] UDPBlockNr = {buffer[2], buffer[3]};

                        DataOutputStream dos = new DataOutputStream(byteOut);
                        dos.write(inBound.getData(), 4, inBound.getLength() - 4);

                        byte[] ACK = {0, OP_ACK, UDPBlockNr[0], UDPBlockNr[1]};

                        DatagramPacket ack = new DatagramPacket(ACK, ACK.length, tftp_server, inBound.getPort());
                        socket.send(ack);
                    }
                } while (!isLastPacket(inBound));
                writeFile(byteOut, fileName);
            }
            catch (IOException ex) {
                System.err.println(ex);
            }
        }
    }

    private static void writeFile(ByteArrayOutputStream toFile, String fileName) {
        try {
            FileOutputStream output;
            File file;
            file = new File(fileName);
            output = new FileOutputStream(file);
            output.write(toFile.toByteArray());
            output.close();
        }
        catch (FileNotFoundException ex) {
            System.out.println("FileNotFoundException: " + ex);
        }
        catch (IOException ioe) {
            System.out.println("IOException: " + ioe);
        }

    }

    private static boolean isLastPacket(DatagramPacket pakki) {
        if (pakki.getLength() < 512)
            return true;
        else
            return false;
    }

    //Create data for reguestType (RRQ/WRQ), Filename, Transfermode (ASCII/BINARY), 
    private static ByteArrayOutputStream createData(int requestType, String fileName, int transferMode) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeShort(requestType);
        dos.writeBytes(fileName);
        dos.writeBytes("\0");
        if (transferMode == MODE_NETASCII) {
            dos.writeBytes("netascii");
        } else if (transferMode == MODE_BINARY) {
            dos.writeBytes("octet");
        }
        dos.writeBytes("\0");
        return baos;
    }

    private static void readData(byte[] array) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(array);
        DataInputStream dis = new DataInputStream(bais);

        int opcode = dis.readShort();
        String file = new String();
        String mode = new String();

        char ch; // You could also use a BufferedReader instead of these loops.
        while ((ch = dis.readChar()) != '\0') {
            file += ch;
        }
        while ((ch = dis.readChar()) != '\0') {
            mode += ch;
        }
        System.out.println("Opcode: " + opcode);
        System.out.println("File: " + file);
        System.out.println("Mode: " + mode);
    }
}