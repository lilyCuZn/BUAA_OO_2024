import com.oocourse.spec2.exceptions.EqualPersonIdException;

import java.util.HashMap;

public class MyEqualPersonIdException extends EqualPersonIdException {
    private static int count = 0;
    private int id;
    private static HashMap<Integer, Integer> idcount = new HashMap<>();

    public MyEqualPersonIdException(int id) {
        this.id = id;
        count++;
        idcount.merge(id, 1, Integer::sum);
    }

    @Override
    public void print() {
        System.out.println("epi-" + count + ", " + id + "-" + idcount.get(id));
    }
}
