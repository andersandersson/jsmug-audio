package jsmug.audio;

public interface Audio {
	public boolean init();
	public void finish();
	public String getDefaultDeviceName();
	public String[] getDeviceNames();
	public Sound newSound(String filename);
	public Sound newSoundStream(String filename);
	public void update(double deltaTime);
}
