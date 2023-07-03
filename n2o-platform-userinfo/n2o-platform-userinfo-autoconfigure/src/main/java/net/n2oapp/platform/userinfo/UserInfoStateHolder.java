package net.n2oapp.platform.userinfo;

public class UserInfoStateHolder {
    private UserInfoStateHolder() {
    }

    private static final ThreadLocal<Boolean> userInfoStateHolder = ThreadLocal.withInitial(() -> null);

    public static void set(boolean translate) {
        userInfoStateHolder.set(translate);
    }

    public static void clear() {
        userInfoStateHolder.remove();
    }

    public static Boolean get() {
        return userInfoStateHolder.get();
    }
}
