import com.oocourse.spec2.exceptions.EqualRelationException;

import java.util.HashMap;

public class MyEqualRelationException extends EqualRelationException {
    private static int count = 0;
    private int id1;
    private int id2;
    private static HashMap<Integer, Integer> idcount = new HashMap<>();

    public MyEqualRelationException(int id1, int id2) {
        this.id1 = Math.min(id1, id2);
        this.id2 = Math.max(id1, id2);
        count++;
        if (id1 != id2) {
            idcount.merge(id1, 1, Integer::sum);
            idcount.merge(id2, 1, Integer::sum);
        } else {
            idcount.merge(id1, 1, Integer::sum);
        }
    }

    @Override
    public void print() {
        System.out.println("er-" + count + ", " + id1 + "-" + idcount.get(id1)
                + ", " + id2 + "-" + idcount.get(id2));
    }
}