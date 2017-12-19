import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ClientBetterUpperCaseUDPFaultTolerant {
    private static final int BUFFER_SIZE = 1024;
    private static final int INT_SIZE = 4;

    private static String decodeMessage(ByteBuffer buffer) {
        int length = BUFFER_SIZE - buffer.remaining();
        buffer.rewind();

        int size = buffer.getInt();
        byte[] charsetByte = new byte[size];
        buffer.get(charsetByte);

        byte[] msg = new byte[length - INT_SIZE - size];
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

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 3) {
            throw new IllegalStateException("Bad usage: ClientBetterUpperCaseUDP.java [HOST] [PORT] [CHARSET]");
        }

        //SETUP
        String host = args[0];
        int port = Integer.valueOf(args[1]);
        String charset = args[2];
        SocketAddress address = new InetSocketAddress(host, port);
        DatagramChannel chan = DatagramChannel.open();
        ArrayBlockingQueue<String> bq = new ArrayBlockingQueue<>(100);

        new Thread(() -> {
            ByteBuffer bOut = ByteBuffer.allocate(BUFFER_SIZE);

            for (;;) {
                try {
                    chan.receive(bOut);
                    String str = decodeMessage(bOut);
                    bq.offer(str);
                    bOut.clear();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }).start();

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                ByteBuffer bIn = encodeMessage(scanner.nextLine(), charset);
                bIn.flip();
                chan.send(bIn, address);

                String str;
                while ((str = bq.poll(1, TimeUnit.SECONDS)) == null) {
                    System.out.println("\t** RETRY **");
                    bIn.rewind();
                    chan.send(bIn, address);
                }
                System.out.println("RESPONSE : " + str);
            }
        }

        chan.close();
    }
}
