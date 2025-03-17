import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class Player implements KeyListener, MouseMotionListener {
    private TransformGroup playerTG;
    private Transform3D playerTransform;
    private Vector3f playerPosition;
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

    private void updatePlayerPosition() {
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

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        float dx = 0, dz = 0;

        switch (keyCode) {
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
        }

        // update player position
        playerPosition.x += dx;
        playerPosition.z += dz;
        updatePlayerPosition();

        // start playing footsteps sound if not already playing
        if (!isMoving && (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_D)) {
            isMoving = true;
            soundPlayer.startFootsteps(); // Start looping the footsteps sound
        }
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
    public void mouseDragged(MouseEvent e) {
        // calculate mouse movement delta (becareful, canvas is 800x800)
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
    public void mouseMoved(MouseEvent e) {}
}