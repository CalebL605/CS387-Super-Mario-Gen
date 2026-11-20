package levelGenerators.npclrf.chunks;

import java.util.Random;

import engine.core.MarioLevelModel;

public final class StaircaseExitChunk implements Chunk {
    private static final int MIN_STEP_COUNT = 6;
    private static final int MAX_STEP_COUNT = 8;
    private static final int APPROACH_LENGTH = 3;
    private static final int HEADROOM = 2;

    @Override
    public String name() {
        return "staircase-exit";
    }

    @Override
    public int difficulty() {
        return 1;
    }

    @Override
    public int minWidth() {
        return APPROACH_LENGTH + MAX_STEP_COUNT + 3;
    }

    @Override
    public int maxWidth() {
        return 16;
    }

    @Override
    public int place(MarioLevelModel model, int x, int width, int groundHeight, Random random) {
        ChunkTerrain.prepareGround(this, model, x, width, groundHeight, MIN_STEP_COUNT + HEADROOM, random);
        model.setBlock(x + width - 2, groundHeight - 1, MarioLevelModel.MARIO_EXIT);

        int maxSteps = Math.min(MAX_STEP_COUNT, groundHeight - HEADROOM);
        int stepCount = MIN_STEP_COUNT + random.nextInt(maxSteps - MIN_STEP_COUNT + 1);
        for (int step = 1; step <= stepCount; step++) {
            int stepX = x + APPROACH_LENGTH + step - 1;
            model.setRectangle(stepX, groundHeight - step, 1, step, MarioLevelModel.PYRAMID_BLOCK);
        }
        return groundHeight;
    }
}
