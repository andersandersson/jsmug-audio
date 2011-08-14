import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.Locale;
import java.util.Scanner;

import jsmug.audio.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;

import org.newdawn.slick.util.Log;

public class AudioTest {
	public static void main(String[] args) {
		new AudioTest().execute();
	}

	public void execute() {
		try {
		    Display.setDisplayMode(new DisplayMode(800,600));
		    Display.create();
		} catch (LWJGLException e) {
		    e.printStackTrace();
		    System.exit(0);
		}

		Audio audio = new OpenALAudio();
		audio.init();
		
		
		WaveData waveFile = null;
		try {
			waveFile = WaveData.create(new FileInputStream("test.wav"));
			waveFile.dispose();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		PCMFloatChannel c1 = null;
		PCMFloatChannel c2 = null;
		PCMFloatChannel c3 = null;
		PCMFloatChannel c4 = null;
		PCMFloatChannel c5 = null;
		PCMFloatChannel c6 = null;
		PCMFloatChannel c7 = null;
		PCMFloatChannel c8 = null;

		try {
			c1 = new OggFloatChannel(new FileInputStream("music1_bass.ogg").getChannel());
			c2 = new OggFloatChannel(new FileInputStream("music1_bass2.ogg").getChannel());
			c3 = new OggFloatChannel(new FileInputStream("music1_chord.ogg").getChannel());
			c4 = new OggFloatChannel(new FileInputStream("music1_hh.ogg").getChannel());
			c5 = new OggFloatChannel(new FileInputStream("music1_kick.ogg").getChannel());
			c6 = new OggFloatChannel(new FileInputStream("music1_lead.ogg").getChannel());
			c7 = new OggFloatChannel(new FileInputStream("music1_lead2.ogg").getChannel());
			c8 = new OggFloatChannel(new FileInputStream("music1_lead3.ogg").getChannel());
//			c1 = new OggFloatChannel(new FileInputStream("test.ogg").getChannel());
//			c2 = new OggFloatChannel(new FileInputStream("test.ogg").getChannel());
//			c3 = new OggFloatChannel(new FileInputStream("test.ogg").getChannel());
//			c4 = new OggFloatChannel(new FileInputStream("test.ogg").getChannel());
//			c5 = new OggFloatChannel(new FileInputStream("test.ogg").getChannel());
//			c6 = new OggFloatChannel(new FileInputStream("test.ogg").getChannel());
//			c7 = new OggFloatChannel(new FileInputStream("test.ogg").getChannel());
//			c8 = new OggFloatChannel(new FileInputStream("test.ogg").getChannel());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PCMFloatChannelMixer mixer = new PCMFloatChannelMixer(c1.getSampleRate(), c2.getChannels());
		mixer.add(c1, 1.0);
		mixer.add(c2, 1.0);
		mixer.add(c3, 1.0);
		mixer.add(c4, 1.0);
		mixer.add(c5, 1.0);
		mixer.add(c6, 1.0);
		mixer.add(c7, 1.0);
		mixer.add(c8, 1.0);
//		mixer.fadeOut(0, 3.0);
//		mixer.fadeOut(1, 3.0);
//		mixer.fadeOut(2, 3.0);
//		mixer.fadeOut(3, 3.0);
//		mixer.fadeOut(5, 3.0);
//		mixer.fadeOut(6, 3.0);
//		mixer.fadeOut(7, 3.0);
		
//		try {
//			Locale.setDefault(Locale.US);
//			FileWriter writer = new FileWriter("test.data");
//			FloatBuffer buf = BufferUtils.createFloatBuffer(10000);
//			mixer.read(buf);
//			
//			for(int i=0; i<10000; i++) { 
//				writer.write(String.format("%.3f\n", buf.get(i)));
//			}
//			
//			writer.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		//Sound sound = audio.newSoundStream(mixer);
		Sound sound = audio.newSoundStream(mixer);
		sound.setLooping(true);
		sound.play();

		CueManager cueManager = new CueManager();
		CueSheet cueSheet = new CueSheet();
		//cueSheet.addCue(0.0, 5.0, new DoubleCue(sound, "pitch", 0.7, 1.1), new Functions.SinSquare(1.0, 0.5));
		cueSheet.addCue(0.0, 3.0, new Cue(sound, "pitch", 0.7, 1.1, new Functions.InterpolateDouble()));
		cueSheet.addCue(3.0, 6.0, new Cue(sound, "pitch", 1.1, 1.1, new Functions.InterpolateDouble()));
		cueSheet.addCue(6.0, 7.0, new Cue(sound, "pitch", 1.1, 0.1, new Functions.InterpolateDouble()));
		cueSheet.addCue(0.0, 7.0, new Cue(sound, "volume", 0.1, 1.0, new Functions.InterpolateDouble(), new Functions.SinSquare(1.0, 1.0)));
		
		//cueSheet.addCue(0.0, 3.0, new Cue<Double>("volume", 0.4, 1.0, new Functions.InterpolateDouble()));
		//cueSheet.addCue(0.0, 10.0, new Cue<Double>("volume", 0.4, 1.0, new Functions.InterpolateDouble()), new Functions.SinSquare(1.0, 2));
		cueSheet.setLoopMode(CueSheet.LoopMode.NORMAL);
		cueManager.addCueSheet(cueSheet);

		int fps = 60;
		while (!Display.isCloseRequested()) {
			cueManager.update(1.0/fps);
			audio.update(1.0/fps);
			Display.sync(fps);
		    Display.update();
		}
			
		audio.finish();
		Display.destroy();
	}
}
