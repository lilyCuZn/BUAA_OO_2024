import com.oocourse.spec2.exceptions.EqualTagIdException;

import java.util.HashMap;

public class MyEqualTagIdException extends EqualTagIdException {
    private static int count = 0;
    private int id;
    private static HashMap<Integer, Integer> idcount = new HashMap<>();

    public MyEqualTagIdException(int id) {
        this.id = id;
        count++;
        idcount.merge(id, 1, Integer::sum);
    }

    @Override
    public void print() {
        System.out.println("eti-" + count + ", " + id + "-" + idcount.get(id));
    }
}
