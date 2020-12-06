package net.n2oapp.platform.jaxrs.seek;

public enum RequestedPageEnum {

    /**
     * Запрос за первой страницей?
     */
    FIRST(false),

    /**
     * Запрос за следующей страницей?
     */
    NEXT(true),

    /**
     * Запрос за предыдущей страницей?
     */
    PREV(true),

    /**
     * Запрос за последней страницей?
     */
    LAST(false);

    private final boolean pivotsNecessary;

    RequestedPageEnum(boolean pivotsNecessary) {
        this.pivotsNecessary = pivotsNecessary;
    }

    public boolean isPivotsNecessary() {
        return pivotsNecessary;
    }

}
