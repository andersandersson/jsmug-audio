package jsmug.audio;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALCcontext;
import org.lwjgl.openal.ALCdevice;

public class OpenALAudio implements Audio {
	private ALCcontext context;
	private ALCdevice device;
	
	private FloatBuffer listenerPosition;
	private FloatBuffer listenerVelocity;
	private FloatBuffer listenerOrientation;
	
	private List<OpenALSound> sounds;
	
	private IntBuffer sources;
	
	private int soundBufferSize = 409600;
	private FloatBuffer soundFloatBuffer;
	private ByteBuffer soundByteBuffer;
	
	public OpenALAudio() {
		this.sounds = new LinkedList<OpenALSound>();
		this.listenerPosition = BufferUtils.createFloatBuffer(3);
		this.listenerVelocity = BufferUtils.createFloatBuffer(3);
		this.listenerOrientation = BufferUtils.createFloatBuffer(6);
		this.soundByteBuffer = BufferUtils.createByteBuffer(this.soundBufferSize*2);
		this.soundFloatBuffer = BufferUtils.createFloatBuffer(this.soundBufferSize);
		this.sources = BufferUtils.createIntBuffer(64);
	}
	
	public boolean init() {
		try {
			AL.create();
			this.context = ALC10.alcGetCurrentContext();
			this.device = ALC10.alcGetContextsDevice(this.context);
			this.resetListener();

			AL10.alGenSources(this.sources);
			
			if(AL10.alGetError() != 0) {
				return false;
			}
			
		} catch (LWJGLException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	public void finish() {
		AL.destroy();
	}
	
	public String getDefaultDeviceName() {
		if(ALC10.alcIsExtensionPresent(null, "ALC_ENUMERATION_EXT")) {
			return ALC10.alcGetString(null, ALC10.ALC_DEFAULT_DEVICE_SPECIFIER);
		}
		
		return null;
	}
	
	public String[] getDeviceNames() {
		if(ALC10.alcIsExtensionPresent(null, "ALC_ENUMERATION_EXT")) {
			String devicesString = ALC10.alcGetString(null, ALC11.ALC_ALL_DEVICES_SPECIFIER);
			return devicesString.split("\0\0");
		}
		
		return null;
	}
	
	public Sound newSound(String filename) {
		try {
			FileInputStream input = new FileInputStream(filename);
			OggFloatChannel channel  = new OggFloatChannel(input.getChannel());
			return this.newSound(channel);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Sound newSound(PCMFloatChannel channel) {
		OpenALSound sound = new OpenALSound(channel, this.soundBufferSize);
		this.sounds.add(sound);
		return sound;
	}
	
	
	public Sound newSoundStream(String filename) {
		try {
			FileInputStream input = new FileInputStream(filename);
			OggFloatChannel channel = new OggFloatChannel(input.getChannel());
			return this.newSoundStream(channel);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public Sound newSoundStream(PCMFloatChannel channel) {
		OpenALSound sound = new OpenALSound(channel);
		try {
			System.out.println("SIZE: "+channel.size());
		} catch (ClosedChannelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.sounds.add(sound);
		return sound;
	}
	
	
	public void update(double deltaTime) {
		for(OpenALSound s : this.sounds) {
			this.updateSound(s, deltaTime);
		}
	}
	

	private void resetListener() {
		this.listenerPosition.put(new float[]{0.0f, 0.0f, 0.0f}).rewind();
		this.listenerVelocity.put(new float[]{0.0f, 0.0f, 0.0f}).rewind();
		this.listenerOrientation.put(new float[]{0.0f, 0.0f, -1.0f,  0.0f, 1.0f, 0.0f}).rewind();
		
		AL10.alListener(AL10.AL_POSITION, this.listenerPosition);
		AL10.alListener(AL10.AL_VELOCITY, this.listenerVelocity);
		AL10.alListener(AL10.AL_ORIENTATION, this.listenerOrientation);
	}
	
	private int findFreeSource() {
		this.sources.position(0);
		while(this.sources.hasRemaining()) {
			int id = this.sources.get();
			int state = AL10.alGetSourcei(id, AL10.AL_SOURCE_STATE);
			
			if (state != AL10.AL_PLAYING && state != AL10.AL_PAUSED) {
				AL10.alSourceStop(id);
				AL10.alSourcei(id, AL10.AL_BUFFER, 0);
                return id;
			}
		}

		return -1;
	}
	

	private int createBuffer() {
		int id = AL10.alGenBuffers();

		return id;
	}
	
	
	private int fillBuffer(int buffer, OpenALSound sound) {
		this.soundFloatBuffer.clear();

		// If we reach end of stream and is looping, reset stream and play on
		if(sound.read(this.soundFloatBuffer) == -1) {
			if(sound.isLooping()) {
				sound.reset();
				this.soundFloatBuffer.clear();
				sound.read(this.soundFloatBuffer);
			} else {
				// No more data to fill buffer with
				return -1;
			}
		}
		this.soundFloatBuffer.flip();
		this.soundByteBuffer.clear();				
		PCMUtils.convertFloatToByte(this.soundFloatBuffer, this.soundByteBuffer);		
		this.soundByteBuffer.flip();
		
		int format = 0;
		if(sound.getChannels() == 1 && sound.getBits() == 8) {
			format = AL10.AL_FORMAT_MONO8;
		} else if(sound.getChannels() == 1 && sound.getBits() == 16) {
			format = AL10.AL_FORMAT_MONO16;
		} else if(sound.getChannels() == 2 && sound.getBits() == 8) {
			format = AL10.AL_FORMAT_STEREO8;
		} else if(sound.getChannels() == 2 && sound.getBits() == 16) {
			format = AL10.AL_FORMAT_STEREO16;
		} else {
			format = -1;
		}

		int length = this.soundByteBuffer.limit();
		
		AL10.alBufferData(buffer, format, this.soundByteBuffer, sound.getSampleRate());
		
		return length;
	}
	
	
	private void playSound(OpenALSound sound) {
		int source = sound.getSource();
		if(source != 0) {
			AL10.alSourcef(source, AL10.AL_PITCH, 1.0f);
			AL10.alSourcef(source, AL10.AL_GAIN, sound.getVolume());
			AL10.alSource(source, AL10.AL_POSITION, this.listenerPosition);
			AL10.alSource(source, AL10.AL_VELOCITY, this.listenerPosition);
			AL10.alSourcePlay(source);
		}
	}
	
	private void stopSource(int source) {
		if(source != 0) {
			AL10.alSourceStop(source);
			AL10.alSourcei(source, AL10.AL_BUFFER, 0);
		}
	}
	
	private void stopSound(OpenALSound sound) {
		int source = sound.getSource();
		this.stopSource(source);
		sound.setSource(0);
	}
	
	private void updateSound(OpenALSound sound, double deltaTime) {
		int source = sound.getSource();
		
		// The sound is not playing
		if(sound.isStopped() && sound.getSource() > 0) {
			this.stopSound(sound);
			return;
		}
		
		// If the sound has a stop source request
		if(sound.getStopSource() != 0) {
			this.stopSource(sound.getStopSource());
			sound.setStopSource(0);
		}
		
		// If the sound has no source yet, generate it
		if(source == 0) {
			source = this.findFreeSource();
			sound.setSource(source);
		}
		
		if(sound.isPlaying() && sound.isStream()) {
			int state = AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE);
			int queued = AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED);
			int processed = AL10.alGetSourcei(source, AL10.AL_BUFFERS_PROCESSED);
			
			// If the sound has stopped, the stream reached its end and it is
			// not looping, stop it and reset stream/data
			if(state == AL10.AL_STOPPED && sound.eos() && !sound.isLooping()) {
				this.stopSound(sound);
				sound.reset();
				return;
			}
			
			// Make sure we have 3 buffers on a stream
			if(queued < 20) {
				for(int i=0; i<3-queued; i++) {
					int buffer = this.createBuffer();
					int length = this.fillBuffer(buffer, sound);
					
					// Only queue if buffer got positive length
					if(length > 0) {
						AL10.alSourceQueueBuffers(source, buffer);
					}
				}
			}
			
			// Refill processed buffers
			if(processed > 0) {
				int buffer = AL10.alSourceUnqueueBuffers(source);
				int length = this.fillBuffer(buffer, sound);

				// Only queue if buffer got positive length
				if(length > 0) {
					AL10.alSourceQueueBuffers(source, buffer);
				}
			}
			
			// If the sound is not playing, play it
			if(state != AL10.AL_PLAYING) {
				this.playSound(sound);
			}
		} 
		else if(sound.isPlaying() && !sound.isStream()) {
			int state = AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE);
			
			// If the buffer is out and stream is at end, stop sound
			if(state == AL10.AL_STOPPED && sound.eos()) {
				this.stopSound(sound);
				return;
			}

			// Set sound to looping if requested
			if(sound.isLooping()) {
				AL10.alSourcei(source, AL10.AL_LOOPING, AL10.AL_TRUE);
			} else {
				AL10.alSourcei(source, AL10.AL_LOOPING, AL10.AL_FALSE);
			}
			
			// If the sound is not playing, give it a buffer and play it
			if(state != AL10.AL_PLAYING) {
				int buffer = sound.getBuffer();
				
				// If the sound has no buffer, create it
				if(buffer == 0) {
					buffer = this.createBuffer();
					sound.setBuffer(buffer);
					int length = this.fillBuffer(buffer, sound);
				}
				
				// Connect buffer to source
				AL10.alSourcei(source, AL10.AL_BUFFER, buffer);			
				this.playSound(sound);
			}			
		}
		else if(sound.isPaused()) {
			int state = AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE);
			
			// If the sound is playing, pause it
			if(state == AL10.AL_PLAYING) {
				AL10.alSourcei(source, AL10.AL_SOURCE_STATE, AL10.AL_PAUSED);
			}
		}
	}
}
