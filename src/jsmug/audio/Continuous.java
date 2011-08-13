package jsmug.audio;

public interface Continuous<T> {
	public T eval(T from, T to, double percentage);
}
