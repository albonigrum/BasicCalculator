package BasicCalculator;

import java.math.BigInteger;

public class EBigInteger implements Evaluable<EBigInteger> {
    BigInteger value;

    public EBigInteger(BigInteger value) {
        this.value = value;
    }

    @Override
    public EBigInteger from(String s) {
        return new EBigInteger(new BigInteger(s));
    }

    @Override
    public EBigInteger add(EBigInteger second) {
        return new EBigInteger(value.add(second.value));
    }

    @Override
    public EBigInteger subtract(EBigInteger second) {
        return new EBigInteger(value.subtract(second.value));
    }

    @Override
    public EBigInteger negate() {
        return new EBigInteger(value.negate());
    }

    @Override
    public EBigInteger multiply(EBigInteger second) {
        return new EBigInteger(value.multiply(second.value));
    }

    @Override
    public EBigInteger divide(EBigInteger second) {
        return new EBigInteger(value.divide(second.value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
