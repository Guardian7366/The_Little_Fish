package main;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

/** Small preferences file, written only when a setting changes. */
public final class Config {
    private final GamePanel gp;
    private final Path file;

    public Config(GamePanel gp) {
        this(gp, Path.of("config.txt"));
    }

    Config(GamePanel gp, Path file) {
        this.gp = gp;
        this.file = file;
    }

    public void saveConfig() {
        Path temporary = file.resolveSibling("config.txt.tmp");
        try {
            Files.write(temporary, List.of(gp.fullScreenOn ? "On" : "Off",
                Integer.toString(gp.music.volumeScale), Integer.toString(gp.se.volumeScale)), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            System.err.println("Cannot save preferences: " + ex.getMessage());
        }
    }

    public void loadConfig() {
        if (!Files.exists(file)) return;
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            if (lines.size() != 3 || !(lines.get(0).equals("On") || lines.get(0).equals("Off")))
                throw new IOException("Invalid preferences");
            int music = Integer.parseInt(lines.get(1));
            int effects = Integer.parseInt(lines.get(2));
            if (music < 0 || music > 5 || effects < 0 || effects > 5) throw new IOException("Invalid volume");
            gp.fullScreenOn = lines.get(0).equals("On");
            gp.music.volumeScale = music;
            gp.se.volumeScale = effects;
        } catch (IOException | NumberFormatException ex) {
            System.err.println("Using default preferences: " + ex.getMessage());
        }
    }
}
