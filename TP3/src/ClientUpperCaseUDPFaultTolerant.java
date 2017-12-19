import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.channels.DatagramChannel;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ClientUpperCaseUDPFaultTolerant {
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            throw new IllegalStateException();
        }

        InetAddress address = InetAddress.getByName(args[0]);
        int port = Integer.parseInt(args[1]);
        ArrayBlockingQueue<DatagramPacket> bq = new ArrayBlockingQueue<>(100);

        new Thread(() -> {
            try {
                Scanner scanner = new Scanner(System.in);
                DatagramSocket socket = new DatagramSocket();
                while (scanner.hasNextLine()) {
                    byte[] in = scanner.nextLine().getBytes();
                    DatagramPacket request = new DatagramPacket(in, in.length, address, port);
                    socket.send(request);

                    byte[] out = new byte[255];
                    DatagramPacket response = new DatagramPacket(out, out.length);
                    socket.receive(response);
                    bq.offer(response);
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }).start();

        new Thread(() -> {
            try {
                DatagramPacket r = bq.poll(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }).start();
    }
}
