package levelGenerators.npclrf.chunks;

import java.util.Random;

import engine.core.MarioLevelModel;
import levelGenerators.npclrf.JumpLimits;

/**
 * A bottomless pit crossed on floating platforms (one-way platforms or brick ledges) at varying
 * heights. Every hop, including the last one down to the far ground, is chosen within
 * {@link JumpLimits#maxGap(int)} for its rise.
 */
public final class FloatingPlatformChunk implements Chunk {
    private static final int GROUND_PADDING = 3;
    private static final int MIN_PLATFORM = 2;
    private static final int MAX_PLATFORM = 4;
    private static final int MAX_PLATFORM_HEIGHT = 5; // standing surface rows above the ground
    private static final int MAX_STEP = 2; // rows up or down between consecutive surfaces

    @Override
    public String name() {
        return "floating-platforms";
    }

    @Override
    public int difficulty() {
        return 3;
    }

    @Override
    public int minWidth() {
        return 16;
    }

    @Override
    public int maxWidth() {
        return 32;
    }

    @Override
    public int place(MarioLevelModel model, int x, int width, int groundHeight, Random random) {
        ChunkTerrain.prepareGround(this, model, x, width, groundHeight, MAX_PLATFORM_HEIGHT + 4, random);

        int pitX = x + GROUND_PADDING;
        int pitEnd = x + width - GROUND_PADDING; // first column of the far ground
        model.setRectangle(pitX, 0, pitEnd - pitX, model.getHeight(), MarioLevelModel.EMPTY);

        int surface = 0; // height of the current standing surface above the ground
        int cursor = pitX; // first empty column after the current surface
        boolean redKoopaPlaced = false;
        while (pitEnd - cursor > JumpLimits.maxGap(-surface)) {
            int lowest = Math.max(1, surface - MAX_STEP);
            int highest = Math.min(MAX_PLATFORM_HEIGHT, surface + MAX_STEP);
            int next = lowest + random.nextInt(highest - lowest + 1);
            int remaining = pitEnd - cursor;
            int length = MIN_PLATFORM + random.nextInt(MAX_PLATFORM - MIN_PLATFORM + 1);
            int maxGap = Math.min(JumpLimits.maxGap(next - surface), remaining - length - 1);
            int gap = 1 + random.nextInt(Math.max(1, maxGap));

            int platformX = cursor + gap;
            int platformY = groundHeight - next;
            char tile = random.nextBoolean() ? MarioLevelModel.PLATFORM : MarioLevelModel.NORMAL_BRICK;
            model.setRectangle(platformX, platformY, length, 1, tile);
            if (!redKoopaPlaced && length >= 3 && random.nextInt(3) == 0) {
                // Red koopas turn at ledges, so one on a wide platform patrols it without falling.
                model.setBlock(platformX + 1, platformY - 1, MarioLevelModel.RED_KOOPA);
                redKoopaPlaced = true;
            }
            surface = next;
            cursor = platformX + length;
        }
        return groundHeight;
    }
}
