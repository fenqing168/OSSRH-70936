package cn.fenqing.spring.validation.code;

import cn.fenqing.spring.validation.annotation.ActivateValidation;
import cn.fenqing.spring.validation.bean.ValidationErrorInfo;
import cn.fenqing.spring.validation.bean.ValidationResult;
import cn.fenqing.spring.validation.exception.ValidationException;
import cn.fenqing.spring.validation.utils.ReflectUtils;
import cn.hutool.core.collection.CollUtil;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * @author fenqing
 * @version 0.0.1
 */
public class ValidationCglibProxyHandler implements MethodInterceptor {

    private final Object oldObject;

    private final Map<Method, List<Annotation>[]> methodMap;

    private final Map<Annotation, Class<Annotation>> annotationClassMap;

    private Map<Method, ActivateValidation> annotationAndMethods;

    private static Method VALIDATION_METHOD;

    static {
        try {
            VALIDATION_METHOD = ValidationRuleHeadler.class.getMethod("validation", Annotation.class, ActivateValidation.class,  Object.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public ValidationCglibProxyHandler(Object oldObject, Map<Method, List<Annotation>[]> methodMap, Map<Method, ActivateValidation> annotationAndMethods) {
        this.oldObject = oldObject;
        this.methodMap = methodMap;
        this.annotationAndMethods = annotationAndMethods;
        annotationClassMap = new HashMap<>();
        methodMap.forEach((k, v) -> {
            for (List<Annotation> annotations : v) {
                annotations.forEach(annotation -> annotationClassMap.put(annotation, ReflectUtils.getAnnotationType(annotation)));
            }
        });
    }


    private Class<Annotation> getClass(Annotation annotation){
        return annotationClassMap.get(annotation);
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Method thisMethod = ReflectUtils.findThisMethod(oldObject, method);
        List<Annotation>[] lists = methodMap.get(thisMethod);
        if(lists != null){
            List<ValidationErrorInfo> validationErrorInfos = new ArrayList<>();
            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < lists.length; i++) {
                ValidationErrorInfo validationErrorInfo = new ValidationErrorInfo();
                validationErrorInfo.setIndex(i);
                validationErrorInfo.setParameterName(parameters[i].getName());
                List<Annotation> list = lists[i];
                List<String> messages = new ArrayList<>();
                for (Annotation annotation : list) {

                    List<? extends ValidationRuleHeadler<? extends Annotation>> rule = ValidationRulePool.getRule(getClass(annotation));
                    for (ValidationRuleHeadler<? extends Annotation> validationRuleHeadler : rule) {
                        ActivateValidation activateValidation = annotationAndMethods.get(thisMethod);
                        Class<?> group = activateValidation.group();
                        Class[] jsr303Groups = ReflectUtils.getJsr303Groups(annotation);
                        if(Arrays.asList(jsr303Groups).contains(group)){
                            ValidationResult validationResult = (ValidationResult) VALIDATION_METHOD
                                    .invoke(validationRuleHeadler, annotation, annotationAndMethods.get(thisMethod), objects[i]);
                            if (!validationResult.isOk()){
                                messages.add(validationResult.getMessage());
                            }
                        }
                    }
                }
                if(CollUtil.isNotEmpty(messages)){
                    validationErrorInfo.setMessage(messages);
                    validationErrorInfos.add(validationErrorInfo);
                }
            }
            if(CollUtil.isNotEmpty(validationErrorInfos)){
                throw new ValidationException("校验异常", method, validationErrorInfos);
            }
        }
        return method.invoke(oldObject, objects);
    }
}
