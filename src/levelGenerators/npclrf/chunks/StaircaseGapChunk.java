package levelGenerators.npclrf.chunks;

import java.util.Random;

import engine.core.MarioLevelModel;

public final class StaircaseGapChunk implements Chunk {
    private static final int STEP_COUNT = 3;
    private static final int GROUND_PADDING = 3;
    private static final int RUNWAY_WIDTH = 12;
    private static final int LANDING_WIDTH = 6;
    // Measured with equal-height surfaces, full running speed, and a held jump.
    private static final int GAP_WIDTH = 11;

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
        return 48;
    }

    @Override
    public int maxWidth() {
        return 64;
    }

    @Override
    public int place(MarioLevelModel model, int x, int width, int groundHeight, Random random) {
        ChunkTerrain.prepareGround(this, model, x, width, groundHeight, STEP_COUNT + 3, random);

        int fixedWidth = 2 * GROUND_PADDING + 2 * STEP_COUNT + RUNWAY_WIDTH + GAP_WIDTH + LANDING_WIDTH;
        int extraWidth = width - fixedWidth;
        int runwayWidth = RUNWAY_WIDTH + extraWidth / 2;
        int landingWidth = LANDING_WIDTH + extraWidth - extraWidth / 2;
        int runwayX = x + GROUND_PADDING + STEP_COUNT;
        int gapX = runwayX + runwayWidth;
        int landingX = gapX + GAP_WIDTH;
        int descendingX = landingX + landingWidth;
        int topY = groundHeight - STEP_COUNT;

        for (int step = 1; step <= STEP_COUNT; step++) {
            model.setRectangle(x + GROUND_PADDING + step - 1, groundHeight - step,
                    1, step, MarioLevelModel.PYRAMID_BLOCK);
            int descendingHeight = STEP_COUNT - step + 1;
            model.setRectangle(descendingX + step - 1, groundHeight - descendingHeight,
                    1, descendingHeight, MarioLevelModel.PYRAMID_BLOCK);
        }
        model.setRectangle(runwayX, topY, runwayWidth, STEP_COUNT, MarioLevelModel.PYRAMID_BLOCK);
        model.setRectangle(gapX, 0, GAP_WIDTH, model.getHeight(), MarioLevelModel.EMPTY);
        model.setRectangle(landingX, topY, landingWidth, STEP_COUNT, MarioLevelModel.PYRAMID_BLOCK);
        return groundHeight;
    }
}
