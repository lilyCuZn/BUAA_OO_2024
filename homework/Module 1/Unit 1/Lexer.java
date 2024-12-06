import java.util.ArrayList;

public class Lexer {
    private final String input;
    private int pos = 0;
    private String curToken;

    private boolean isDigit;//包含数字和未知数

    private ArrayList<String> tokenList;

    public Lexer(String input) {
        this.input = input;
        //this.tokenList = new ArrayList<>();
        //this.tokenTypeList = new ArrayList<>();
        this.next();
    }

    /*private void lexAnal()//把input解析成token流
    {
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (Character.isDigit(ch)) {
                StringBuilder sb = new StringBuilder();
                while (i < input.length() && Character.isDigit(input.charAt(i))) {
                    sb.append(input.charAt(i));把表达式附加到StringBuilder之后
                    ++i;
                }
                String numString =  sb.toString();

                tokenList.add(numString);
                tokenTypeList.add(TokenType.NUM);
            }
            else {
                switch (ch) {
                    case '+': {
                        tokenList.add("+");
                        tokenTypeList.add(TokenType.PLUS);
                        break;
                    }
                    case '-': {
                        tokenList.add("-");
                        tokenTypeList.add(TokenType.MINUS);
                        break;
                    }
                    case '*': {
                        tokenList.add("*");
                        tokenTypeList.add(TokenType.MULTI);
                        break;
                    }
                    case '(': {
                        tokenList.add("(");
                        tokenTypeList.add(TokenType.LP);
                        break;
                    }
                    case ')': {
                        tokenList.add(")");
                        tokenTypeList.add(TokenType.RP);
                        break;
                    }
                    case 'x': {
                        tokenList.add("x");
                        tokenTypeList.add(TokenType.X);
                        break;
                    }
                    default:
                }
            }
        }
    }*/

    private String getNumber() {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos)/* TODO */);//把表达式附加到StringBuilder之后
            ++pos;
        }

        return sb.toString();
    }

    public void next() {
        if (pos == input.length()) {
            return;
        }

        char c = input.charAt(pos);
        if (Character.isDigit(c)) {
            curToken = getNumber()/* TODO */;
            isDigit = true;
        }
        else if (c == '+' || c == '-' || c == '*' || c == '(' || c == ')' || c == '^'/* TODO */) {
            pos += 1;
            curToken = String.valueOf(c);//转换为字符串
        }
        else if (c == 'x')
        {
            curToken = String.valueOf(c);
            ++pos;
        }
    }

    public String peek() {
        return this.curToken;
    }
}
