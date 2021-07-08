package cn.fenqing.spring.validation.annotation;

import java.lang.annotation.*;

/**
 * @author fenqing
 * @version 0.0.1
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ActivateValidation {

    /**
     * 分组
     * @return 分组
     */
    Class<?> group();
}
