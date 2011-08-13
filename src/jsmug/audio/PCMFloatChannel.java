package jsmug.audio;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;

public interface PCMFloatChannel extends Channel, PCMInfo {
	public void close();

	public long position();
	public PCMFloatChannel position(long position);

	public long read(FloatBuffer dst);
	public long read(FloatBuffer dst, long position);
	public long read(FloatBuffer dsts[]);
	public long read(FloatBuffer dsts[], int offset, int length);
	
	public long size();
}
