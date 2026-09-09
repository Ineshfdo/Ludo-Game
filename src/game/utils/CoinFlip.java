package game.utils;

import java.util.Random;

public class CoinFlip {
    private static CoinFlip instance;
    private Random random;

    private CoinFlip() {
        random = new Random();
    }

    // Initializes the coin flip on the first call.
    public static CoinFlip getInstance() {
        if (instance == null) {
            instance = new CoinFlip();
        }
        return instance;
    }

    // Sets a fixed seed for the random number generator.
    // Ensures sequence of tosses is predictable for automated testing.
    // param seed The seed value

    public void setSeed(long seed) {
        random = new Random(seed);
    }

    // Generates a deterministic coin toss.
    // Return true for Heads (Clockwise), false for Tails (Counter-clockwise).

    public boolean flip() {
        return random.nextBoolean();
    }
}
