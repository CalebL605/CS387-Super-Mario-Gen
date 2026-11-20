package levelGenerators.npclrf.chunks;

import java.util.Objects;
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
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(random, "random");

        if (width < minWidth() || width > maxWidth()) {
            throw new IllegalArgumentException("Flat chunk width must be between 4 and 16.");
        }
        if (x < 0 || x > model.getWidth() - width) {
            throw new IllegalArgumentException("Chunk columns must fit inside the level.");
        }
        if (groundHeight < 3 || groundHeight >= model.getHeight()) {
            throw new IllegalArgumentException("Ground must be inside the level with at least three rows above it.");
        }

        model.setRectangle(x, 0, width, model.getHeight(), MarioLevelModel.EMPTY);
        model.setRectangle(x, groundHeight, width, model.getHeight() - groundHeight, MarioLevelModel.GROUND);
        return groundHeight;
    }
}
