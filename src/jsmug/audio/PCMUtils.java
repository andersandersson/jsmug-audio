package jsmug.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.ClosedChannelException;

public final class PCMUtils {
	static private final boolean bigEndian = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN);

	private PCMUtils() {		
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

			if (PCMUtils.bigEndian) {
				dst.put((byte)(val >>> 8));
				dst.put((byte)(val));
			} else {
				dst.put((byte)(val));
				dst.put((byte)(val >>> 8));
			}
		}
	}

	// Mix two buffers and divide output with the sum of the inputs RMS (root mean square = sqrt(x1*x1+...+xn*xn) )
	public static void mixBuffers(FloatBuffer src, FloatBuffer dst) throws ClosedChannelException {
		int limit = src.limit();
		double RMS = 0.0, srcValue, dstValue;
		
		for(int i=0; i<limit; i++) {
			srcValue = src.get(i);
			dstValue = dst.get(i);
			
			RMS += (srcValue+dstValue)*(srcValue+dstValue);
			
			dstValue += srcValue;
			
			dst.put(i, (float) dstValue);
		}
		
		RMS = Math.sqrt(RMS/((double)limit));

		if(RMS > 1.0) {
			for(int i=0; i<limit; i++) {
				dst.put(i, dst.get(i)/((float)RMS) );
			}
		}
	}
}
