package net.n2oapp.platform.userinfo;

public class TranslateUserInfoHolder {

    private static final ThreadLocal<Boolean> translateUserInfo = ThreadLocal.withInitial(() -> Boolean.TRUE);

    public static void set(boolean translate) {
        translateUserInfo.set(translate);
    }

    public static void clear() {
        translateUserInfo.remove();
    }

    public static Boolean get() {
        return translateUserInfo.get();
    }
}
