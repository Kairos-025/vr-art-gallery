import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;

import org.jogamp.java3d.*;
import org.jogamp.vecmath.*;

public class ProjectVideos {

    // Arrays for multiple video properties
    private static final int[] FRAME_COUNTS = {83, 83, 100, 83};  // Example counts for different videos
    private static final String[] IMAGE_DIRS = {"ProjectFiles/videos/video1/", "ProjectFiles/videos/video2/", "ProjectFiles/videos/video3/", "ProjectFiles/videos/video4/"};
    private static final String[] IMAGE_PREFIXES = {"video1_", "video2_", "video3_", "video4_"};
    private static final String IMAGE_EXT = ".jpg";

    // Arrays to hold user-defined positions and rotations for each video
    private static final Vector3d[] POSITIONS = {
        new Vector3d(68, -2.25, 44.80),     // Position for video1
        new Vector3d(68, -2.25, -44.80),    // Position for video2
        new Vector3d(-68, -2.25, 44.80),    // Position for video3
        new Vector3d(-68, -2.25, -44.80)    // Position for video4
    };
    private static final double[] ROTATIONS = {Math.PI, 2 * Math.PI, Math.PI, 2 * Math.PI}; // Rotations for each video

    // Array to hold all frames for each video
    private BufferedImage[][] allFrames;
    private Texture2D videoTexture;

    public ProjectVideos() {
        loadAllFrames();
    }

    /**
     * Loads all video frames into a 2D array (each video has its own set of frames).
     */
    private void loadAllFrames() {
        allFrames = new BufferedImage[FRAME_COUNTS.length][];
        for (int i = 0; i < FRAME_COUNTS.length; i++) {
            allFrames[i] = new BufferedImage[FRAME_COUNTS[i]];
            for (int j = 0; j < FRAME_COUNTS[i]; j++) {
                String filename = IMAGE_DIRS[i] + IMAGE_PREFIXES[i] + String.format("%03d", j) + IMAGE_EXT;
                File file = new File(filename);
                if (!file.exists()) {
                    System.err.println("File not found: " + filename);
                    continue;
                }
                try {
                    allFrames[i][j] = ImageIO.read(file);
                    if (allFrames[i][j] == null) {
                        System.err.println("Failed to load image: " + filename);
                    }
                } catch (IOException e) {
                    System.err.println("Error loading frame: " + filename + " - " + e.getMessage());
                }
            }
        }
    }

    /**
     * Creates video screens for all videos (a quad with dynamic textures) and applies their transformations.
     */
    public BranchGroup createVideoScreens() {
        BranchGroup sceneBG = new BranchGroup();

        // Iterate through all videos
        for (int i = 0; i < FRAME_COUNTS.length; i++) {
            // Create video screen for each video
            sceneBG.addChild(createVideoScreen(i));
        }

        sceneBG.compile();
        return sceneBG;
    }

    /**
     * Creates a single video screen (a quad with dynamic texture) for a specific video index.
     */
    private BranchGroup createVideoScreen(int videoIndex) {
        BranchGroup videoScene = new BranchGroup();

        // Create a quad that will serve as the video screen.
        QuadArray quad = new QuadArray(4, GeometryArray.COORDINATES | GeometryArray.TEXTURE_COORDINATE_2);

        // Define quad vertices (adjust size as needed)
        Point3f p0 = new Point3f(-1.0f, -0.5f, 0.0f);
        Point3f p1 = new Point3f(1.0f, -0.5f, 0.0f);
        Point3f p2 = new Point3f(1.0f, 0.5f, 0.0f);
        Point3f p3 = new Point3f(-1.0f, 0.5f, 0.0f);
        Point3f[] coords = {p0, p1, p2, p3};
        quad.setCoordinates(0, coords);

        // Adjust texture coordinates to crop
        float cropAmount = 0.2f; // 20% crop
        TexCoord2f t0 = new TexCoord2f(cropAmount, 0.0f);
        TexCoord2f t1 = new TexCoord2f(1.0f - cropAmount, 0.0f);
        TexCoord2f t2 = new TexCoord2f(1.0f - cropAmount, 1.0f);
        TexCoord2f t3 = new TexCoord2f(cropAmount, 1.0f);
        TexCoord2f[] texCoords = {t0, t1, t2, t3};
        quad.setTextureCoordinates(0, 0, texCoords);

        // Create Texture2D for the video
        int width = allFrames[videoIndex][0].getWidth();
        int height = allFrames[videoIndex][0].getHeight();
        videoTexture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA, width, height);
        videoTexture.setImage(0, new ImageComponent2D(ImageComponent2D.FORMAT_RGBA, allFrames[videoIndex][0]));
        videoTexture.setCapability(Texture.ALLOW_IMAGE_WRITE);
        videoTexture.setBoundaryModeS(Texture.WRAP);
        videoTexture.setBoundaryModeT(Texture.WRAP);

        Appearance videoAppearance = new Appearance();
        videoAppearance.setTexture(videoTexture);

        Shape3D videoShape = new Shape3D(quad, videoAppearance);

        // Position and transform for this video
        Transform3D baseTransform = new Transform3D();
        Transform3D scale = new Transform3D();
        scale.setScale(new Vector3d(3.6, 12.6, 3.6));
        Transform3D rotate = new Transform3D();
        rotate.rotY(ROTATIONS[videoIndex]);  // Example: rotating each video differently
        scale.mul(rotate);
        Transform3D translate = new Transform3D();
        translate.setTranslation(new Vector3d(POSITIONS[videoIndex])); // Position offset per video
        translate.mul(scale);
        baseTransform.set(translate);
        
        TransformGroup videoTG = new TransformGroup(baseTransform);
        videoTG.addChild(videoShape);
        
        // Add the video quad to the scene
        videoScene.addChild(videoTG);
        
        // Add behavior to update the video texture periodically
        VideoPlayerBehavior behavior = new VideoPlayerBehavior(videoTexture, allFrames[videoIndex]);
        behavior.setSchedulingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100.0));
        videoScene.addChild(behavior);
        
        videoScene.compile();
        return videoScene;
    }
    
    /**
     * Inner class for the behavior that updates the video texture.
     * It cycles through the frames at a fixed interval.
     */
    private class VideoPlayerBehavior extends Behavior {
        private Texture2D texture;
        private BufferedImage[] frames;
        private int currentFrame = 0;
        private long frameDuration = 100;  // 100ms per frame
        private WakeupCriterion wakeup = new WakeupOnElapsedTime(frameDuration);
        
        public VideoPlayerBehavior(Texture2D texture, BufferedImage[] frames) {
            this.texture = texture;
            this.frames = frames;
        }
        
        @Override
        public void initialize() {
            wakeupOn(wakeup);
        }
        
        @Override
        public void processStimulus(Iterator<WakeupCriterion> criteria) {
            currentFrame = (currentFrame + 1) % frames.length;
            
            ImageComponent2D imageComponent = new ImageComponent2D(
                    ImageComponent2D.FORMAT_RGBA, frames[currentFrame]);
            texture.setImage(0, imageComponent);
            
            wakeupOn(new WakeupOnElapsedTime(frameDuration));
        }
    }
}
