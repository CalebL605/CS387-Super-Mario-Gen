package levelGenerators.npclrf;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import engine.core.MarioLevelModel;
import engine.core.MarioTimer;

public final class Check {
    private Check() {} // Class is not instantiable

    public static void main(String[] args) {
        try {
            System.exit(check(args));
        } catch (IllegalArgumentException exception) {
            System.err.println("Invalid arguments: " + exception.getMessage());
            printUsage(System.err);
            System.exit(2);
        } catch (IOException exception) {
            System.err.println("Could not read level file: " + exception.getMessage());
            System.exit(1);
        }
    }

    // return the process exit status: 0 when every level is playable, 3 otherwise
    private static int check(String[] args) throws IOException {
        List<Path> files = new ArrayList<>();
        int margin = JumpLimits.MARGIN;
        long firstSeed = 0;
        long lastSeed = -1;
        int width = 200;
        for (int index = 0; index < args.length; index++) {
            String arg = args[index];
            switch (arg) {
                case "--measured" -> margin = 0;
                case "--seeds" -> {
                    firstSeed = Long.parseLong(value(args, ++index, arg));
                    lastSeed = Long.parseLong(value(args, ++index, arg));
                    if (lastSeed < firstSeed) {
                        throw new IllegalArgumentException("--seeds needs <from> <= <to>.");
                    }
                }
                case "--width" -> width = Integer.parseInt(value(args, ++index, arg));
                case "--spot-check" -> {
                    return CheckerSpotCheck.run() ? 0 : 3;
                }
                case "--chunks" -> {
                    return ChunkAudit.run() ? 0 : 3;
                }
                case "--help", "-h" -> {
                    printUsage(System.out);
                    return 0;
                }
                default -> {
                    if (arg.startsWith("--")) {
                        throw new IllegalArgumentException("Unknown option " + arg);
                    }
                    files.add(Path.of(arg));
                }
            }
        }
        if (lastSeed >= firstSeed) {
            return checkSeeds(firstSeed, lastSeed, width);
        }
        if (files.isEmpty()) {
            throw new IllegalArgumentException("Give at least one level file, --seeds or --spot-check.");
        }

        int failures = 0;
        for (Path file : files) {
            PlayabilityChecker.Result result = PlayabilityChecker.check(
                    Files.readString(file, StandardCharsets.UTF_8), margin);
            if (!result.playable()) {
                failures++;
            }
            System.out.printf("%-4s %s%s%n", result.playable() ? "PASS" : "FAIL", file,
                    result.playable() ? "" : " (column " + result.failColumn() + ": " + result.reason() + ")");
        }
        System.out.printf("%d of %d levels playable%n", files.size() - failures, files.size());
        return failures == 0 ? 0 : 3;
    }

    /**
     * Generates each seed twice, checks the level, confirms both copies are identical, and prints the
     * generate-and-test counts as CSV followed by a summary.
     */
    private static int checkSeeds(long firstSeed, long lastSeed, int width) {
        if (width < LevelGenerator.MIN_WIDTH) {
            throw new IllegalArgumentException("--width must be at least " + LevelGenerator.MIN_WIDTH + ".");
        }
        LevelGenerator generator = new LevelGenerator(firstSeed);
        int levels = 0;
        int failures = 0;
        int mismatches = 0;
        int retries = 0;
        int fallbacks = 0;
        int maxRetries = 0;
        System.out.println("seed,playable,reproducible,retries,fallbacks");
        for (long seed = firstSeed; seed <= lastSeed; seed++) {
            generator.setRandomSeed(seed);
            String level = generator.getGeneratedLevel(new MarioLevelModel(width, Generate.LEVEL_HEIGHT),
                    new MarioTimer(Generate.GENERATION_TIME));
            LevelGenerator.Stats stats = generator.lastStats();
            generator.setRandomSeed(seed);
            boolean reproducible = level.equals(generator.getGeneratedLevel(
                    new MarioLevelModel(width, Generate.LEVEL_HEIGHT), new MarioTimer(Generate.GENERATION_TIME)));
            boolean playable = PlayabilityChecker.check(level).playable();

            levels++;
            failures += playable ? 0 : 1;
            mismatches += reproducible ? 0 : 1;
            retries += stats.retries();
            fallbacks += stats.fallbacks();
            maxRetries = Math.max(maxRetries, stats.retries());
            System.out.printf("%d,%b,%b,%d,%d%n", seed, playable, reproducible, stats.retries(), stats.fallbacks());
            if (seed == Long.MAX_VALUE) {
                break;
            }
        }
        System.out.printf("Seeds %d-%d: %d of %d playable, %d not reproducible; retries total %d (max %d per level), "
                + "flat fallbacks %d%n", firstSeed, lastSeed, levels - failures, levels, mismatches, retries,
                maxRetries, fallbacks);
        return failures == 0 && mismatches == 0 ? 0 : 3;
    }

    private static String value(String[] args, int index, String option) {
        if (index >= args.length || args[index].startsWith("--")) {
            throw new IllegalArgumentException("Missing value for " + option);
        }
        return args[index];
    }

    private static void printUsage(PrintStream output) {
        output.print("""
                Usage: java -cp out levelGenerators.npclrf.Check [--measured] <level file>...
                       java -cp out levelGenerators.npclrf.Check --seeds <from> <to> [--width <int>]
                       java -cp out levelGenerators.npclrf.Check --spot-check
                       java -cp out levelGenerators.npclrf.Check --chunks

                Runs the static playability checker on each level file and prints PASS or FAIL.
                By default it uses the design limits the generator builds to (measured minus a
                1-tile margin). --measured uses the raw engine measurements instead.
                --seeds generates every seed in the range twice (default width 200), checks it, and
                prints seed,playable,reproducible,retries,fallbacks as CSV plus a summary line.
                --spot-check runs hand-built levels with known answers.
                --chunks places every grammar chunk alone at every width and checks it.
                Exits with status 3 if any level is unplayable or not reproducible.
                """);
    }
}
