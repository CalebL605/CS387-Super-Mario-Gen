package levelGenerators.npclrf.chunks;

import java.util.Random;

import engine.core.MarioLevelModel;

/**
 * A reward section: a row of bricks and question blocks four rows above the ground, as in the
 * original levels, with a few coins floating at jump height. Nothing here blocks the way.
 */
public final class BlockRowChunk implements Chunk {
    private static final int GROUND_PADDING = 2;
    private static final int BLOCK_HEIGHT = 4; // rows above the ground, leaving 3 empty rows to walk under
    private static final int COIN_HEIGHT = 2;
    private static final int MIN_ROW = 3;
    private static final int MAX_ROW = 8;
    private static final char[] BLOCKS = {
            MarioLevelModel.NORMAL_BRICK, MarioLevelModel.NORMAL_BRICK,
            MarioLevelModel.COIN_QUESTION_BLOCK, MarioLevelModel.COIN_BRICK};

    @Override
    public String name() {
        return "block-row";
    }

    @Override
    public int difficulty() {
        return 0;
    }

    @Override
    public int minWidth() {
        return 2 * GROUND_PADDING + MIN_ROW;
    }

    @Override
    public int maxWidth() {
        return 16;
    }

    @Override
    public int place(MarioLevelModel model, int x, int width, int groundHeight, Random random) {
        ChunkTerrain.prepareGround(this, model, x, width, groundHeight, BLOCK_HEIGHT + 2, random);

        int interior = width - 2 * GROUND_PADDING;
        int rowLength = MIN_ROW + random.nextInt(Math.min(MAX_ROW, interior) - MIN_ROW + 1);
        int rowX = x + GROUND_PADDING + random.nextInt(interior - rowLength + 1);
        int blockY = groundHeight - BLOCK_HEIGHT;
        for (int column = rowX; column < rowX + rowLength; column++) {
            model.setBlock(column, blockY, BLOCKS[random.nextInt(BLOCKS.length)]);
        }
        // One power-up block in some rows, like the originals.
        if (random.nextInt(3) == 0) {
            model.setBlock(rowX + random.nextInt(rowLength), blockY, MarioLevelModel.SPECIAL_QUESTION_BLOCK);
        }

        int coinCount = Math.min(interior, 2 + random.nextInt(3));
        int coinX = x + GROUND_PADDING + random.nextInt(interior - coinCount + 1);
        model.setRectangle(coinX, groundHeight - COIN_HEIGHT, coinCount, 1, MarioLevelModel.COIN);
        return groundHeight;
    }
}
