import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileInspector {
	private final static int BUFFER_SIZE = 8;
	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			throw new IllegalStateException("Bad usage: p.java [INPUT]");
		}
		ByteChannel in = Files.newByteChannel(Paths.get(args[0]), StandardOpenOption.READ);
		ByteBuffer bb = ByteBuffer.allocate(BUFFER_SIZE);
		int nb = 0;
		while ((nb = in.read(bb)) != -1) {
			System.out.println(nb + " bytes read");
			bb.flip();
			while (bb.hasRemaining()) {
				byte b = bb.get();
				System.out.println("octet :" + b + " (char : " + (char)b + ")");
			}
			bb.clear();
		}
	    in.close();
	}
}
