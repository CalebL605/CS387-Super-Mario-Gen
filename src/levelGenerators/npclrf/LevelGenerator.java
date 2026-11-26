package levelGenerators.npclrf;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import engine.core.MarioLevelGenerator;
import engine.core.MarioLevelModel;
import engine.core.MarioTimer;
import levelGenerators.npclrf.chunks.Chunk;
import levelGenerators.npclrf.chunks.ExitChunk;
import levelGenerators.npclrf.chunks.StartChunk;

public class LevelGenerator implements MarioLevelGenerator {
    public static final int MIN_WIDTH = PacingGrammar.MIN_WIDTH;
    /** Times a failing section is re-placed with new random choices before it falls back to flat ground. */
    public static final int MAX_SECTION_RETRIES = 3;
    private static final int MIN_HEIGHT = 10;
    private final Random random;
    private Stats lastStats = new Stats(0, 0);

    /**
     * Generate-and-test counts for the most recent level.
     *
     * @param retries   sections re-placed with new random choices after a failed check
     * @param fallbacks sections replaced by flat ground after using up their retries
     */
    public record Stats(int retries, int fallbacks) {
    }

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
        Chunk[] chunks = new Chunk[sections.size()];
        int[] starts = new int[sections.size()];
        int[] entryHeights = new int[sections.size()];
        int x = 0;
        for (int index = 0; index < sections.size(); index++) {
            chunks[index] = sections.get(index).chunk();
            starts[index] = x;
            x += sections.get(index).width();
        }

        model.clearMap();
        int groundHeight = model.getHeight() - 2;
        for (int index = 0; index < sections.size(); index++) {
            entryHeights[index] = groundHeight;
            groundHeight = chunks[index].place(model, starts[index], sections.get(index).width(), groundHeight, random);
        }

        // Generate & test [PCG II slide 47]: check the whole level, then re-place the section where
        // Mario gets stuck. Every retry draws from the same seeded Random, so retries are reproducible.
        int[] sectionRetries = new int[sections.size()];
        boolean[] flattened = new boolean[sections.size()];
        int retries = 0;
        int fallbacks = 0;
        while (true) {
            PlayabilityChecker.Result result = PlayabilityChecker.check(model.getMap());
            if (result.playable()) {
                break;
            }
            int failing = failingSection(result.failColumn(), starts, flattened);
            if (failing < 0) {
                throw new IllegalStateException("Every section is flat but the level is unplayable: " + result.reason());
            }
            if (sectionRetries[failing] < MAX_SECTION_RETRIES) {
                sectionRetries[failing]++;
                retries++;
            } else {
                // Out of retries: fall back to flat ground, which the checker always accepts.
                chunks[failing] = fallback(failing, sections.size());
                flattened[failing] = true;
                fallbacks++;
            }
            placeFrom(failing, model, sections, chunks, starts, entryHeights);
        }
        lastStats = new Stats(retries, fallbacks);
        return model.getMap();
    }

    /**
     * Re-places section {@code first}. If its exit height changed, the following sections are
     * re-placed too, until one exits at the height the next section already expects.
     */
    private void placeFrom(int first, MarioLevelModel model, List<PacingGrammar.Section> sections,
            Chunk[] chunks, int[] starts, int[] entryHeights) {
        for (int index = first; index < sections.size(); index++) {
            int exitHeight = chunks[index].place(model, starts[index], sections.get(index).width(),
                    entryHeights[index], random);
            if (index + 1 == sections.size() || entryHeights[index + 1] == exitHeight) {
                return;
            }
            entryHeights[index + 1] = exitHeight;
        }
    }

    /** The section holding the fail column, or the nearest one to its right that is not yet flat. */
    private static int failingSection(int failColumn, int[] starts, boolean[] flattened) {
        int section = 0;
        while (section + 1 < starts.length && starts[section + 1] <= failColumn) {
            section++;
        }
        for (int index = section; index < starts.length; index++) {
            if (!flattened[index]) {
                return index;
            }
        }
        for (int index = section - 1; index >= 0; index--) {
            if (!flattened[index]) {
                return index;
            }
        }
        return -1;
    }

    private static Chunk fallback(int section, int sectionCount) {
        if (section == 0) {
            return new StartChunk();
        }
        if (section == sectionCount - 1) {
            return new ExitChunk();
        }
        return FLAT_GROUND;
    }

    /** Flat ground of any width; the fallback for a section that keeps failing the check. */
    private static final Chunk FLAT_GROUND = new Chunk() {
        @Override
        public String name() {
            return "flat-fallback";
        }

        @Override
        public int difficulty() {
            return 0;
        }

        @Override
        public int minWidth() {
            return 1;
        }

        @Override
        public int maxWidth() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int place(MarioLevelModel model, int x, int width, int groundHeight, Random random) {
            model.setRectangle(x, 0, width, model.getHeight(), MarioLevelModel.EMPTY);
            model.setRectangle(x, groundHeight, width, model.getHeight() - groundHeight, MarioLevelModel.GROUND);
            return groundHeight;
        }
    };

    /** Generate-and-test counts for the level returned by the latest {@link #getGeneratedLevel} call. */
    public Stats lastStats() {
        return lastStats;
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
