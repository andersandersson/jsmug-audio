package jsmug.audio;

import java.lang.reflect.Method;

public abstract class Cue {
	public abstract void update(double percentage);
	protected Object target = null;
	protected Method method = null;
	
	private Method findAnnotatedMethod(Class<?> parent, String value) {
		Method method = null;
		
		if(parent == null) {
			return null;
		}
		
		for(Method m : parent.getMethods()) {
			if(m.isAnnotationPresent(CueTarget.class)) {
				if(m.getAnnotation(CueTarget.class).value().equals(value)) {
					return m;
				}
			}
		}

		for(Class<?> i : parent.getInterfaces()) {
			method = this.findAnnotatedMethod(i, value);
			
			if(method != null) {
				return method;
			}
		}
		
		return this.findAnnotatedMethod(parent.getSuperclass(), value);
	}
	
	public Cue(Object target, String value) {
		this.target = target;
		this.method = this.findAnnotatedMethod(target.getClass(), value);
	}	
}
