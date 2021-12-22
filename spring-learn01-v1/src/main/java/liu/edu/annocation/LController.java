package liu.edu.annocation;

import java.lang.annotation.*;


/**
 * 自定义注解（与Spring@Controller同作用）
 */
@Target(ElementType.TYPE)
@Retention( RetentionPolicy.RUNTIME)
@Documented
public @interface LController {
    String value() default "";



}
