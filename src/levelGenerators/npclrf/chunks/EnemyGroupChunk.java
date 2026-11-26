package levelGenerators.npclrf.chunks;

import java.util.Random;

import engine.core.MarioLevelModel;

/**
 * A group of two to four enemies on flat ground (goombas, koopas, and at most one winged enemy), at
 * least two tiles apart, sometimes under a row of bricks as in the original levels.
 */
public final class EnemyGroupChunk implements Chunk {
    private static final int GROUND_PADDING = 3;
    private static final int SPACING = 2;
    private static final int BRICK_HEIGHT = 4;
    private static final char[] WALKERS = {
            MarioLevelModel.GOOMBA, MarioLevelModel.GOOMBA, MarioLevelModel.GREEN_KOOPA, MarioLevelModel.RED_KOOPA};
    private static final char[] FLYERS = {MarioLevelModel.GOOMBA_WINGED, MarioLevelModel.GREEN_KOOPA_WINGED};

    @Override
    public String name() {
        return "enemy-group";
    }

    @Override
    public int difficulty() {
        return 2;
    }

    @Override
    public int minWidth() {
        return 12;
    }

    @Override
    public int maxWidth() {
        return 24;
    }

    @Override
    public int place(MarioLevelModel model, int x, int width, int groundHeight, Random random) {
        ChunkTerrain.prepareGround(this, model, x, width, groundHeight, BRICK_HEIGHT + 2, random);

        int interior = width - 2 * GROUND_PADDING;
        int maxEnemies = Math.min(4, (interior + SPACING) / (SPACING + 1));
        int enemyCount = 2 + random.nextInt(maxEnemies - 1);
        int spare = interior - enemyCount - (enemyCount - 1) * SPACING;
        int enemyX = x + GROUND_PADDING;
        int flyer = random.nextInt(2) == 0 ? random.nextInt(enemyCount) : -1;
        for (int index = 0; index < enemyCount; index++) {
            int shift = random.nextInt(spare + 1);
            enemyX += shift;
            spare -= shift;
            char enemy = index == flyer
                    ? FLYERS[random.nextInt(FLYERS.length)] : WALKERS[random.nextInt(WALKERS.length)];
            model.setBlock(enemyX, groundHeight - 1, enemy);
            enemyX += 1 + SPACING;
        }

        if (random.nextInt(2) == 0) {
            int brickLength = 3 + random.nextInt(Math.min(6, interior) - 2);
            int brickX = x + GROUND_PADDING + random.nextInt(interior - brickLength + 1);
            model.setRectangle(brickX, groundHeight - BRICK_HEIGHT, brickLength, 1, MarioLevelModel.NORMAL_BRICK);
            model.setBlock(brickX + random.nextInt(brickLength), groundHeight - BRICK_HEIGHT,
                    MarioLevelModel.COIN_QUESTION_BLOCK);
        }
        return groundHeight;
    }
}
