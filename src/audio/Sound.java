package audio;

public interface Sound extends PCMInfo {
	public void play();
	public void pause();
	public void resume();
	public void stop();
	
	public boolean isStream();
	public boolean isLooping();
	public boolean isPlaying();
	public boolean isPaused();
	public boolean isStopped();
	
	public float getVolume();
	public boolean seek(int position);
	
	public void setLooping(boolean looping);
	public void setVolume(float volume);
}