package fr.upem.net.udp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ClientIdBetterUpperCaseUDP {
    private static ArrayList<String> readLinesFromFile(String filename, Charset charset) throws IOException {
        return new ArrayList<>(Files.readAllLines(Paths.get(filename), charset));
    }

    private static void writeLinesToFile(List<String> lines, String filename, Charset charset) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(filename), charset)) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        }
    }

    private static void checkFiles(String lowercaseFile, String uppercaseFile, Charset charset) throws IOException {
        ArrayList<String> lcLines = readLinesFromFile(lowercaseFile, charset);
        ArrayList<String> ucLines = readLinesFromFile(uppercaseFile, charset);

        if (lcLines.size()!=ucLines.size()){
            throw new IllegalStateException("The two files have a different number of lines.");
        }

        int index = 0;
        for (String lowerLine : lcLines) {
            if (!lowerLine.toUpperCase().equals(ucLines.get(index))) {
                throw new IllegalStateException("Problem on line " + index);
            }
            index++;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 4) {
            throw new IllegalStateException("Bad usage: ClientIdBetterUpperCaseUDP.java [CHARSET] [FILENAME] [HOST] [PORT]");
        }

        Charset charset = Charset.forName(args[0]);
        String filename = args[1];
        String host = args[2];
        int port = Integer.valueOf(args[3]);
        InetSocketAddress address = new InetSocketAddress(host, port);

        List<String> lines = readLinesFromFile(filename, charset);

        try (Requester requester = new BurstRequester(address)) {
            requester.open();
            List<String> linesUpperCase = requester.toUpperCase(lines, charset);
            writeLinesToFile(linesUpperCase,filename + ".UPPERCASE", charset);
            checkFiles(filename,filename + ".UPPERCASE", charset);
        }

        System.out.println("Everything is ok!");
    }
}