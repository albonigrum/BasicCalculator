package BasicCalculator;

public class EDouble implements Evaluable<EDouble>{
    Double value;

    EDouble(Double value) {
        this.value = value;
    }

    @Override
    public EDouble from(String s) {
        return new EDouble(Double.parseDouble(s));
    }

    @Override
    public EDouble add(EDouble second) {
        return new EDouble(value + second.value);
    }

    @Override
    public EDouble subtract(EDouble second) {
        return new EDouble(value - second.value);
    }

    @Override
    public EDouble negate() {
        return new EDouble(-value);
    }

    @Override
    public EDouble multiply(EDouble second) {
        return new EDouble(value * second.value);
    }

    @Override
    public EDouble divide(EDouble second) {
        return new EDouble(value / second.value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
