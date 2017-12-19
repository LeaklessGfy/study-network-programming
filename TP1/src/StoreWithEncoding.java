import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

public class StoreWithEncoding {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            throw new IllegalStateException("Bad usage: p.java [CHARSET] [OUTPUT]");
        }

        Charset charset = Charset.forName(args[0]);
        Path path = Paths.get(args[1]);
        BufferedWriter bOut = Files.newBufferedWriter(
                path,
                charset,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                bOut.write(input);
                bOut.newLine();
            }
        }

        bOut.close();
    }
}
