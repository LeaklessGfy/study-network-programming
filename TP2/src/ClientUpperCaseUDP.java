import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

public class ClientUpperCaseUDP {
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            throw new IllegalStateException("Bad usage: ClientUpperCaseUDP.java [HOST] [PORT] [CHARSET]");
        }

        //SETUP
        String host = args[0];
        int port = Integer.valueOf(args[1]);
        Charset charset = Charset.forName(args[2]);
        SocketAddress address = new InetSocketAddress(host, port);
        DatagramChannel chan = DatagramChannel.open();

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                byte[] in = scanner.nextLine().getBytes(charset);
                ByteBuffer bIn = ByteBuffer.wrap(in);
                chan.send(bIn, address);

                byte[] out = new byte[in.length];
                ByteBuffer bOut = ByteBuffer.wrap(out);
                chan.receive(bOut);

                System.out.println(new String(out, 0, out.length, charset));
            }
        }

        chan.close();
    }
}
