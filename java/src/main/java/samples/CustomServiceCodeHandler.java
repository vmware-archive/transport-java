package samples;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomServiceCodeHandler {
    RunStage stage() default RunStage.After;
    String methodName() default "";
}