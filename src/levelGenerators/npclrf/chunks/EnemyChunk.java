package levelGenerators.npclrf.chunks;

import java.util.Random;

import engine.core.MarioLevelModel;

public final class EnemyChunk implements Chunk {
    private static final int GROUND_PADDING = 3;
    private final FlatChunk ground = new FlatChunk();

    @Override
    public String name() {
        return "enemy";
    }

    @Override
    public int difficulty() {
        return 1;
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
            throw new IllegalArgumentException("Enemy chunk width must be between 8 and 16.");
        }
        ground.place(model, x, width, groundHeight, random);

        int positionCount = width - 2 * GROUND_PADDING;
        int enemyX = x + GROUND_PADDING + random.nextInt(positionCount);
        model.setBlock(enemyX, groundHeight - 1, MarioLevelModel.GOOMBA);
        return groundHeight;
    }
}
