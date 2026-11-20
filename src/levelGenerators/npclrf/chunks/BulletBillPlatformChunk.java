package levelGenerators.npclrf.chunks;

import java.util.Random;

import engine.core.MarioLevelModel;

public final class BulletBillPlatformChunk implements Chunk {
    private static final int GROUND_PADDING = 6;
    private static final int PLATFORM_COUNT = 3;

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

        int interiorWidth = width - 2 * GROUND_PADDING;
        for (int index = 0; index < PLATFORM_COUNT; index++) {
            int sectionStart = x + GROUND_PADDING + index * interiorWidth / PLATFORM_COUNT;
            int sectionEnd = x + GROUND_PADDING + (index + 1) * interiorWidth / PLATFORM_COUNT;
            int gapWidth = 3 + random.nextInt(3);
            int gapX = sectionStart + (sectionEnd - sectionStart - gapWidth) / 2;
            int platformX = gapX - 1;
            int platformWidth = gapWidth + 2;
            int platformY = groundHeight - 2 - index % 2;

            model.setRectangle(gapX, groundHeight, gapWidth,
                    model.getHeight() - groundHeight, MarioLevelModel.EMPTY);
            model.setRectangle(platformX, platformY, platformWidth, 1, MarioLevelModel.PLATFORM);
            model.setBlock(platformX + platformWidth / 2, platformY - 1, MarioLevelModel.BULLET_BILL);
            char enemy = index % 2 == 0
                    ? MarioLevelModel.GOOMBA_WINGED : MarioLevelModel.GREEN_KOOPA_WINGED;
            model.setBlock(platformX + 1, platformY - 1, enemy);
        }
        return groundHeight;
    }
}
