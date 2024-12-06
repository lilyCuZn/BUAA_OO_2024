import expr.Expr;

import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        Processor processor = new Processor();
        String proInput = processor.process(input);

        //System.out.println(proInput);

        Lexer lexer = new Lexer(proInput);//lexer词法分析器，分解原始字符串，得到token流
        Parser parser = new Parser(lexer);//parser语法分析器，构建语法树，也就是token之间的逻辑关系

        Expr expr = parser.parseExpr();
        //System.out.println(expr);
        Simplifier simplifier = new Simplifier();
        String output = simplifier.simplify(expr);
        //System.out.println("Output is " + output);
        String proOutput = processor.mergeAddAndSub(output);
        proOutput = processor.delForwardZero(proOutput);
        proOutput = processor.adjust(proOutput);
        proOutput = processor.delForAdd(proOutput);
        System.out.println(proOutput);
    }
}
