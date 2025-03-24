package Project;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.Box;
import org.jogamp.java3d.utils.geometry.Primitive;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.java3d.utils.picking.PickCanvas;
import org.jogamp.java3d.utils.picking.PickResult;
import org.jogamp.java3d.utils.picking.PickTool;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.*;

public class Project extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
    private static final long serialVersionUID = 1L;
    private static JFrame frame;
    private Canvas3D canvas3D;
    // TransformGroup for the viewer (i.e., the view platform)
    private static TransformGroup viewTG;
    private static BranchGroup sceneBG;
    
    // Global camera rotation variables (in radians)
    private double yawAngle = 0.0;    // horizontal rotation
    private double pitchAngle = 0.0;  // vertical rotation (clamped)
    // Global camera translation (x, y, z)
    private Vector3d cameraTranslation = new Vector3d(0, 0, 2);
    
    // Mouse sensitivity (reduced)
    private final double mouseSensitivity = 0.003;
    // Keyboard movement step (increased)
    private final double moveStep = 0.5;
    // Set to track currently pressed keys
    private final Set<Integer> keysDown = new HashSet<>();
    private Timer movementTimer;
    
    // Fixed y-coordinate (eye height); remains constant
    private final double fixedY = cameraTranslation.y;
    
    // Room bounds for movement restrictions
    private final double X_BOUND = 96.0;
    private final double Z_BOUND = 46.0;

    // --- Dynamic Light Fields ---
    private TransformGroup dynamicLightTG;
    private PointLight dynamicLight;
    
    
    private Transform3D defaultViewTransform = new Transform3D();
    private boolean inCameraView = false;

    
    /**
     * Helper method to create an Appearance with a tiled texture.
     * PolygonAttributes are set to CULL_NONE so interior surfaces are visible.
     */
    private static Appearance createTiledTextureAppearance(String filename) {
        TextureLoader loader = new TextureLoader(filename, null);
        Texture texture = loader.getTexture();
        Appearance appearance = new Appearance();

        if (texture != null) {
            texture.setBoundaryModeS(Texture.WRAP);
            texture.setBoundaryModeT(Texture.WRAP);
            texture.setMinFilter(Texture.BASE_LEVEL_LINEAR);
            texture.setMagFilter(Texture.BASE_LEVEL_LINEAR);

            TextureAttributes texAttr = new TextureAttributes();
            texAttr.setTextureMode(TextureAttributes.MODULATE);
            
            Transform3D texTransform = new Transform3D();
            texTransform.setScale(new Vector3d(10.0, 10.0, 1.0)); // Increase scaling to tile the texture
            texAttr.setTextureTransform(texTransform);
            
            appearance.setTexture(texture);
            appearance.setTextureAttributes(texAttr);

            PolygonAttributes pa = new PolygonAttributes();
            pa.setCullFace(PolygonAttributes.CULL_NONE);
            appearance.setPolygonAttributes(pa);
        } else {
            System.err.println("Error loading texture: " + filename);
        }
        return appearance;
    }

    /**
     * Create and return the scene BranchGroup.
     * A large cuboid (non-cubic room) is created.
     * Updated half extents: width = 100, height = 15, depth = 50 (overall room: 200 x 30 x 100)
     */
    private static BranchGroup setup_Scene() {
        BranchGroup sceneBG = new BranchGroup();
        // Enable adding children later even after compilation.
        sceneBG.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        sceneBG.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);

        // Create a large room using a Box.
        Box room = new Box(100f, 15f, 50f, 
                Primitive.GENERATE_NORMALS | Primitive.GENERATE_TEXTURE_COORDS | Primitive.ENABLE_APPEARANCE_MODIFY, null);

        // Apply tiled textures to each face.
        ((Shape3D) room.getShape(Box.FRONT)).setAppearance(createTiledTextureAppearance("Projects/ImageWall.jpg"));
        ((Shape3D) room.getShape(Box.BACK)).setAppearance(createTiledTextureAppearance("Projects/ImageWall.jpg"));
        ((Shape3D) room.getShape(Box.LEFT)).setAppearance(createTiledTextureAppearance("Projects/ImageWall.jpg"));
        ((Shape3D) room.getShape(Box.RIGHT)).setAppearance(createTiledTextureAppearance("Projects/ImageWall.jpg"));
        ((Shape3D) room.getShape(Box.TOP)).setAppearance(createTiledTextureAppearance("Projects/ImageCeiling.jpg"));
        ((Shape3D) room.getShape(Box.BOTTOM)).setAppearance(createTiledTextureAppearance("Projects/ImageFloor.jpg"));

        sceneBG.addChild(room);
        sceneBG.addChild(createStaticLights());
        sceneBG.addChild(ProjectImages.addGalleryImages());
        //sceneBG.addChild(ProjectAnimations.addMorphingGalleryImages());
        //sceneBG.addChild(ProjectObjects.addWallObjects());
        
        sceneBG.addChild(ProjectObjects.addObjects());


        // Note: Do not compile sceneBG here if you plan to add more children later.
        sceneBG.compile();
        return sceneBG;
    }

    /**
     * Rebuilds and updates the viewer transform based on current yaw, pitch, and translation.
     */
    private void updateViewTransform() {
        Transform3D yawTransform = new Transform3D();
        yawTransform.rotY(yawAngle);
        Transform3D pitchTransform = new Transform3D();
        pitchTransform.rotX(pitchAngle);
        Transform3D rotation = new Transform3D();
        rotation.mul(yawTransform, pitchTransform);
        
        Transform3D viewTransform = new Transform3D();
        viewTransform.set(rotation);
        viewTransform.setTranslation(cameraTranslation);
        viewTG.setTransform(viewTransform);
    }
    
 // Add this method to your Project class (or another utility class)
    private static BranchGroup createStaticLights() {
        BranchGroup lightBG = new BranchGroup();

        // Add a subtle ambient light to provide overall illumination.
        AmbientLight ambient = new AmbientLight(new Color3f(0.2f, 0.2f, 0.2f));
        ambient.setInfluencingBounds(new BoundingSphere(new Point3d(0, 0, 0), 1000));
        lightBG.addChild(ambient);

        // Create point lights with attenuation so brightness falls off with distance.
        // Front wall (z = 50) light:
        PointLight frontLight = new PointLight(
                new Color3f(0.7f, 0.7f, 0.7f),
                new Point3f(0f, 0f, 50f),
                new Point3f(1f, 0.1f, 0.05f)
        );
        frontLight.setInfluencingBounds(new BoundingSphere(new Point3d(0, 0, 0), 1000));
        lightBG.addChild(frontLight);

        // Back wall (z = -50) light:
        PointLight backLight = new PointLight(
                new Color3f(0.7f, 0.7f, 0.7f),
                new Point3f(0f, 0f, -50f),
                new Point3f(1f, 0.1f, 0.05f)
        );
        backLight.setInfluencingBounds(new BoundingSphere(new Point3d(0, 0, 0), 1000));
        lightBG.addChild(backLight);

        // Left wall (x = -100) light:
        PointLight leftLight = new PointLight(
                new Color3f(0.7f, 0.7f, 0.7f),
                new Point3f(-100f, 0f, 0f),
                new Point3f(1f, 0.1f, 0.05f)
        );
        leftLight.setInfluencingBounds(new BoundingSphere(new Point3d(0, 0, 0), 1000));
        lightBG.addChild(leftLight);

        // Right wall (x = 100) light:
        PointLight rightLight = new PointLight(
                new Color3f(0.7f, 0.7f, 0.7f),
                new Point3f(100f, 0f, 0f),
                new Point3f(1f, 0.1f, 0.05f)
        );
        rightLight.setInfluencingBounds(new BoundingSphere(new Point3d(0, 0, 0), 1000));
        lightBG.addChild(rightLight);

        lightBG.compile();
        return lightBG;
    }

    
    /**
     * Adds a dynamic point light to the scene that follows the player's (camera's) position.
     * To avoid the RestrictedAccessException, we wrap the dynamic light's TransformGroup inside a BranchGroup.
     */
    private void addDynamicLight() {
        dynamicLight = new PointLight(
            new Color3f(1f, 1f, 1f),
            new Point3f((float) cameraTranslation.x, (float) cameraTranslation.y, (float) cameraTranslation.z),
            new Point3f(1f, 0f, 0f)
        );
        // Set the light to influence a large area.
        dynamicLight.setInfluencingBounds(new BoundingSphere(new Point3d(), 1000.0));
        dynamicLightTG = new TransformGroup();
        dynamicLightTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        dynamicLightTG.addChild(dynamicLight);
        
        // Wrap the dynamicLightTG inside a BranchGroup.
        BranchGroup lightBG = new BranchGroup();
        lightBG.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        lightBG.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        lightBG.addChild(dynamicLightTG);
        
        sceneBG.addChild(lightBG);
    }
    
    /**
     * Updates the dynamic light's position so that it follows the player's camera.
     */
    private void updateDynamicLight() {
        if (dynamicLightTG != null) {
            Transform3D lightTransform = new Transform3D();
            lightTransform.setTranslation(cameraTranslation);
            dynamicLightTG.setTransform(lightTransform);
        }
    }
    
    /**
     * Constructor: sets up the canvas, scene, viewer, dynamic lighting, and listeners.
     * The Canvas3D background color is set using a java.awt.Color.
     */
    public Project() {
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        canvas3D = new Canvas3D(config);
        // Set the canvas background to light grey.
        canvas3D.setBackground(new Color(0.8f, 0.8f, 0.8f));
        
        canvas3D.addMouseListener(this);
        canvas3D.addMouseMotionListener(this);
        canvas3D.addKeyListener(this);
        canvas3D.setFocusable(true);
        canvas3D.requestFocus();
        
        setLayout(new BorderLayout());
        add("Center", canvas3D);
        
        SimpleUniverse su = new SimpleUniverse(canvas3D);
        
        // Adjust clipping planes.
        View view = su.getViewer().getView();
        view.setFrontClipDistance(0.01);
        view.setBackClipDistance(1000.0);

        sceneBG = setup_Scene();
        su.addBranchGraph(sceneBG);
        viewTG = su.getViewingPlatform().getViewPlatformTransform();
        viewTG.getTransform(defaultViewTransform);
        updateViewTransform();
        
        // Add the dynamic light to the scene.
        addDynamicLight();
        
        movementTimer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateMovement();
            }
        });
        movementTimer.start();
        
        frame.setSize(800, 800);
        frame.setVisible(true);
    }
    
    /**
     * Updates the camera translation based on keys pressed (W, A, S, D).
     * Movement is restricted to the horizontal (x-z) plane.
     * Also updates the dynamic light position.
     */
    private void updateMovement() {
        if (keysDown.isEmpty())
            return;

        Vector3d localMove = new Vector3d(0, 0, 0);

        if (keysDown.contains(KeyEvent.VK_W)) {
            localMove.z -= moveStep;
        }
        if (keysDown.contains(KeyEvent.VK_S)) {
            localMove.z += moveStep;
        }
        if (keysDown.contains(KeyEvent.VK_A)) {
            localMove.x -= moveStep;
        }
        if (keysDown.contains(KeyEvent.VK_D)) {
            localMove.x += moveStep;
        }

        // Rotate movement direction by yaw.
        Matrix3d yawRot = new Matrix3d();
        yawRot.rotY(yawAngle);
        yawRot.transform(localMove);

        // Apply movement.
        cameraTranslation.add(localMove);

        // Restrict movement inside the room.
        cameraTranslation.x = Math.max(-X_BOUND, Math.min(X_BOUND, cameraTranslation.x));
        cameraTranslation.z = Math.max(-Z_BOUND, Math.min(Z_BOUND, cameraTranslation.z));
        cameraTranslation.y = fixedY; // Keep y fixed.

        updateViewTransform();
        updateDynamicLight();
    }
    
    /**
     * Main entry point.
     */
    public static void main(String[] args) {
        frame = new JFrame("Museum - First Person View");
        Project proj = new Project();
        frame.getContentPane().add(proj);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    private void switchToCameraView(Transform3D camTransform) {
        // Extract the camera's world position T.
        Vector3d T = new Vector3d();
        camTransform.get(T);
        
        // Define the room center.
        Point3d roomCenter = new Point3d(0, 0, 0);
        
        // Compute the forward vector from T to the room center.
        Vector3d F = new Vector3d();
        F.sub(new Vector3d(roomCenter), T);  // F = roomCenter - T
        F.normalize();
        
        // Choose an offset so the viewer is moved from the camera's position toward the room center.
        double offset = 5.0; // Adjust as needed.
        
        // Compute the new viewer (eye) position.
        Vector3d eyePos = new Vector3d(T);
        eyePos.add(new Vector3d(F.x * offset, F.y * offset, F.z * offset));
        
        // Build the view transform so that the eye is at eyePos and is looking at the room center.
        Transform3D newView = new Transform3D();
        newView.lookAt(new Point3d(eyePos), roomCenter, new Vector3d(0, 1, 0));
        newView.invert();  // Invert the transform as required by Java 3D.
        
        // Set the viewer's transform.
        viewTG.setTransform(newView);
    }




    // Revert to the default first-person view.
    private void revertToFirstPersonView() {
        viewTG.setTransform(defaultViewTransform);
        inCameraView = false;
    }

    // Traverse upward in the scene graph to find a TransformGroup that holds a Transform3D in its user data.
    private Transform3D findCameraTransform(Node node) {
        while (node != null) {
            if (node instanceof TransformGroup) {
                Object data = ((TransformGroup) node).getUserData();
                if (data instanceof Transform3D) {
                    return (Transform3D) data;
                }
            }
            node = node.getParent();
        }
        return null;
    }

    
    // MouseListener methods.
    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            PickCanvas pickCanvas = new PickCanvas(canvas3D, sceneBG);
            pickCanvas.setMode(PickTool.GEOMETRY);
            pickCanvas.setShapeLocation(e);
            PickResult result = pickCanvas.pickClosest();
            if (result != null) {
                Node pickedNode = result.getObject();
                Transform3D camTransform = findCameraTransform(pickedNode);
                if (camTransform != null) {
                    switchToCameraView(camTransform);
                }
            }
        }
    }

    
    private int lastMouseX, lastMouseY;
    
    @Override
    public void mousePressed(MouseEvent e) {
        lastMouseX = e.getX();
        lastMouseY = e.getY();

        if (SwingUtilities.isRightMouseButton(e)) {
            // Use a picking mechanism to determine which object was clicked.
        	PickCanvas pickCanvas = new PickCanvas(canvas3D, sceneBG);
        	pickCanvas.setMode(PickTool.GEOMETRY);
        	// Increase tolerance (in pixels); try a value like 0.0f (adjust as needed)
        	pickCanvas.setTolerance(0.0f);
        	pickCanvas.setShapeLocation(e);
        	PickResult result = pickCanvas.pickClosest();
            if (result != null) {
                Node pickedNode = result.getObject();
                // Traverse upward to find the BranchGroup that holds our userData (the Alpha).
                BranchGroup morphBG = findMorphGroup(pickedNode);
                if (morphBG != null) {
                    Alpha alpha = (Alpha) morphBG.getUserData();
                    if (alpha != null) {
                        // Toggle the mode:
                        if (alpha.getMode() != 0) {
                            // Pause: set mode to 0. This "freezes" the Alpha at its current phase.
                            alpha.setMode(0);
                        } else {
                            // Resume: restore mode so it continues from where it stopped.
                            alpha.setMode(Alpha.INCREASING_ENABLE | Alpha.DECREASING_ENABLE);
                        }
                    }
                }
            }
        }
    }

    // Utility method to traverse upward until we find a node that is a BranchGroup with userData.
    private BranchGroup findMorphGroup(Node node) {
        while (node != null) {
            if (node instanceof BranchGroup && node.getUserData() != null) {
                return (BranchGroup) node;
            }
            node = node.getParent();
        }
        return null;
    }


    @Override
    public void mouseReleased(MouseEvent e) { }
    
    @Override
    public void mouseEntered(MouseEvent e) { }
    
    @Override
    public void mouseExited(MouseEvent e) { }
    
    /**
     * MouseMotionListener method: updates yaw and pitch based on dragging.
     * Horizontal drags update yaw; vertical drags update pitch (clamped to ±45°).
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        int currentX = e.getX();
        int currentY = e.getY();
        int dx = currentX - lastMouseX;
        int dy = currentY - lastMouseY;
        
        yawAngle += dx * mouseSensitivity;
        pitchAngle += dy * mouseSensitivity;
        double maxPitch = Math.toRadians(45);
        if (pitchAngle > maxPitch) pitchAngle = maxPitch;
        if (pitchAngle < -maxPitch) pitchAngle = -maxPitch;
        
        updateViewTransform();
        
        lastMouseX = currentX;
        lastMouseY = currentY;
    }
    
    @Override
    public void mouseMoved(MouseEvent e) { }
    
    // KeyListener methods.
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && inCameraView) {
            revertToFirstPersonView();
        }
        keysDown.add(e.getKeyCode());
    }

    
    @Override
    public void keyReleased(KeyEvent e) {
        keysDown.remove(e.getKeyCode());
    }
    
    @Override
    public void keyTyped(KeyEvent e) { }
}
