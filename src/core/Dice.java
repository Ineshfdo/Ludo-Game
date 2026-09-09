package core;

import java.util.Random;

public class Dice {
    private static Dice instance;
    private Random random;

    private Dice() {
        random = new Random(6L);
    }

    // Initializes the dice on the first call.
    public static Dice getInstance() {
        if (instance == null) {
            instance = new Dice();
        }
        return instance;
    }

    // Generates a random dice roll.
    // Return An integer between 1 and 6

    public int roll() {
        return random.nextInt(6) + 1;
    }
}
