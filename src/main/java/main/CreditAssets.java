package main;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

final class CreditAssets {
    private static final Map<String,BufferedImage> IMAGES=new HashMap<>();

    static InputStream open(String path) throws IOException {
        var stream=CreditAssets.class.getResourceAsStream(path);
        if (stream==null) throw new IOException("Missing resource: "+path);
        return stream;
    }

    static BufferedImage image(String name) {
        return art("/credits/"+name);
    }

    static BufferedImage art(String path) {
        return IMAGES.computeIfAbsent(path,key -> {
            try (var stream=open(key)) {
                var image=ImageIO.read(stream);
                if (image==null) throw new IOException("Invalid image");
                return image;
            } catch (IOException error) {
                throw new IllegalStateException("Cannot load "+key,error);
            }
        });
    }

    private CreditAssets() {}
}
