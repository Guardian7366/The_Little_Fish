package main;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;

/** Shares immutable sprite images; callers must never paint into a cached image. */
public final class SpriteCache {
    private record Key(String path, int width, int height) {}
    private static final ConcurrentHashMap<Key, BufferedImage> IMAGES = new ConcurrentHashMap<>();

    private SpriteCache() {}

    public static BufferedImage load(String resourcePath, int width, int height) {
        if (resourcePath == null || !resourcePath.startsWith("/") || width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid sprite path or dimensions: " + resourcePath);
        }
        return IMAGES.computeIfAbsent(new Key(resourcePath, width, height), SpriteCache::read);
    }

    private static BufferedImage read(Key key) {
        try (InputStream stream = SpriteCache.class.getResourceAsStream(key.path())) {
            if (stream == null) throw new IOException("Resource is missing");
            BufferedImage original = ImageIO.read(stream);
            if (original == null) throw new IOException("Unsupported image format");
            int type = original.getType() == BufferedImage.TYPE_CUSTOM
                    ? BufferedImage.TYPE_INT_ARGB : original.getType();
            // Match the original Java2D scaling, including image type and pixel sampling.
            BufferedImage scaled = new BufferedImage(key.width(), key.height(), type);
            Graphics2D graphics = scaled.createGraphics();
            try {
                graphics.drawImage(original, 0, 0, key.width(), key.height(), null);
            } finally {
                graphics.dispose();
            }
            return scaled;
        } catch (IOException error) {
            throw new IllegalStateException("Cannot load sprite " + key.path(), error);
        }
    }

    public static int cachedImageCount() {
        return IMAGES.size();
    }
}
