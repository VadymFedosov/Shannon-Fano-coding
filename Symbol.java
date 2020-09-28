package ShannonFano;

public class Symbol implements Comparable<Symbol>{
    final private char character;
    final private double probability;

    public Symbol(char character, double probability) {
        this.character = character;
        this.probability = probability;
    }

    public char getCharacter() {
        return character;
    }

    public double getProbability() {
        return probability;
    }

    @Override
    public int compareTo(Symbol other) {
        if (this.probability < other.probability ) {
            return 1;
        } else if (this.probability > other.probability){
            return -1;
        }
        return 0;
    }
}
