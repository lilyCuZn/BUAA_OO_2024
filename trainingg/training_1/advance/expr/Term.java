package expr;

import java.util.HashSet;
import java.util.Iterator;

public class Term {
    private final HashSet<Factor> factors;//Why hashset?

    public Term() {
        this.factors = new HashSet<>();
    }

    public void addFactor(Factor factor) {
        //System.out.println(factor);
        this.factors.add(factor);
    }

    public String toString() {
        Iterator<Factor> iter = factors.iterator();
       /* for (Factor obj : factors)
        {
            System.out.println(obj);
        }*/
        StringBuilder sb = new StringBuilder();
        sb.append(iter.next().toString());
        if (iter.hasNext()) {
            sb.append(" ");
            sb.append(iter.next().toString());
            sb.append(" *");
            while (iter.hasNext()) {
                sb.append(" ");
                sb.append(iter.next().toString());
                sb.append(" *");
            }
        }
        //System.out.println(sb.toString());
        return sb.toString();
    }
}
