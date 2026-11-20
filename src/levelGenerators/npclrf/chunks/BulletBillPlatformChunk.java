package levelGenerators.npclrf.chunks;

import java.util.Random;

import engine.core.MarioLevelModel;

public final class BulletBillPlatformChunk implements Chunk {
    private static final int GROUND_PADDING = 6;
    private static final int STEPS_PER_SET = 3;
    private static final int HEIGHT_STEP = 4;
    private static final int PLATFORM_OVERLAP = 2;
    private static final int CANNON_HEIGHT = 2;
    private static final int MIN_PLATFORM_WIDTH = 6;
    private static final int MAX_PLATFORM_WIDTH = 10;

    @Override
    public String name() {
        return "bullet-bill-platform";
    }

    @Override
    public int difficulty() {
        return 4;
    }

    @Override
    public int minWidth() {
        int platformCount = 2 * STEPS_PER_SET;
        return 2 * GROUND_PADDING + platformCount * MIN_PLATFORM_WIDTH
                - (platformCount - 1) * PLATFORM_OVERLAP;
    }

    @Override
    public int maxWidth() {
        int platformCount = 2 * STEPS_PER_SET;
        return 2 * GROUND_PADDING + platformCount * MAX_PLATFORM_WIDTH
                - (platformCount - 1) * PLATFORM_OVERLAP;
    }

    @Override
    public int place(MarioLevelModel model, int x, int width, int groundHeight, Random random) {
        ChunkTerrain.prepareGround(this, model, x, width, groundHeight, 8, random);

        // Shorter maps use fewer steps so the taller cannon heads stay inside the level.
        int maxPlatformHeight = groundHeight - (CANNON_HEIGHT + 1) - 1;
        int stepsPerSet = Math.min(STEPS_PER_SET, (maxPlatformHeight - 2) / HEIGHT_STEP + 1);
        int platformCount = 2 * stepsPerSet;
        int interiorWidth = width - 2 * GROUND_PADDING;
        int platformSpace = interiorWidth + PLATFORM_OVERLAP * (platformCount - 1);
        int platformX = x + GROUND_PADDING;
        model.setRectangle(platformX, groundHeight, interiorWidth,
                model.getHeight() - groundHeight, MarioLevelModel.EMPTY);

        for (int index = 0; index < platformCount; index++) {
            int platformWidth = platformSpace / platformCount
                    + (index < platformSpace % platformCount ? 1 : 0);
            int platformHeight = (index < stepsPerSet ? 2 : 1) + index % stepsPerSet * HEIGHT_STEP;
            int platformY = groundHeight - platformHeight;
            model.setRectangle(platformX, platformY, platformWidth, 1, MarioLevelModel.PLATFORM);
            // The rightmost two columns sit beneath the next ascending platform's overlap.
            int cannonX = platformX + platformWidth - 2;
            model.setRectangle(cannonX, platformY - CANNON_HEIGHT,
                    1, CANNON_HEIGHT, MarioLevelModel.BULLET_BILL);
            model.setRectangle(cannonX + 1, platformY - CANNON_HEIGHT - 1,
                    1, CANNON_HEIGHT + 1, MarioLevelModel.BULLET_BILL);
            if (index % 2 == 0) {
                char enemy = index == 2
                        ? MarioLevelModel.GREEN_KOOPA_WINGED : MarioLevelModel.GOOMBA_WINGED;
                model.setBlock(platformX + 1, platformY - 1, enemy);
            }
            platformX += platformWidth - PLATFORM_OVERLAP;
        }
        return groundHeight;
    }
}
