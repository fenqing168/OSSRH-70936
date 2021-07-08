package cn.fenqing.spring.validation.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author fenqing
 * @version 0.0.1
 */
@Getter
@Setter
public class ValidationErrorInfo {

    private int index;

    private String parameterName;

    private List<String> message;

}
