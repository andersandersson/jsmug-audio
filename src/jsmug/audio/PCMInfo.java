package jsmug.audio;

public interface PCMInfo {
	public int getSampleRate();
	public int getChannels();
	public int getBits();
	
	public float getVolume();
	public void setVolume(float volume);
}
