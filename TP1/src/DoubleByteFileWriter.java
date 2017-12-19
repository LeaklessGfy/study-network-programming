import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class DoubleByteFileWriter {
    private static final int BUFFER_SIZE = 10;
    private static final int CHAR = 65;

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            throw new IllegalStateException("Bad usage: p.java [INPUT] [OUTPUT]");
        }

        ByteChannel in = Files.newByteChannel(Paths.get(args[0]), StandardOpenOption.READ);
        ByteChannel out = Files.newByteChannel(
                Paths.get(args[1]),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );
        ByteBuffer bIn = ByteBuffer.allocate(BUFFER_SIZE);

        int nb = 0;
        while ((nb = in.read(bIn)) != -1) {
            System.out.println(nb + " bytes read");
            ByteBuffer bOut = ByteBuffer.allocate(nb * 2);
            bIn.flip();
            while (bIn.hasRemaining()) {
                byte b = bIn.get();
                bOut.put(b);
                if (b == CHAR) {
                    bOut.put(b);
                }
            }
            bOut.flip();
            System.out.println(out.write(bOut) + " bytes write");
            bIn.clear();
            bOut.clear();
        }

        in.close();
        out.close();
    }
}
