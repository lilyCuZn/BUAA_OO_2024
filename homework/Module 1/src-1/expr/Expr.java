package expr;

import java.util.ArrayList;
import java.util.Iterator;

public class Expr implements Factor {
    private final ArrayList<Term> terms;

    private final ArrayList<String> ops;//每个表达式中的+/-

    private int flag;//表示到哪个ops了

    public Expr() {
        this.terms = new ArrayList<>();
        this.ops = new ArrayList<>();
        this.flag = 0;
    }

    public void addTerm(Term term, String op) {
        this.terms.add(term);
        this.ops.add(op);
    }

    public String toString() {
        flag = 0;
        Iterator<Term> iter = terms.iterator();
        StringBuilder sb = new StringBuilder();
        sb.append(iter.next().toString());
        ++flag;
        while (iter.hasNext()) {
            sb.append(" ");
            sb.append(iter.next().toString());
            String op = ops.get(flag++);
            sb.append(" " + op);
        }
        return sb.toString();
    }
}
