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
    public EBigInteger negate() {
        return new EBigInteger(value.negate());
    }

    @Override
    public EBigInteger subtract(EBigInteger second) {
        return new EBigInteger(value.subtract(second.value));
    }

    @Override
    public EBigInteger add(EBigInteger second) {
        return new EBigInteger(value.add(second.value));
    }
}
