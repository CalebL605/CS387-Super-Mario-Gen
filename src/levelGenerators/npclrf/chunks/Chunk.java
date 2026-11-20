package levelGenerators.npclrf.chunks;

import java.util.Random;

import engine.core.MarioLevelModel;

public interface Chunk {
    String name();

    int difficulty(); // Return 0 for a recovery section, or 1 through 3 for increasing difficulty. Or 4 for a special section.

    int minWidth();

    int maxWidth();

    /**
     * Replace the tiles in columns [x, x + width), preserving all other columns.
     * Ground height is the zero-based row of the first solid ground tile.
     * Starter chunks require at least three rows above that ground and enter
     * and exit at the same ground height. Start and exit chunks place
     * their own markers. The assembler places those chunks at the level
     * boundaries and preserves their marker columns.
     *
     * @param model destination level
     * @param x first column owned by this chunk
     * @param width allocated width, within minWidth() and maxWidth()
     * @param groundHeight ground row at the entrance
     * @param random source for every random choice made by this chunk
     * @return ground row at the exit
     * @throws IllegalArgumentException if the placement dimensions are invalid
     * @throws NullPointerException if the model or random source is null
     */
    int place(MarioLevelModel model, int x, int width, int groundHeight, Random random);
}
