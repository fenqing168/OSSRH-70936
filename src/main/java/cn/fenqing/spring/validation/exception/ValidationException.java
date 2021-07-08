package cn.fenqing.spring.validation.exception;

import cn.fenqing.spring.validation.bean.ValidationErrorInfo;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author fenqing
 * @version 0.0.1
 */
@Getter
public class ValidationException extends RuntimeException{

    public ValidationException(String message, Method method, List<ValidationErrorInfo> validationErrorInfos) {
        super(message);
        this.method = method;
        this.validationErrorInfos = validationErrorInfos;
    }

    private final Method method;

    private final List<ValidationErrorInfo> validationErrorInfos;

}
