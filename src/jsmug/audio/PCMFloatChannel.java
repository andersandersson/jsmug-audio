package jsmug.audio;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;

public interface PCMFloatChannel extends Channel, PCMInfo {
	public void close();

	public long position() throws ClosedChannelException;
	public PCMFloatChannel position(long position) throws ClosedChannelException;

	public long read(FloatBuffer dst) throws ClosedChannelException;
	public long read(FloatBuffer dst, long position) throws ClosedChannelException;
	public long read(FloatBuffer dsts[]) throws ClosedChannelException;
	public long read(FloatBuffer dsts[], int offset, int length) throws ClosedChannelException;
	
	public long size() throws ClosedChannelException;
}
