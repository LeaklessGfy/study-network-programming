import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

public class StoreWithByteOrder {
    private static final int LONG_SIZE = 8;

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            throw new IllegalStateException("Bad usage: p.java [BYTEORDER: LE, BE] [OUTPUT]");
        }

        Path path = Paths.get(args[1]);
        ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
        if (args[0].equals("LE")) {
            byteOrder = ByteOrder.LITTLE_ENDIAN;
        } else if (args[0].equals("BE")) {
            byteOrder = ByteOrder.BIG_ENDIAN;
        }

        ByteChannel out = Files.newByteChannel(
                path,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );
        ByteBuffer bOut = ByteBuffer.allocate(LONG_SIZE);
        bOut.order(byteOrder);

        try (Scanner scanner = new Scanner(System.in)) {
            Long l = scanner.nextLong();
            bOut.putLong(l);
            bOut.flip();
            out.write(bOut);
            bOut.clear();
        }

        out.close();
    }
}
