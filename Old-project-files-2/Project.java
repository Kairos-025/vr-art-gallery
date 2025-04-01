
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.Box;
import org.jogamp.java3d.utils.geometry.Cylinder;
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
    private double moveStep = 0.2;	// TODO: Make moveStep Final again and reset to 0.5
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

    
    
    static Cylinder hitBox;
    static TransformGroup hitBoxTG;
    static Transform3D hitBoxTF;
    static Vector3d hitBoxVec = new Vector3d(0,-8,2);
    static float hitBoxPrevX;
    static float hitBoxPrevZ;
    
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

        
        
        // Create appearance for player hitbox 
        Appearance app = new Appearance();
        app.setColoringAttributes(new ColoringAttributes(new Color3f(0.0f, 1.0f, 1.0f), ColoringAttributes.NICEST));
        app.setPolygonAttributes(new PolygonAttributes());
	   	app.getPolygonAttributes().setBackFaceNormalFlip(true);
	   	app.getPolygonAttributes().setCullFace(PolygonAttributes.CULL_NONE);
	   	
	   	app.setTransparencyAttributes(new TransparencyAttributes());
	   	app.getTransparencyAttributes().setTransparency(0.5f);
	   	app.getTransparencyAttributes().setTransparencyMode(TransparencyAttributes.NICEST);
	   	
	   	app.setMaterial(new Material());
	   	app.getMaterial().setAmbientColor  (new Color3f(1f, 1f, 1f));
	   	app.getMaterial().setDiffuseColor  (new Color3f(0f, 1f, 1f));
	   	app.getMaterial().setSpecularColor (new Color3f(0.35f, 0.35f, 0.35f));
	   	app.getMaterial().setEmissiveColor (new Color3f(0f, 0f, 0f));
	   	app.getMaterial().setLightingEnable(true);
	   	
	   	app.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
	   	app.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ);
   	
	   	
	   	
	   	// LOOKHERE: Create player hitbox 
	   	hitBox = new Cylinder(2.8f, 1f, 32, app);
	   	hitBox.setCapability(Box.ALLOW_COLLISION_BOUNDS_READ);
	   	hitBox.setCapability(Box.ALLOW_COLLIDABLE_READ);
	   	
	   	hitBoxVec = new Vector3d(0,-8,2);
	   	hitBoxTF = new Transform3D();
	   	hitBoxTF.setTranslation(hitBoxVec);
	   	
	   	hitBoxPrevX = 0;
	   	hitBoxPrevZ = 2;
	   	
	   	hitBoxTG = new TransformGroup(hitBoxTF);
	   	hitBoxTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	   	hitBoxTG.addChild(hitBox);
	   	sceneBG.addChild(hitBoxTG);
	   	
	   	CollisionDetector cd = new CollisionDetector(hitBox);
	   	cd.setSchedulingBounds(new BoundingSphere(new Point3d(), 20.0));	// formerly Commons.twenty_bs
	   	sceneBG.addChild(cd);
        
        
        
        
		Material   mtl = new Material();
		mtl.setShininess(32);
		mtl.setAmbientColor  (new Color3f(0f, 0f, 0f));
		mtl.setDiffuseColor  (new Color3f(1f, 1f,1f));	//color
		mtl.setSpecularColor (new Color3f(0f, 0f, 0f));
		mtl.setEmissiveColor (new Color3f(0f, 0f, 0f));
		mtl.setLightingEnable(true);
        
        
        Appearance testapp = new Appearance();
        Box testbox= new Box(12f, 10f, 12f, Box.GENERATE_NORMALS, new Appearance());
        testapp.setMaterial(mtl);
        testapp.setPolygonAttributes(new PolygonAttributes());
        testapp.getPolygonAttributes().setCullFace(PolygonAttributes.CULL_NONE);
        testapp.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.BLENDED, 0.5f));
        
        Box testboxLines= new Box(4f, 10f, 5f, Box.GENERATE_NORMALS, new Appearance());
        testboxLines.getAppearance().setPolygonAttributes(new PolygonAttributes());
        testboxLines.getAppearance().getPolygonAttributes().setPolygonMode(PolygonAttributes.POLYGON_LINE);
        testboxLines.getAppearance().setMaterial(new Material());
        testboxLines.getAppearance().getMaterial().setDiffuseColor  (new Color3f(1f, 1f, 1f));
        testboxLines.getAppearance().getPolygonAttributes().setCullFace(PolygonAttributes.CULL_NONE);
        
        
        
        Appearance testapp2 = new Appearance();
        
		Material   mtl2 = new Material();
		mtl2.setShininess(32);
		mtl2.setAmbientColor  (new Color3f(0f, 0f, 0f));
		mtl2.setDiffuseColor  (new Color3f(1f, 1f, 0f));
		mtl2.setSpecularColor (new Color3f(0f, 0f, 0f));
		mtl2.setEmissiveColor (new Color3f(0f, 0f, 0f));
		mtl2.setLightingEnable(true);
		testapp2.setMaterial(mtl2);
		testapp2.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.BLENDED, 0f));
		testapp2.setPolygonAttributes(new PolygonAttributes());
		testapp2.getPolygonAttributes().setBackFaceNormalFlip(true);
		testbox.getShape(Box.BOTTOM).setAppearance(testapp2);
		
        /*
        	From facing T-Rex:
        		Side 0 is right side (color on outside) ("FRONT")
        		Side 1 is left  side (color on outside) ("BACK")
        		Side 2 is far side (color on outside)	("RIGHT")
        		Side 3 is near side (color on outside)	("LEFT")	(PROBLEM SIDE)
        		
        		Side 4 is ?? side
        		Side 5 is ?? side
        */
		
        for(int i=0; i<6 && testbox.getChild(i)!=null; i++)
        {
        	if(i==3) {testbox.getShape(i).setAppearance(testapp2); }
        	else {	testbox.getShape(i).setAppearance(testapp) 	; }
        }
        
        BoundingBox bb = new BoundingBox(new Point3d(-12, -10, -12), new Point3d(12,10,12));
        testbox.setAlternateCollisionTarget(true);
        testbox.setCollisionBounds(bb);
        System.out.println("testbox.getCollisionBounds(): "+(testbox.getCollisionBounds()));

        
        Transform3D testboxTF = new Transform3D();
        testboxTF.setTranslation(new Vector3d(15,-4,-25));
        
        
        
        
       
        
       
        Box testbox3= new Box(9f, 15f, 9f, Box.GENERATE_NORMALS, new Appearance());
        testbox3.setAppearance(testapp);

        BoundingBox bb3 = new BoundingBox(new Point3d(-9f, -15f, -9f), new Point3d(9f, 15f, 9f));
        testbox3.setAlternateCollisionTarget(true);
        testbox3.setCollisionBounds(bb3);
        
        Transform3D testboxTF3 = new Transform3D();
        testboxTF3.setTranslation(new Vector3d(-85,-4, 20));
        
        TransformGroup testbox3TG = new TransformGroup(testboxTF3);
        testbox3TG.addChild(testbox3);
        sceneBG.addChild(testbox3TG);
        
        
        
        Box testbox4= new Box(9f, 15f, 9f, Box.GENERATE_NORMALS, new Appearance());
        testbox4.setAppearance(testapp);

        BoundingBox bb4 = new BoundingBox(new Point3d(-9f, -15f, -9f), new Point3d(9f, 15f, 9f));
        testbox4.setAlternateCollisionTarget(true);
        testbox4.setCollisionBounds(bb4);
        
        Transform3D testboxTF4 = new Transform3D();
        testboxTF4.setTranslation(new Vector3d(-85, -4, -20));
        
        TransformGroup testbox4TG = new TransformGroup(testboxTF4);
        testbox4TG.addChild(testbox4);
        sceneBG.addChild(testbox4TG);
        
        
        
        
        
        //System.out.println("testbox3.getCollisionBounds(): "+(testbox3.getCollisionBounds()));
        
                
        
        TransformGroup testboxTG = new TransformGroup(testboxTF);
        testboxTG.addChild(testbox);
        testboxTG.addChild(testboxLines);
        sceneBG.addChild(testboxTG);
        //System.out.println("testbox.getCollisionBounds(): "+(testbox.getCollisionBounds()));
        
       
        
        // Create a large room using a Box.
        Box room = new Box(100f, 15f, 50f, Primitive.GENERATE_NORMALS | Primitive.GENERATE_TEXTURE_COORDS | Primitive.ENABLE_APPEARANCE_MODIFY, null);
        room.setCollidable(false);	// Set non-collidable to allow for implementing custom collision
        
        
        
        // Apply tiled textures to each face.
        ((Shape3D) room.getShape(Box.FRONT)).setAppearance(createTiledTextureAppearance("Projects/ImageWall.jpg"));
        ((Shape3D) room.getShape(Box.BACK)).setAppearance(createTiledTextureAppearance("Projects/ImageWall.jpg"));
        ((Shape3D) room.getShape(Box.LEFT)).setAppearance(createTiledTextureAppearance("Projects/ImageWall.jpg"));
        ((Shape3D) room.getShape(Box.RIGHT)).setAppearance(createTiledTextureAppearance("Projects/ImageWall.jpg"));
        ((Shape3D) room.getShape(Box.TOP)).setAppearance(createTiledTextureAppearance("Projects/ImageCeiling.jpg"));
        ((Shape3D) room.getShape(Box.BOTTOM)).setAppearance(createTiledTextureAppearance("Projects/ImageFloor.jpg"));

        sceneBG.addChild(room);
        sceneBG.addChild(createStaticLights());
        //TODO: Re-enable this line: 
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
    
 // Add method to Project/other utility class
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
    
    
    
    /** Constructor: sets up the canvas, scene, viewer, dynamic lighting, and listeners.
     * The Canvas3D background color is set using a java.awt.Color. */
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

        // MINE View view = su.getViewer().getView();
        view.setFieldOfView(Math.toRadians(80)); // MINE
        //MINE view.setBackClipDistance(100.0); 
        
        
        sceneBG = setup_Scene();
        su.addBranchGraph(sceneBG);
        viewTG = su.getViewingPlatform().getViewPlatformTransform();
        viewTG.getTransform(defaultViewTransform);
        updateViewTransform();
        
        // Add the dynamic light to the scene.
        addDynamicLight();
            	
        
        movementTimer = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateMovement();
                dummyCollisionTest();
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
    static float initX = (float)hitBoxVec.getX();
    static float initZ = (float)hitBoxVec.getZ();
    
    //TODO: Remove temp SHIFT(sprint) and Z (go through walls) keys
    private void updateMovement() {
        if (keysDown.isEmpty())
            return;

        Boolean justCam=false;
        
        Vector3d localMove = new Vector3d(0, 0, 0);
        
        // REMOVE SHIFT KEY SPRINT
        if (keysDown.contains(16)) {
            if(moveStep==0.5) { moveStep=2;}
            else 			  { moveStep=0.5;}
            
        }
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
        if (keysDown.contains(KeyEvent.VK_Z)) 
        {
            System.out.println("KEY Z DOWN");
        	justCam = !justCam;
        }

        if(!(keysDown.contains(KeyEvent.VK_W)) && !(keysDown.contains(KeyEvent.VK_A)) && !(keysDown.contains(KeyEvent.VK_S)) && !(keysDown.contains(KeyEvent.VK_D)) && !(keysDown.contains(KeyEvent.VK_Z)))
        {
        	return;
        }
        
        // Rotate movement direction by yaw.
        Matrix3d yawRot = new Matrix3d();
        yawRot.rotY(yawAngle);
        yawRot.transform(localMove);

        // Apply movement.
        //cameraTranslation.add(localMove);
        
        System.out.printf("Start: (%.8f, %.8f)\n",(float)hitBoxVec.getX(), (float)hitBoxVec.getZ());

        initX = (float)hitBoxVec.getX();
        initZ = (float)hitBoxVec.getZ();
        
        // LOOKHERE: Move hitbox (temp)
        float newX = initX;
        float newZ = initZ;
        
        hitBoxVec.add(localMove);
        hitBoxTF.setTranslation(hitBoxVec);
        hitBoxTG.setTransform(hitBoxTF);
        
        if(CollisionDetector.inCollision) 
        {
    	    hitBoxVec.set(new Vector3d(initX, hitBoxVec.getY(),initZ));
    	    hitBoxTF.setTranslation(hitBoxVec);
    	    hitBoxTG.setTransform(hitBoxTF);
        	
    	    // Test X:
        	newX = initX+((float)localMove.x);
    	    hitBoxVec.set(new Vector3d(newX, hitBoxVec.getY(),initZ));
    	    hitBoxTF.setTranslation(hitBoxVec);
    	    hitBoxTG.setTransform(hitBoxTF);

            if(!CollisionDetector.inCollision) 
            {
            	System.out.println("X good");
            }
            else
            {
            	System.out.println("X bad");
            	newX = initX-2*((float)localMove.x);
            }
        	
        	
    	    // Test Z:
        	newZ = initZ+((float)localMove.z);
    	    hitBoxVec.set(new Vector3d(initX, hitBoxVec.getY(),newZ));
    	    hitBoxTF.setTranslation(hitBoxVec);
    	    hitBoxTG.setTransform(hitBoxTF);

            if(!CollisionDetector.inCollision) 
            {
            	System.out.println("Z good");
            }
            else
            {
            	System.out.println("Z bad");
            	newZ = initZ-2*((float)localMove.z);
            }
    	    hitBoxVec.set(new Vector3d(initX, hitBoxVec.getY(),initZ));
    	    hitBoxTF.setTranslation(hitBoxVec);
    	    hitBoxTG.setTransform(hitBoxTF);
        }
        
        else
        {
        	newX = initX+((float)localMove.x);
        	newZ = initZ+((float)localMove.z);
        	System.out.println("No col");
        }	
        
        	hitBoxVec.set(new Vector3d(newX, hitBoxVec.getY(),newZ));
	        hitBoxTF.setTranslation(hitBoxVec);
	        hitBoxTG.setTransform(hitBoxTF); 
    	
	    
		// Restrict camera movement inside the room.
		cameraTranslation.x = Math.max(-X_BOUND, Math.min(X_BOUND, newX));			// cameraTranslation.x = Math.max(-X_BOUND, Math.min(X_BOUND, cameraTranslation.x));
		cameraTranslation.z = Math.max(-Z_BOUND, Math.min(Z_BOUND, newZ));			// cameraTranslation.z = Math.max(-Z_BOUND, Math.min(Z_BOUND, cameraTranslation.z));
	    cameraTranslation.y = fixedY; // Keep y fixed.
	
	    // Keep hitbox in room   
	    if(!keysDown.contains(KeyEvent.VK_Z)) {
	    hitBoxVec.set(new Vector3d(cameraTranslation.x, hitBoxVec.getY(),cameraTranslation.z));
	    hitBoxTF.setTranslation(hitBoxVec);
	    hitBoxTG.setTransform(hitBoxTF);
	    }

	    // Record new old values
	    hitBoxPrevX = (float)cameraTranslation.x;
	    hitBoxPrevZ = (float)cameraTranslation.z;
        
       // if(CollisionDetector.inCollision && ((float)cameraTranslation.x!=initX || cameraTranslation.z!=initZ))
        //{	// for viewer updates
		    updateViewTransform();
		    updateDynamicLight();
       // }
        
        
        
        System.out.printf("Final: (%.8f, %.8f)\n",(float)hitBoxVec.getX(), (float)hitBoxVec.getZ());
        if(initX != (float)hitBoxVec.getX())
        { System.out.printf("  X moved: %.8f\n", ((float)hitBoxVec.getX()-initX)); }
        else
        { System.out.printf("  X same : %.8f\n", ((float)hitBoxVec.getX()-initX)); }
        
        if(initZ != (float)hitBoxVec.getZ())
        { System.out.printf("  Z moved: %.8f\n\n", ((float)hitBoxVec.getZ()-initZ)); }
        else
        { System.out.printf("  Z same : %.8f\n\n", ((float)hitBoxVec.getZ()-initZ)); }
        
    }
    
    public static Boolean dummyCollisionTest()
    {
    	return(CollisionDetector.inCollision);
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
        
        // TODO: Reset maxPitch: double maxPitch = Math.toRadians(45);
        double maxPitch = Math.toRadians(90);	// custom - remove
        
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






// CollisionDetector for player's hitbox
class CollisionDetector extends Behavior
{
	static boolean wasInCollision=false;
	static boolean inCollision=false;
	static Vector3f lastNonCol = new Vector3f(new Point3d(0d, 0.6d, 5d));

	private Cylinder hitboxCyl;
	private ColoringAttributes shapeColoring;
	private Appearance shapeAppearance;
	private WakeupOnCollisionEntry wEnter;
	private WakeupOnCollisionExit wExit;
	private WakeupOnCollisionMovement wMoveInCol;
	
	
	//public CollisionDetector(Box b)
	public CollisionDetector(Cylinder b)
	{
		hitboxCyl=b;	//save the original color of "shape"
		shapeAppearance=hitboxCyl.getAppearance();
		shapeColoring=shapeAppearance.getColoringAttributes();
		inCollision=false;
	}
	public void initialize()
	{
		wEnter=new WakeupOnCollisionEntry(hitboxCyl, WakeupOnCollisionEntry.USE_GEOMETRY);
		wExit =new WakeupOnCollisionExit(hitboxCyl, WakeupOnCollisionExit.USE_GEOMETRY);
		wMoveInCol = new WakeupOnCollisionMovement(hitboxCyl, WakeupOnCollisionMovement.USE_GEOMETRY);
		System.out.printf("\u001B[32m");
		wakeupOn(wEnter);	// initialize the behavior
	}
	
	public void processStimulus(Iterator<WakeupCriterion>criteria)
	{
		inCollision=!inCollision;	// collision has taken place
		System.out.println("(current inCollision: "+inCollision+")");
		
		if(wEnter.hasTriggered() && wExit.hasTriggered())
		{
			System.out.println("\u001B[33mBOTH TRIGGERED (current inCollision: "+inCollision+")\u001B[33m");
			Project.hitBoxVec.add(new Vector3d((float)-Project.hitBoxVec.getX(), 0, (float)-Project.hitBoxVec.getZ()));
		Project.hitBoxTF.setTranslation(Project.hitBoxVec);
			Project.hitBoxTG.setTransform(Project.hitBoxTF);
			
			
			float newX = Project.initX-((float) Project.hitBoxVec.getX());
			float newZ = Project.initZ-(((float) Project.hitBoxVec.getZ()));
			Project.hitBoxVec.add(new Vector3d(newX, 0,newZ));
			Project.hitBoxTF.setTranslation(Project.hitBoxVec);
			Project.hitBoxTG.setTransform(Project.hitBoxTF);
			
		}
		
		else
		{
			System.out.print("\u001B[0m");
			//In Col
						
			if(inCollision) {
				//System.out.printf("\u001B[31mIn collision: (%.8f, %.8f)\u001B[0m\n", (float) Project.cylVec.getX(),(float) Project.cylVec.getZ());
				
				if(!(wEnter.hasTriggered() && wExit.hasTriggered())) { System.out.printf("\u001B[31m"); }
				System.out.printf("In collision: (%.8f, %.8f)\n", (float) Project.hitBoxVec.getX(),(float) Project.hitBoxVec.getZ());
				
				//Project.hitBoxVec.add(new Vector3d((float)-Project.hitBoxVec.getX(), 0, (float)-Project.hitBoxVec.getZ()));
				//Project.hitBoxTF.setTranslation(Project.hitBoxVec);
				//Project.hitBoxTG.setTransform(Project.hitBoxTF);
				
				float newX = Project.initX-((float) Project.hitBoxVec.getX());
				float newZ = Project.initZ-(((float) Project.hitBoxVec.getZ()));
				Project.hitBoxVec.add(new Vector3d(newX, 0,newZ));
				Project.hitBoxTF.setTranslation(Project.hitBoxVec);
				Project.hitBoxTG.setTransform(Project.hitBoxTF);
				
				
				WakeupCriterion[] wc = new WakeupCriterion[] {wExit, wMoveInCol};
				WakeupOr wo = new WakeupOr(wc);
				//wakeupOn(wo);
	
				
				wakeupOn(new WakeupOnCollisionExit(hitboxCyl, WakeupOnCollisionExit.USE_GEOMETRY));// keep the color until no collision
			}
			
			// Not in Col
			if (!inCollision) {
				if(!(wEnter.hasTriggered() && wExit.hasTriggered())) { System.out.printf("\u001B[32m"); }
				System.out.printf("Not in collision: (%.8f, %.8f)\n", (float)Project.hitBoxVec.getX(), (float)Project.hitBoxVec.getZ());
				//System.out.printf("\u001B[32mnot in collision: (%.8f, %.8f)\u001B[0m\n", (float)Project.cylVec.getX(), (float)Project.cylVec.getZ());
				
				//Project.cylVec.set(new Point3f((float)Project.cylVec.getX(), (float)Project.cylVec.getY(), (float)Project.cylVec.getX()));
				//Project.cylTF.setTranslation(Project.cylVec);
				//Project.cylTG.setTransform(Project.cylTF);
				
				Project.hitBoxPrevX = (float)Project.hitBoxVec.getX();
				Project.hitBoxPrevZ = (float)Project.hitBoxVec.getZ();
				
				WakeupCriterion[] wc = new WakeupCriterion[] {wEnter, wMoveInCol};
				WakeupOr wo = new WakeupOr(wc);
				//wakeupOn(wo);
				
				wakeupOn(new WakeupOnCollisionEntry(hitboxCyl, WakeupOnCollisionEntry.USE_GEOMETRY));	// wait for collision happens
				
				//wakeupOn(wMoveInCol); 
				
			} 
		}

		
		System.out.println("wEnter    : "+wEnter.hasTriggered());
		System.out.println("wExit     : "+wExit.hasTriggered());
		System.out.println("wMoveInCol: "+wMoveInCol.hasTriggered()+"\n");
	} 
	
}


