package players;

// Represents a single playing piece on the board.
// Encapsulates all state related to a piece to maintain high cohesion.

public class LudoPiece {
    private String id;
    private PlayerColor color;
    
    // States: "BASE", "STANDARD", "HOME_STRAIGHT", "HOME"
    private String state; 
    
    private int position;
    
    // The 3 explicit directions:
    private boolean xChoiceDirectionClockwise; // The direction chosen at the X cell
    private boolean combinedBlockDirectionClockwise; // The shared direction when moving as a block
    private boolean breakBlockDirectionClockwise; // The direction to resume if the block breaks

    private int approachPasses;
    private int captures;

    // Alpha Aura Effects (ENERGIZED, SICK)
    private String individualAuraEffect;
    private int individualAuraRoundsRemaining;
    private String blockAuraEffect;
    private int blockAuraRoundsRemaining;
    
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
        this.individualAuraEffect = "NONE";
        this.individualAuraRoundsRemaining = 0;
        this.blockAuraEffect = "NONE";
        this.blockAuraRoundsRemaining = 0;
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
        this.individualAuraEffect = "NONE";
        this.individualAuraRoundsRemaining = 0;
        this.blockAuraEffect = "NONE";
        this.blockAuraRoundsRemaining = 0;
        this.betaFreezeRoundsRemaining = 0;
    }

    public String getIndividualAuraEffect() {
        return individualAuraEffect;
    }

    public void setIndividualAuraEffect(String individualAuraEffect) {
        this.individualAuraEffect = individualAuraEffect;
    }

    public int getIndividualAuraRoundsRemaining() {
        return individualAuraRoundsRemaining;
    }

    public void setIndividualAuraRoundsRemaining(int individualAuraRoundsRemaining) {
        this.individualAuraRoundsRemaining = individualAuraRoundsRemaining;
    }

    public String getBlockAuraEffect() {
        return blockAuraEffect;
    }

    public void setBlockAuraEffect(String blockAuraEffect) {
        this.blockAuraEffect = blockAuraEffect;
    }

    public int getBlockAuraRoundsRemaining() {
        return blockAuraRoundsRemaining;
    }

    public void setBlockAuraRoundsRemaining(int blockAuraRoundsRemaining) {
        this.blockAuraRoundsRemaining = blockAuraRoundsRemaining;
    }

    public int getBetaFreezeRoundsRemaining() {
        return betaFreezeRoundsRemaining;
    }

    public void setBetaFreezeRoundsRemaining(int betaFreezeRoundsRemaining) {
        this.betaFreezeRoundsRemaining = betaFreezeRoundsRemaining;
    }
}