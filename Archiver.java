package ShannonFano;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Archiver {
    private ShannonFano.Symbol[] symbols;
    final private Map<Character, String> codewords;

    public Archiver() {
        codewords = new HashMap<>();
    }

    private void shannonFanoAlgo(int begin, int end) {
        if(begin == end) {
            return ;
        } else if (end - begin == 1) {
            if (codewords.containsKey(symbols[begin].getCharacter())) {
                codewords.put(symbols[begin].getCharacter(), codewords.get(symbols[begin].getCharacter()) + "0");
            } else {
                codewords.put(symbols[end].getCharacter(), "0");
            }
            if (codewords.containsKey(symbols[end].getCharacter())) {
                codewords.put(symbols[end].getCharacter(),  codewords.get(symbols[end].getCharacter()) + "1");
            } else {
                codewords.put(symbols[end].getCharacter(), "1");
            }
        } else {
            int i;
            double prob = 0;
            for(i = begin; i <= end; i++) {
                prob += symbols[i].getProbability();
            }
            double tempPr = 0;
            int middle = -1;
            for( i = begin; i <= end; i++) {
                tempPr += symbols[i].getProbability();
                if(tempPr <= prob /2) {
                    if (codewords.containsKey(symbols[i].getCharacter())) {
                        codewords.put(symbols[i].getCharacter(), codewords.get(symbols[i].getCharacter()) + "0");
                    } else {
                        codewords.put(symbols[i].getCharacter(), "0");
                    }
                } else {
                    if (codewords.containsKey(symbols[i].getCharacter())) {
                        codewords.put(symbols[i].getCharacter(), codewords.get(symbols[i].getCharacter()) + "1");
                    } else {
                        codewords.put(symbols[i].getCharacter(), "1");
                    }
                    if( middle < 0 ) {
                        middle = i;
                    }
                }
            }
            if(middle < 0) {
                middle = begin + 1;
            }

            shannonFanoAlgo(begin, middle - 1);
            shannonFanoAlgo(middle, end);
        }
    }

    public void encodeFile(String inputFilename, String outputFilename) {
        int totalAmount = 0;
        Map<Character, Integer> counts = new HashMap<>();

        try(FileInputStream fis = new FileInputStream(inputFilename);
            InputStreamReader isr = new InputStreamReader(fis, "UTF8");
            BufferedReader reader = new BufferedReader(isr)) {
            int c;
            while((c = reader.read()) != -1) {
                char ch = (char)c;
                if(ch == '\n') {
                    ch = '₴';
                }
                if((int)ch == 13) {
                    continue;
                }
                if (counts.containsKey(ch)) {
                    counts.put(ch, counts.get(ch) + 1);
                } else {
                    counts.put(ch, 1);
                }
                totalAmount++;
            }
        } catch(IOException ex){
            System.out.println(ex.getMessage());
        }

        int symAmount = counts.size();
        symbols = new ShannonFano.Symbol[symAmount];

        int i = 0;
        for (Map.Entry<Character, Integer> entry : counts.entrySet()) {
            symbols[i] = new ShannonFano.Symbol(entry.getKey(),(double)entry.getValue() / totalAmount );
            i++;
        }
        Arrays.sort(symbols);

        shannonFanoAlgo(0 , symAmount -1) ;

        try (FileOutputStream fos = new FileOutputStream(new File(outputFilename));
             Writer myWriter = new OutputStreamWriter(fos, "UTF8")) {
            myWriter.write(totalAmount + "\n");
            for(i = 0; i < symAmount; i++) {
                myWriter.write(symbols[i].getCharacter() + "₴`" + codewords.get(symbols[i].getCharacter()) + "~:");
            }
            myWriter.write("\n");
            try(FileInputStream fis = new FileInputStream(inputFilename);
                InputStreamReader isr = new InputStreamReader(fis, "UTF8");
                BufferedReader reader = new BufferedReader(isr)) {
                int c;
                int shift = 1;
                byte writtenByte = (byte)0;
                byte out;
                while((c = reader.read()) != -1) {
                    char ch = (char)c;
                    if(ch == '\n') {
                        ch = '₴';
                    }
                    if((int)ch == 13) {
                        continue;
                    }
                    for (char symbol: codewords.get(ch).toCharArray()) {
                        out = (byte)(symbol == '0' ? 0:1);
                        writtenByte |= out << (8-shift);
                        shift++;
                        if(shift > 8) {
                            myWriter.write(writtenByte);
                            shift = 1;
                            writtenByte = 0;
                        }
                    }
                }
                if (shift != 1) {
                    myWriter.write(writtenByte);
                }
            } catch(IOException ex){

                System.out.println(ex.getMessage());
            }
            myWriter.write("\n");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void decodeFile(String inputFilename, String outputFilename) {
        int totalAmount;
        File file = new File(inputFilename);
        try (FileOutputStream fos = new FileOutputStream(outputFilename);
             Writer myWriter = new OutputStreamWriter(fos, "UTF8")) {
            try (FileInputStream fis = new FileInputStream(file);
                 InputStreamReader isr = new InputStreamReader(fis, "UTF8");
                 BufferedReader br = new BufferedReader(isr)) {
                String line;
                String[] st;
                String[] st_key;
                if ((line = br.readLine()) != null) {
                    totalAmount = Integer.parseInt(line);
                    line = br.readLine();
                    st = line.split("~:");
                    for (String s : st) {
                        st_key = s.split("₴`");
                        codewords.put(st_key[0].toCharArray()[0], st_key[1]);
                    }

                    StringBuilder symbolCode = new StringBuilder();
                    int c;
                    int totalCount = 0;
                    while ((c = br.read()) != -1) {
                        char ch = (char)c;
                        String byteSt = String.format("%8s", Integer.toBinaryString(ch & 0xFF)).replace(' ', '0');
                        for(char addedCode: byteSt.toCharArray()) {
                            if(totalCount != totalAmount) {
                                symbolCode.append(addedCode);
                                for (Map.Entry<Character, String> entry : codewords.entrySet()) {
                                    if (entry.getValue().equals(symbolCode.toString())) {
                                        symbolCode = new StringBuilder();
                                        if(entry.getKey() == '₴') {
                                            myWriter.write('\n');
                                        } else {
                                            myWriter.write(entry.getKey());
                                        }
                                        totalCount++;
                                        break;
                                    }
                                }
                            } else {
                                break;
                            }
                        }
                    }
                    myWriter.write("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}

