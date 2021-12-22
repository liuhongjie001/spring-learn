package liu.edu.annocation;

import java.lang.annotation.*;


/**
 * 自定义注解（与Spring@Autowried同作用）
 */
@Target(ElementType.FIELD)
@Retention( RetentionPolicy.RUNTIME)
@Documented
public @interface LAutoWried {
    String value() default "";

}
