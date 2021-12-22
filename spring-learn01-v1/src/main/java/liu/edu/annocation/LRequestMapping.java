package liu.edu.annocation;

import java.lang.annotation.*;


/**
 * 自定义注解（与Spring@RequestMapping同作用）
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention( RetentionPolicy.RUNTIME)
@Documented
public @interface LRequestMapping {
    String value() default "";
}
