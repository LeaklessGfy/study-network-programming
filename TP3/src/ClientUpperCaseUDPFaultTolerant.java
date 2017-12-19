import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ClientUpperCaseUDPFaultTolerant {
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            throw new IllegalStateException("Bad usage: ClientUpperCaseUDPFaultTolerant.java [HOST] [PORT] [CHARSET]");
        }

        //SETUP
        String host = args[0];
        int port = Integer.valueOf(args[1]);
        Charset charset = Charset.forName(args[2]);
        SocketAddress address = new InetSocketAddress(host, port);
        DatagramChannel chan = DatagramChannel.open();
        ArrayBlockingQueue<String> bq = new ArrayBlockingQueue<>(100);

        new Thread(() -> {
            ByteBuffer bOut = ByteBuffer.allocate(BUFFER_SIZE);

            for (;;) {
                try {
                    chan.receive(bOut);
                    String str = new String(bOut.array(), 0, BUFFER_SIZE - bOut.remaining(), charset);
                    bOut.clear();
                    bq.offer(str);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    return;
                }
            }
        }).start();

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                byte[] in = scanner.nextLine().getBytes(charset);
                ByteBuffer bIn = ByteBuffer.wrap(in);
                chan.send(bIn, address);

                try {
                    String str;
                    while ((str = bq.poll(1, TimeUnit.SECONDS)) == null) {
                        System.out.println("\t** RETRY **");
                        bIn.rewind();
                        chan.send(bIn, address);
                    }
                    System.out.println("RESPONSE : " + str);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                    return;
                }
            }
        }

        chan.close();
    }
}
