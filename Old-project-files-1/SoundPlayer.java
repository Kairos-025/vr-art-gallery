
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundPlayer {

    private Clip museumSoundClip; // clip for MuseumSoundEffect.wav
    private Clip footstepsClip;   // clip for footsteps.wav

    public SoundPlayer() {
        // load MuseumSoundEffect.wav
        try {
            File museumSoundFile = new File("sounds/MuseumSoundEffect.wav");
            AudioInputStream museumSoundStream = AudioSystem.getAudioInputStream(museumSoundFile);
            museumSoundClip = AudioSystem.getClip();
            museumSoundClip.open(museumSoundStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
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
}