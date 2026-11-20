package levelGenerators.npclrf.chunks;

import java.util.Random;

import engine.core.MarioLevelModel;

public final class FlatChunk implements Chunk {
    @Override
    public String name() {
        return "flat";
    }

    @Override
    public int difficulty() {
        return 0;
    }

    @Override
    public int minWidth() {
        return 4;
    }

    @Override
    public int maxWidth() {
        return 16;
    }

    @Override
    public int place(MarioLevelModel model, int x, int width, int groundHeight, Random random) {
        ChunkTerrain.prepareGround(this, model, x, width, groundHeight, 3, random);
        return groundHeight;
    }
}
