package levelGenerators.npclrf.chunks;

import java.util.Random;

import engine.core.MarioLevelModel;
import levelGenerators.npclrf.JumpLimits;

/**
 * Two to four pipes of varying height (at most {@link JumpLimits#MAX_RISE}) with flat ground between
 * them. Some pipes hold a piranha plant and a goomba sometimes walks between them.
 */
public final class PipeRunChunk implements Chunk {
    private static final int PIPE_WIDTH = 2;
    private static final int GROUND_PADDING = 3;
    private static final int MIN_SPACING = 3;
    private static final int MAX_SPACING = 5;
    private static final int MIN_PIPES = 2;
    private static final int MAX_PIPES = 4;

    @Override
    public String name() {
        return "pipe-run";
    }

    @Override
    public int difficulty() {
        return 2;
    }

    @Override
    public int minWidth() {
        return 2 * GROUND_PADDING + MIN_PIPES * PIPE_WIDTH + (MIN_PIPES - 1) * MIN_SPACING;
    }

    @Override
    public int maxWidth() {
        return 2 * GROUND_PADDING + MAX_PIPES * PIPE_WIDTH + (MAX_PIPES - 1) * MAX_SPACING;
    }

    @Override
    public int place(MarioLevelModel model, int x, int width, int groundHeight, Random random) {
        ChunkTerrain.prepareGround(this, model, x, width, groundHeight, JumpLimits.MAX_RISE + 3, random);

        int interior = width - 2 * GROUND_PADDING;
        int pipeCount = Math.min(MAX_PIPES, (interior + MIN_SPACING) / (PIPE_WIDTH + MIN_SPACING));
        int spare = interior - pipeCount * PIPE_WIDTH - (pipeCount - 1) * MIN_SPACING;
        int pipeX = x + GROUND_PADDING;
        for (int index = 0; index < pipeCount; index++) {
            int height = 2 + random.nextInt(JumpLimits.MAX_RISE - 1);
            int pipeY = groundHeight - height;
            model.setRectangle(pipeX, pipeY, PIPE_WIDTH, height, MarioLevelModel.PIPE);
            if (random.nextInt(5) < 2) {
                model.setBlock(pipeX, pipeY, MarioLevelModel.PIPE_FLOWER);
            }
            if (index == pipeCount - 1) {
                break;
            }
            int spacing = MIN_SPACING + Math.min(spare, random.nextInt(MAX_SPACING - MIN_SPACING + 1));
            spare -= spacing - MIN_SPACING;
            if (random.nextInt(3) == 0) {
                model.setBlock(pipeX + PIPE_WIDTH + 1 + random.nextInt(spacing - 2), groundHeight - 1,
                        MarioLevelModel.GOOMBA);
            }
            pipeX += PIPE_WIDTH + spacing;
        }
        return groundHeight;
    }
}
