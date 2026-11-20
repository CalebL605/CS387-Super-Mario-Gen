package levelGenerators.npclrf.chunks;

import java.util.Random;

import engine.core.MarioLevelModel;

public final class PiranhaKoopaChunk implements Chunk {
    private static final int GROUND_PADDING = 3;
    private static final int PIPE_WIDTH = 2;
    private static final int PIPE_HEIGHT = 3;

    @Override
    public String name() {
        return "piranha-koopa";
    }

    @Override
    public int difficulty() {
        return 3;
    }

    @Override
    public int minWidth() {
        return 24;
    }

    @Override
    public int maxWidth() {
        return 48;
    }

    @Override
    public int place(MarioLevelModel model, int x, int width, int groundHeight, Random random) {
        ChunkTerrain.prepareGround(this, model, x, width, groundHeight, PIPE_HEIGHT + 3, random);

        int leftPipeX = x + GROUND_PADDING;
        int rightPipeX = x + width - GROUND_PADDING - PIPE_WIDTH;
        int pipeY = groundHeight - PIPE_HEIGHT;
        model.setRectangle(leftPipeX, pipeY, PIPE_WIDTH, PIPE_HEIGHT, MarioLevelModel.PIPE);
        model.setRectangle(rightPipeX, pipeY, PIPE_WIDTH, PIPE_HEIGHT, MarioLevelModel.PIPE);
        model.setBlock(leftPipeX, pipeY, MarioLevelModel.PIPE_FLOWER);
        model.setBlock(rightPipeX, pipeY, MarioLevelModel.PIPE_FLOWER);

        int koopaCount = 5 + (width - minWidth()) / 4;
        int chamberX = leftPipeX + PIPE_WIDTH + 1;
        int chamberWidth = rightPipeX - 1 - chamberX;
        for (int index = 0; index < koopaCount; index++) {
            int sectionStart = chamberX + index * chamberWidth / koopaCount;
            int sectionEnd = chamberX + (index + 1) * chamberWidth / koopaCount;
            int koopaX = sectionStart + random.nextInt(sectionEnd - sectionStart);
            model.setBlock(koopaX, groundHeight - 1, MarioLevelModel.GREEN_KOOPA);
        }
        return groundHeight;
    }
}
