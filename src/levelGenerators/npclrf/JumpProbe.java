package levelGenerators.npclrf;

import java.util.Arrays;

import engine.core.MarioWorld;
import engine.helper.GameStatus;
import engine.helper.MarioActions;

/**
 * Measures small Mario's jump limits by running the engine headless, tick by tick.
 * Nothing here reads the clock or uses an agent's search, so every run prints the same numbers.
 *
 * <p>Each trial builds a two-ledge test level: a takeoff ledge, a gap of {@code gap} empty columns,
 * then a landing ledge whose top is {@code rise} rows higher (negative means lower) with the flag on it.
 * A scripted policy holds right (and optionally run) and holds jump from a chosen tick onward.
 * A trial succeeds when the flag is reached. For each (gap, rise) pair the probe tries every jump tick.
 *
 * <ul>
 *   <li><b>standing</b>: Mario spawns at the ledge edge, settles at rest, then presses right and jump together.</li>
 *   <li><b>running</b>: Mario holds right and run from a long runway and jumps at the best tick.</li>
 * </ul>
 *
 * Run with {@code java -cp out levelGenerators.npclrf.JumpProbe}.
 */
public final class JumpProbe {
    private static final int HEIGHT = 16;
    private static final int BASE_TOP = 9; // takeoff ledge top row; leaves room for higher and lower landings
    private static final int RUNWAY = 24;
    private static final int LANDING = 12;
    private static final int SETTLE_TICKS = 12;
    private static final int MAX_JUMP_TICK = 120;
    private static final int MAX_TICKS = 400;
    private static final int MAX_RISE = 6;
    private static final int MIN_RISE = -6;
    private static final int MAX_GAP = 14;

    private enum Start { STANDING_WALK, STANDING_RUN, RUNNING }

    private JumpProbe() {} // Class is not instantiable

    public static void main(String[] args) {
        System.out.println("Small Mario jump limits measured in the engine (tiles; rise > 0 means a higher landing)");
        System.out.println();
        traceFlatJump(Start.STANDING_WALK);
        traceFlatJump(Start.STANDING_RUN);
        traceFlatJump(Start.RUNNING);
        System.out.println();

        for (Start start : Start.values()) {
            System.out.printf("%-14s max height (gap 0): %d%n", label(start), maxRise(start, 0));
        }
        System.out.println();
        System.out.print("max gap by rise    ");
        for (int rise = MAX_RISE; rise >= MIN_RISE; rise--) {
            System.out.printf("%4d", rise);
        }
        System.out.println();
        for (Start start : Start.values()) {
            System.out.printf("%-19s", label(start));
            for (int rise = MAX_RISE; rise >= MIN_RISE; rise--) {
                int gap = maxGap(start, rise);
                System.out.print(gap < 0 ? "   -" : "%4d".formatted(gap));
            }
            System.out.println();
        }
        System.out.println();
        System.out.print("max gap by headroom");
        for (int headroom = 1; headroom <= JumpLimits.MEASURED_STANDING_GAP_BY_HEADROOM.length; headroom++) {
            System.out.printf("%4d", headroom);
        }
        System.out.println("   (empty rows above a level ledge, under a solid ceiling)");
        for (Start start : Start.values()) {
            System.out.printf("%-19s", label(start));
            for (int headroom = 1; headroom <= JumpLimits.MEASURED_STANDING_GAP_BY_HEADROOM.length; headroom++) {
                int gap = maxGapUnderCeiling(start, headroom);
                System.out.print(gap < 0 ? "   -" : "%4d".formatted(gap));
            }
            System.out.println();
        }
        System.out.println();
        verifyJumpLimits();
    }

    /** Fails if the constants in {@link JumpLimits} no longer match what the engine does. */
    private static void verifyJumpLimits() {
        boolean matches = maxRise(Start.RUNNING, 0) == JumpLimits.MEASURED_MAX_RISE
                && maxRise(Start.STANDING_RUN, 0) == JumpLimits.MEASURED_MAX_RISE
                && maxGap(Start.STANDING_RUN, 0) == JumpLimits.MEASURED_STANDING_GAP
                && maxGap(Start.RUNNING, 0) == JumpLimits.MEASURED_RUNNING_GAP;
        int[] table = JumpLimits.MEASURED_STANDING_GAP_BY_RISE;
        for (int index = 0; index < table.length; index++) {
            matches &= maxGap(Start.STANDING_RUN, JumpLimits.MEASURED_MAX_RISE - index) == table[index];
        }
        int[] ceilingTable = JumpLimits.MEASURED_STANDING_GAP_BY_HEADROOM;
        for (int index = 0; index < ceilingTable.length; index++) {
            matches &= maxGapUnderCeiling(Start.STANDING_RUN, index + 1) == ceilingTable[index];
        }
        if (!matches) {
            System.out.println("JumpLimits does NOT match these measurements; update JumpLimits.java.");
            System.exit(1);
        }
        System.out.printf("JumpLimits matches. Design limits (measured - %d): max rise %d, max level gap %d%n",
                JumpLimits.MARGIN, JumpLimits.MAX_RISE, JumpLimits.MAX_GAP);
    }

    private static String label(Start start) {
        return switch (start) {
            case STANDING_WALK -> "standing";
            case STANDING_RUN -> "standing+run";
            case RUNNING -> "running";
        };
    }

    /** Largest rise Mario can land on across the given gap, or Integer.MIN_VALUE if none. */
    private static int maxRise(Start start, int gap) {
        int best = Integer.MIN_VALUE;
        for (int rise = MIN_RISE; rise <= MAX_RISE; rise++) {
            if (clears(start, gap, rise)) {
                best = rise;
            }
        }
        return best;
    }

    /** Largest gap Mario can clear onto a landing {@code rise} rows higher, or -1 if even gap 0 fails. */
    private static int maxGap(Start start, int rise) {
        int best = -1;
        for (int gap = 0; gap <= MAX_GAP; gap++) {
            if (clears(start, gap, rise)) {
                best = gap;
            }
        }
        return best;
    }

    /** Largest level gap Mario can clear under a ceiling that leaves {@code headroom} empty rows above the ledges. */
    private static int maxGapUnderCeiling(Start start, int headroom) {
        int best = -1;
        for (int gap = 0; gap <= MAX_GAP; gap++) {
            if (clears(start, gap, 0, headroom)) {
                best = gap;
            }
        }
        return best;
    }

    private static boolean clears(Start start, int gap, int rise) {
        return clears(start, gap, rise, 0);
    }

    private static boolean clears(Start start, int gap, int rise, int headroom) {
        String level = buildLevel(start, gap, rise, headroom);
        if (start != Start.RUNNING) {
            return run(level, start, SETTLE_TICKS);
        }
        for (int jumpTick = 0; jumpTick <= MAX_JUMP_TICK; jumpTick++) {
            if (run(level, start, jumpTick)) {
                return true;
            }
        }
        return false;
    }

    private static boolean run(String level, Start start, int jumpTick) {
        MarioWorld world = newWorld(level);
        for (int tick = 0; tick < MAX_TICKS && world.gameStatus == GameStatus.RUNNING; tick++) {
            world.update(actions(start, tick, jumpTick));
        }
        return world.gameStatus == GameStatus.WIN;
    }

    private static boolean[] actions(Start start, int tick, int jumpTick) {
        boolean[] actions = new boolean[MarioActions.numberOfActions()];
        boolean go = start == Start.RUNNING || tick >= jumpTick;
        actions[MarioActions.RIGHT.getValue()] = go;
        actions[MarioActions.SPEED.getValue()] = go && start != Start.STANDING_WALK;
        actions[MarioActions.JUMP.getValue()] = tick >= jumpTick;
        return actions;
    }

    private static MarioWorld newWorld(String level) {
        MarioWorld world = new MarioWorld(null);
        world.visuals = false;
        world.initializeLevel(level, 1000 * 60);
        world.mario.isLarge = false;
        world.mario.isFire = false;
        world.update(new boolean[MarioActions.numberOfActions()]);
        return world;
    }

    /** Prints the apex height and the horizontal distance covered in the air by one jump over flat ground. */
    private static void traceFlatJump(Start start) {
        String level = buildLevel(start, 0, 0, 0).replace('F', '-');
        int jumpTick = start == Start.RUNNING ? 35 : SETTLE_TICKS; // 35 ticks reaches top speed inside the runway
        MarioWorld world = newWorld(level);
        float takeoffX = Float.NaN;
        float takeoffY = Float.NaN;
        float apexY = Float.MAX_VALUE;
        float landX = Float.NaN;
        float topSpeed = 0;
        for (int tick = 0; tick < MAX_TICKS; tick++) {
            boolean wasOnGround = world.mario.onGround;
            float x = world.mario.x;
            float y = world.mario.y;
            boolean[] actions = actions(start, tick, jumpTick);
            // Release jump after landing so the trace records exactly one jump.
            if (!Float.isNaN(takeoffX) && world.mario.onGround) {
                landX = world.mario.x;
                break;
            }
            world.update(actions);
            topSpeed = Math.max(topSpeed, world.mario.x - x);
            if (Float.isNaN(takeoffX) && wasOnGround && !world.mario.onGround) {
                takeoffX = x;
                takeoffY = y;
            }
            if (!Float.isNaN(takeoffX)) {
                apexY = Math.min(apexY, world.mario.y);
            }
        }
        System.out.printf("%-14s apex %.1f px (%.2f tiles), air distance %.1f px (%.2f tiles), speed %.2f px/tick%n",
                label(start), takeoffY - apexY, (takeoffY - apexY) / 16f,
                landX - takeoffX, (landX - takeoffX) / 16f, topSpeed);
    }

    /** @param headroom empty rows between the ledge surface and a solid ceiling over the whole level; 0 for none */
    private static String buildLevel(Start start, int gap, int rise, int headroom) {
        int edge = RUNWAY; // first column after the takeoff ledge
        int width = edge + gap + LANDING;
        char[][] rows = new char[HEIGHT][width];
        for (char[] row : rows) {
            Arrays.fill(row, '-');
        }
        int landingTop = BASE_TOP - rise;
        for (int x = 0; x < width; x++) {
            int top = x < edge ? BASE_TOP : x < edge + gap ? HEIGHT : landingTop;
            for (int y = top; y < HEIGHT; y++) {
                rows[y][x] = 'X';
            }
        }
        if (headroom > 0) {
            Arrays.fill(rows[BASE_TOP - headroom - 1], 'X');
        }
        // Mario spawns at the centre of the M tile, so a standing start is on the last ledge tile.
        int spawn = start == Start.RUNNING ? 1 : edge - 1;
        rows[BASE_TOP - 1][spawn] = 'M';
        rows[landingTop - 1][edge + gap + LANDING / 2] = 'F';
        StringBuilder level = new StringBuilder();
        for (char[] row : rows) {
            level.append(row).append('\n');
        }
        return level.toString();
    }
}
