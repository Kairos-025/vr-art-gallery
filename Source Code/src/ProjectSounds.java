import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProjectSounds {

    private Clip museumSoundClip; // clip for MuseumSoundEffect.wav
    private Clip footstepsClip;   // clip for footsteps.wav
    private Clip trexClip;        // clip for T-rex-roar.wav
    private Clip switchClickClip; // clip for switchClick.wav
    
    boolean isPlayingBack = false;
    
    public ProjectSounds() {
        // load MuseumSoundEffect.wav
        try {
            File museumSoundFile = new File("sounds/MuseumSoundEffect.wav");
            AudioInputStream museumSoundStream = AudioSystem.getAudioInputStream(museumSoundFile);
            museumSoundClip = AudioSystem.getClip();
            museumSoundClip.open(museumSoundStream);
            isPlayingBack = true;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
            museumSoundClip = null;
        }

        // load footsteps.wav
        try {
            File footstepsFile = new File("sounds/footsteps.wav");
            AudioInputStream footstepsStream = AudioSystem.getAudioInputStream(footstepsFile);
            footstepsClip = AudioSystem.getClip();
            footstepsClip.open(footstepsStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }

        // load T-rex-roar.wav
        try {
            File trexFile = new File("sounds/T-rex-roar.wav");
            AudioInputStream trexStream = AudioSystem.getAudioInputStream(trexFile);
            trexClip = AudioSystem.getClip();
            trexClip.open(trexStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
            trexClip = null;
        }
        
        // load switchClick.wav
        try {
            File switchClickFile = new File("sounds/switchClick.wav");
            AudioInputStream switchClickStream = AudioSystem.getAudioInputStream(switchClickFile);
            switchClickClip = AudioSystem.getClip();
            switchClickClip.open(switchClickStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
            switchClickClip = null;
        }
        
        
    }

    public void pauseMuseumSound() {
        if (museumSoundClip != null && museumSoundClip.isRunning()) {
            museumSoundClip.stop();
            isPlayingBack = false;
        }
    }

    public void resumeMuseumSound() {
        if (museumSoundClip != null && !museumSoundClip.isRunning()) {
            museumSoundClip.loop(Clip.LOOP_CONTINUOUSLY);
            isPlayingBack = true;
        }
    }
    
    
    public void toggleMuseumSound()
    {
    	if(museumSoundClip.isRunning()) {
    		pauseMuseumSound();
    	}
    	else {
    		playMuseumSound();
    	}
    	
    }
    
    
    public boolean isPlaying() {
        return isPlayingBack;
    }
    
    public void playBackGround() {
        if (museumSoundClip == null)
            return;
        if (isPlayingBack) {
            museumSoundClip.stop();
        }
        else {
            museumSoundClip.loop(10);
        }
        isPlayingBack = !isPlayingBack;
    }
    
    // play the museum sound effect on loop
    public void playMuseumSound() {
        if (museumSoundClip != null) {
            museumSoundClip.setFramePosition(0); // Rewind to the beginning
            museumSoundClip.loop(Clip.LOOP_CONTINUOUSLY); // Loop indefinitely
        }
    }

    // start looping the footsteps sound
    public void startFootsteps() {
        if (footstepsClip != null && !footstepsClip.isRunning()) {
            footstepsClip.setFramePosition(0); // Rewind to the beginning
            footstepsClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    // stop the footsteps sound
    public void stopFootsteps() {
        if (footstepsClip != null && footstepsClip.isRunning()) {
            footstepsClip.stop();
        }
    }
    
    // New method: Play the T-rex-roar sound for a few seconds
    public void playTrexRoar() {
        if (trexClip != null) {
            // If already playing, stop to restart the sound.
            if (trexClip.isRunning()) {
                trexClip.stop();
            }
            trexClip.setFramePosition(0); // Rewind to beginning
            trexClip.start();
            
            // Create a Swing Timer to stop playback after 3000 milliseconds (3 seconds)
            int delay = 6000; // delay in milliseconds
            new Timer(delay, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    trexClip.stop();
                    ((Timer)e.getSource()).stop(); // ensure the timer only runs once
                }
            }).start();
        }
    }
    
    
    // play the switch click sound
    public void playSwitchClick() {
        if (switchClickClip != null) {
            // If already playing, stop to restart the sound.
            if (switchClickClip.isRunning()) {
            	switchClickClip.stop();
            }
        	switchClickClip.setFramePosition(5980); // Rewind to beginning of used sound
        	switchClickClip.start();				// (desired beginning not at index 0)
        	
        	
            // Create a Swing Timer to stop playback after 250 milliseconds (0.25 seconds)
            int delay = 250; // delay in milliseconds
            new Timer(delay, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                	switchClickClip.stop();
                    ((Timer)e.getSource()).stop(); // ensure the timer only runs once
                }
            }).start();
        	
        	
        }
    }
    
    
}

