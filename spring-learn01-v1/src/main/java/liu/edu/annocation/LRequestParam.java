package liu.edu.annocation;

import java.lang.annotation.*;


/**
 * 自定义注解（与Spring@RequestParam同作用）
 */
@Target(ElementType.PARAMETER)
@Retention( RetentionPolicy.RUNTIME)
@Documented
public @interface LRequestParam {
    String value() default "";

}
