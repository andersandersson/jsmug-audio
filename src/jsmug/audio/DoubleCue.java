package jsmug.audio;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.annotation.Annotation;;

public class DoubleCue extends Cue {
	private double fromValue = 0.0;
	private double toValue = 0.0;

	public DoubleCue(Object target, String value, double fromValue, double toValue) {
		super(target, value);
		
		this.fromValue = fromValue;
		this.toValue = toValue;
	}
	
	@Override
	public void update(double percentage) {
		try {
			this.method.invoke(this.target, (Double)(percentage*(this.toValue - this.fromValue) + this.fromValue));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}
