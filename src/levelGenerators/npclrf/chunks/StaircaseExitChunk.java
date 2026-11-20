package levelGenerators.npclrf.chunks;

import java.util.Random;

import engine.core.MarioLevelModel;

public final class StaircaseExitChunk implements Chunk {
    private static final int STEP_COUNT = 3;
    private static final int APPROACH_LENGTH = 3;
    private static final int HEADROOM = 3;
    private final ExitChunk exit = new ExitChunk();

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
        return 12;
    }

    @Override
    public int maxWidth() {
        return 16;
    }

    @Override
    public int place(MarioLevelModel model, int x, int width, int groundHeight, Random random) {
        if (width < minWidth() || width > maxWidth()) {
            throw new IllegalArgumentException("Staircase exit chunk width must be between 12 and 16.");
        }
        if (groundHeight < STEP_COUNT + HEADROOM) {
            throw new IllegalArgumentException("Staircase exit needs at least six rows above the ground.");
        }
        exit.place(model, x, width, groundHeight, random);

        for (int step = 1; step <= STEP_COUNT; step++) {
            int stepX = x + APPROACH_LENGTH + step - 1;
            model.setRectangle(stepX, groundHeight - step, 1, step, MarioLevelModel.PYRAMID_BLOCK);
        }
        return groundHeight;
    }
}
