import expr.Expr;

import java.util.ArrayList;
import java.util.Stack;

public class Simplifier {
    public Simplifier()
    {

    }

    public String simplify(Expr expr)
    {
        String suffix = expr.toString();//后缀表达式
        ArrayList<String> suffixs = getListString(suffix);
        Stack<Formula> stack = new Stack<>();

        for (String item : suffixs) {
            //System.out.println(item);
            if (item.matches("\\d+") | item.matches("x")) {
                stack.push(new Formula(item));
            }
            else {
                Formula form1 = stack.pop();//?可以这样吗
                Formula form2 = stack.pop();
                Formula result = new Formula();
                if (item.equals("+")) {
                    result = form1.add(form2);
                }
                else if (item.equals("-")) {
                    result = form2.sub(form1);
                }
                else if (item.equals("*")) {
                    result = form1.mult(form2);
                }
                else if (item.equals("^")) {
                    result = form2.pow(form1);
                }
                stack.push(result);
            }
        }

        return stack.pop().toString();
    }

    public ArrayList<String> getListString(String exprString)
    {
        String[] exprs = exprString.split(" ");
        ArrayList<String> list = new ArrayList<String>();
        for (String ele : exprs) {
            list.add(ele);
        }
        return list;
    }
}
