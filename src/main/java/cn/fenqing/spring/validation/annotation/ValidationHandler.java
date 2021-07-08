package cn.fenqing.spring.validation.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author fenqing
 * @version 0.0.1
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Component
public @interface ValidationHandler {

    int order() default 0;

}
