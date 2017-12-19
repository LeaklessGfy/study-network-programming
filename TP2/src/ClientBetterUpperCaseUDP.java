import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

public class ClientBetterUpperCaseUDP {
	private static final int BUFFER_SIZE = 1024;
	private static final int INT_SIZE = 4;

    private static String decodeMessage(ByteBuffer buffer) {
        int size = buffer.getInt();
        byte[] charsetByte = new byte[size];
        buffer.get(charsetByte);

        byte[] msg = new byte[buffer.remaining()];
        buffer.get(msg);

        String charset = new String(charsetByte, 0, size);
        return new String(msg, 0, msg.length, Charset.forName(charset));
    }

    private static ByteBuffer encodeMessage(String msg, String charsetName) {
        byte[] charsetBytes = charsetName.getBytes();
        byte[] msgBytes = Charset.forName(charsetName).encode(msg).array();

        ByteBuffer bb = ByteBuffer.allocate(INT_SIZE + charsetBytes.length + msgBytes.length);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(charsetName.length());
        bb.order(ByteOrder.nativeOrder());
        bb.put(charsetBytes);
        bb.put(msgBytes);

        return bb;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            throw new IllegalStateException("Bad usage: ClientBetterUpperCaseUDP.java [HOST] [PORT] [CHARSET]");
        }

        //SETUP
        String host = args[0];
        int port = Integer.valueOf(args[1]);
        String charset = args[2];
        SocketAddress address = new InetSocketAddress(host, port);
        DatagramChannel chan = DatagramChannel.open();
        ByteBuffer bOut = ByteBuffer.allocateDirect(BUFFER_SIZE);

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                ByteBuffer bIn = encodeMessage(scanner.nextLine(), charset);
                bIn.flip();
                chan.send(bIn, address);

                bOut.clear();
                chan.receive(bOut);
                bOut.flip();

                System.out.println(decodeMessage(bOut));
            }
        }

        chan.close();
    }

}
