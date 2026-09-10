package game.states;

/**
 * Helper factory to convert a state name string back to a PieceState object.
 * This is only needed in simulation loops inside MovementManager that track
 * state transitions using local String variables for performance reasons.
 */
public class StateFactory {
    public static PieceState fromName(String stateName) {
        switch (stateName) {
            case "BASE":
                return new BaseState();
            case "STANDARD":
                return new StandardPathState();
            case "HOME_STRAIGHT":
                return new HomeStraightState();
            case "HOME":
                return new HomeState();
            default:
                return new StandardPathState();
        }
    }
}
