package main;

import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.regex.*;
import javax.imageio.ImageIO;

/** Verify every production candidate against the prepared contract, not a regenerated image. */
public final class FifthArtRegression {
    public static void main(String[] args)throws Exception {
        Path root=Path.of("design/quinta-zona");String json=Files.readString(root.resolve("assets/manifest.json"));
        Matcher m=Pattern.compile("\\{\\s*\"path\":\\s*\"([^\"]+)\",\\s*\"width\":\\s*(\\d+),\\s*\"height\":\\s*(\\d+),\\s*\"alpha_min\":\\s*(\\d+),\\s*\"alpha_max\":\\s*(\\d+),\\s*\"sha256\":\\s*\"([a-f0-9]+)\"\\s*\\}").matcher(json);
        int count=0;
        while(m.find()) {
            String path=m.group(1),hash=m.group(6);Path production=Path.of("src/main/resources/quinta").resolve(path.substring("assets/".length()));
            byte[] bytes=Files.readAllBytes(production);
            if(!hash.equals(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes))))throw new AssertionError("hash "+path);
            if(!java.util.Arrays.equals(bytes,Files.readAllBytes(root.resolve(path))))throw new AssertionError("source mismatch "+path);
            var image=ImageIO.read(production.toFile());int lo=255,hi=0;
            for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++){int a=image.getRGB(x,y)>>>24;lo=Math.min(lo,a);hi=Math.max(hi,a);}
            if(image.getWidth()!=Integer.parseInt(m.group(2))||image.getHeight()!=Integer.parseInt(m.group(3))||lo!=Integer.parseInt(m.group(4))||hi!=Integer.parseInt(m.group(5)))throw new AssertionError("dimensions/alpha "+path);
            count++;
        }
        if(count!=32)throw new AssertionError("Expected all 32 manifest entries, got "+count);
        System.out.println("FIFTH_ART PASS: 32 prepared PNGs, exact hashes/bytes, dimensions and alpha");
    }
}
