package net.n2oapp.platform.selection.api;

public class FenwickTree {

    private final int[] val;

    public FenwickTree(final int n) {
        this.val = new int[n + 1];
    }

    /**
     * Увеличить на единицу значение, ассоциированное с индексом {@code i}
     */
    public void increment(int i) {
        i++;
        while (i < val.length) {
            val[i]++;
            i += i & -i;
        }
    }

    /**
     * @return Сумма значений до индекса {@code i}
     */
    public int sum(int i) {
        i++;
        int res = 0;
        while (i > 0) {
            res += val[i];
            i -= i & -i;
        }
        return res;
    }

}
