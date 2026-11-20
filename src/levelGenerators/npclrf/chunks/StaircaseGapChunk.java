package levelGenerators.npclrf.chunks;

import java.util.Random;

import engine.core.MarioLevelModel;

public final class StaircaseGapChunk implements Chunk {
    private static final int MIN_STEP_COUNT = 4;
    private static final int MAX_STEP_COUNT = 8;
    private static final int GROUND_PADDING = 3;
    private static final int RUNWAY_WIDTH = 1;
    private static final int LANDING_WIDTH = 1;
    private static final int GAP_WIDTH = 8;

    @Override
    public String name() {
        return "staircase-gap";
    }

    @Override
    public int difficulty() {
        return 2;
    }

    @Override
    public int minWidth() {
        return 24;
    }

    @Override
    public int maxWidth() {
        return 40;
    }

    @Override
    public int place(MarioLevelModel model, int x, int width, int groundHeight, Random random) {
        ChunkTerrain.prepareGround(this, model, x, width, groundHeight, MIN_STEP_COUNT + 3, random);

        int fixedWidth = 2 * GROUND_PADDING + RUNWAY_WIDTH + GAP_WIDTH + LANDING_WIDTH;
        int maxSteps = Math.min(MAX_STEP_COUNT, Math.min(groundHeight - 3, (width - fixedWidth) / 2));
        int stepCount = MIN_STEP_COUNT + random.nextInt(maxSteps - MIN_STEP_COUNT + 1);
        fixedWidth += 2 * stepCount;
        int extraWidth = width - fixedWidth;
        int stairsX = x + GROUND_PADDING + extraWidth / 2;
        int runwayX = stairsX + stepCount;
        int gapX = runwayX + RUNWAY_WIDTH;
        int landingX = gapX + GAP_WIDTH;
        int descendingX = landingX + LANDING_WIDTH;
        int topY = groundHeight - stepCount;

        for (int step = 1; step <= stepCount; step++) {
            model.setRectangle(stairsX + step - 1, groundHeight - step,
                    1, step, MarioLevelModel.PYRAMID_BLOCK);
            int descendingHeight = stepCount - step + 1;
            model.setRectangle(descendingX + step - 1, groundHeight - descendingHeight,
                    1, descendingHeight, MarioLevelModel.PYRAMID_BLOCK);
        }
        model.setRectangle(runwayX, topY, RUNWAY_WIDTH, stepCount, MarioLevelModel.PYRAMID_BLOCK);
        model.setRectangle(gapX, 0, GAP_WIDTH, model.getHeight(), MarioLevelModel.EMPTY);
        model.setRectangle(landingX, topY, LANDING_WIDTH, stepCount, MarioLevelModel.PYRAMID_BLOCK);
        return groundHeight;
    }
}
