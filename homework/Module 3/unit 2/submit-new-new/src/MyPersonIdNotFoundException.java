import com.oocourse.spec2.exceptions.PersonIdNotFoundException;

import java.util.HashMap;

public class MyPersonIdNotFoundException extends PersonIdNotFoundException {
    private static int count = 0;
    private int id;
    private static HashMap<Integer, Integer> idcount = new HashMap<>();

    public MyPersonIdNotFoundException(int id) {
        this.id = id;
        count++;
        idcount.merge(id, 1, Integer::sum);
    }

    @Override
    public void print() {
        System.out.println("pinf-" + count + ", " + id + "-" + idcount.get(id));
    }
}
