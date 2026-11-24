package levelGenerators.npclrf.chunks;

import java.util.Random;

import engine.core.MarioLevelModel;

public final class SinglePipeChunk implements Chunk {
    private static final int PIPE_WIDTH = 2;
    private static final int GROUND_PADDING = 3;

    @Override
    public String name() {
        return "single-pipe";
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
        ChunkTerrain.prepareGround(this, model, x, width, groundHeight, 6, random);

        int pipeHeight = 2 + random.nextInt(3);
        int positionCount = width - PIPE_WIDTH - 2 * GROUND_PADDING + 1;
        int pipeX = x + GROUND_PADDING + random.nextInt(positionCount);
        model.setRectangle(pipeX, groundHeight - pipeHeight, PIPE_WIDTH, pipeHeight, MarioLevelModel.PIPE);
        return groundHeight;
    }
}
