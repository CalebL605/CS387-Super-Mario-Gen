package levelGenerators.npclrf.chunks;

import java.util.Objects;
import java.util.Random;

import engine.core.MarioLevelModel;

final class ChunkTerrain {
    private ChunkTerrain() {}

    static void prepareGround(Chunk chunk, MarioLevelModel model, int x, int width,
            int groundHeight, int headroom, Random random) {
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(random, "random");

        if (width < chunk.minWidth() || width > chunk.maxWidth()) {
            throw new IllegalArgumentException(chunk.name() + " chunk width must be between "
                    + chunk.minWidth() + " and " + chunk.maxWidth() + ".");
        }
        if (x < 0 || x > model.getWidth() - width) {
            throw new IllegalArgumentException("Chunk columns must fit inside the level.");
        }
        if (groundHeight < headroom || groundHeight >= model.getHeight()) {
            throw new IllegalArgumentException("Ground must be inside the level with at least "
                    + headroom + " rows above it.");
        }

        model.setRectangle(x, 0, width, model.getHeight(), MarioLevelModel.EMPTY);
        model.setRectangle(x, groundHeight, width, model.getHeight() - groundHeight, MarioLevelModel.GROUND);
    }
}
