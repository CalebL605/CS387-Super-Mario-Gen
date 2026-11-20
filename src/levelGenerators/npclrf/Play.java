package levelGenerators.npclrf;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import engine.core.MarioGame;
import engine.core.MarioResult;

public final class Play {
    private static final int GAME_TIME_SECONDS = 20;
    private static final int MARIO_STATE = 0;

    public static void main(String[] args) {
        try {
            play(args);
        } catch (IllegalArgumentException exception) {
            System.err.println("Invalid arguments: " + exception.getMessage());
            printUsage(System.err);
            System.exit(2);
        } catch (IOException exception) {
            System.err.println("Could not read level file: " + exception.getMessage());
            System.exit(1);
        }
    }

    private static void play(String[] args) throws IOException {
        Path levelPath = Path.of("levels", "npclrf", "lvl-1.txt");
        boolean humanControlled = false;

        for (int index = 0; index < args.length; index++) {
            String option = args[index];
            switch (option) {
                case "--level" -> {
                    if (index + 1 >= args.length || args[index + 1].isBlank()
                            || args[index + 1].startsWith("--")) {
                        throw new IllegalArgumentException("Missing value for --level");
                    }
                    levelPath = Path.of(args[++index]);
                }
                case "--human" -> humanControlled = true;
                case "--agent" -> humanControlled = false;
                case "--help", "-h" -> {
                    printUsage(System.out);
                    return;
                }
                default -> throw new IllegalArgumentException("Unknown option " + option);
            }
        }

        String level = Files.readString(levelPath, StandardCharsets.UTF_8);
        if (level.isBlank()) {
            throw new IllegalArgumentException("Level file is empty: " + levelPath);
        }

        System.out.printf("Opening %s (%s mode)%n", levelPath, humanControlled ? "human" : "agent");
        MarioGame game = new MarioGame();
        MarioResult result = humanControlled
                ? game.playGame(level, GAME_TIME_SECONDS, MARIO_STATE)
                : game.runGame(new agents.robinBaumgarten.Agent(), level, GAME_TIME_SECONDS, MARIO_STATE, true);

        System.out.printf("Game Status: %s Percentage Completion: %.1f%%%nCoins: %d Kills: %d Remaining Time: %d seconds%n",
                result.getGameStatus(),
                result.getCompletionPercentage() * 100,
                result.getCurrentCoins(),
                result.getKillsTotal(),
                (int) Math.ceil(result.getRemainingTime() / 1000f));
    }

    private static void printUsage(PrintStream output) {
        output.print("""
                Usage: java -cp out levelGenerators.npclrf.Play [options]

                  --level <path>  Saved level file (default levels/npclrf/lvl-1.txt)
                  --agent         Watch the Robin Baumgarten agent play (default)
                  --human         Play using the keyboard
                  --help, -h      Shows this help message

                Run from the repository root so the framework can load the level file.
                Keyboard controls are Left/Right to move, Down to duck, S to jump, and A to run/shoot.
                """);
    }
}
