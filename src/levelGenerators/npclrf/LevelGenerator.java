package levelGenerators.npclrf;

import java.util.Random;

import engine.core.MarioLevelGenerator;
import engine.core.MarioLevelModel;
import engine.core.MarioTimer;

public class LevelGenerator implements MarioLevelGenerator {
    private final Random random;

    public LevelGenerator(long randomSeed) {
        this.random = new Random(randomSeed);
    }

    @Override
    public String getGeneratedLevel(MarioLevelModel model, MarioTimer timer) {
        // TODO (#6): Assemble start, paced challenge/recovery chunks, and exit.
        // Clear the level map before generating a new level
        model.clearMap();

        int width = model.getWidth();
        int height = model.getHeight();

        // Fill the bottom two rows with solid ground.
        int groundY = height - 2;
        model.setRectangle(0, groundY, width, 2, MarioLevelModel.GROUND);

        // Place Mario and the finish directly above the ground on opposite ends.
        model.setBlock(1, groundY - 1, MarioLevelModel.MARIO_START);
        model.setBlock(width - 2, groundY - 1, MarioLevelModel.MARIO_EXIT);

        // Each interior column has a 15% chance of containing a coin.
        for (int x = 2; x < width - 2; x++) {
            if (random.nextDouble() < 0.15) {
                model.setBlock(x, groundY - 2, MarioLevelModel.COIN);
            }
        }

        return model.getMap();
    }

    @Override
    public String getGeneratorName() {
        return "NPCLRF Level Generator";
    }

    // Method to set the random seed for reproducibility
    public void setRandomSeed(long randomSeed) {
        this.random.setSeed(randomSeed);
    }

}
