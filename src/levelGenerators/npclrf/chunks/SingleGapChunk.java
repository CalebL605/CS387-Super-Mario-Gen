package levelGenerators.npclrf.chunks;

import java.util.Random;

import engine.core.MarioLevelModel;

public final class SingleGapChunk implements Chunk {
    private static final int GROUND_PADDING = 2;

    @Override
    public String name() {
        return "single-gap";
    }

    @Override
    public int difficulty() {
        return 1;
    }

    @Override
    public int minWidth() {
        return 8;
    }

    @Override
    public int maxWidth() {
        return 16;
    }

    @Override
    public int place(MarioLevelModel model, int x, int width, int groundHeight, Random random) {
        ChunkTerrain.prepareGround(this, model, x, width, groundHeight, 3, random);

        int gapWidth = 2 + random.nextInt(3);
        int positionCount = width - gapWidth - 2 * GROUND_PADDING + 1;
        int gapX = x + GROUND_PADDING + random.nextInt(positionCount);
        model.setRectangle(gapX, groundHeight, gapWidth, model.getHeight() - groundHeight, MarioLevelModel.EMPTY);
        return groundHeight;
    }
}
