package fr.upem.net.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.util.List;

public abstract class Requester implements AutoCloseable {
    static final int BUFFER_SIZE = 1024;

    private DatagramChannel dc;
    private final InetSocketAddress serverAddress;

    Requester(InetSocketAddress serverAddress){
        this.serverAddress = serverAddress;
    }

    public void open() throws IOException {
        if (dc != null) {
            throw new IllegalStateException("Requester already opened.");
        }
        dc = DatagramChannel.open();
        dc.bind(null);
    }

    public void close() throws IOException {
        if (dc == null) {
            throw new IllegalStateException("Requester was never opened.");
        }
        dc.close();
    }

    static String decodeString(ByteBuffer bb) {
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.getLong();
        int size = bb.getInt();
        bb.order(ByteOrder.nativeOrder());

        byte[] charset = new byte[size];
        bb.get(charset);

        byte[] msg = new byte[bb.remaining()];
        bb.get(msg);

        return new String(msg, 0, msg.length, Charset.forName(new String(charset)));
    }

    static ByteBuffer createPacket(long requestNumber, String msg, Charset charset) {
        String charsetName = charset.name();
        ByteBuffer bb = ByteBuffer.allocate(BUFFER_SIZE);
        bb.putLong(requestNumber);
        bb.putInt(charsetName.length());
        bb.put(charsetName.getBytes());
        bb.put(charset.encode(msg));
        return bb;
    }

    void send(ByteBuffer buff) throws IOException {
        dc.send(buff, serverAddress);
    }

    void receive(ByteBuffer buff) throws IOException {
        dc.receive(buff);
    }

    public abstract List<String> toUpperCase(List<String> list,Charset cs) throws IOException, InterruptedException;
}
