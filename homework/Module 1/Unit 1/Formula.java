import java.math.BigInteger;
import java.util.ArrayList;

public class Formula {
    //private HashMap<BigInteger, BigInteger> contents;//a * x ^ b,即为<b,a>，因为b是key，一个指数对应唯一的一个系数

    private ArrayList<BigInteger> contents;

    public Formula() {
        contents = new ArrayList<>();
    }

    public Formula(String content)
    {
        contents = new ArrayList<>();
        if (content.matches("\\d+")) {
            contents.add(new BigInteger(content));//0---content
            //contents.put(new BigInteger("0"), new BigInteger(content));//x的0次方
        }
        else if (content.matches("x")) {
            contents.add(BigInteger.valueOf(0));
            contents.add(BigInteger.valueOf(1));
            //contents.put(new BigInteger("1"), new BigInteger("1"));
        }
    }

    public Formula add(Formula other)
    {
        int a = this.contents.size();
        int b = other.contents.size();
        Formula newForm = new Formula();
        int i = 0;
        for (; (i < a) && (i < b); i++)
        {
            BigInteger sum = this.contents.get(i).add(other.contents.get(i));
            newForm.contents.add(sum);
        }

        while (i < a) {
            BigInteger sum = this.contents.get(i);
            newForm.contents.add(sum);
            ++i;
        }
        while (i < b) {
            BigInteger sum = other.contents.get(i);
            newForm.contents.add(sum);
            ++i;
        }

        return newForm;
    }

    public Formula sub(Formula other)
    {
        int a = this.contents.size();
        int b = other.contents.size();
        Formula newForm = new Formula();
        int i = 0;
        for (; (i < a) && (i < b); i++)
        {
            BigInteger sum = this.contents.get(i).subtract(other.contents.get(i));
            newForm.contents.add(sum);
        }

        while (i < a) {
            BigInteger sum = this.contents.get(i);
            newForm.contents.add(sum);
            ++i;
        }
        while (i < b) {
            BigInteger sum = other.contents.get(i);
            newForm.contents.add(sum.negate());
            ++i;
        }

        return newForm;
    }

    public Formula mult(Formula other) {
        int a = this.contents.size();
        int b = other.contents.size();
        Formula newForm = new Formula();
        for (int i = 0; i < a + b; i++) {
            newForm.contents.add(BigInteger.valueOf(0));
            //System.out.println(newForm);
        } //初始化

        for (int i = 0; i < a; i++) {
            for (int j = 0; j < b; j++) {
                BigInteger index1 = this.contents.get(i);
                BigInteger index2 = other.contents.get(j);
                //System.out.println("index1 * index2 is " + index1.multiply(index2));
                //System.out.println("newForm contents get(i+j) is " + newForm.contents.get(i + j));
                //System.out.println((index1.multiply(index2)).add(newForm.contents.get(i + j)));
                newForm.contents.set(i + j, (index1.multiply(index2)).
                        add(newForm.contents.get(i + j)));//会取到未被赋值的地方
                //System.out.println(newForm);
            }
        }
        return newForm;
    }

    public Formula pow(Formula other) {
        //这里的other一定是数字
        int n = other.contents.get(0).intValue();//得到常数c，它是指数
        Formula newForm = new Formula();
        if (n == 0) {
            newForm.contents.add(new BigInteger("1"));
        }
        else {
            newForm = this;
            for (int i = 1; i < n; i++) {
                newForm = newForm.mult(this);
            }
        }
        return newForm;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean isZero = true;
        //判断是不是一个0
        for (int i = 0; i < contents.size(); i++) {
            if (contents.get(i).compareTo(BigInteger.ZERO) != 0) {
                isZero = false;
                break;
            }
        }

        if (isZero) {
            return "0";
        }
        else {
            if (contents.get(0).compareTo(BigInteger.ZERO) != 0) {
                sb.append("+" + contents.get(0) + " ");
            }
            for (int i = 1; i < contents.size(); i++) {
                if (contents.get(i).compareTo(BigInteger.ZERO) != 0) {
                    sb.append("+ ");
                    if (contents.get(i).compareTo(BigInteger.valueOf(-1)) == 0) { //系数是-1
                        sb.append("-");
                    }
                    else if (contents.get(i).compareTo(BigInteger.ONE) != 0) { //系数不是1
                        sb.append(contents.get(i) + " * ");
                    }
                    sb.append("x ");
                    if (i != 1) {
                        sb.append("^ " + i);
                    }
                }
            }
            return sb.toString();
        }
    }
}
