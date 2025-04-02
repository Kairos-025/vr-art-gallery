
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
    
    // --- Object Boundaries ---
    private static final boolean SHOW_POSITION = true;  // toggle position prints
    private static final boolean SHOW_COLLISIONS = true; // toggle collision prints
    private final double[][] OBJECT_BOUNDS = {
        // format: {minX, maxX, minZ, maxZ} for each object
        {88.0, X_BOUND, 37.0, Z_BOUND},   // right side of dinosaur flower pot
        {88.0, X_BOUND, -46.0, -39.0},    // left side of dinosaur flower pot
        {-96.0, -88.0, 38.0, 46.0},       // left side of portrait flower pot
        {-96.0, -89.0, -46.0, -39.0},     // right side of portrait flower pot
        {-93.5, -81.5, -27.0, -13.5},     // right side of portrait floating statue
        {-77.5, -73.0, -23.0, -17.0},     // right side of portrait control panel
        {-92.5, -80.0, 12.5, 26.5},       // left side of portrait floating statue
        {-77.5, -73.0, 17.0, 23.0},       // left side of portrait control panel
        {65.0, 94.0, -21.0, 11.3},        // dinosaur
        {55.7, 60.0, -8.0, -2.2},         // dinosaur control panel
        {66.0, 75.0, 13.5, 25.5}		  // robot
    };
    
    // --- Dynamic Light Fields ---
    private PointLight dynamicLight;
    private TransformGroup dynamicLightTG;
    
    // --- Ceiling Light Field ---
    private static BranchGroup lightBG;
    
    
    // --- SOUND ---
    private SoundPlayer soundPlayer;
    
    // --- View --- 
    private Transform3D defaultViewTransform = new Transform3D();
    private boolean inCameraView = false;
    
    // --- Wireframe Mode Toggle ---
    private boolean wireframeMode = false;

    
    /** Helper method to create an Appearance with a tiled texture.
     *  PolygonAttributes are set to CULL_NONE so interior surfaces are visible. */
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
            
            // Make object respond to scene lighting (by setting material)
    	    appearance.setMaterial(ProjectObjects.createLightingMaterial(new Color3f(0.15f, 0.15f, 0.15f), new Color3f(0.3f, 0.3f, 0.3f), new Color3f(1.0f, 1.0f, 1.0f), 32f));    
        } 
        
        else {
            System.err.println("Error loading texture: " + filename);
        }
        
        return appearance;
    }

    /** Create and return the scene BranchGroup.
     *  A large cuboid (non-cubic room) is created.
     *  Updated half extents: width = 100, height = 15, depth = 50 (overall room: 200 x 30 x 100) */
    private BranchGroup setup_Scene() {
        BranchGroup sceneBG = new BranchGroup();
        // Enable adding children later even after compilation.
        sceneBG.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        sceneBG.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);

        // Create a large room using a Box.
        Box room = new Box(100f, 15f, 50f, 
                Primitive.GENERATE_NORMALS | Primitive.GENERATE_TEXTURE_COORDS | Primitive.ENABLE_APPEARANCE_MODIFY, null);

        // Apply tiled textures to each face.
        ((Shape3D) room.getShape(Box.FRONT)).setAppearance(createTiledTextureAppearance("ProjectFiles/ImageWall.jpg"));
        ((Shape3D) room.getShape(Box.BACK)).setAppearance(createTiledTextureAppearance("ProjectFiles/ImageWall.jpg"));
        ((Shape3D) room.getShape(Box.LEFT)).setAppearance(createTiledTextureAppearance("ProjectFiles/ImageWall.jpg"));
        ((Shape3D) room.getShape(Box.RIGHT)).setAppearance(createTiledTextureAppearance("ProjectFiles/ImageWall.jpg"));
        ((Shape3D) room.getShape(Box.TOP)).setAppearance(createTiledTextureAppearance("ProjectFiles/ImageCeiling.jpg"));
        ((Shape3D) room.getShape(Box.BOTTOM)).setAppearance(createTiledTextureAppearance("ProjectFiles/ImageFloor.jpg"));

                
        sceneBG.addChild(room);
        sceneBG.addChild(ProjectImages.addGalleryImages());
        sceneBG.addChild(ProjectObjects.addObjects());

        // Note: Do not compile sceneBG here if you plan to add more children later.
        sceneBG.compile();
        return sceneBG;
    }

    /** Rebuilds and updates the viewer transform based on current yaw, pitch, and translation. */
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
    


    /** Adds a dynamic point light to the scene that follows the player's (camera's) position.
     *  To avoid the RestrictedAccessException, we wrap the dynamic light's TransformGroup inside a BranchGroup. */  
    private void addDynamicLight() {
        dynamicLight = new PointLight(
            new Color3f(0.7f, 0.7f, 0.7f),
            new Point3f((float) cameraTranslation.x, (float) cameraTranslation.y, (float) cameraTranslation.z),
            new Point3f(1f, 0.025f, 0f)
        );
        
        dynamicLight.setCapability(PointLight.ALLOW_STATE_WRITE);
        
        // Set the light to influence a large area.
        dynamicLight.setInfluencingBounds(new BoundingSphere(new Point3d(), 1000.0));
        dynamicLightTG = new TransformGroup();
        dynamicLightTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        dynamicLightTG.addChild(dynamicLight);
        
        // Wrap the dynamicLightTG inside a BranchGroup.
        BranchGroup dynLightBG = new BranchGroup();
        dynLightBG.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        dynLightBG.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        dynLightBG.addChild(dynamicLightTG);
        
        sceneBG.addChild(dynLightBG);
    }
    
    /** Updates the dynamic light's position so that it follows the player's camera. */
    private void updateDynamicLight() {
        if (dynamicLightTG != null) {
            Transform3D lightTransform = new Transform3D();
            lightTransform.setTranslation(cameraTranslation);
            dynamicLightTG.setTransform(lightTransform);
        }
    }
    
    /** Toggle wireframe mode for the entire scene.
     *  Pressing the 'M' key will call this function. */
    private void toggleWireframeMode() {
        wireframeMode = !wireframeMode;
        toggleWireframeModeRecursive(sceneBG, wireframeMode);
        System.out.println("Wireframe mode: " + (wireframeMode ? "ON" : "OFF"));
    }
    
    
    /** Recursively traverse the scene graph and toggle polygon mode on each Shape3D. */
    private void toggleWireframeModeRecursive(Node node, boolean wireframe) {
        if (node == null) return;
        if (node instanceof Shape3D) {
            Appearance app = ((Shape3D) node).getAppearance();
            if (app != null) {
                PolygonAttributes pa = app.getPolygonAttributes();
                if (pa == null) {
                    pa = new PolygonAttributes();
                    app.setPolygonAttributes(pa);
                }
                if (wireframe) {
                    pa.setPolygonMode(PolygonAttributes.POLYGON_LINE);
                } else {
                    pa.setPolygonMode(PolygonAttributes.POLYGON_FILL);
                }
            }
        } else if (node instanceof Group) {
            Group group = (Group) node;
            for (int i = 0; i < group.numChildren(); i++) {
                toggleWireframeModeRecursive(group.getChild(i), wireframe);
            }
        }
    }
    
    /** Constructor: sets up the canvas, scene, viewer, dynamic lighting, sound, and listeners.
     *  The Canvas3D background color is set using a java.awt.Color.     */
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
        
        // Add the ceiling & ambient lights to the scene
        createStaticCeilingLights();
        sceneBG.addChild(lightBG);        
        
        // adding SOUND
        soundPlayer = new SoundPlayer();
        soundPlayer.playMuseumSound();
        
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
    
    /** Toggles the overhead ceiling lights (swaps between white and colorful)*/
    private static void toggleCeilingLights()
    {    	
    	for (int i=0; i<lightBG.numChildren(); i++)
    	{
    		if(lightBG.getChild(i) instanceof Light)
    		{
	    		Boolean bool = ((Light) lightBG.getChild(i)).getEnable();
	    		((Light) lightBG.getChild(i)).setEnable(!bool);	// toggle the ceiling lights  
    		}
    	}
    }
    
    
    
    // Create ceiling lights (white and colorful) to illuminate the scene
    private BranchGroup createStaticCeilingLights() {
        lightBG = new BranchGroup();
                
       // Create colored ceiling lights
       PointLight coloredLights[] = new PointLight[5];
       coloredLights[0] = (new PointLight(new Color3f(1.0f, 1.0f, 0.0f), new Point3f(0.0f, 9.5f, 0.000f), new Point3f(0.4f, 0.05f, 0.00f)));	// yellow
       coloredLights[1] = (new PointLight(new Color3f(0.0f, 1.0f, 0.3f), new Point3f(60f, 9.50f, 22.50f), new Point3f(0.4f, 0.05f, 0.00f)));	// green 
       coloredLights[2] = (new PointLight(new Color3f(0.0f, 0.0f, 1.0f), new Point3f(60f, 9.50f, -18.5f), new Point3f(0.4f, 0.05f, 0.00f)));	// blue
       coloredLights[3] = (new PointLight(new Color3f(1.0f, 0.0f, 1.0f), new Point3f(-60f, 9.5f, -22.5f), new Point3f(0.4f, 0.05f, 0.00f))); 	// purple
       coloredLights[4] = (new PointLight(new Color3f(1.0f, 0.0f, 0.2f), new Point3f(-60f, 9.5f, 18.50f), new Point3f(0.4f, 0.05f, 0.00f))); 	// red
       
       // Set influencing bounds
       coloredLights[0].setInfluencingBounds(new BoundingSphere(new Point3d(0.0f, 9.5f, 00.00f), 1000));
       coloredLights[1].setInfluencingBounds(new BoundingSphere(new Point3d(60f,  9.5f, 22.50f), 1000));
       coloredLights[2].setInfluencingBounds(new BoundingSphere(new Point3d(60f,  9.5f, -18.5f), 1000));
       coloredLights[3].setInfluencingBounds(new BoundingSphere(new Point3d(-60f, 9.5f, -22.5f), 1000));
       coloredLights[4].setInfluencingBounds(new BoundingSphere(new Point3d(-60f, 9.5f, 18.50f), 1000));
        
       // Set capability, disable at start, and add to lightBG
       for(int i=0; i<coloredLights.length; i++) {
    	   coloredLights[i].setCapability(PointLight.ALLOW_STATE_WRITE | PointLight.ALLOW_STATE_READ);
    	   coloredLights[i].setEnable(false);	// start turned off
    	   lightBG.addChild(coloredLights[i]);
       }
       
                
       // Create white ceiling lights
       PointLight whiteLights[] = new PointLight[5];
       whiteLights[0] = new PointLight(new Color3f(0.700f, 0.7f, 0.700f), new Point3f(0.0f, 9.5f, 0.0f),  new Point3f(0.4f, 0.05f, 0.00f));
       whiteLights[1] = new PointLight(new Color3f(0.725f, 0.7f, 0.675f), new Point3f(60f, 9.5f, 22.5f),  new Point3f(0.4f, 0.05f, 0.00f));	
       whiteLights[2] = new PointLight(new Color3f(0.675f, 0.7f, 0.725f), new Point3f(60f, 9.5f, -18.5f), new Point3f(0.4f, 0.05f, 0.00f));	
       whiteLights[3] = new PointLight(new Color3f(0.725f, 0.7f, 0.675f), new Point3f(-60f, 9.5f, -22.5f),new Point3f(0.4f, 0.05f, 0.00f)); 
       whiteLights[4] = new PointLight(new Color3f(0.675f, 0.7f, 0.725f), new Point3f(-60f, 9.5f, 18.5f), new Point3f(0.4f, 0.05f, 0.00f));  
        
       // Set influencing bounds
       whiteLights[0].setInfluencingBounds(new BoundingSphere(new Point3d(0.0f, 9.5f, 0.0f), 1000));
       whiteLights[1].setInfluencingBounds(new BoundingSphere(new Point3d(60f, 9.5f, 22.5f), 1000));
       whiteLights[2].setInfluencingBounds(new BoundingSphere(new Point3d(60f, 9.5f, -18.5f), 1000));
       whiteLights[3].setInfluencingBounds(new BoundingSphere(new Point3d(-60f, 9.5f, -22.5f), 1000));
       whiteLights[4].setInfluencingBounds(new BoundingSphere(new Point3d(-60f, 9.5f, 18.5f), 1000));
        
       // Set capability, enable at start, and add to lightBG
       for(int i=0; i<whiteLights.length; i++) {
    	   whiteLights[i].setCapability(PointLight.ALLOW_STATE_WRITE | PointLight.ALLOW_STATE_READ);
    	   whiteLights[i].setEnable(true);	// start turned on
           lightBG.addChild(whiteLights[i]);
       }
        
       lightBG.compile();
       return lightBG;
    }

    
    
    
    
    
    /** Updates the camera translation based on keys pressed (W, A, S, D).
     *  Movement is restricted to the horizontal (x-z) plane.
     *  Also updates the dynamic light position.     */
    private void updateMovement() {
        // check if any movement keys are pressed (WASD)
        boolean isMoving = keysDown.contains(KeyEvent.VK_W) || 
                           keysDown.contains(KeyEvent.VK_A) ||
                           keysDown.contains(KeyEvent.VK_S) || 
                           keysDown.contains(KeyEvent.VK_D);

        // control footstep sounds based on movement
        if (isMoving) {
            soundPlayer.startFootsteps();
        } else {
            soundPlayer.stopFootsteps();
        }

        // skip movement calculations if no keys are pressed
        if (!isMoving) {
            return;
        }

        Vector3d localMove = new Vector3d(0, 0, 0);

        // Calculate movement vectors based on pressed keys
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
      
        
        // Rotate movement direction by current yaw angle
        Matrix3d yawRot = new Matrix3d();
        yawRot.rotY(yawAngle);
        yawRot.transform(localMove);

        Vector3d originalPos = new Vector3d(cameraTranslation);
        cameraTranslation.add(localMove);

        // position debug print
        if (SHOW_POSITION && isMoving) {
            //System.out.printf("[POS] X: %.2f, Z: %.2f%n", cameraTranslation.x, cameraTranslation.z);
        }
        
        // Restrict movement within room bounds
        cameraTranslation.x = Math.max(-X_BOUND, Math.min(X_BOUND, cameraTranslation.x));
        cameraTranslation.z = Math.max(-Z_BOUND, Math.min(Z_BOUND, cameraTranslation.z));
        cameraTranslation.y = fixedY; // Maintain constant height
        
        // check object collisions
        boolean collided = false;
        for (double[] bounds : OBJECT_BOUNDS) {
            if (cameraTranslation.x >= bounds[0] && 
                cameraTranslation.x <= bounds[1] && 
                cameraTranslation.z >= bounds[2] && 
                cameraTranslation.z <= bounds[3]) {
                // collision detected
                collided = true;
                if (SHOW_COLLISIONS) {
                    //System.out.printf("[COLLISION] Hit object at X:%.1f-%.1f, Z:%.1f-%.1f%n",
                                   //bounds[0], bounds[1], bounds[2], bounds[3]);
                }
                cameraTranslation.set(originalPos);
                break;
            }
        }

        // wall collision debug
        if (SHOW_COLLISIONS && !collided && 
            (Math.abs(cameraTranslation.x) == X_BOUND || 
             Math.abs(cameraTranslation.z) == Z_BOUND)) {
            //System.out.println("[COLLISION] Hit wall boundary");
        }

        // Update view and lighting
        updateViewTransform();
        updateDynamicLight();
    }
    
    /**
     * Main entry point.
     */
    public static void main(String[] args) {
        frame = new JFrame("Museum");
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
        double offset = 8.0; // Adjust as needed (5.0).
        
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

    // Traverse upward in the scene graph to find a Transform3D in user data.
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
    
    private TransformGroup findSwitchTG(Node node) {
        while (node != null) {
            if (node instanceof TransformGroup) {
                Object data = ((TransformGroup) node).getUserData();
                if (data instanceof String) {
                    String s = (String) data;
                    if ("switch-off".equals(s) || "switch-on".equals(s)) {
                        return (TransformGroup) node;
                    }
                }
            }
            node = node.getParent();
        }
        return null;
    }
    
    private void updateIndicatorColor(TransformGroup indicatorTG, Color3f color) {
        // Only traverse the children of the indicatorTG
        for (int i = 0; i < indicatorTG.numChildren(); i++) {
            Node child = indicatorTG.getChild(i);
            updateIndicatorColorInNode(child, color);
        }
    }

    private void updateIndicatorColorInNode(Node node, Color3f color) {
        if (node instanceof Shape3D) {
            Appearance app = ((Shape3D) node).getAppearance();
            if (app != null) {
                Material mat = app.getMaterial();
                if (mat != null) {
                    mat.setDiffuseColor(color);
                    mat.setEmissiveColor(color);
                    
                    mat.setAmbientColor(new Color3f(0.2f,0.2f,0.2f)); // new maybe remove
                }
            }
        } else if (node instanceof Group) {
            Group group = (Group) node;
            for (int i = 0; i < group.numChildren(); i++) {
                updateIndicatorColorInNode(group.getChild(i), color);
            }
        }
    }

    

    
    
    
    @Override
    public void mouseClicked(MouseEvent e) {
        PickCanvas pickCanvas = new PickCanvas(canvas3D, sceneBG);
        pickCanvas.setMode(PickTool.GEOMETRY);
        pickCanvas.setShapeLocation(e);
        PickResult result = pickCanvas.pickClosest();
        
        if (result != null) {
            Node pickedNode = result.getObject();
            
            if (isRobotNode(pickedNode)) {
                if (ProjectObjects.robotHeadRotator != null) {
                    toggleRobotHeadAnimation();
                }
                return;
            }
            
            String controlType = getControlType(pickedNode);
            if (controlType != null) {
                // Process the control type.
            	
            	// Toggle rotation of man statue
                if (controlType.equals("man-rotation") && ProjectObjects.manRotationTG != null) {
                    RotationInterpolator rotator = (RotationInterpolator) ProjectObjects.manRotationTG.getUserData();
                    ProjectAnimations.toggleAlpha(rotator.getAlpha());
                    soundPlayer.playSwitchClick();
                } 
                // Toggle levitation of man statue
                else if (controlType.equals("man-levitation") && ProjectObjects.manLevitationTG != null) {
                    ProjectAnimations.Levitation levitator = (ProjectAnimations.Levitation) ProjectObjects.manLevitationTG.getUserData();
                    levitator.togglePause();
                    soundPlayer.playSwitchClick();
                } 
                
                // Toggle rotation of woman statue
                else if (controlType.equals("woman-rotation") && ProjectObjects.womanRotationTG != null) {
                	RotationInterpolator rotator = (RotationInterpolator) ProjectObjects.womanRotationTG.getUserData();
                    ProjectAnimations.toggleAlpha(rotator.getAlpha());
                    soundPlayer.playSwitchClick();
                }
                // Toggle levitation of woman statue
                else if (controlType.equals("woman-levitation") && ProjectObjects.womanLevitationTG != null) {
                	ProjectAnimations.Levitation levitator = (ProjectAnimations.Levitation) ProjectObjects.womanLevitationTG.getUserData();
                    levitator.togglePause();
                    soundPlayer.playSwitchClick();
                }
                
                // Play the T-rex sound.
                else if (controlType.equals("trex-sound")) {
                	soundPlayer.playSwitchClick();
                	soundPlayer.playTrexRoar();
                }
                
                return;
            }
            
            
            // Check if the picked node is a switch:
            TransformGroup switchTG = findSwitchTG(pickedNode);
            
            if (switchTG != null) {
            	
                // Retrieve current state, update to new state.
                String currentState = (String) switchTG.getUserData();
                String newState = "switch-off".equals(currentState) ? "switch-on" : "switch-off";
                switchTG.setUserData(newState);
                
                // Determine new indicator color based on new state
                Color3f newIndClr = "switch-off".equals(newState) ? new Color3f(1f, 0f, 0f) : new Color3f(0f, 1f, 0f);
                
                // Update switch's on/off orientation
                Transform3D currentTransform = new Transform3D(); 
                Transform3D rotation180 = new Transform3D(); 
                switchTG.getTransform(currentTransform); 	// Get the current transform.
                rotation180.rotZ(Math.PI);					// Create a 180° rotation about Y.
                currentTransform.mul(rotation180);			// Multiply the current transform by the new rotation.
                switchTG.setTransform(currentTransform);	// Update the current transform
                soundPlayer.playSwitchClick();				// Play switchClick sound effect
                
                // First switch: toggle colorful lights and update switch's indicator.
                if (switchTG == ProjectObjects.switch1TG) {
                    toggleCeilingLights();     
                    updateIndicatorColor(ProjectObjects.indicator1TG, newIndClr);
                } 
                
                // Second switch: toggle background music and update switch's indicator.
                else if (switchTG == ProjectObjects.switch2TG) {
                	soundPlayer.toggleMuseumSound();
                    updateIndicatorColor(ProjectObjects.indicator2TG, newIndClr);
                }
                return;
            }
            
            
            // If the picked object is not a switch, check for a camera view change.
            Transform3D camTransform = findCameraTransform(pickedNode);
            if (camTransform != null) {
                switchToCameraView(camTransform);
            }
            
        }	
        
    }

    /** Check if node is part of robot */
    private boolean isRobotNode(Node node) {
        while (node != null) {
            if (node instanceof TransformGroup) {
                Object data = ((TransformGroup) node).getUserData();
                if (data instanceof String && ((String)data).equals("robot")) {
                    return true;
                }
            }
            node = node.getParent();
        }
        return false;
    }

    /** Start/stop robot head rotation */
    private void toggleRobotHeadAnimation() {
        Alpha alpha = ProjectObjects.robotHeadRotator.getAlpha();
        ProjectAnimations.toggleAlpha(alpha);
    }


    /** Get controlType ("trex-sound" or starts with "man-"/"woman-") */
    private String getControlType(Node node) {
        while (node != null) {
            Object data = node.getUserData();
            if (data instanceof String) {
                String controlType = (String) data;
                if (controlType.startsWith("man-") || controlType.startsWith("woman-") || controlType.equals("trex-sound")) {
                    return controlType;
                }
            }
            node = node.getParent();
        }
        return null;
    }
    
    private int lastMouseX, lastMouseY;
    
    @Override
    public void mousePressed(MouseEvent e) {
        lastMouseX = e.getX();
        lastMouseY = e.getY();

        if (SwingUtilities.isRightMouseButton(e)) {
            PickCanvas pickCanvas = new PickCanvas(canvas3D, sceneBG);
            pickCanvas.setMode(PickTool.GEOMETRY);
            pickCanvas.setTolerance(0.0f);
            pickCanvas.setShapeLocation(e);
            PickResult result = pickCanvas.pickClosest();
            if (result != null) {
                Node pickedNode = result.getObject();
                BranchGroup morphBG = findMorphGroup(pickedNode);
                if (morphBG != null) {
                    Alpha alpha = (Alpha) morphBG.getUserData();
                    if (alpha != null) {
                        if (alpha.getMode() != 0) {
                            alpha.setMode(0);
                        } else {
                            alpha.setMode(Alpha.INCREASING_ENABLE | Alpha.DECREASING_ENABLE);
                        }
                    }
                }
            }
        }
    }

    
    // Utility method to traverse upward until we find a BranchGroup with userData.
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
    
    
    /** MouseMotionListener method: updates yaw and pitch based on dragging.
     *  Horizontal drags update yaw; vertical drags update pitch (clamped to ±45°). */
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
        if (e.getKeyCode() == KeyEvent.VK_M) {
            toggleWireframeMode();
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
