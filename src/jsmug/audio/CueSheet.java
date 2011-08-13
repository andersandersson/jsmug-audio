package jsmug.audio;

import java.util.LinkedList;
import java.util.List;

public class CueSheet {
	public enum LoopMode {
		PINGPONG, NORMAL, NONE;
	}
	
	private class CueSheetEntry {
		public double start;
		public double end;
		public Cue cue;
		public Function<Double,Double> timeTransform = null;
		
		public CueSheetEntry(Cue cue, double start, double end, Function<Double,Double> timeTransform) {
			this.start = start;
			this.end = end;
			this.cue = cue;
			this.timeTransform = timeTransform;
		}
	}
	
	private double currentTime = 0.0;
	private double timeDirection = 1.0;
	private double lastEntryEnd = 0.0;
	
	private LoopMode loopMode = LoopMode.NONE;
	
	private List<CueSheetEntry> cues;
	
	public CueSheet() {
		this.cues = new LinkedList<CueSheetEntry>();
	}
	
	public boolean seek(double time) {
		this.currentTime = time;
		boolean active = false;
		double percentage = 0.0;

		if(this.loopMode != LoopMode.NONE) {
			active = true;
		}
		
		if(this.loopMode == LoopMode.NORMAL && this.currentTime > lastEntryEnd) {
			this.currentTime = 0.0;
		}

		if(this.loopMode == LoopMode.PINGPONG && this.currentTime > lastEntryEnd) {
			this.currentTime = lastEntryEnd;
			this.timeDirection = -1;
		}
		
		if(this.loopMode == LoopMode.PINGPONG && this.currentTime < 0) {
			this.currentTime = 0;
			this.timeDirection = 1;
		}
		
		for(CueSheetEntry entry : this.cues) {
			if(entry.start <= this.currentTime && this.currentTime <= entry.end) {
				active = true;
			} 
			
			if(entry.start != entry.end) {
				if(this.currentTime <= entry.start) {
					percentage = 0.0;
				} 
				else if(this.currentTime >= entry.end) {
					percentage = 1.0;
				} else {
					percentage = (this.currentTime - entry.start) / (entry.end - entry.start); 
				}
			} else {
				percentage = 1.0;
			}
			
			if(entry.timeTransform != null) {
				percentage = entry.timeTransform.eval(percentage); 
			}
			
			entry.cue.update(percentage);
		}
		
		return !active;
	}
	
	public boolean update(double deltatime) {
		return this.seek(this.currentTime + deltatime*this.timeDirection);
	}
	
 	public void addCue(double start, double end, Cue cue) {
 		this.addCue(start, end, cue, null);
	}
 	
 	public void addCue(double start, double end, Cue cue, Function<Double,Double> timeTransform) {
 		if(end > this.lastEntryEnd) {
 			this.lastEntryEnd = end;
 		}
 		
 		this.cues.add(new CueSheetEntry(cue, start, end, timeTransform));
 	}
 	
 	public void setLoopMode(LoopMode mode) {
 		this.loopMode = mode;
 	}

 	public LoopMode getLoopMode() {
 		return this.loopMode;
 	}
}
