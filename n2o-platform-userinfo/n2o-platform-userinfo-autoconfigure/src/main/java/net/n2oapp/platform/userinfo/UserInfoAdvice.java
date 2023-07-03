package net.n2oapp.platform.userinfo;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class UserInfoAdvice {

    @Pointcut("@annotation(net.n2oapp.platform.userinfo.UserInfo)")
    public void userinfoTranslateMethods() {
    }

    @Pointcut("within(@net.n2oapp.platform.userinfo.UserInfo *) && execution(* *(..))")
    public void userinfoTranslateClass() {
    }

    @Before("userinfoTranslateMethods()")
    public void injectMethodContext(JoinPoint jp) {
        UserInfoStateHolder.set(((MethodSignature) jp.getSignature()).getMethod().getAnnotation(UserInfo.class).value());
    }

    @After("userinfoTranslateMethods()")
    public void clearMethodContext() {
        UserInfoStateHolder.clear();
    }

    @Before("userinfoTranslateClass()")
    public void injectClassContext(JoinPoint jp) {
        UserInfoStateHolder.set(((UserInfo) jp.getSignature().getDeclaringType().getAnnotation(UserInfo.class)).value());
    }

    @After("userinfoTranslateClass()")
    public void clearClassContext() {
        UserInfoStateHolder.clear();
    }
}
