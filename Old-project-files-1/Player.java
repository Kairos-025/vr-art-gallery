
import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.Box;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.*;


import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class Player implements KeyListener, MouseMotionListener {
	protected static TransformGroup playerTG = new TransformGroup();
    protected static Transform3D playerTransform = new Transform3D();
    protected static Vector3f playerPosition;
    private float lastNonColX=0;
    private float lastNonColZ=5;
    
    private float playerYaw = 0; // horizontal rotation (left/right)
    private float playerPitch = 0; // vertical rotation (up/down)
    private float playerSpeed = 0.1f;
    private SimpleUniverse su;
    private SoundPlayer soundPlayer; // soundPlayer instance for playing sounds
    private boolean isMoving = false; // flag to track if the player is moving

	
    public Player(SimpleUniverse su, SoundPlayer soundPlayer) {
        this.su = su;
        this.soundPlayer = soundPlayer;

        
        playerTG = new TransformGroup();
        playerTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        playerTransform = new Transform3D();
        playerPosition = new Vector3f(0, 1, 5);
        playerTransform.setTranslation(playerPosition);
        playerTG.setTransform(playerTransform);

        updateViewer();
    }

    public TransformGroup getPlayerTransformGroup() {
        return playerTG;
    }

    
    public void updatePlayerPosition() {
        playerTransform.setTranslation(playerPosition);
        playerTG.setTransform(playerTransform);
        updateViewer();
    }

    private void updateViewer() {
    	
        Transform3D viewerTransform = new Transform3D();

        // apply yaw (horizontal rotation)
        Transform3D yawRotation = new Transform3D();
        yawRotation.rotY(Math.toRadians(playerYaw));

        // apply pitch (vertical rotation)
        Transform3D pitchRotation = new Transform3D();
        pitchRotation.rotX(Math.toRadians(playerPitch));

        // combine yaw and pitch rotations
        viewerTransform.mul(yawRotation, pitchRotation);
        viewerTransform.setTranslation(new Vector3f(playerPosition));
        su.getViewingPlatform().getViewPlatformTransform().setTransform(viewerTransform);
    }

    public String checkCol(Boolean bool)
    {
        String textWhite = "\u001B[37m";
        String textGreen = "\u001B[32m";
        String textRed   = "\u001B[31m";
        String textReset = "\u001B[0m";

    	if(bool) { return(textRed+"true"+textReset); } 
    	else { return(textGreen+"false"+textReset); }
    }
    
    public Boolean nextVecCollides(Vector3f vec)
    {
    	MuseumNew.pbtest.setTranslation(vec);
    	return(CollisionDetector.inCollision);
    }
    
    
    public Vector3f checkNextPosition(float dx, float dz)
    {
    	float newX = playerPosition.x;
    	float newZ = playerPosition.z;
    	
    	//MuseumNew.updateDynamicLightPosition(new Vector3f(playerPosition.x+dx, 1, playerPosition.z+dz));
    	
    		
   		System.out.printf("requestPos: (%.8f, %.8f) - %s\n", playerPosition.x+dx, playerPosition.z+dz, checkCol(nextVecCollides(new Vector3f(playerPosition.x+dx, 1, playerPosition.z+dz))));
   		MuseumNew.pbtest.setTranslation(new Vector3f(playerPosition.x+dx, 1, playerPosition.z+dz));
    	
   		
    	
    	if(!CollisionDetector.inCollision)
    	{ return new Vector3f(playerPosition.x+dx, 1, playerPosition.z+dz); }
    	
    	else
    	{
    		MuseumNew.pbtest.setTranslation(new Vector3f(playerPosition.x, 1, playerPosition.z));
    		if(CollisionDetector.inCollision)
    		{
    			MuseumNew.pbtest.setTranslation(new Vector3f(playerPosition.x+dx, 1, CollisionDetector.lastNonCol.getZ()));
    			if(!CollisionDetector.inCollision)
    			{
    				newX = playerPosition.x+dx;
    			}
    			else
    			{
    				newX = playerPosition.x;
    			}
    			
    			MuseumNew.pbtest.setTranslation(new Vector3f(CollisionDetector.lastNonCol.getX(), 1, playerPosition.z+dz));
    			if(!CollisionDetector.inCollision)
    			{
    				newZ = playerPosition.x+dz;
    			}
    			else
    			{
    				newZ = playerPosition.z;
    			}	
    		}
    		else
    		{
    			MuseumNew.pbtest.setTranslation(new Vector3f(playerPosition.x+dx, 1, playerPosition.z));
    			if(!CollisionDetector.inCollision)
    			{
    				newX = playerPosition.x+dx;
    			}
    			else
    			{
    				newX = playerPosition.x;
    			}
    			
    			MuseumNew.pbtest.setTranslation(new Vector3f(playerPosition.x, 1, playerPosition.z+dz));
    			if(!CollisionDetector.inCollision)
    			{
    				newZ = playerPosition.z+dz;
    			}
    			else
    			{
    				newZ = playerPosition.z;
    			}	
    		}	
    		
    	}
    	return new Vector3f(new Point3d(newX, 0.6, newZ));
    }
    
    Boolean prevCol = false;
    
    @Override
    public void keyPressed(KeyEvent e) {


//        new Thread(() -> {
//            try {
//            	
//            	
//            	System.out.println("Sleeping"); 
//            	Thread.sleep(1000);
//            	System.out.println("Done sleeping");
//            	
                int keyCode = e.getKeyCode();
                float dx = 0, dz = 0;
        //CollisionDetector.inCollision = false;
        updatePlayerPosition();
                
 		if(!prevCol)
 		{
 			CollisionDetector.lastNonCol.setX(playerPosition.x); 
 			CollisionDetector.lastNonCol.setZ(playerPosition.z);
 		}
        
 		
       
        playerSpeed = 0.1f;	//TO DO: TEMP SPEED REDUCTION (Original: 0.1)
    	
    	switch (keyCode) {
    		case KeyEvent.VK_R:
    			playerPosition = new Vector3f(0, 1, 5);
    			updatePlayerPosition();
    			break;
    		case KeyEvent.VK_W:
                dx = -(float) Math.sin(Math.toRadians(playerYaw)) * playerSpeed;
                dz = -(float) Math.cos(Math.toRadians(playerYaw)) * playerSpeed;
                break;
            case KeyEvent.VK_S:
                dx = (float) Math.sin(Math.toRadians(playerYaw)) * playerSpeed;
                dz = (float) Math.cos(Math.toRadians(playerYaw)) * playerSpeed;
                break;
            case KeyEvent.VK_A:
                dx = (float) Math.sin(Math.toRadians(playerYaw - 90)) * playerSpeed;
                dz = (float) Math.cos(Math.toRadians(playerYaw - 90)) * playerSpeed;
                break;
            case KeyEvent.VK_D:
                dx = (float) Math.sin(Math.toRadians(playerYaw + 90)) * playerSpeed;
                dz = (float) Math.cos(Math.toRadians(playerYaw + 90)) * playerSpeed;
                break;
            default: return;
        }
    	
     	

    	
    	if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_D)
    	{ 
        	
    		System.out.println("In collision at end of last move: "+checkCol(prevCol));
     		System.out.printf("\ncurrentPos: (%.8f, %.8f) - %s\n", playerPosition.x, playerPosition.z, checkCol(CollisionDetector.inCollision));
    		System.out.printf("lastNonCol: (%.8f, %.8f)\n", CollisionDetector.lastNonCol.getX(), CollisionDetector.lastNonCol.getZ());
    		

    		
    		Vector3f temp = checkNextPosition(dx,dz);
    		playerPosition.x = temp.getX();
    		playerPosition.z = temp.getZ();
    		
    		updatePlayerPosition(); 
	    	prevCol = CollisionDetector.inCollision;
	    	

	    	
	        // start playing footsteps sound if not already playing
	        if (!isMoving && (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_D)) {
	            isMoving = true;
	            soundPlayer.startFootsteps(); // Start looping the footsteps sound
	        }
	        
	        
	       
        
    }
                
//            } 
//        catch (InterruptedException e1) {
//             e1.printStackTrace();
//         }
//        }).start();    
}

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();

        // stop playing footsteps sound if movement keys are released
        if (isMoving && (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_D)) {
            isMoving = false;
            soundPlayer.stopFootsteps(); // stop the footsteps sound
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    //TODO: Switch back to mouseDragged
    public void mouseMoved(MouseEvent e) {
        // calculate mouse movement delta (be careful, canvas is 800x800)
        int deltaX = e.getX() - 400;
        int deltaY = e.getY() - 400;

        // update yaw
        playerYaw -= deltaX * 0.1f; // Invert deltaX to fix left/right inversion

        // update pitch
        playerPitch -= deltaY * 0.1f;
        playerPitch = Math.max(-90, Math.min(90, playerPitch)); // limit pitch to ±90 degrees

        updateViewer();

        try {
            java.awt.Robot robot = new java.awt.Robot();
            robot.mouseMove(e.getComponent().getLocationOnScreen().x + 400,
                            e.getComponent().getLocationOnScreen().y + 400);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
  //TODO: Switch back to mouseMoved
    public void mouseDragged(MouseEvent e) {}
}