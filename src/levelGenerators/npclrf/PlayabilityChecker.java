package levelGenerators.npclrf;

public final class PlayabilityChecker {
    private PlayabilityChecker() {} // Class is not instantiable

    /**
     * Fail column is zero-based, or -1 when no failure column is available.
     */
    public record Result(boolean playable, int failColumn, String reason) {
    }

    /**
     * @throws UnsupportedOperationException until the reachability checker is implemented
     */
    public static Result check(String level) {
        throw new UnsupportedOperationException("Playability checking is not implemented yet.");
    }
}
