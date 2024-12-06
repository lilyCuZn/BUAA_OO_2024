public class Lexer {
    private final String input;
    private int pos = 0;
    private String curToken;

    public Lexer(String input) {
        this.input = input;
        this.next();
    }

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
        } else if (c == '+' || c == '*' || c == '(' || c == ')'/* TODO */) {
            pos += 1;
            curToken = String.valueOf(c);//转换为字符串
        }
    }

    public String peek() {
        return this.curToken;
    }
}
