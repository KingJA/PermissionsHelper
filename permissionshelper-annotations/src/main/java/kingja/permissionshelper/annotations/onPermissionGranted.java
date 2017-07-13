package kingja.permissionshelper.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description:TODO
 * Create Time:2017/7/11 16:42
 * Author:KingJA
 * Email:kingjavip@gmail.com
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface onPermissionGranted {
    String[] value();
}
