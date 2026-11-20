package levelGenerators.npclrf.chunks;

import java.util.Random;

import engine.core.MarioLevelModel;

public final class BulletBillPlatformChunk implements Chunk {
    private static final int GROUND_PADDING = 6;
    // Two ascending stair sets use even and odd heights to cover different firing rows.
    private static final int[] PLATFORM_HEIGHTS = {2, 4, 6, 1, 3, 5};
    private static final int[] LOW_PLATFORM_HEIGHTS = {2, 4, 1, 3};
    private static final int PLATFORM_GAP = 2;
    private static final int CANNON_HEIGHT = 2;

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
        return 48;
    }

    @Override
    public int maxWidth() {
        return 80;
    }

    @Override
    public int place(MarioLevelModel model, int x, int width, int groundHeight, Random random) {
        ChunkTerrain.prepareGround(this, model, x, width, groundHeight, 8, random);

        int[] heights = groundHeight >= 10 ? PLATFORM_HEIGHTS : LOW_PLATFORM_HEIGHTS;
        int interiorWidth = width - 2 * GROUND_PADDING;
        int platformSpace = interiorWidth - PLATFORM_GAP * (heights.length - 1);
        int platformX = x + GROUND_PADDING;
        model.setRectangle(platformX, groundHeight, interiorWidth,
                model.getHeight() - groundHeight, MarioLevelModel.EMPTY);

        for (int index = 0; index < heights.length; index++) {
            int platformWidth = platformSpace / heights.length
                    + (index < platformSpace % heights.length ? 1 : 0);
            int platformY = groundHeight - heights[index];
            model.setRectangle(platformX, platformY, platformWidth, 1, MarioLevelModel.PLATFORM);
            model.setRectangle(platformX + platformWidth - 2, platformY - CANNON_HEIGHT,
                    1, CANNON_HEIGHT, MarioLevelModel.BULLET_BILL);
            if (index % 2 == 0) {
                char enemy = index == 2
                        ? MarioLevelModel.GREEN_KOOPA_WINGED : MarioLevelModel.GOOMBA_WINGED;
                model.setBlock(platformX + 1, platformY - 1, enemy);
            }
            platformX += platformWidth + PLATFORM_GAP;
        }
        return groundHeight;
    }
}
