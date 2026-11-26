package levelGenerators.npclrf.chunks;

import java.util.Random;

import engine.core.MarioLevelModel;
import levelGenerators.npclrf.JumpLimits;

/**
 * Several gaps of different widths, from 1 up to {@link JumpLimits#MAX_GAP}, separated by short
 * islands of ground at the entry height.
 */
public final class GapRunChunk implements Chunk {
    private static final int GROUND_PADDING = 2;
    private static final int MIN_ISLAND = 2;
    private static final int MAX_ISLAND = 4;

    @Override
    public String name() {
        return "gap-run";
    }

    @Override
    public int difficulty() {
        return 2;
    }

    @Override
    public int minWidth() {
        return 2 * GROUND_PADDING + 2 * JumpLimits.MAX_GAP + MIN_ISLAND;
    }

    @Override
    public int maxWidth() {
        return 28;
    }

    @Override
    public int place(MarioLevelModel model, int x, int width, int groundHeight, Random random) {
        ChunkTerrain.prepareGround(this, model, x, width, groundHeight, 3, random);

        int cursor = x + GROUND_PADDING;
        int end = x + width - GROUND_PADDING;
        int gaps = 0;
        while (true) {
            // Make sure there are always at least two gaps, the second as wide as the room allows.
            int gapWidth = 1 + random.nextInt(JumpLimits.MAX_GAP);
            if (cursor + gapWidth > end) {
                if (gaps >= 2) {
                    break;
                }
                gapWidth = end - cursor;
            }
            model.setRectangle(cursor, groundHeight, gapWidth, model.getHeight() - groundHeight,
                    MarioLevelModel.EMPTY);
            gaps++;
            cursor += gapWidth + MIN_ISLAND + random.nextInt(MAX_ISLAND - MIN_ISLAND + 1);
            if (cursor >= end) {
                break;
            }
        }
        return groundHeight;
    }
}
