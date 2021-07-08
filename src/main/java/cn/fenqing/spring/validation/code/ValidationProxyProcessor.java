package cn.fenqing.spring.validation.code;

import cn.fenqing.spring.validation.annotation.ActivateValidation;
import cn.fenqing.spring.validation.annotation.ValidationHandler;
import cn.fenqing.spring.validation.utils.ReflectUtils;
import cn.hutool.core.util.TypeUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author fenqing
 * @version 0.0.1
 */
@Configuration
@Slf4j
public class ValidationProxyProcessor implements BeanPostProcessor {

    /**
     * postProcessAfterInitialization 当bean创建完毕后触发
     *
     * @return java.lang.Object
     * @author fenqing
     * @version 0.0.1
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //尝试使用cglib动态代理
        Object proxy = cglilbProxy(bean, beanName);
        if(proxy != null){
            return proxy;
        }
        registerHandler(bean, beanName);
        return bean;
    }

    /**
     * registerHandler 注册规则对象
     * @author fenqing
     * @version 0.0.1
     * @param bean bean
     * @param beanName bean名称
     * @return void
     */
    private void registerHandler(Object bean, String beanName){
        if(bean instanceof ValidationRuleHeadler){
            ValidationHandler annotation = bean.getClass().getAnnotation(ValidationHandler.class);
            if (annotation != null){
                Type typeArgument = TypeUtil.getTypeArgument(bean.getClass(), 0);
                Class<Annotation> aClass = (Class<Annotation>) TypeUtil.getClass(typeArgument);
                ValidationRulePool.registerRule(aClass, (ValidationRuleHeadler<?>) bean, annotation.order());
            }
        }
    }

    /**
     * jdk动态代理对象
     * @param bean bean
     * @param beanName beanname
     * @return 结果
     */
    private Object jdkProxy(Object bean, String beanName){
        //获取该bean的方法
        Class<?> clazz = bean.getClass();
        List<Class<?>> classes = ReflectUtils.scanClassFamily(clazz);
        Map<Class<?>, ActivateValidation> classActivateValidationMap = ReflectUtils.scanClassAnnotation(classes, ActivateValidation.class);
        Map<Method, ActivateValidation> methodActivateValidationMap = new HashMap<>(8);
        for (Map.Entry<Class<?>, ActivateValidation> classActivateValidationEntry : classActivateValidationMap.entrySet()) {
            ActivateValidation value = classActivateValidationEntry.getValue();
            if(value != null){
                List<Method> nonStaticMethod = ReflectUtils.getNonStaticMethod(clazz);
                for (Method method : nonStaticMethod) {
                    methodActivateValidationMap.put(method, value);
                }
                break;
            }
        }
        Map<Method, ActivateValidation> annotationAndMethods = ReflectUtils.getAnnotationAndMethods(clazz, ActivateValidation.class);
        methodActivateValidationMap.putAll(annotationAndMethods);
        if(methodActivateValidationMap.size() > 0){
            Map<Method, List<Annotation>[]> methodMap = methodActivateValidationMap.keySet().stream()
                    .collect(Collectors.toMap(Function.identity(), ReflectUtils::methodAnnotations));
            ValidationProxyHandler validationProxyHandler = new ValidationProxyHandler(bean, methodMap, methodActivateValidationMap);
            return Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), validationProxyHandler);
        }
        return null;
    }

    /**
     * jdk动态代理对象
     * @param bean bean
     * @param beanName beanname
     * @return 结果
     */
    private Object cglilbProxy(Object bean, String beanName){
        //获取该bean的方法
        Class<?> clazz = bean.getClass();
        List<Class<?>> classes = ReflectUtils.scanClassFamily(clazz);
        Map<Class<?>, ActivateValidation> classActivateValidationMap = ReflectUtils.scanClassAnnotation(classes, ActivateValidation.class);
        Map<Method, ActivateValidation> methodActivateValidationMap = new HashMap<>(8);
        for (Map.Entry<Class<?>, ActivateValidation> classActivateValidationEntry : classActivateValidationMap.entrySet()) {
            ActivateValidation value = classActivateValidationEntry.getValue();
            if(value != null){
                List<Method> nonStaticMethod = ReflectUtils.getNonStaticMethod(clazz);
                for (Method method : nonStaticMethod) {
                    methodActivateValidationMap.put(method, value);
                }
                break;
            }
        }
        Map<Method, ActivateValidation> annotationAndMethods = ReflectUtils.getAnnotationAndMethods(clazz, ActivateValidation.class);
        methodActivateValidationMap.putAll(annotationAndMethods);
        if(methodActivateValidationMap.size() > 0){
            Map<Method, List<Annotation>[]> methodMap = methodActivateValidationMap.keySet().stream()
                    .collect(Collectors.toMap(Function.identity(), ReflectUtils::methodAnnotations));
            ValidationCglibProxyHandler validationProxyHandler =
                    new ValidationCglibProxyHandler(bean, methodMap, methodActivateValidationMap);

            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(clazz);
            enhancer.setCallback(validationProxyHandler);
            return enhancer.create();
        }
        return null;
    }
}
