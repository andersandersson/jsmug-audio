package jsmug.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public final class PCMBufferUtils {
	static private final boolean bigEndian = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN);

	private PCMBufferUtils() {		
	}
	
	public static void convertFloatToByte(FloatBuffer src, ByteBuffer dst) {
		float next;
		
		while(src.hasRemaining()) {
			next = src.get();
			
			int val = (int)(next*32768);

			if (val > 32767) {
				val = 32767;
			}
			if (val < -32768) {
				val = -32768;
			}

			if (PCMBufferUtils.bigEndian) {
				dst.put((byte)(val >>> 8));
				dst.put((byte)(val));
			} else {
				dst.put((byte)(val));
				dst.put((byte)(val >>> 8));
			}
		}
	}
}
