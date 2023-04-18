package net.n2oapp.platform.userinfo;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class UserInfoTranslationAdvice {

    @Pointcut("@annotation(TranslateUserInfo)")
    public void userinfoTranslateMethods() {
    }

    @Pointcut("within(@TranslateUserInfo *) && execution(* *(..))")
    public void userinfoTranslateClass() {
    }

    @Before("userinfoTranslateMethods()")
    public void injectMethodContext(JoinPoint jp) {
        TranslateUserInfoHolder.set(((MethodSignature) jp.getSignature()).getMethod().getAnnotation(TranslateUserInfo.class).value());
    }

    @After("userinfoTranslateMethods()")
    public void clearMethodContext() {
        TranslateUserInfoHolder.clear();
    }

    @Before("userinfoTranslateClass()")
    public void injectClassContext(JoinPoint jp) {
        TranslateUserInfoHolder.set(((TranslateUserInfo) jp.getSignature().getDeclaringType().getAnnotation(TranslateUserInfo.class)).value());
    }

    @After("userinfoTranslateClass()")
    public void clearClassContext() {
        TranslateUserInfoHolder.clear();
    }
}
