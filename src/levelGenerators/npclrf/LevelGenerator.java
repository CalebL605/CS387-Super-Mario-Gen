package levelGenerators.npclrf;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import engine.core.MarioLevelGenerator;
import engine.core.MarioLevelModel;
import engine.core.MarioTimer;

public class LevelGenerator implements MarioLevelGenerator {
    public static final int MIN_WIDTH = PacingGrammar.MIN_WIDTH;
    private static final int MIN_HEIGHT = 10;
    private final Random random;

    public LevelGenerator(long randomSeed) {
        this.random = new Random(randomSeed);
    }

    @Override
    public String getGeneratedLevel(MarioLevelModel model, MarioTimer timer) {
        Objects.requireNonNull(model, "model");
        if (model.getWidth() < MIN_WIDTH) {
            throw new IllegalArgumentException("Level width must be at least " + MIN_WIDTH + " tiles.");
        }
        if (model.getHeight() < MIN_HEIGHT) {
            throw new IllegalArgumentException("Level height must be at least " + MIN_HEIGHT + " tiles.");
        }

        List<PacingGrammar.Section> sections = PacingGrammar.plan(model.getWidth(), random);
        model.clearMap();
        int x = 0;
        int groundHeight = model.getHeight() - 2;
        for (PacingGrammar.Section section : sections) {
            groundHeight = section.chunk().place(model, x, section.width(), groundHeight, random);
            x += section.width();
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
