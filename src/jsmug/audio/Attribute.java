package jsmug.audio;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Attribute {
	String value();
	String access() default "get";
	String type() default "double";
	String min() default "";
	String max() default "";
}
