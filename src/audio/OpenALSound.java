package audio;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

class OpenALSound implements Sound {
	boolean isPlaying = false;
	boolean isPaused = false;
	boolean isStopped = true;
	boolean isStream = true;
	boolean isLooping = false;
	boolean eos = true;
	int source = 0;
	int stopSource = 0;
	int buffer = 0;
	
	float volume = 1.0f;

	int sampleRate = 0;
	int bits = 0;
	int channels = 0;
	
	PCMFloatStream input;
	FloatBuffer data = null;
	
	public OpenALSound(PCMFloatStream input) {
		this.input = input;
		this.eos = false;
		this.isStream = true;
		
		this.sampleRate = input.getSampleRate();
		this.bits = input.getBits();
		this.channels = input.getChannels();

	}
	
	public OpenALSound(PCMFloatStream input, int bufferSize) {
		this.input = input;
		this.eos = false;
		this.isStream = false;
		
		this.sampleRate = input.getSampleRate();
		this.bits = input.getBits();
		this.channels = input.getChannels();

		FloatBuffer tmp = BufferUtils.createFloatBuffer(bufferSize);
		FloatBuffer tmp2;
		
		// Allocate tmp buffer for about 10s of data
		while(input.read(tmp) > 0) {
			tmp2 = BufferUtils.createFloatBuffer(tmp.limit()+bufferSize);
			tmp.flip();
			tmp2.put(tmp);
			tmp = tmp2;
		}
		
		// Force stream mode if we don't fit into one buffer
		if(tmp.position() >= bufferSize) {
			this.isStream = true;
		}

		this.input = null;
		this.data = tmp;
		this.data.flip();
	}

	public int read(FloatBuffer dst) {
		int length = 0;
		
		if(this.data != null) {
			if(!this.data.hasRemaining()) {
				this.eos = true;
				return -1;
			}
			
			while(dst.hasRemaining() && this.data.hasRemaining()) {
				dst.put(this.data.get());
				length += 1;
			}
			
			if(!this.data.hasRemaining()) {
				this.eos = true;
			}
		} else {
			length = this.input.read(dst);
		}
		
		return length;
	}

	public int getStopSource() {
		return this.stopSource;
	}

	public void setStopSource(int stopSource) {
		this.stopSource = stopSource;
	}

	public int getSource() {
		return this.source;
	}

	public void setSource(int source) {
		this.source = source;
	}
	
	public int getBuffer() {
		return this.buffer;
	}

	public void setBuffer(int buffer) {
		this.buffer = buffer;
	}
	
	@Override
	public void play() {
		if(this.isPlaying || this.isPaused) {
			this.stop();
		}
		
		this.isPlaying = true;
		this.isPaused = false;
		this.isStopped = false;
	}

	@Override
	public void pause() {
		this.isPlaying = false;
		this.isPaused = true;
	}

	@Override
	public void resume() {
		this.isPlaying = true;
		this.isPaused = false;
	}

	@Override
	public void stop() {
		this.isPlaying = false;
		this.isPaused = false;
		this.isStopped = true;
		
		if(this.source != 0) {
			this.stopSource = this.source;
			this.source = 0;
		}
		
		this.reset();
	}

	@Override
	public int getSampleRate() {
		return this.sampleRate;
	}

	@Override
	public int getChannels() {
		return this.channels;
	}

	@Override
	public int getBits() {
		return this.bits;
	}
	
	public void reset() {
		if(this.input != null) {
			this.input.reset();
		} 
		
		if(this.data != null) {
			this.data.position(0);
		}

		this.eos = false;
	}

	@Override
	public void setLooping(boolean looping) {
		this.isLooping = looping;
	}

	@Override
	public void setVolume(float volume) {
		this.volume = volume;
	}

	@Override
	public boolean isStream() {
		return this.isStream;
	}

	@Override
	public boolean isLooping() {
		return this.isLooping;
	}

	public boolean isPlaying() {
		return this.isPlaying;
	}
	
	@Override
	public float getVolume() {
		return this.volume;
	}

	@Override
	public boolean isPaused() {
		return this.isPaused;
	}
	
	@Override
	public boolean isStopped() {
		return this.isStopped;
	}
	
	public boolean eos() {
		return this.eos;
	}

	@Override
	public boolean seek(int position) {
		if(this.data != null) {
			if(this.data.limit() >= position) {
				this.data.position(position);
				return true;
			} else {
				return false;
			}
		}

		if(this.input != null) {
			return this.input.seek(position);
		}
		
		return false;
	}
}
