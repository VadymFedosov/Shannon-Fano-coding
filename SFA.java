package ShannonFano;

public class SFA {
    public static void main(String[] args) {
        String inputFilename = args[1];
        Archiver coder = new Archiver();
        if (args[0].equals("-c")) {
            String outputFilename1 = "compressed.txt";
            coder.encodeFile(inputFilename, outputFilename1);
        } else if (args[0].equals("-d")) {
            String outputFilename = "decompressed.txt";
            coder.decodeFile(inputFilename, outputFilename);
        }
    }
}

