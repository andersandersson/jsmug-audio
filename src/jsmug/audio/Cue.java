package jsmug.audio;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Cue<T> {
	protected Object target = null;
	protected Method method = null;
	protected String name = "";
	protected T startValue = null;
	protected T endValue = null;
	protected Continuous<T> interpolator = null;
	
	private Method findAnnotatedMethod(Class<?> parent) {
		Method method = null;
		
		if(parent == null) {
			return null;
		}
		
		for(Method m : parent.getMethods()) {
			if(m.isAnnotationPresent(Attribute.class)) {
				if(m.getAnnotation(Attribute.class).value().equals(this.name) &&
				   m.getAnnotation(Attribute.class).access().equals("set")) {
					return m;
				}
			}
		}

		for(Class<?> i : parent.getInterfaces()) {
			method = this.findAnnotatedMethod(i);
			
			if(method != null) {
				return method;
			}
		}
		
		return this.findAnnotatedMethod(parent.getSuperclass());
	}
	
	public Cue(String name, T startValue, T endValue, Continuous<T> interpolator) {
		this.name = name;
		this.startValue = startValue;
		this.endValue = endValue;
		this.interpolator = interpolator;
	}
	
	public void setTarget(Object target) {
		this.target = target;
		
		if(target != null) {
			this.method = this.findAnnotatedMethod(target.getClass());
		}
	}
	
	public Object getTarget() {
		return this.target;
	}

	public void eval(double percentage) {
		if(this.method == null) {
			return;
		}
		
		try {
			this.method.invoke(this.target, this.interpolator.eval(this.startValue, this.endValue, percentage));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}
