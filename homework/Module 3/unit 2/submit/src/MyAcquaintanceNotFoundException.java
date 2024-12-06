import com.oocourse.spec2.exceptions.AcquaintanceNotFoundException;

import java.util.HashMap;

public class MyAcquaintanceNotFoundException extends AcquaintanceNotFoundException {
    private static int count = 0;
    private int id;
    private static HashMap<Integer, Integer> idcount = new HashMap<>();

    public MyAcquaintanceNotFoundException(int id) {
        this.id = id;
        count++;
        idcount.merge(id, 1, Integer::sum);
    }

    @Override
    public void print() {
        System.out.println("anf-" + count + ", " + id + "-" + idcount.get(id));
    }
}
