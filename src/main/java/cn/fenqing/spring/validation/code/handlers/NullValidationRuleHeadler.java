package cn.fenqing.spring.validation.code.handlers;


import cn.fenqing.spring.validation.annotation.ActivateValidation;
import cn.fenqing.spring.validation.annotation.ValidationHandler;
import cn.fenqing.spring.validation.bean.ValidationResult;
import cn.fenqing.spring.validation.code.ValidationRuleHeadler;

import javax.validation.constraints.Null;
import java.util.Objects;

/**
 * @author fenqing
 * @version 0.0.1
 */
@ValidationHandler
public class NullValidationRuleHeadler implements ValidationRuleHeadler<Null> {

    @Override
    public ValidationResult validation(Null annotation, ActivateValidation activateValidation, Object object) {
        if(Objects.nonNull(object)){
            return ValidationResult.error(annotation.message());
        }
        return ValidationResult.ok();
    }

}
