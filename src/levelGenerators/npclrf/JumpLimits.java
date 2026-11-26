package levelGenerators.npclrf;

/**
 * Small Mario's jump limits, measured in the engine by {@link JumpProbe}, shared by the chunks and
 * the {@link PlayabilityChecker}.
 *
 * <p>The design limits assume a jump from a standstill on the last ledge tile, holding run and right.
 * That way no section ever depends on a running start, so the checker does not need to track
 * horizontal speed. Each design limit is the measurement minus {@link #MARGIN} tile.
 *
 * <p>Distances are in tiles. A gap is the number of empty columns between the takeoff ledge and the
 * landing ledge. A rise is how many rows higher the landing surface is (negative when it is lower).
 */
public final class JumpLimits {
    private JumpLimits() {} // Class is not instantiable

    public static final int MEASURED_MAX_RISE = 4; // Highest landing above takeoff surface, from any start
    public static final int MEASURED_STANDING_GAP = 5; // Widest level gap from standstill run
    public static final int MEASURED_RUNNING_GAP = 11; // Widest gap with long running start (for reference)
    public static final int MARGIN = 1; // Safety margin subtracted from every measurement
    public static final int MAX_RISE = MEASURED_MAX_RISE - MARGIN; // Highest step or ledge to jump onto
    public static final int MAX_GAP = MEASURED_STANDING_GAP - MARGIN; // Widest gap a section may place
    static final int TABLE_MIN_RISE = -6;

    /**
     * Measured widest standing gap for rises 4 down to -6; index 0 is rise {@link #MEASURED_MAX_RISE}.
     * Lower landings beyond the table use the last entry.
     */
    static final int[] MEASURED_STANDING_GAP_BY_RISE = {2, 3, 4, 5, 5, 6, 7, 7, 8, 9, 9};

    /**
     * Measured widest standing level gap under a solid ceiling, for 1 to 6 empty rows of headroom
     * above the ledges; index 0 is one row (just Mario's own row). Taller headroom uses the last entry.
     */
    static final int[] MEASURED_STANDING_GAP_BY_HEADROOM = {0, 1, 1, 3, 5, 5};

    /**
     * Widest gap, in empty columns, that a section may ask Mario to clear onto a landing {@code rise}
     * rows higher than the takeoff ledge (negative for a lower landing).
     *
     * @return the design gap limit, or -1 when the landing is too high to reach
     */
    public static int maxGap(int rise) {
        return gapLimit(rise, MEASURED_STANDING_GAP_BY_HEADROOM.length + Math.max(rise, 0), MARGIN);
    }

    /**
     * Like {@link #maxGap(int)}, but also limited by a ceiling: {@code headroom} is the number of empty
     * rows above the takeoff surface along the jump (at least 1, Mario's own row).
     *
     * @return the design gap limit, or -1 when the jump is not allowed
     */
    public static int maxGap(int rise, int headroom) {
        return gapLimit(rise, headroom, MARGIN);
    }

    /**
     * Gap limit with an explicit margin: {@link #MARGIN} gives the design limits, 0 the raw measurements.
     * Landing on a rise needs the rise, plus Mario's own row, plus the margin, in headroom. The ceiling
     * table was measured for level jumps, so a rising jump is looked up by the headroom left above
     * its landing ({@code headroom - rise}), not above its takeoff.
     *
     * @return the widest gap in empty columns, or -1 when the jump is not possible
     */
    public static int gapLimit(int rise, int headroom, int margin) {
        if (rise > MEASURED_MAX_RISE - margin || headroom < 1 || (rise > 0 && headroom < rise + 1 + margin)) {
            return -1;
        }
        int[] byRise = MEASURED_STANDING_GAP_BY_RISE;
        int[] byHeadroom = MEASURED_STANDING_GAP_BY_HEADROOM;
        int open = byRise[MEASURED_MAX_RISE - Math.max(rise, TABLE_MIN_RISE)];
        int underCeiling = byHeadroom[Math.min(headroom - Math.max(rise, 0), byHeadroom.length) - 1];
        return Math.min(open, underCeiling) - margin;
    }
}
