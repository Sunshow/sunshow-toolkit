package lombok;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * author: sunshow.
 */
public class LombokBuilderUtils {

    private static final Logger logger = LoggerFactory.getLogger(LombokBuilderUtils.class);

    public static <Builder, Dest, PropertiesSource> void copyPropertiesToBuilder(Builder builder, Class<Dest> destClass, PropertiesSource source) {
        Field[] fields = destClass.getDeclaredFields();
        Class<?> builderClass = builder.getClass();
        Map<String, Method> methodMap = StreamSupport.stream(Arrays.spliterator(builderClass.getDeclaredMethods()), false).collect(Collectors.toMap(Method::getName, Function.identity()));
        for (Field field : fields) {
            field.setAccessible(true);
            int modifiers = field.getModifiers();
            if (Modifier.isPrivate(modifiers) && !Modifier.isStatic(modifiers)) {
                if (methodMap.containsKey(field.getName())) {
                    try {
                        Object val = field.get(source);
                        if (val != null) {
                            methodMap.get(field.getName()).invoke(builder, val);
                        }
                    } catch (Exception e) {
                        logger.error("复制属性出错, field=" + field.getName(), e);
                    }
                }
            }
        }
    }

}
