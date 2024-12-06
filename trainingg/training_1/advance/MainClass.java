import expr.Expr;

import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.next();

        Lexer lexer = new Lexer(input);//lexer词法分析器，分解原始字符串，得到token流
        Parser parser = new Parser(lexer);//parser语法分析器，构建语法树，也就是token之间的逻辑关系

        Expr expr = parser.parseExpr();
        System.out.println(expr);
    }
}
