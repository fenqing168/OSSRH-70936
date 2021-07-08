package cn.fenqing.spring.validation.code;


import cn.fenqing.spring.validation.annotation.ActivateValidation;
import cn.fenqing.spring.validation.bean.ValidationResult;

import java.lang.annotation.Annotation;

/**
 * @author fenqing
 * @version 0.0.1
 */
public interface ValidationRuleHeadler<T extends Annotation> {

    /**
     * 校验方法
     * @param annotation 注解类型
     * @param activateValidation 包含group
     * @param object 校验对象
     * @return 结果
     */
    ValidationResult validation(T annotation, ActivateValidation activateValidation, Object object);

}
