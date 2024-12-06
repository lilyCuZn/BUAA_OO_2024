package expr;

import java.util.ArrayList;
import java.util.Iterator;

public class Basic implements Factor {
    private final ArrayList<Factor> factors;

    public Basic() {
        this.factors = new ArrayList<>();
    }

    public String toString() {
        Iterator<Factor> iter = factors.iterator();
        StringBuilder sb = new StringBuilder();
        sb.append(iter.next().toString());
        while (iter.hasNext()) {
            sb.append(" ");
            sb.append(iter.next().toString());
            sb.append(" ^");
        }
        return sb.toString();
    }

    public void addFactor(Factor factor)
    {
        this.factors.add(factor);
    }
}
