package cn.fenqing.spring.validation.utils;

import cn.hutool.core.util.ReflectUtil;
import lombok.SneakyThrows;

import java.io.Closeable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fenqing
 * @version 0.0.1
 */
public class ReflectUtils {

    private static final Set<Class<?>> EXCLUDE_CLASS = new HashSet<>();

    static {
        EXCLUDE_CLASS.add(Closeable.class);
        EXCLUDE_CLASS.add(AutoCloseable.class);
    }

    /**
     * 有注解的方法
     * @param clazz 类
     * @param annClass annClass
     * @return 结果
     */
    public static Method[] getHasAnnotationMethods(Class<?> clazz, Class<? extends Annotation> annClass) {
        Method[] methods = ReflectUtil.getPublicMethods(clazz);
        Map<String, List<Method>> nameMapping = Arrays.stream(methods).collect(Collectors.groupingBy(Method::getName));
        Set<Method> methodSet = new HashSet<>();
        Deque<Class> deque = new ArrayDeque<>();
        deque.push(clazz);
        while (!deque.isEmpty()) {
            Class claTemp = deque.pop();
            Method[] anInterfaceMethods = ReflectUtil.getMethods(claTemp, method -> method.isAnnotationPresent(annClass));
            for (Method method : anInterfaceMethods) {
                String name = method.getName();
                List<Method> methods1 = nameMapping.get(name);
                if (methods1 != null) {
                    for (Method methodItem : methods1) {
                        Class<?>[] parameterTypes = methodItem.getParameterTypes();
                        Class<?>[] parameterTypes1 = method.getParameterTypes();
                        boolean flag = true;
                        for (int i = 0; i < parameterTypes.length; i++) {
                            if (parameterTypes[i] != parameterTypes1[i]) {
                                flag = false;
                                break;
                            }
                        }
                        if (flag) {
                            methodSet.add(method);
                            break;
                        }
                    }
                }
            }
            Class superclass = claTemp.getSuperclass();
            if(superclass != null){
                deque.push(superclass);
            }
            Class[] interfaces = claTemp.getInterfaces();
            for (Class anInterface : interfaces) {
                deque.push(anInterface);
            }
        }
        return methodSet.toArray(new Method[0]);
    }

    /**
     * 获取方法上的注解和对象
     * @param clazz clazz
     * @param annClass annclass
     * @param <T>  T
     * @return 结果
     */
    public static <T extends Annotation> Map<Method, T> getAnnotationAndMethods(Class<?> clazz, Class<T> annClass) {
        Method[] methods = ReflectUtil.getPublicMethods(clazz);
        Map<String, List<Method>> nameMapping = Arrays.stream(methods).collect(Collectors.groupingBy(Method::getName));
        Map<Method, T> res = new HashMap<>(8);
        Deque<Class> deque = new ArrayDeque<>();
        deque.push(clazz);
        while (!deque.isEmpty()) {
            Class claTemp = deque.pop();
            Method[] anInterfaceMethods = ReflectUtil.getMethods(claTemp, method -> !Modifier.isStatic(method.getModifiers()));
            Map<Method, T> methodMap = new HashMap<>();
            for (Method method : anInterfaceMethods){
                methodMap.put(method, method.getAnnotation(annClass));
            }
            for (Method method : methodMap.keySet()) {
                T annotation = methodMap.get(method);
                if(res.containsKey(method) || annotation == null){
                    continue;
                }
                String name = method.getName();
                List<Method> methods1 = nameMapping.get(name);
                if (methods1 != null) {
                    for (Method methodItem : methods1) {
                        Class<?>[] parameterTypes = methodItem.getParameterTypes();
                        Class<?>[] parameterTypes1 = method.getParameterTypes();
                        boolean flag = true;
                        for (int i = 0; i < parameterTypes.length; i++) {
                            if (parameterTypes[i] != parameterTypes1[i]) {
                                flag = false;
                                break;
                            }
                        }
                        if (flag) {
                            if(!res.containsKey(method)){
                                res.put(method, annotation);
                            }
                            break;
                        }
                    }
                }
            }
            Class superclass = claTemp.getSuperclass();
            if(superclass != null){
                deque.push(superclass);
            }
            Class[] interfaces = claTemp.getInterfaces();
            for (Class anInterface : interfaces) {
                deque.push(anInterface);
            }
        }
        return res;
    }

    /**
     * 类上是否有注解
     * @param clazz c
     * @param annClass a
     * @return 结果
     */
    public static boolean classHasAnnotation(Class<?> clazz, Class<? extends Annotation> annClass){
        return clazz.isAnnotationPresent(annClass)
                || clazz.getSuperclass().isAnnotationPresent(annClass)
                || Arrays.stream(clazz.getInterfaces()).anyMatch(claitem -> claitem.isAnnotationPresent(annClass));
    }

    /**
     * 获取方法
     * @return 结果
     */
    private static Method[] getMethodUp(Method method){
        List<Method> res = new ArrayList<>();
        Deque<Class> deque = new ArrayDeque<>();
        deque.push(method.getDeclaringClass());
        while (!deque.isEmpty()) {
            Class clazz = deque.pop();
            try {
                Method temp = clazz.getMethod(method.getName(), method.getParameterTypes());
                res.add(temp);
            } catch (NoSuchMethodException e) {
                //
            }
            Class superclass = clazz.getSuperclass();
            if(superclass != null){
                deque.push(superclass);
            }
            Class[] interfaces = clazz.getInterfaces();
            for (Class anInterface : interfaces) {
                deque.push(anInterface);
            }
        }
        return res.toArray(new Method[0]);
    }

    /**
     * 获取该方法每个参数的注解，以及父类，接口上等
     * @param method m
     * @return 结果
     */
    public static List<Annotation>[] methodAnnotations(Method method){
        //当前方法
        List<Annotation>[] res = new List[method.getParameters().length];
        Method[] methodUp = getMethodUp(method);
        for (Method method1 : methodUp) {
            Parameter[] parameters = method1.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                Annotation[] annotations = parameter.getAnnotations();
                List<Annotation> re = res[i];
                if(re == null){
                    re = new ArrayList<>();
                    res[i] = re;
                }
                if(annotations.length > 0){
                    re.addAll(Arrays.asList(annotations));
                }
            }
        }
        return res;
    }

    @SneakyThrows
    public static Method findThisMethod(Object obj, Method method){
        return obj.getClass().getMethod(method.getName(), method.getParameterTypes());
    }

    @SneakyThrows
    public static Class<Annotation> getAnnotationType(Annotation annotation) {
        Field h = annotation.getClass().getSuperclass().getDeclaredField("h");
        h.setAccessible(true);
        Object hv = h.get(annotation);
        Field type = hv.getClass().getDeclaredField("type");
        type.setAccessible(true);
        return (Class<Annotation>) type.get(hv);
    }

    /**
     * 获取类的父类以及接口
     * @param clazz c
     * @return 结果
     */
    public static List<Class<?>> scanClassFamily(Class<?> clazz){
        Deque<Class> deque = new LinkedList<>();
        List<Class<?>> res = new ArrayList<>();
        deque.push(clazz);
        while (!deque.isEmpty()) {
            Class claTemp = deque.pollLast();
            if(EXCLUDE_CLASS.contains(claTemp)){
                continue;
            }
            res.add(claTemp);
            Class superclass = claTemp.getSuperclass();
            if(superclass != null){
                deque.push(superclass);
            }
            Class[] interfaces = claTemp.getInterfaces();
            for (Class anInterface : interfaces) {
                deque.push(anInterface);
            }
        }
        return res;
    }

    /**
     * scanClassAnnotation 扫描
     * @author fenqing
     * @version 0.0.1
     * @param classes 类
     * @param annotationClass 注解
     * @param <T> t
     * @return a
     */
    public static <T extends Annotation> Map<Class<?>, T> scanClassAnnotation(List<Class<?>> classes, Class<T> annotationClass){
        Map<Class<?>, T> res = new HashMap<>();
        classes.forEach(aClass -> res.put(aClass, aClass.getAnnotation(annotationClass)));
        return res;
    }

    /**
     * 获取非静态的
     * @param clazz c
     * @return 结果
     */
    public static List<Method> getNonStaticMethod(Class<?> clazz){
        Method[] methods = clazz.getMethods();
        return Arrays.stream(methods).filter(method -> !Modifier.isStatic(clazz.getModifiers())).collect(Collectors.toList());
    }

    @SneakyThrows
    public static Class[] getJsr303Groups(Annotation annotation){
        Class<? extends Annotation> aClass = ReflectUtils.getAnnotationType(annotation);
        Method gruops = aClass.getDeclaredMethod("groups");
        return (Class[]) gruops.invoke(annotation);
    }
}
