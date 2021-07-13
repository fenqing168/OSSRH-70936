package cn.fenqing.spring.validation.code.handlers;

import cn.fenqing.spring.validation.annotation.ActivateValidation;
import cn.fenqing.spring.validation.bean.ValidationResult;
import cn.fenqing.spring.validation.code.ValidationRuleHeadler;
import cn.hutool.core.math.MathUtil;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;

/**
 * @author fenqing
 * @version 0.0.1
 * @date 2021/7/13 14:23
 * @description
 */
public class MaxValidationRuleHeadler  implements ValidationRuleHeadler<Max> {
    @Override
    public ValidationResult validation(Max annotation, ActivateValidation activateValidation, Object object) {
        if(object instanceof Number){
            if(((Number) object).doubleValue() > annotation.value()){
                return ValidationResult.error(annotation.message());
            }else{
                return ValidationResult.ok();
            }
        }else{
            return ValidationResult.error(annotation.message());
        }
    }
}
