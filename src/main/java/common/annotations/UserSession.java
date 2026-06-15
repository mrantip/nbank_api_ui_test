package common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UserSession {
    int value() default 1; // количество пользователей
    int auth() default 1; // какой пользователь авторизован (индекс с 1)
    boolean withAccount() default false; // создать аккаунт для авторизованного пользователя
    boolean withAccountForAll() default false; // создать аккаунты для всех пользователей
    boolean withDeposit() default false;      // делать ли депозиты
    int depositCount() default 3;             // количество депозитов (по умолчанию 3)
    double depositAmount() default 5000.0;    // сумма депозита (по умолчанию максимальная)
}