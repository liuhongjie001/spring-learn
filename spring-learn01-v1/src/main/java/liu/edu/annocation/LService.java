package liu.edu.annocation;

import java.lang.annotation.*;


/**
 * 自定义注解（与Spring@Service同作用）
 */
@Target(ElementType.TYPE)
@Retention( RetentionPolicy.RUNTIME)
@Documented
public @interface LService {
    String value() default "";
}
