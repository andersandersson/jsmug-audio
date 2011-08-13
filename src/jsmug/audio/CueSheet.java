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
	
	private Object target = null;
	
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
			if(this.timeDirection < 0) {
				// If we pass this entrys start, make sure it affects the result 
				if(entry.start > this.currentTime) {
					if(entry.timeTransform != null) {
						entry.cue.eval(entry.timeTransform.eval(0.0));
					} else {
						entry.cue.eval(0.0);
					}
				}
			} else {
				// If we pass this entrys end, make sure it affects the result 
				if(entry.end < this.currentTime) {
					if(entry.timeTransform != null) {
						entry.cue.eval(entry.timeTransform.eval(1.0));
					} else {
						entry.cue.eval(1.0);
					}
				}
			}
			
			// If we are in between this entrys times
			if(entry.start <= this.currentTime && this.currentTime <= entry.end) {
				if(entry.end != entry.start) {
					percentage = (this.currentTime - entry.start) / (entry.end - entry.start);
				} else {
					percentage = 1.0;
				}
				
				if(entry.timeTransform != null) {
					entry.cue.eval(entry.timeTransform.eval(percentage));
				} else {
					entry.cue.eval(percentage);
				}
				
				active = true;
			}
		}
		
		return !active;
	}
	
	public boolean update(double deltatime) {
		return this.seek(this.currentTime + deltatime*this.timeDirection);
	}
	
 	public<T> void addCue(double start, double end, Cue<T> cue, Function<Double,Double> timeTransform) {
 		if(end > this.lastEntryEnd) {
 			this.lastEntryEnd = end;
 		}
 		
 		cue.setTarget(this.target);
 		
 		this.cues.add(new CueSheetEntry(cue, start, end, timeTransform));
 	}
 	
 	public<T> void addCue(double start, double end, Cue<T> cue) {
 		if(end > this.lastEntryEnd) {
 			this.lastEntryEnd = end;
 		}
 		
 		cue.setTarget(this.target);
 		
 		this.cues.add(new CueSheetEntry(cue, start, end, null));
 	}
 	
 	public void setLoopMode(LoopMode mode) {
 		this.loopMode = mode;
 	}

 	public LoopMode getLoopMode() {
 		return this.loopMode;
 	}
 	
 	public void setTarget(Object target) {
 		for(CueSheetEntry entry : this.cues) {
 			entry.cue.setTarget(target);
 		}
 	}
}
