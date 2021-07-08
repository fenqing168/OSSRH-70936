package cn.fenqing.spring.validation.code.handlers;

import cn.fenqing.spring.validation.annotation.ActivateValidation;
import cn.fenqing.spring.validation.annotation.ValidationHandler;
import cn.fenqing.spring.validation.bean.ValidationResult;
import cn.fenqing.spring.validation.code.ValidationRuleHeadler;
import cn.hutool.core.util.ObjectUtil;

import javax.validation.constraints.NotEmpty;

/**
 * @author fenqing
 * @version 0.0.1
 */
@ValidationHandler
public class NotEmptyValidationRuleHeadler implements ValidationRuleHeadler<NotEmpty> {

    @Override
    public ValidationResult validation(NotEmpty annotation, ActivateValidation activateValidation, Object object) {
        if(ObjectUtil.isEmpty(object)){
            return ValidationResult.error(annotation.message());
        }
        return ValidationResult.ok();
    }

}
