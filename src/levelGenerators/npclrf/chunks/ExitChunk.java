package levelGenerators.npclrf.chunks;

import java.util.Random;

import engine.core.MarioLevelModel;

public final class ExitChunk implements Chunk {
    private final FlatChunk ground = new FlatChunk();

    @Override
    public String name() {
        return "exit";
    }

    @Override
    public int difficulty() {
        return 0;
    }

    @Override
    public int minWidth() {
        return 8;
    }

    @Override
    public int maxWidth() {
        return 16;
    }

    @Override
    public int place(MarioLevelModel model, int x, int width, int groundHeight, Random random) {
        if (width < minWidth() || width > maxWidth()) {
            throw new IllegalArgumentException("Exit chunk width must be between 8 and 16.");
        }
        ground.place(model, x, width, groundHeight, random);
        model.setBlock(x + width - 2, groundHeight - 1, MarioLevelModel.MARIO_EXIT);
        return groundHeight;
    }
}
