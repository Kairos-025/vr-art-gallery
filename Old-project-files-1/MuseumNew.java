
import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.Box;
import org.jogamp.java3d.utils.geometry.ColorCube;
import org.jogamp.java3d.utils.geometry.Cylinder;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import javax.swing.*;
import java.awt.*;
import java.util.Iterator;

import org.jogamp.vecmath.*;


public class MuseumNew extends JPanel {
    private static final long serialVersionUID = 1L;
    private static JFrame frame;
    private Canvas3D canvas;
    private SimpleUniverse su;
    private BranchGroup sceneBG;
    
    private PointLight dynamicLight; // dynamic light that follows the player
    private static TransformGroup dynamicLightTransformGroup;
    
   // static Box playerBox;
    static Box playerBox;
    
    private PointLight[] staticLights; // static lights
    private TransformGroup[] staticLightTransformGroups;
    private Player player;
    private SoundPlayer soundPlayer;

    
    public MuseumNew() {
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        canvas = new Canvas3D(config);
        su = new SimpleUniverse(canvas);

        sceneBG = new BranchGroup();
        
        // sound
        soundPlayer = new SoundPlayer();
        soundPlayer.playMuseumSound();

        // create the museum layout using the imported model
        sceneBG.addChild(MuseumObjectsNew.createMuseumLayout());

        // create the player and pass the SoundPlayer instance
        player = new Player(su, soundPlayer);
        sceneBG.addChild(player.getPlayerTransformGroup());
        
       
        // static lights
        // addStaticLights();

        // Increase FOV to 90 degrees
        View view = su.getViewer().getView();
        view.setFieldOfView(Math.toRadians(80)); 
        view.setBackClipDistance(100.0); 		 

        
        
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

  /*  private void addStaticLights() {
        Vector3f[] lightPositions = {
            new Vector3f(7, 1, 0),
            new Vector3f(0, 1, 0),
        };

        staticLights = new PointLight[2];
        staticLightTransformGroups = new TransformGroup[2];

        for (int i = 0; i < 2; i++) {
            staticLights[i] = new PointLight(
                new Color3f(1, 1, 1),
                new Point3f(lightPositions[i]),
                new Point3f(1, 0, 0)
            );
            staticLights[i].setInfluencingBounds(new BoundingSphere(new Point3d(), 100.0));

            staticLightTransformGroups[i] = new TransformGroup();
            staticLightTransformGroups[i].setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            staticLightTransformGroups[i].addChild(staticLights[i]);

            sceneBG.addChild(staticLightTransformGroups[i]);

            System.out.println("Static Light " + (i + 1) + " Position: " + lightPositions[i]);
        }
    }  */

    static Transform3D pbtest = new Transform3D();
    
    private void addDynamicLight() {
       
    	Appearance app = Commons.set_Appearance(Commons.White);
    	
    	//app.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.BLENDED, 0.99f));
    	//playerBox = new Box(0.25f, 0.1f, 0.25f, Box.GENERATE_NORMALS, app);
    	playerBox = new Box(0.1f, 0.05f, 0.1f, Box.GENERATE_NORMALS, app);
    	PolygonAttributes pa = new PolygonAttributes();
    	pa.setBackFaceNormalFlip(true);
    	pa.setCullFace(PolygonAttributes.CULL_NONE);
    	app.setPolygonAttributes(pa);
    	TransparencyAttributes ta = new TransparencyAttributes();
    	//ta.setTransparency(1f);
    	ta.setTransparency(0.5f);
    	ta.setTransparencyMode(TransparencyAttributes.NICEST);
    	app.setTransparencyAttributes(ta);
    	app.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
    	app.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ);
    	playerBox.setCapability(Box.ALLOW_COLLISION_BOUNDS_READ);
    	playerBox.setCapability(Box.ALLOW_COLLIDABLE_READ);
    	
    	
    	
    	dynamicLight = new PointLight(	new Color3f(1, 1, 1),
        								new Point3f(0, 0, 0),
        								new Point3f(1, 0, 0)  );
        dynamicLight.setInfluencingBounds(new BoundingSphere(new Point3d(), 100.0));
        
        
        //
        Transform3D pbtest = new Transform3D();
        pbtest.setTranslation(new Vector3f(new Point3f(0f,-0.4f,0f)));
        TransformGroup pbtestTG = new TransformGroup(pbtest);
        pbtestTG.addChild(playerBox);
        //
        
        //playerBox.setBoundsAutoCompute(true);
        //System.out.println(playerBox.getBounds());
        // Center=(-0.002526763415691189, -0.03529389725309994, -0.002526763415691189)  Radius=0.1682216516380146
        
//        
//        System.out.println(playerBox.getCollisionBounds());
//        
//        BoundingBox playerBB = new BoundingBox();
//        playerBB.setLower(new Point3d((Player.playerPosition.getX()-0.05f), (Player.playerPosition.getY()-0f), (Player.playerPosition.getZ()-0.05f)));
//        playerBB.setUpper(new Point3d((Player.playerPosition.getX()+0.05f), (Player.playerPosition.getY()+0.05f), (Player.playerPosition.getZ()+0.05f)));
//        
//        Player.playerTG.addChild(playerBB);
//        
//        playerBox.setAlternateCollisionTarget(true);
//        playerBox.setBounds(playerBB);
//        playerBox.setCollisionBounds(playerBB);
//        
		CollisionDetector cd = new CollisionDetector(playerBox);
		cd.setSchedulingBounds(Commons.twenty_BS);
		pbtestTG.addChild(cd);
        
        
        dynamicLightTransformGroup = new TransformGroup();
        dynamicLightTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        dynamicLightTransformGroup.addChild(dynamicLight);
        
        //dynamicLightTransformGroup.addChild(playerBox);
        dynamicLightTransformGroup.addChild(pbtestTG);
        
        sceneBG.addChild(dynamicLightTransformGroup);

        // a thread to update the light's position based on the player's position
        new Thread(() -> {
            while (true) {
                try {
                	
                  	//playerBox
                    
                	
                    // get the player's current position
                    Transform3D playerTransform = new Transform3D();
                    player.getPlayerTransformGroup().getTransform(playerTransform);
                    
                    Vector3f playerPosition = new Vector3f();
                    playerTransform.get(playerPosition);

                    
                    updateDynamicLightPosition(playerPosition);
                    
                    //System.out.println("playerPosition: " +playerPosition);
                    //System.out.println("playerTransform: " +playerTransform);
                    
                    // avoid CPU/GPU usage
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    static void updateDynamicLightPosition(Vector3f playerPosition) {
        Transform3D transform3D = new Transform3D();
        transform3D.setTranslation(playerPosition);
        dynamicLightTransformGroup.setTransform(transform3D);

        //System.out.println("Dynamic Light Position: " + playerPosition);
    }

    public static void main(String[] args) {
        frame = new JFrame("NEW Museum NEW");
        frame.getContentPane().add(new MuseumNew());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}


// CollisionDetector for 
class CollisionDetector extends Behavior
{
	static boolean wasInCollision=false;
	static boolean inCollision=false;
	//static Vector3f lastNonCol = new Vector3f(new Point3d(0d, 0.6d, 5d));
	static Point3f lastNonCol = new Point3f(0f, 0.6f, 5f);
	
	//private Box box;
	private Box box;
	private ColoringAttributes shapeColoring;
	private Appearance shapeAppearance;
	private WakeupOnCollisionEntry wEnter;
	private WakeupOnCollisionExit wExit;
	private WakeupOnCollisionMovement wMoveInCol;
	
	//public CollisionDetector(Box b)
	public CollisionDetector(Box b)
	{
		box=b;	//save the original color of "shape"
		shapeAppearance=box.getAppearance();
		shapeColoring=shapeAppearance.getColoringAttributes();
		inCollision=false;
	}
	public void initialize()
	{
		wEnter = new WakeupOnCollisionEntry(box, WakeupOnCollisionEntry.USE_BOUNDS);
		wExit = new WakeupOnCollisionExit(box, WakeupOnCollisionExit.USE_BOUNDS);
		wMoveInCol = new WakeupOnCollisionMovement(box, WakeupOnCollisionMovement.USE_BOUNDS);
		wakeupOn(wEnter);	// initialize the behavior
	}
	
	public void processStimulus(Iterator<WakeupCriterion>criteria)
	{
		inCollision=!inCollision;	// collision has taken place
		
		
		//In Col
		if(inCollision) {
			Transform3D temp = new Transform3D();
			box.getLocalToVworld(temp);
			Vector3f tempVec = new Vector3f();
			temp.get(tempVec);
			System.out.printf("In collision: (%.8f, %.8f)\n", tempVec.getX(), tempVec.getZ());
			
 			if(wEnter.hasTriggered())
 			{
 				if(Player.playerPosition.getX() > lastNonCol.getX())
 					Player.playerPosition.setX(lastNonCol.getX()-0.1f);
 				else
 					Player.playerPosition.setX(lastNonCol.getX()+0.1f);
 				
 				
 				if(Player.playerPosition.getZ() > lastNonCol.getZ())
 					Player.playerPosition.setZ(lastNonCol.getZ()-0.1f);
 				else
 					Player.playerPosition.setZ(lastNonCol.getZ()+0.1f);
 				
 				
// 				Player.playerPosition.setX(lastNonCol.getX());
// 				Player.playerPosition.setZ(lastNonCol.getZ()); 
 				System.out.printf("Moved to: (%.8f, %.8f)\n", Player.playerPosition.getX(), Player.playerPosition.getZ());
 			}
 			
			wakeupOn(wExit);// keep the color until no collision
			
		}
		
		// Not in Col
		else{
//			Transform3D temp = new Transform3D();
//			box.getLocalToVworld(temp);
//			temp.get(lastNonCol);
			
			
			
			wakeupOn(wEnter);	// wait for collision happens
			//System.out.print("not in collision: "+ temp+"\n");
			System.out.printf("not in collision: (%.8f, %.8f)\n", lastNonCol.getX(), lastNonCol.getZ());
			//wakeupOn(wMoveInCol);// keep the color until no collision
			
		} 
		System.out.println("wEnter    : "+wEnter.hasTriggered());
		System.out.println("wExit     : "+wExit.hasTriggered());
		System.out.println("wMoveInCol: "+wMoveInCol.hasTriggered());
		
//		wEnter = new WakeupOnCollisionEntry(box, WakeupOnCollisionEntry.USE_BOUNDS);
//		wExit = new WakeupOnCollisionExit(box, WakeupOnCollisionExit.USE_BOUNDS);
//		wMoveInCol = new WakeupOnCollisionMovement(box, WakeupOnCollisionMovement.USE_BOUNDS);
	} 
	
}

