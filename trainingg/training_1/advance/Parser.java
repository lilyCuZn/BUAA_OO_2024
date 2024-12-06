import expr.Expr;
import expr.Factor;
import expr.Number;
import expr.Term;

import java.math.BigInteger;

public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Expr parseExpr() {
        Expr expr = new Expr();
        expr.addTerm(parseTerm());

        while (lexer.peek().equals("+")/* TODO */) {
            lexer.next();
            expr.addTerm(parseTerm());
        }
        //System.out.println(expr);
        return expr;
    }

    public Term parseTerm() {
        Term term = new Term();
        term.addFactor(parseFactor());

        while (lexer.peek().equals("*")) {
            lexer.next();
            term.addFactor(parseFactor());/* TODO */
        }
        //System.out.println(term);
        return term;
    }

    public Factor parseFactor() {
        if (lexer.peek().equals("(")) {
            lexer.next();
            Factor expr = parseExpr();//提取一个表达式
            lexer.next();//?跳过后面的）/* TODO */
            return expr;
        } else {
            BigInteger num = new BigInteger(lexer.peek());/* TODO */
            lexer.next();
            //System.out.println(num);
            return new Number(num);
        }
    }
}
