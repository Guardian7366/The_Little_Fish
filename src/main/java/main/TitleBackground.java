package main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.w3c.dom.Node;

/** Decode once before the loop; playback never reads files or waits for an image observer. */
public final class TitleBackground {
    private record Animation(BufferedImage[] frames, int[] delays) { }
    private static final class Cache { static final Animation VALUE = load(); }
    private final Animation animation = Cache.VALUE;
    private int index, elapsed;

    private static Animation load() {
        ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
        try (InputStream source = TitleBackground.class.getResourceAsStream("/menu/ocean.gif");
             ImageInputStream input = ImageIO.createImageInputStream(source)) {
            reader.setInput(input);
            int count = reader.getNumImages(true);
            BufferedImage[] frames = new BufferedImage[count]; int[] delays = new int[count];
            for (int i=0;i<count;i++) {
                frames[i] = reader.read(i);
                if(frames[i].getWidth()!=320 || frames[i].getHeight()!=192)
                    throw new IllegalStateException("Menu GIF must contain full 320x192 frames");
                Node root=reader.getImageMetadata(i).getAsTree("javax_imageio_gif_image_1.0");
                for(Node node=root.getFirstChild();node!=null;node=node.getNextSibling())
                    if(node.getNodeName().equals("GraphicControlExtension"))
                        delays[i]=Integer.parseInt(node.getAttributes().getNamedItem("delayTime").getNodeValue())*10;
                if(delays[i]<=0) throw new IllegalStateException("Menu GIF needs explicit frame timing");
            }
            return new Animation(frames,delays);
        } catch(Exception e) { throw new IllegalStateException("Cannot load menu animation",e); }
        finally { reader.dispose(); }
    }
    public void update() {
        elapsed+=1000; // Milliseconds multiplied by 60: exact timing at 60 simulation ticks/sec.
        while(elapsed>=animation.delays[index]*60) {
            elapsed-=animation.delays[index]*60; index=(index+1)%animation.frames.length;
        }
    }
    int frameIndex() { return index; }
    int frameCount() { return animation.frames.length; }
    public void draw(Graphics2D target) {
        Graphics2D g=(Graphics2D)target.create();
        g.clipRect(0,0,960,576);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(animation.frames[index],0,0,960,576,null);
        g.dispose();
    }
}
