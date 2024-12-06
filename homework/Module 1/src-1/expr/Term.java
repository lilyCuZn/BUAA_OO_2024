package expr;

import java.util.ArrayList;
import java.util.Iterator;

public class Term {
    private final ArrayList<Factor> basics;

    public Term() {
        this.basics = new ArrayList<>();
    }

    public void addBasic(Basic basic) {
        //System.out.println(factor);
        this.basics.add(basic);
    }

    public String toString() {
        Iterator<Factor> iter = basics.iterator();
        /* for (Factor obj : factors)
        {
            System.out.println(obj);
        }*/
        StringBuilder sb = new StringBuilder();
        sb.append(iter.next().toString());
        while (iter.hasNext()) {
            sb.append(" ");
            sb.append(iter.next().toString());
            sb.append(" *");
        }
        //System.out.println(sb.toString());
        return sb.toString();
    }
}
