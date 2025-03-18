import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import javax.swing.*;
import java.awt.*;
import org.jogamp.vecmath.*;

public class Museum extends JPanel {
    private static final long serialVersionUID = 1L;
    private static JFrame frame;
    private Canvas3D canvas;
    private SimpleUniverse su;
    private BranchGroup sceneBG;
    private PointLight dynamicLight; // dynamic light that follows the player
    private TransformGroup dynamicLightTransformGroup;
    private Player player;
    private SoundPlayer soundPlayer;

    public Museum() {
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        canvas = new Canvas3D(config);
        su = new SimpleUniverse(canvas);

        sceneBG = new BranchGroup();
        
        // sound
        soundPlayer = new SoundPlayer();
        soundPlayer.playMuseumSound();

        // create the museum layout using the imported model
        sceneBG.addChild(MuseumObjects.createMuseumLayout());

        // create the player and pass the SoundPlayer instance
        player = new Player(su, soundPlayer);
        sceneBG.addChild(player.getPlayerTransformGroup());

        // dynamic lighting that follows the player
        addDynamicLight();

        // input listeners
        canvas.addKeyListener(player);
        canvas.addMouseMotionListener(player);
        canvas.setFocusable(true);
        canvas.requestFocus();

        sceneBG.compile();
        su.addBranchGraph(sceneBG);

        setLayout(new BorderLayout());
        add("Center", canvas);
        frame.setSize(800, 800);
        frame.setVisible(true);
    }

    private void addDynamicLight() {
        dynamicLight = new PointLight(
            new Color3f(1, 1, 1),
            new Point3f(0, 0, 0),
            new Point3f(1, 0, 0)
        );
        dynamicLight.setInfluencingBounds(new BoundingSphere(new Point3d(), 100.0));

        dynamicLightTransformGroup = new TransformGroup();
        dynamicLightTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        dynamicLightTransformGroup.addChild(dynamicLight);

        sceneBG.addChild(dynamicLightTransformGroup);

        // a thread to update the light's position based on the player's position
        new Thread(() -> {
            while (true) {
                try {
                    // get the player's current position
                    Transform3D playerTransform = new Transform3D();
                    player.getPlayerTransformGroup().getTransform(playerTransform);
                    Vector3f playerPosition = new Vector3f();
                    playerTransform.get(playerPosition);

                    updateDynamicLightPosition(playerPosition);

                    // avoid CPU/GPU usage
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void updateDynamicLightPosition(Vector3f playerPosition) {
        Transform3D transform3D = new Transform3D();
        transform3D.setTranslation(playerPosition);

        dynamicLightTransformGroup.setTransform(transform3D);

        System.out.println("Dynamic Light Position: " + playerPosition);
    }

    public static void main(String[] args) {
        frame = new JFrame("Museum");
        frame.getContentPane().add(new Museum());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
