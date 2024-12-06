import com.oocourse.spec2.exceptions.TagIdNotFoundException;

import java.util.HashMap;

public class MyTagIdNotFoundException extends TagIdNotFoundException {
    private static int count = 0;
    private int id;
    private static HashMap<Integer, Integer> idcount = new HashMap<>();

    public MyTagIdNotFoundException(int id) {
        this.id = id;
        count++;
        idcount.merge(id, 1, Integer::sum);
    }

    @Override
    public void print() {
        System.out.println("tinf-" + count + ", " + id + "-" + idcount.get(id));
    }
}
