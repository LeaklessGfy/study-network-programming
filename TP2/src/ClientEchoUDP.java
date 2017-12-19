import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class ClientEchoUDP {
    private static void printSEND(DatagramChannel chan, byte[] data) throws IOException {
        System.out.println("* SEND");
        System.out.println("\tfrom: " + chan.getLocalAddress());
        System.out.println("\tto: " + chan.getRemoteAddress());
        System.out.println("\tdata: " + new String(data) + ", length: " + data.length);
    }

    private static void printRECEIVE(DatagramChannel chan, byte[] data) throws IOException {
        System.out.println("* RECEIVE");
        System.out.println("\tfrom: " + chan.getRemoteAddress());
        System.out.println("\tto: " + chan.getLocalAddress());
        System.out.println("\tdata: " + new String(data) + ", length: " + data.length);
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            throw new IllegalStateException("Bad usage: ClientEchoUDP.java [HOST] [PORT] [MSG]");
        }

        //SETUP
        String host = args[0];
        int port = Integer.valueOf(args[1]);
        SocketAddress address = new InetSocketAddress(host, port);
        DatagramChannel chan = DatagramChannel.open();

        //SEND
        byte[] in = args[2].getBytes();
        ByteBuffer bIn = ByteBuffer.wrap(in);
        chan.send(bIn, address);
        printSEND(chan, in);

        System.out.print('\n');

        //RECEIVE
        byte[] out = new byte[in.length];
        ByteBuffer bOut = ByteBuffer.wrap(out);
        chan.receive(bOut);
        printRECEIVE(chan, out);

        chan.close();
    }
}
