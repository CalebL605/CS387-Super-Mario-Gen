package levelGenerators.npclrf;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import engine.core.MarioGame;
import engine.core.MarioSprite;
import engine.core.MarioWorld;
import engine.helper.Assets;
import engine.helper.SpriteType;

/**
 * Renders a whole level file to a PNG offscreen, with the engine's own tiles and sprites, wrapped
 * into rows so it stays readable at page width. No window and no game loop: enemies are drawn at
 * their starting tiles.
 */
public final class Screenshot {
    private static final int TILE = 16;
    private static final int ROW_GAP = 8;

    private Screenshot() {} // Class is not instantiable

    public static void main(String[] args) {
        try {
            render(args);
        } catch (IllegalArgumentException exception) {
            System.err.println("Invalid arguments: " + exception.getMessage());
            printUsage(System.err);
            System.exit(2);
        } catch (IOException exception) {
            System.err.println("Could not read or write a file: " + exception.getMessage());
            System.exit(1);
        }
    }

    private static void render(String[] args) throws IOException {
        Path levelPath = null;
        Path outputPath = null;
        int columnsPerRow = 50;
        for (int index = 0; index < args.length; index++) {
            switch (args[index]) {
                case "--level" -> levelPath = Path.of(value(args, ++index, "--level"));
                case "--out" -> outputPath = Path.of(value(args, ++index, "--out"));
                case "--columns" -> columnsPerRow = Integer.parseInt(value(args, ++index, "--columns"));
                case "--help", "-h" -> {
                    printUsage(System.out);
                    return;
                }
                default -> throw new IllegalArgumentException("Unknown option " + args[index]);
            }
        }
        if (levelPath == null || outputPath == null) {
            throw new IllegalArgumentException("--level and --out are required.");
        }
        if (columnsPerRow < MarioGame.width / TILE) {
            throw new IllegalArgumentException("--columns must be at least " + MarioGame.width / TILE + ".");
        }

        String level = Files.readString(levelPath, StandardCharsets.UTF_8);
        BufferedImage strip = renderStrip(level);
        int rowWidth = columnsPerRow * TILE;
        int rows = (strip.getWidth() + rowWidth - 1) / rowWidth;
        int rowHeight = strip.getHeight();
        BufferedImage page = new BufferedImage(Math.min(rowWidth, strip.getWidth()),
                rows * rowHeight + (rows - 1) * ROW_GAP, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = page.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, page.getWidth(), page.getHeight());
        for (int row = 0; row < rows; row++) {
            int sourceX = row * rowWidth;
            int width = Math.min(rowWidth, strip.getWidth() - sourceX);
            int targetY = row * (rowHeight + ROW_GAP);
            graphics.drawImage(strip, 0, targetY, width, targetY + rowHeight,
                    sourceX, 0, sourceX + width, rowHeight, null);
        }
        graphics.dispose();

        Path parent = outputPath.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        ImageIO.write(page, "png", outputPath.toFile());
        System.out.printf("Wrote %s (%dx%d)%n", outputPath, page.getWidth(), page.getHeight());
    }

    /** Renders the level one screen at a time, left to right, into one image as tall as the level. */
    private static BufferedImage renderStrip(String level) {
        String[] lines = level.split("\r?\n");
        int levelWidth = lines[0].length() * TILE;
        int levelHeight = lines.length * TILE;
        BufferedImage strip = new BufferedImage(levelWidth, levelHeight, BufferedImage.TYPE_INT_RGB);
        GraphicsConfiguration configuration = strip.createGraphics().getDeviceConfiguration();
        Assets.init(configuration); // reads img/ relative to the working directory

        MarioWorld world = new MarioWorld(null);
        world.visuals = true;
        world.initializeLevel(level, 1000);
        world.initializeVisuals(configuration);
        world.mario.isLarge = false;
        world.mario.updateGraphics();

        BufferedImage screen = new BufferedImage(MarioGame.width, levelHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D stripGraphics = strip.createGraphics();
        for (int cameraX = 0; cameraX < levelWidth; cameraX += MarioGame.width) {
            int shownX = Math.min(cameraX, levelWidth - MarioGame.width);
            world.cameraX = shownX;
            world.cameraY = 0;
            Graphics2D graphics = screen.createGraphics();
            world.render(graphics);
            drawEnemies(world, lines, graphics, shownX);
            graphics.dispose();
            stripGraphics.drawImage(screen, shownX, 0, null);
        }
        stripGraphics.dispose();
        return strip;
    }

    private static void drawEnemies(MarioWorld world, String[] lines, Graphics2D graphics, int cameraX) {
        for (int x = cameraX / TILE; x <= (cameraX + MarioGame.width) / TILE && x < lines[0].length(); x++) {
            for (int y = 0; y < lines.length; y++) {
                SpriteType type = world.level.getSpriteType(x, y);
                if (type == SpriteType.NONE) {
                    continue;
                }
                MarioSprite sprite = type.spawnSprite(true, x, y, -1);
                sprite.world = world;
                sprite.render(graphics);
            }
        }
    }

    private static String value(String[] args, int index, String option) {
        if (index >= args.length || args[index].startsWith("--")) {
            throw new IllegalArgumentException("Missing value for " + option);
        }
        return args[index];
    }

    private static void printUsage(PrintStream output) {
        output.print("""
                Usage: java -cp out levelGenerators.npclrf.Screenshot --level <file> --out <png> [--columns <int>]

                Renders the whole level offscreen to a PNG, wrapped into rows of --columns tiles (default 50).
                Run from the repository root so the engine can load the sprites in img/.
                """);
    }
}
