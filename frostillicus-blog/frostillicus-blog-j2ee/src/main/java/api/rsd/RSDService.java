package api.rsd;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation should be applied to any class that should contribute to
 * the services list in the RSD manifest.
 * 
 * @author Jesse Gallagher
 * @since 2.3.0
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface RSDService {
	String name();
	boolean preferred();
	String basePath();
	
	boolean microblog() default false;
	boolean blog() default true;
}
