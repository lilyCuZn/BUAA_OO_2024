import expr.Basic;
import expr.Expr;
import expr.Factor;
import expr.Number;
import expr.Number;
import expr.Term;
import expr.Variable;

import java.math.BigInteger;

public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Expr parseExpr() {
        Expr expr = new Expr();
        expr.addTerm(parseTerm(), "+");

        while (lexer.peek().equals("+") || lexer.peek().equals("-")/* TODO */) {
            String op = lexer.peek();
            lexer.next();
            expr.addTerm(parseTerm(), op);
        }
        //System.out.println(expr);
        return expr;
    }

    public Term parseTerm() {
        Term term = new Term();
        term.addBasic(parseBasic());

        while (lexer.peek().equals("*")) {
            lexer.next();
            term.addBasic(parseBasic());/* TODO */
        }
        //System.out.println(term);
        return term;
    }

    public Basic parseBasic() {
        Basic basic = new Basic();
        basic.addFactor(parseFactor());

        while (lexer.peek().equals("^")) { //while改成if?
            lexer.next();
            basic.addFactor(parseFactor());
        }
        return basic;
    }

    public Factor parseFactor() {
        if (lexer.peek().equals("(")) {
            lexer.next();
            Factor expr = parseExpr();//提取一个表达式
            lexer.next();//?跳过后面的）/* TODO */
            return expr;
        }
        else if (lexer.peek().equals("x")) {
            String var = lexer.peek();
            lexer.next();
            return new Variable(var);
        }
        else {
            BigInteger num = new BigInteger(lexer.peek());/* TODO */
            lexer.next();
            //System.out.println(num);
            return new Number(num);
        }
    }
}
