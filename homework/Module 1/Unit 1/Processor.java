public class Processor {
    public Processor()
    {

    }

    public String process(String str) {
        Processor processor = new Processor();
        String proStr = processor.mergeAddAndSub(str);
        proStr = processor.delForwardZero(proStr);
        proStr = processor.addZero(proStr);

        return proStr;
    }

    public String mergeAddAndSub(String originPoly)
    {
        String processedPoly = originPoly.replaceAll("\\s", "");
        char[] processedChars = processedPoly.toCharArray();

        for (int i = 0; i < processedPoly.length() - 1; i++) {
            char ch1 = processedChars[i];
            char ch2 = processedChars[i + 1];
            if (ch1 == '+' || ch1 == '-') {
                if (ch2 == '+' || ch2 == '-') {
                    if (ch1 == ch2) {
                        processedChars[i] = ' ';
                        processedChars[i + 1] = '+';
                    } else {
                        processedChars[i] = ' ';
                        processedChars[i + 1] = '-';
                    }
                }
            }
        }
        processedPoly = new String(processedChars);
        processedPoly = processedPoly.replaceAll("\\s", "");


        //processedPoly = addZero(processedPoly);
        return processedPoly;
    }

    public String delForwardZero(String str) {
        String proString = str.replaceAll("0*(\\d+)", "$1");
        /*str = str.replaceAll("-0+", "-");
        str = str.replaceAll("\\*0+", "\\*");
        str = str.replaceAll("\\^0+", "\\^");*/

        return proString;
    }

    public String addZero(String str) { //假如expr的首项是符号，添加0
        String string = str;
        if (string.startsWith("+") || string.startsWith("-")) {
            string = "0" + string;
        }

        string = string.replaceAll("\\(\\+", "\\("); //(+2
        string = string.replaceAll("\\(\\-", "\\(0\\-"); //(-2
        string = string.replaceAll("\\*\\+", "\\*"); //*+2
        string = string.replaceAll("\\*-", "\\*\\(0-1)\\*"); //*-2
        string = string.replaceAll("\\^\\+", "\\^"); //^+2

        return string;
    }

    public String adjust(String str) {
        String s = str;
        if (s.charAt(0) != '-') {
            return s;
        }
        else {
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) == '+') {
                    String front = str.substring(0, i);
                    String back = str.substring(i);
                    return back + front;
                }
            }
            return s;
        }
    }

    public String delForAdd(String str) {
        String s = str;
        if (s.startsWith("+")) {
            s = s.substring(1);
        }
        return s;
    }
    //验证程序！
    /*public static void main(String[] args) {
        String str = new String("-0000*00005^0003+222003");
        Processor processor = new Processor();
        str = processor.delForwardZero(str);
        System.out.println(str);
    }*/
}
