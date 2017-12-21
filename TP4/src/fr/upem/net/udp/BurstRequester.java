package fr.upem.net.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class BurstRequester extends Requester {
    private static final int TIMEOUT = 300;

    private final ArrayBlockingQueue<ByteBuffer> queue = new ArrayBlockingQueue<>(10);
    private final Thread listener = new Thread(() -> {
        while (!Thread.interrupted()) {
            try {
                ByteBuffer res = ByteBuffer.allocate(BUFFER_SIZE);
                receive(res);
                res.flip();
                queue.offer(res);
            } catch (IOException e) {
                return;
            }
        }
    });

    BurstRequester(InetSocketAddress serverAddress) {
        super(serverAddress);
    }

    @Override
    public void open() throws IOException {
        super.open();
        listener.start();
    }

    @Override
    public void close() throws IOException {
        super.close();
        listener.interrupt();
    }

    @Override
    public List<String> toUpperCase(List<String> lowerList, Charset charset) throws IOException, InterruptedException {
        String[] upperArray = new String[lowerList.size()];
        BitSet set = new BitSet(lowerList.size());

        new Thread(() -> {
            for (int i = 0; i < lowerList.size(); i++) {
                try {
                    sendFor(i, lowerList, charset);
                    Thread.sleep(TIMEOUT);
                } catch (IOException | InterruptedException e) {
                    return;
                }
            }
        }).start();

        for (int i = 0; i < lowerList.size(); i++) {
            ByteBuffer res = queue.poll(TIMEOUT * 2, TimeUnit.MILLISECONDS);
            if (res == null) {
                System.out.println("\tTIMEOUT *" + i);
                sendFor(i, lowerList, charset);
                i--;
                continue;
            }

            int id = (int) res.getLong();
            if (id != i) {
                if (upperArray[i] == null) {
                    System.out.println("\tRETRY *" + i);
                    sendFor(i, lowerList, charset);
                    i--;
                } else {
                    continue;
                }
            }

            System.out.println("Handle : " + id + " / " + i);

            set.set(id);
            res.rewind();
            upperArray[id] = decodeString(res);
        }

        return Arrays.asList(upperArray);
    }

    private void sendFor(int index, List<String> lowerList, Charset charset) throws IOException {
        ByteBuffer req = createPacket(index, lowerList.get(index), charset);
        req.flip();
        send(req);
    }
}
