package fr.upem.net.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class OneByOneRequester extends Requester {
	private static final int TIMEOUT = 300;

	private final BlockingQueue<ByteBuffer> queue = new ArrayBlockingQueue<>(1);
	private final Thread listener = new Thread(() -> {
		while (!Thread.interrupted()) {
			try {
				ByteBuffer bb = ByteBuffer.allocate(BUFFER_SIZE);
				receive(bb);
				queue.offer(bb);
			} catch (IOException e) {
				return;
			}
		}
	});

	OneByOneRequester(InetSocketAddress serverAddress) {
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

        long index = 0;
        for (String msg : lowerList) {
            upperList.add(toUpperCase(index, msg, charset));
            index++;
        }

        return upperList;
    }

	private String toUpperCase(long index, String msg, Charset charset) throws IOException, InterruptedException {
		ByteBuffer req = createPacket(index, msg, charset);
		ByteBuffer res = null;
		req.flip();
		send(req);
		while ((res = queue.poll(TIMEOUT, TimeUnit.SECONDS)) == null) {
			System.out.println("Retry");
			req.rewind();
			send(req);
		}
		res.flip();
		return decodeString(res);
	}
}