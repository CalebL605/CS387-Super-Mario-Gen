package levelGenerators.npclrf;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Deterministic, static stand-in for an agent playthrough: checks that small Mario can get from
 * {@code M} to the exit column using only moves inside the measured {@link JumpLimits}.
 *
 * The search is a breadth-first search over a navigation graph [Movement I slide 19]. Its nodes
 * are standing cells. An empty tile whose tile below is solid or a one-way platform. Its edges
 * are one column left or right, falling straight down if there is no floor. The engine wins as
 * soon as Mario's x passes the exit column at any height, so reaching any column at or beyond counts.
 *
 * Simplifications to keep the check sound for the levels we generate:
 * - Enemies are ignored. The checker models terrain only, chunks keep enemies on walkable ground
 * where they can be jumped or stomped, and the A* agent playthrough is the evaluation that includes
 * them.
 * - Cannons and pipes are solid terrain (piranha pipes too), so Mario can stand on them.
 * - Bricks are solid + small Mario cannot break them. Hidden blocks block a jump but cannot be
 * stood on. Coins are empty.
 * - Mario is small (one tile tall) and never needs a running start
 */
public final class PlayabilityChecker {
    private static final String SOLID = "X#SCULDQ@!?tT<>[]*Bb";
    private static final String PLATFORM = "%";
    private static final String CEILING_ONLY = "12";
    private static final int MAX_HEADROOM = JumpLimits.MEASURED_STANDING_GAP_BY_HEADROOM.length;
    private static final int MAX_JUMP_COLUMNS = JumpLimits.gapLimit(JumpLimits.TABLE_MIN_RISE, MAX_HEADROOM, 0) + 1;

    private PlayabilityChecker() {} // Class is not instantiable

    /**
     * Fail column is zero-based, or -1 when no failure column is available.
     * For an unplayable level it is the first column to the right of everything Mario can reach.
     */
    public record Result(boolean playable, int failColumn, String reason) {
    }

    /** Checks the level against the design limits that the chunks are built to (measured minus the margin). */
    public static Result check(String level) {
        return check(level, JumpLimits.MARGIN);
    }

    /**
     * Checks the level with an explicit safety margin: {@link JumpLimits#MARGIN} for the design limits,
     * or 0 to allow everything the engine measurements allow (used to compare against the original levels).
     */
    public static Result check(String level, int margin) {
        Objects.requireNonNull(level, "level");
        Grid grid = Grid.parse(level, margin);
        if (grid == null) {
            return new Result(false, -1, "level is empty");
        }
        if (grid.startY < 0) {
            return new Result(false, -1, "level has no Mario start (M) and no floor in column 0");
        }

        int startY = grid.fall(grid.startX, grid.startY);
        if (startY < 0) {
            return new Result(false, grid.startX, "Mario falls out of the level at the start");
        }
        if (grid.startX >= grid.exitX) {
            return new Result(true, -1, "start is at or past the exit");
        }

        boolean[] visited = new boolean[grid.width * grid.height];
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        visited[startY * grid.width + grid.startX] = true;
        queue.add(startY * grid.width + grid.startX);
        int furthest = grid.startX;

        while (!queue.isEmpty()) {
            int node = queue.poll();
            int x = node % grid.width;
            int y = node / grid.width;
            furthest = Math.max(furthest, x);

            for (int direction = -1; direction <= 1; direction += 2) {
                // Walk, or walk off a ledge and fall straight down.
                int walkX = x + direction;
                if (grid.inColumns(walkX) && grid.passable(walkX, y)) {
                    if (walkX >= grid.exitX) {
                        return new Result(true, -1, "exit reachable");
                    }
                    int landY = grid.fall(walkX, y);
                    if (landY >= 0 && !visited[landY * grid.width + walkX]) {
                        visited[landY * grid.width + walkX] = true;
                        queue.add(landY * grid.width + walkX);
                    }
                }

                // Jump to a standing cell in a column up to MAX_JUMP_COLUMNS away.
                for (int distance = 1; distance <= MAX_JUMP_COLUMNS; distance++) {
                    int landX = x + direction * distance;
                    if (!grid.inColumns(landX)) {
                        break;
                    }
                    for (int landY : grid.standingRows[landX]) {
                        if (visited[landY * grid.width + landX] || !grid.canJump(x, y, landX, landY)) {
                            continue;
                        }
                        if (landX >= grid.exitX) {
                            return new Result(true, -1, "exit reachable");
                        }
                        visited[landY * grid.width + landX] = true;
                        queue.add(landY * grid.width + landX);
                    }
                }
            }
        }

        int failColumn = Math.min(furthest + 1, grid.width - 1);
        return new Result(false, failColumn, "exit unreachable: Mario gets no further than column "
                + furthest + "; " + grid.describe(failColumn));
    }

    private static final class Grid {
        final char[][] tiles; // [row][column]
        final int width;
        final int height;
        final int startX;
        final int startY;
        final int exitX;
        final List<Integer>[] standingRows;
        final int margin;

        private Grid(char[][] tiles, int startX, int startY, int exitX, int margin) {
            this.margin = margin;
            this.tiles = tiles;
            this.height = tiles.length;
            this.width = tiles[0].length;
            this.startX = startX;
            this.startY = startY;
            this.exitX = exitX;
            @SuppressWarnings("unchecked")
            List<Integer>[] rows = new List[width];
            for (int x = 0; x < width; x++) {
                rows[x] = new ArrayList<>();
                for (int y = 0; y < height; y++) {
                    if (standable(x, y)) {
                        rows[x].add(y);
                    }
                }
            }
            this.standingRows = rows;
        }

        static Grid parse(String level, int margin) {
            List<String> lines = new ArrayList<>();
            for (String line : level.split("\n", -1)) {
                lines.add(line.endsWith("\r") ? line.substring(0, line.length() - 1) : line);
            }
            while (!lines.isEmpty() && lines.get(lines.size() - 1).isEmpty()) {
                lines.remove(lines.size() - 1);
            }
            int width = lines.stream().mapToInt(String::length).max().orElse(0);
            if (width == 0) {
                return null;
            }

            char[][] tiles = new char[lines.size()][width];
            int startX = -1;
            int startY = -1;
            int exitX = width - 1; // the engine's default when there is no F
            for (int y = 0; y < lines.size(); y++) {
                String line = lines.get(y);
                for (int x = 0; x < width; x++) {
                    char tile = x < line.length() ? line.charAt(x) : '-';
                    // Like the engine, the last M and F in reading order win.
                    if (tile == 'M') {
                        startX = x;
                        startY = y;
                    } else if (tile == 'F') {
                        exitX = x;
                    }
                    tiles[y][x] = tile;
                }
            }
            if (startX < 0) {
                // The engine's default: column 0, on the first floor found going up from the bottom.
                startX = 0;
                startY = -1;
                for (int y = tiles.length - 1; y >= 0; y--) {
                    if (SOLID.indexOf(tiles[y][0]) < 0 && y + 1 < tiles.length && SOLID.indexOf(tiles[y + 1][0]) >= 0) {
                        startY = y;
                        break;
                    }
                }
            }
            return new Grid(tiles, startX, startY, exitX, margin);
        }

        boolean inColumns(int x) {
            return x >= 0 && x < width;
        }

        private char tile(int x, int y) {
            return y < 0 || y >= height ? '-' : tiles[y][x];
        }

        boolean passable(int x, int y) {
            return SOLID.indexOf(tile(x, y)) < 0;
        }

        /** Mario's body can move through this cell in any direction during a jump. */
        private boolean clear(int x, int y) {
            return passable(x, y) && CEILING_ONLY.indexOf(tile(x, y)) < 0;
        }

        private boolean standable(int x, int y) {
            if (y < 0 || y + 1 >= height || !passable(x, y)) {
                return false;
            }
            char below = tiles[y + 1][x];
            return SOLID.indexOf(below) >= 0 || PLATFORM.indexOf(below) >= 0;
        }

        /** Row where Mario lands falling straight down from (x, y), or -1 if he falls out. */
        int fall(int x, int y) {
            for (int row = Math.max(y, 0); row < height; row++) {
                if (!passable(x, row)) {
                    return -1; // landed inside a block; cannot happen when starting from a passable cell
                }
                if (standable(x, row)) {
                    return row;
                }
            }
            return -1;
        }

        boolean canJump(int x, int y, int landX, int landY) {
            int rise = y - landY;
            if (rise > JumpLimits.MEASURED_MAX_RISE - margin) {
                return false;
            }
            int top = Math.min(y, landY);
            int bottom = Math.max(y, landY);
            int left = Math.min(x, landX);
            int right = Math.max(x, landX);

            // The bounding box between the ledges: the takeoff column up to the higher ledge, the
            // landing column down to the landing cell, and every column in between to the lower ledge.
            for (int column = left; column <= right; column++) {
                int lowest = column == x ? y : column == landX ? landY : bottom;
                for (int row = top; row <= lowest; row++) {
                    if (!clear(column, row)) {
                        return false;
                    }
                }
            }

            int headroom = MAX_HEADROOM;
            for (int column = left; column <= right && headroom > 0; column++) {
                headroom = Math.min(headroom, clearRowsUp(column, top));
            }
            headroom += Math.max(rise, 0); // measured from the takeoff surface, which is lower
            int gap = right - left - 1;
            return gap <= JumpLimits.gapLimit(rise, headroom, margin);
        }

        /** Empty rows from {@code row} upward (inclusive), capped at the largest measured headroom. */
        private int clearRowsUp(int x, int row) {
            int count = 0;
            while (count < MAX_HEADROOM && clear(x, row - count)) {
                count++;
            }
            return count;
        }

        String describe(int x) {
            boolean empty = true;
            int top = height;
            for (int y = 0; y < height; y++) {
                if (!passable(x, y) || PLATFORM.indexOf(tiles[y][x]) >= 0) {
                    empty = false;
                    top = Math.min(top, y);
                }
            }
            if (empty) {
                int gapEnd = x;
                while (gapEnd + 1 < width && standingRows[gapEnd + 1].isEmpty()) {
                    gapEnd++;
                }
                return "gap of " + (gapEnd - x + 1) + " columns starting at column " + x;
            }
            return "column " + x + " is blocked (highest tile at row " + top + ")";
        }
    }
}
