package jsmug.audio;

import java.nio.FloatBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.BufferUtils;

public class PCMFloatChannelMixer implements PCMFloatChannel {
	private List<PCMFloatChannel> inputChannels = null;
	private int sampleRate = 0;
	private int channels = 0;
	private float volume;
	
	private long sampleCount = 0;
	
	private boolean endOfStream = false;
	
	private FloatBuffer mixerBuffer;

	public PCMFloatChannelMixer(int sampleRate, int channels) {
		this.inputChannels = new LinkedList<PCMFloatChannel>();
		this.sampleRate = sampleRate;
		this.channels = channels;
		this.mixerBuffer = BufferUtils.createFloatBuffer(4096);
	}
	
	public boolean add(PCMFloatChannel channel) {
		return this.inputChannels.add(channel);
	}
	
	public boolean remove(PCMFloatChannel channel) {
		return this.inputChannels.remove(channel);
	}
	
	@Override
	public boolean isOpen() {
		for(PCMFloatChannel channel : this.inputChannels) {
			if(channel.isOpen()) {
				return true;
			}
		}
		
		return false;
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
		return 16;
	}

	@Override
	public void close() {
		for(PCMFloatChannel channel : this.inputChannels) {
			if(channel.isOpen()) {
				channel.close();
			}
		}
		
		this.inputChannels.clear();
	}

	@Override
	public long position() throws ClosedChannelException {
		return this.sampleCount;
	}

	@Override
	public PCMFloatChannel position(long position) throws ClosedChannelException {
		for(PCMFloatChannel channel : this.inputChannels) {
			if(channel.isOpen()) {
				channel.position(position);
			}
		}

		return this;
	}

	@Override
	public long read(FloatBuffer dst) throws ClosedChannelException {
		return this.read(new FloatBuffer[]{dst}, 0, 1);
	}

	@Override
	public long read(FloatBuffer dst, long position) throws ClosedChannelException {
		long oldPosition = this.sampleCount;
		
		this.position(this.sampleCount);
		
		long length = this.read(dst);
		
		this.position(oldPosition);
		
		return length;
	}

	@Override
	public long read(FloatBuffer[] dsts) throws ClosedChannelException {
		return this.read(dsts, 0, dsts.length);
	}

	
	@Override
	public long read(FloatBuffer[] dsts, int offset, int length) throws ClosedChannelException {
		int currentDst = offset;
		long sampleCountStart = this.sampleCount;
		
		if(this.inputChannels.size() == 0) {
			return 0;
		}
		
		while(!this.endOfStream && currentDst < length+offset) {
			this.sampleCount += this.inputChannels.get(0).read(dsts[currentDst]);
			
			if(dsts[currentDst].capacity() > this.mixerBuffer.capacity()) {
				this.mixerBuffer = BufferUtils.createFloatBuffer(dsts[currentDst].capacity());
			}
			
			this.mixerBuffer.limit(dsts[currentDst].capacity());
			
			for(int i=1; i<this.inputChannels.size(); i++) {
				this.mixerBuffer.position(0);
				this.inputChannels.get(i).read(this.mixerBuffer);
				PCMUtils.mixBuffers(this.mixerBuffer, dsts[currentDst]);
			}
			
			currentDst++;
		}
		
		return this.sampleCount - sampleCountStart;
	}

	
	@Override
	public long size() throws ClosedChannelException {
		long size = 0;
		
		for(PCMFloatChannel channel : this.inputChannels) {
			if(channel.isOpen()) {
				long cSize = channel.size();
				
				if(size < cSize) {
					size = cSize;
				}
			}
		}

		return size;
	}

	@Override
	public float getVolume() {
		return this.volume;
	}

	@Override
	public void setVolume(float volume) {
		this.volume = volume;
	}
}
