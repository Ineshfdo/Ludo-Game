package players;

// Represents a single playing piece on the board.

public class LudoPiece {
    private String id;
    private PlayerColor color;

    // States: "BASE", "STANDARD", "HOME_STRAIGHT", "HOME"
    private String state;

    private int position;

    // The 3 explicit directions:
    private boolean xChoiceDirectionClockwise;
    private boolean combinedBlockDirectionClockwise;
    private boolean breakBlockDirectionClockwise;

    private int approachPasses;
    private int captures;

    // Alpha Effects (ENERGIZED, SICK)
    private String individualAlphaEffect;
    private int individualAlphaRoundsRemaining;
    private String blockAlphaEffect;
    private int blockAlphaRoundsRemaining;

    // Beta Effect
    private int betaFreezeRoundsRemaining;

    public static final int POSITION_BASE = -1;
    public static final int POSITION_REMOVED = -2;

    public LudoPiece(String id, PlayerColor color) {
        this.id = id;
        this.color = color;
        this.state = "BASE";
        this.position = POSITION_BASE;
        this.xChoiceDirectionClockwise = true;
        this.combinedBlockDirectionClockwise = true;
        this.breakBlockDirectionClockwise = true;
        this.approachPasses = 0;
        this.captures = 0;
        this.individualAlphaEffect = "NONE";
        this.individualAlphaRoundsRemaining = 0;
        this.blockAlphaEffect = "NONE";
        this.blockAlphaRoundsRemaining = 0;
        this.betaFreezeRoundsRemaining = 0;
    }

    public String getId() {
        return id;
    }

    public PlayerColor getColor() {
        return color;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isXChoiceDirectionClockwise() {
        return xChoiceDirectionClockwise;
    }

    public void setXChoiceDirectionClockwise(boolean clockwise) {
        this.xChoiceDirectionClockwise = clockwise;
        // Automatically sync the break direction to remember the original X choice
        this.breakBlockDirectionClockwise = clockwise;
    }

    public boolean isCombinedBlockDirectionClockwise() {
        return combinedBlockDirectionClockwise;
    }

    public void setCombinedBlockDirectionClockwise(boolean clockwise) {
        this.combinedBlockDirectionClockwise = clockwise;
    }

    public boolean isBreakBlockDirectionClockwise() {
        return breakBlockDirectionClockwise;
    }

    public void setBreakBlockDirectionClockwise(boolean clockwise) {
        this.breakBlockDirectionClockwise = clockwise;
    }

    public int getApproachPasses() {
        return approachPasses;
    }

    public void setApproachPasses(int approachPasses) {
        this.approachPasses = approachPasses;
    }

    public int getCaptures() {
        return captures;
    }

    public void setCaptures(int captures) {
        this.captures = captures;
    }

    public void incrementCaptures() {
        this.captures++;
    }

    public void resetCaptures() {
        this.captures = 0;
    }

    public void resetToDefault() {
        this.state = "BASE";
        this.position = POSITION_BASE;
        this.xChoiceDirectionClockwise = true;
        this.combinedBlockDirectionClockwise = true;
        this.breakBlockDirectionClockwise = true;
        this.approachPasses = 0;
        this.captures = 0;
        this.individualAlphaEffect = "NONE";
        this.individualAlphaRoundsRemaining = 0;
        this.blockAlphaEffect = "NONE";
        this.blockAlphaRoundsRemaining = 0;
        this.betaFreezeRoundsRemaining = 0;
    }

    public String getIndividualAlphaEffect() {
        return individualAlphaEffect;
    }

    public void setIndividualAlphaEffect(String individualAlphaEffect) {
        this.individualAlphaEffect = individualAlphaEffect;
    }

    public int getIndividualAlphaRoundsRemaining() {
        return individualAlphaRoundsRemaining;
    }

    public void setIndividualAlphaRoundsRemaining(int individualAlphaRoundsRemaining) {
        this.individualAlphaRoundsRemaining = individualAlphaRoundsRemaining;
    }

    public String getBlockAlphaEffect() {
        return blockAlphaEffect;
    }

    public void setBlockAlphaEffect(String blockAlphaEffect) {
        this.blockAlphaEffect = blockAlphaEffect;
    }

    public int getBlockAlphaRoundsRemaining() {
        return blockAlphaRoundsRemaining;
    }

    public void setBlockAlphaRoundsRemaining(int blockAlphaRoundsRemaining) {
        this.blockAlphaRoundsRemaining = blockAlphaRoundsRemaining;
    }

    public int getBetaFreezeRoundsRemaining() {
        return betaFreezeRoundsRemaining;
    }

    public void setBetaFreezeRoundsRemaining(int betaFreezeRoundsRemaining) {
        this.betaFreezeRoundsRemaining = betaFreezeRoundsRemaining;
    }
}