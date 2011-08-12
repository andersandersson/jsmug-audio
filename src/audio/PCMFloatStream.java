package audio;

import java.nio.FloatBuffer;

public interface PCMFloatStream extends PCMInfo {
	public int read(FloatBuffer dst);
	public void reset();
	public void close();
	public boolean seek(int position);
	public boolean eos();
}
