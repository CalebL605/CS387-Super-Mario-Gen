package levelGenerators.npclrf.chunks;

import java.util.Random;

import engine.core.MarioLevelModel;

public final class SinglePipeChunk implements Chunk {
    private static final int PIPE_WIDTH = 2;
    private static final int GROUND_PADDING = 3;
    private final FlatChunk ground = new FlatChunk();

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
        if (width < minWidth() || width > maxWidth()) {
            throw new IllegalArgumentException("Single pipe chunk width must be between 8 and 16.");
        }
        ground.place(model, x, width, groundHeight, random);

        int pipeHeight = 1 + random.nextInt(2);
        int positionCount = width - PIPE_WIDTH - 2 * GROUND_PADDING + 1;
        int pipeX = x + GROUND_PADDING + random.nextInt(positionCount);
        model.setRectangle(pipeX, groundHeight - pipeHeight, PIPE_WIDTH, pipeHeight, MarioLevelModel.PIPE);
        return groundHeight;
    }
}
