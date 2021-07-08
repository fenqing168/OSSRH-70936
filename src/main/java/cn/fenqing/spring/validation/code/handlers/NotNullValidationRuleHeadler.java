package cn.fenqing.spring.validation.code.handlers;


import cn.fenqing.spring.validation.annotation.ActivateValidation;
import cn.fenqing.spring.validation.annotation.ValidationHandler;
import cn.fenqing.spring.validation.bean.ValidationResult;
import cn.fenqing.spring.validation.code.ValidationRuleHeadler;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * @author fenqing
 * @version 0.0.1
 */
@ValidationHandler
public class NotNullValidationRuleHeadler implements ValidationRuleHeadler<NotNull> {

    @Override
    public ValidationResult validation(NotNull annotation, ActivateValidation activateValidation, Object object) {
        if(Objects.isNull(object)){
            return ValidationResult.error(annotation.message());
        }
        return ValidationResult.ok();
    }

}
