package levelGenerators.npclrf;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Hand-built (and hand-broken) levels with known answers, run by {@code Check --spot-check}.
 * Each case says whether it should pass with the design limits and with the raw measured limits.
 */
final class CheckerSpotCheck {
    private static final int WIDTH = 32;
    private static final int HEIGHT = 16;
    private static final int GROUND = 14; // first ground row; Mario walks on row 13
    private static final int FEATURE_X = 12;

    private record Case(String name, boolean design, boolean measured, Consumer<char[][]> edit) {
    }

    private static final List<Case> CASES = List.of(
            new Case("flat ground", true, true, rows -> { }),
            new Case("gap of 4 (design limit)", true, true, rows -> gap(rows, 4)),
            new Case("gap of 5 (measured limit)", false, true, rows -> gap(rows, 5)),
            new Case("gap of 6", false, false, rows -> gap(rows, 6)),
            new Case("step of 3 (design limit)", true, true, rows -> wall(rows, FEATURE_X, 3, 1)),
            new Case("pipe of 4 (measured limit)", false, true, rows -> pipe(rows, 4)),
            new Case("wall of 5", false, false, rows -> wall(rows, FEATURE_X, 5, 1)),
            new Case("staircase of 1-high steps up to 8", true, true, rows -> {
                for (int step = 1; step <= 8; step++) {
                    wall(rows, FEATURE_X + step - 1, step, 1);
                }
            }),
            new Case("gap of 4 under a ceiling 5 rows up", true, true, rows -> {
                gap(rows, 4);
                fill(rows, FEATURE_X - 2, GROUND - 6, 8, 1, 'S');
            }),
            new Case("gap of 4 under a ceiling 3 rows up", false, false, rows -> {
                gap(rows, 4);
                // Two rows thick, so Mario cannot simply jump on top of it and walk across.
                fill(rows, FEATURE_X - 2, GROUND - 5, 8, 2, 'S');
            }),
            new Case("climb onto a thin ceiling 3 rows up", false, true, rows -> {
                gap(rows, 4);
                fill(rows, FEATURE_X - 2, GROUND - 4, 8, 1, 'S');
            }),
            new Case("gap of 8 bridged by a one-way platform", true, true, rows -> {
                gap(rows, 8);
                fill(rows, FEATURE_X + 3, GROUND - 2, 2, 1, '%');
            }),
            new Case("gap of 8 with enemies and coins only", false, false, rows -> {
                gap(rows, 8);
                fill(rows, FEATURE_X + 3, GROUND - 2, 2, 1, 'o');
                rows[GROUND - 3][FEATURE_X + 4] = 'G';
            }),
            new Case("cannon of 3 to jump over", true, true, rows -> fill(rows, FEATURE_X, GROUND - 3, 1, 3, '*')),
            new Case("no floor under the start", false, false, rows -> fill(rows, 2, GROUND, 1, 2, '-')),
            new Case("exit sealed by a full-height wall", false, false, rows -> wall(rows, WIDTH - 6, GROUND, 1)));

    private CheckerSpotCheck() {} // Class is not instantiable

    // return true when every case gives its expected answer
    static boolean run() {
        boolean allMatch = true;
        System.out.printf("%-40s %-12s %-12s%n", "case", "design", "measured");
        for (Case testCase : CASES) {
            String level = build(testCase.edit());
            PlayabilityChecker.Result design = PlayabilityChecker.check(level);
            PlayabilityChecker.Result measured = PlayabilityChecker.check(level, 0);
            boolean matches = design.playable() == testCase.design() && measured.playable() == testCase.measured();
            allMatch &= matches;
            System.out.printf("%-40s %-12s %-12s%s%n", testCase.name(), verdict(design), verdict(measured),
                    matches ? "" : "  <-- expected " + testCase.design() + "/" + testCase.measured());
        }
        System.out.println(allMatch ? "All spot checks match." : "Some spot checks do NOT match.");
        return allMatch;
    }

    private static String verdict(PlayabilityChecker.Result result) {
        return result.playable() ? "pass" : "fail@" + result.failColumn();
    }

    private static String build(Consumer<char[][]> edit) {
        char[][] rows = new char[HEIGHT][WIDTH];
        for (char[] row : rows) {
            Arrays.fill(row, '-');
        }
        fill(rows, 0, GROUND, WIDTH, HEIGHT - GROUND, 'X');
        edit.accept(rows);
        rows[GROUND - 1][2] = 'M';
        rows[GROUND - 1][WIDTH - 3] = 'F';
        StringBuilder level = new StringBuilder();
        for (char[] row : rows) {
            level.append(row).append('\n');
        }
        return level.toString();
    }

    private static void gap(char[][] rows, int width) {
        fill(rows, FEATURE_X, GROUND, width, HEIGHT - GROUND, '-');
    }

    private static void wall(char[][] rows, int x, int height, int width) {
        fill(rows, x, GROUND - height, width, height, '#');
    }

    private static void pipe(char[][] rows, int height) {
        fill(rows, FEATURE_X, GROUND - height, 2, height, 't');
    }

    private static void fill(char[][] rows, int x, int y, int width, int height, char tile) {
        for (int row = y; row < y + height; row++) {
            for (int column = x; column < x + width; column++) {
                rows[row][column] = tile;
            }
        }
    }
}
