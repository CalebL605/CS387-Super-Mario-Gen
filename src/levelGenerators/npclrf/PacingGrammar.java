package levelGenerators.npclrf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import levelGenerators.npclrf.chunks.BlockRowChunk;
import levelGenerators.npclrf.chunks.BulletBillPlatformChunk;
import levelGenerators.npclrf.chunks.Chunk;
import levelGenerators.npclrf.chunks.EnemyGroupChunk;
import levelGenerators.npclrf.chunks.ExitChunk;
import levelGenerators.npclrf.chunks.FlatChunk;
import levelGenerators.npclrf.chunks.FloatingPlatformChunk;
import levelGenerators.npclrf.chunks.GapRunChunk;
import levelGenerators.npclrf.chunks.PiranhaKoopaChunk;
import levelGenerators.npclrf.chunks.PipeRunChunk;
import levelGenerators.npclrf.chunks.SingleGapChunk;
import levelGenerators.npclrf.chunks.SingleGoombaChunk;
import levelGenerators.npclrf.chunks.SinglePipeChunk;
import levelGenerators.npclrf.chunks.StaircaseExitChunk;
import levelGenerators.npclrf.chunks.StaircaseGapChunk;
import levelGenerators.npclrf.chunks.StartChunk;

final class PacingGrammar {
    private enum Symbol {
        START, BUILDUP, PEAK, RECOVERY, CLIMAX, EXIT, D0, D1, D2, D3, D4
    }

    record Section(Chunk chunk, int width) {
    }

    private static final List<Symbol> STRUCTURE = List.of(
            Symbol.START, Symbol.BUILDUP, Symbol.PEAK, Symbol.RECOVERY,
            Symbol.BUILDUP, Symbol.CLIMAX, Symbol.EXIT);

    private static final Map<Symbol, List<Chunk>> CHUNKS = Map.of(
            Symbol.START, List.of(new StartChunk()),
            Symbol.EXIT, List.of(new ExitChunk(), new StaircaseExitChunk()),
            Symbol.D0, List.of(new FlatChunk(), new BlockRowChunk()),
            Symbol.D1, List.of(new SingleGapChunk(), new SinglePipeChunk(), new SingleGoombaChunk()),
            Symbol.D2, List.of(new StaircaseGapChunk(), new GapRunChunk(), new PipeRunChunk(), new EnemyGroupChunk()),
            Symbol.D3, List.of(new PiranhaKoopaChunk(), new FloatingPlatformChunk()),
            Symbol.D4, List.of(new BulletBillPlatformChunk()));

    private static final Map<Symbol, List<List<Symbol>>> RULES = Map.of(
            Symbol.BUILDUP, List.of(
                    List.of(Symbol.D1),
                    List.of(Symbol.D1, Symbol.BUILDUP),
                    List.of(Symbol.D1, Symbol.D2)),
            Symbol.PEAK, List.of(
                    List.of(Symbol.D3),
                    List.of(Symbol.D2, Symbol.PEAK)),
            Symbol.RECOVERY, List.of(
                    List.of(Symbol.D0),
                    List.of(Symbol.D0, Symbol.RECOVERY)),
            Symbol.CLIMAX, List.of(
                    List.of(Symbol.D3),
                    List.of(Symbol.D3, Symbol.CLIMAX),
                    List.of(Symbol.D4)));

    static final int MIN_WIDTH = minimumWidth(STRUCTURE);

    private PacingGrammar() {}

    static List<Section> plan(int width, Random random) {
        if (width < MIN_WIDTH) {
            throw new IllegalArgumentException("The pacing grammar needs at least " + MIN_WIDTH + " columns.");
        }

        List<Symbol> symbols = new ArrayList<>(STRUCTURE);
        int minimumTotal = MIN_WIDTH;
        int index = 0;

        // Expand left to right, reserving the minimum width of everything still pending.
        while (index < symbols.size()) {
            Symbol symbol = symbols.get(index);
            List<List<Symbol>> productions = RULES.get(symbol);
            if (productions == null) {
                index++;
                continue;
            }

            int otherMinimum = minimumTotal - minimumWidth(symbol);
            List<List<Symbol>> fitting = new ArrayList<>();
            for (List<Symbol> production : productions) {
                if (minimumWidth(production) <= width - otherMinimum) {
                    fitting.add(production);
                }
            }
            List<Symbol> chosen = fitting.get(random.nextInt(fitting.size()));
            minimumTotal = otherMinimum + minimumWidth(chosen);
            symbols.remove(index);
            symbols.addAll(index, chosen);
        }

        List<Chunk> chunks = new ArrayList<>();
        int minimumRemaining = minimumTotal;
        int available = width;
        long maximumTotal = 0;
        for (Symbol symbol : symbols) {
            minimumRemaining -= minimumWidth(symbol);
            List<Chunk> fitting = new ArrayList<>();
            for (Chunk chunk : CHUNKS.get(symbol)) {
                if (chunk.minWidth() <= available - minimumRemaining) {
                    fitting.add(chunk);
                }
            }
            Chunk chosen = fitting.get(random.nextInt(fitting.size()));
            chunks.add(chosen);
            available -= chosen.minWidth();
            maximumTotal += chosen.maxWidth();
        }

        // Extend the recovery production if the selected chunks cannot span the whole level.
        int recoveryIndex = symbols.indexOf(Symbol.D0);
        FlatChunk recovery = new FlatChunk();
        while (maximumTotal < width) {
            if (available < recovery.minWidth()) {
                throw new IllegalStateException("The selected chunk widths cannot fill the level.");
            }
            chunks.add(recoveryIndex, recovery);
            available -= recovery.minWidth();
            maximumTotal += recovery.maxWidth();
        }

        List<Section> sections = new ArrayList<>();
        long remainingCapacity = maximumTotal - (width - available);
        for (Chunk chunk : chunks) {
            int capacity = chunk.maxWidth() - chunk.minWidth();
            remainingCapacity -= capacity;
            int minimumExtra = (int) Math.max(0L, available - remainingCapacity);
            int maximumExtra = Math.min(capacity, available);
            int extra = minimumExtra + random.nextInt(maximumExtra - minimumExtra + 1);
            sections.add(new Section(chunk, chunk.minWidth() + extra));
            available -= extra;
        }
        return List.copyOf(sections);
    }

    static List<Chunk> chunks() {
        List<Chunk> chunks = new ArrayList<>();
        for (Symbol symbol : Symbol.values()) {
            chunks.addAll(CHUNKS.getOrDefault(symbol, List.of()));
        }
        return List.copyOf(chunks);
    }

    private static int minimumWidth(List<Symbol> symbols) {
        int total = 0;
        for (Symbol symbol : symbols) {
            total += minimumWidth(symbol);
        }
        return total;
    }

    private static int minimumWidth(Symbol symbol) {
        // Minimum terminating forms for the four recursive productions.
        return switch (symbol) {
            case BUILDUP -> minimumWidth(Symbol.D1);
            case PEAK, CLIMAX -> minimumWidth(Symbol.D3);
            case RECOVERY -> minimumWidth(Symbol.D0);
            default -> CHUNKS.get(symbol).stream().mapToInt(Chunk::minWidth).min().orElseThrow();
        };
    }
}
