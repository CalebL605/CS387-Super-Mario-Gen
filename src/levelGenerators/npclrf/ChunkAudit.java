package levelGenerators.npclrf;

import java.util.Random;

import engine.core.MarioLevelModel;
import levelGenerators.npclrf.chunks.Chunk;
import levelGenerators.npclrf.chunks.ExitChunk;
import levelGenerators.npclrf.chunks.StartChunk;

/**
 * Places every chunk in the pacing grammar on its own, between a start and an exit, at every width it
 * allows and for a range of seeds, then runs the playability checker. Run by {@code Check --chunks}.
 * A chunk that passes here stays within the jump limits by itself; generate & test covers how chunks combine.
 */
final class ChunkAudit {
    private static final int HEIGHT = 16;
    private static final int PAD = 8;
    private static final int SEEDS = 50;

    private ChunkAudit() {} // Class is not instantiable

    // return true when every placement of every chunk is playable
    static boolean run() {
        boolean allPass = true;
        System.out.printf("%-24s %-6s %-8s %-10s %s%n", "chunk", "level", "widths", "placements", "unplayable");
        for (Chunk chunk : PacingGrammar.chunks()) {
            if (chunk instanceof StartChunk) {
                continue; // it is the left padding of every placement below
            }
            int placements = 0;
            int failures = 0;
            String example = "";
            for (int width = chunk.minWidth(); width <= chunk.maxWidth(); width++) {
                for (int seed = 0; seed < SEEDS; seed++) {
                    MarioLevelModel model = new MarioLevelModel(PAD + width + PAD, HEIGHT);
                    Random random = new Random(seed);
                    int ground = HEIGHT - 2;
                    ground = new StartChunk().place(model, 0, PAD, ground, random);
                    ground = chunk.place(model, PAD, width, ground, random);
                    new ExitChunk().place(model, PAD + width, PAD, ground, random);
                    PlayabilityChecker.Result result = PlayabilityChecker.check(model.getMap());
                    placements++;
                    if (!result.playable()) {
                        failures++;
                        if (example.isEmpty()) {
                            example = "  e.g. width " + width + " seed " + seed + ": " + result.reason();
                        }
                    }
                }
            }
            allPass &= failures == 0;
            System.out.printf("%-24s %-6d %-8s %-10d %d%s%n", chunk.name(), chunk.difficulty(),
                    chunk.minWidth() + "-" + chunk.maxWidth(), placements, failures, example);
        }
        System.out.println(allPass ? "Every chunk placement is playable." : "Some chunk placements are NOT playable.");
        return allPass;
    }
}
