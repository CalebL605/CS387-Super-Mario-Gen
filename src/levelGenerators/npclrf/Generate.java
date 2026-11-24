package levelGenerators.npclrf;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import engine.core.MarioLevelModel;
import engine.core.MarioTimer;

public final class Generate {
    private static final int LEVEL_HEIGHT = 16;
    private static final long GENERATION_TIME = 5 * 60 * 60 * 1000;

    private Generate() {} // Class is not instantiable

    public static void main(String[] args) {
        try {
            generate(args);
        } catch (IllegalArgumentException exception) {
            System.err.println("Invalid arguments: " + exception.getMessage());
            printUsage(System.err);
            System.exit(2);
        } catch (IOException exception) {
            System.err.println("Could not write level files: " + exception.getMessage());
            System.exit(1);
        }
    }

    private static void generate(String[] args) throws IOException {
        long seed = 1L;
        int count = 1;
        int width = 200;
        Path outputDirectory = Path.of("levels", "npclrf");

        for (int index = 0; index < args.length; index++) {
            String option = args[index];
            switch (option) {
                case "--seed" -> seed = Long.parseLong(optionValue(args, ++index, option));
                case "--count" -> count = Integer.parseInt(optionValue(args, ++index, option));
                case "--width" -> width = Integer.parseInt(optionValue(args, ++index, option));
                case "--out" -> outputDirectory = Path.of(optionValue(args, ++index, option));
                case "--help", "-h" -> {
                    printUsage(System.out);
                    return;
                }
                default -> throw new IllegalArgumentException("Unknown option " + option);
            }
        }

        if (count < 1) {
            throw new IllegalArgumentException("--count must be at least 1.");
        }
        if (width < LevelGenerator.MIN_WIDTH) {
            throw new IllegalArgumentException("--width must be at least " + LevelGenerator.MIN_WIDTH
                    + " tiles for the pacing grammar.");
        }
        if (seed > Long.MAX_VALUE - (count - 1L)) {
            throw new IllegalArgumentException("The requested seed range exceeds Long.MAX_VALUE.");
        }

        Files.createDirectories(outputDirectory);
        LevelGenerator generator = new LevelGenerator(seed);
        MarioLevelModel model = new MarioLevelModel(width, LEVEL_HEIGHT);

        for (int index = 0; index < count; index++) {
            long levelSeed = seed + index;
            generator.setRandomSeed(levelSeed);
            String level = generator.getGeneratedLevel(model, new MarioTimer(GENERATION_TIME));
            Path outputFile = outputDirectory.resolve("lvl-" + (index + 1) + ".txt");

            Files.writeString(outputFile, level, StandardCharsets.UTF_8);
            System.out.printf("Wrote %s (seed %d)%n", outputFile, levelSeed);
        }
    }

    private static String optionValue(String[] args, int index, String option) {
        if (index >= args.length || args[index].isBlank() || args[index].startsWith("--")) {
            throw new IllegalArgumentException("Missing value for " + option);
        }
        return args[index];
    }

    private static void printUsage(PrintStream output) {
        output.print("""
                Usage: java -cp out levelGenerators.npclrf.Generate [options]

                  --seed <long>   Starting seed (default 1)
                  --count <int>   Number of levels, at least 1 (default 1)
                  --width <int>   Width in tiles, at least %d (default 200)
                  --out <path>    Output directory (default levels/npclrf)
                  --help, -h      Shows this help message

                Levels have 16 rows. Files are named lvl-1.txt, lvl-2.txt, and so on.
                Each file uses the next consecutive seed. Existing matching files are replaced.
                """.formatted(LevelGenerator.MIN_WIDTH));
    }
}
