import com.oocourse.spec2.exceptions.RelationNotFoundException;

import java.util.HashMap;

public class MyRelationNotFoundException extends RelationNotFoundException {
    private static int count = 0;
    private int id1;
    private int id2;
    private static HashMap<Integer, Integer> idcount = new HashMap<>();

    public MyRelationNotFoundException(int id1, int id2) {
        this.id1 = Math.min(id1, id2);
        this.id2 = Math.max(id1, id2);
        count++;
        idcount.merge(id1, 1, Integer::sum);
        idcount.merge(id2, 1, Integer::sum);
    }

    @Override
    public void print() {
        System.out.println("rnf-" + count + ", " + id1 + "-" +
                idcount.get(id1) + ", " + id2 + "-" + idcount.get(id2));
    }
}
