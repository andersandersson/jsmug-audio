package jsmug.audio;

public interface Adjustable {
	@CueTarget("volume")
	public void setVolume(double volume);
	public double getVolume();

	@CueTarget("pitch")
	public void setPitch(double pitch);	
	public double getPitch();
}
