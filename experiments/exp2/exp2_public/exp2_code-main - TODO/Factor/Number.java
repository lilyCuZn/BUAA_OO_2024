package Factor;

import java.math.BigInteger;

public class Number implements Factor {
    private BigInteger num;
    
    public Number(String num) {
        this.num = new BigInteger(num);
    }
    
    @Override
    public String toString() {
        return num.toString();
    }
    
    @Override
    public Factor derive() {
        Term term = new Term();
        Number num = new Number("0");
        term.addFactor(num);// TODO 1
        return term;
    }
    
    @Override
    public Factor clone() {
        return new Number(num.toString());
    }
}
