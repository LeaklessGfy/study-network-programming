package fr.upem.net.udp;

import org.omg.CORBA.TIMEOUT;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class BurstRequester extends Requester {
    private static final int TIMEOUT = 300;
    private final ArrayBlockingQueue<ByteBuffer> responses = new ArrayBlockingQueue<>(10);

    private final Thread listener = new Thread(() -> {
        while (!Thread.interrupted()) {
            try {
                ByteBuffer bb = ByteBuffer.allocate(BUFFER_SIZE);
                receive(bb);
                responses.offer(bb);
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
        ArrayList<String> upperList = new ArrayList<>(lowerList.size());
        BitSet set = new BitSet(lowerList.size());

        for (int i = 0; i < lowerList.size(); i++) {
            int index = i;
            new Thread(() -> {
                try {
                    ByteBuffer req = createPacket(index, lowerList.get(index), charset);
                    req.flip();
                    send(req);

                    ByteBuffer bb = ByteBuffer.allocate(BUFFER_SIZE);
                    receive(bb);
                    bb.flip();
                    upperList.add(index, decodeString(bb));
                    set.set(index);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            //sleep TIMEOUT
        }

        //setStreamCheck

        return upperList;
    }
}
