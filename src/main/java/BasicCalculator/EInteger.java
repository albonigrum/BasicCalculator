package BasicCalculator;

class EInteger implements Evaluable<EInteger> {
    Integer value;
    public EInteger(Integer value) {
        this.value = value;
    }
    @Override
    public EInteger from(String s) {
        return new EInteger(Integer.parseInt(s));
    }

    @Override
    public EInteger add(EInteger second) {
        return new EInteger(this.value + second.value);
    }

    @Override
    public EInteger subtract(EInteger second) {
        return new EInteger(this.value - second.value);
    }

    @Override
    public EInteger negate() {
        return new EInteger(-this.value);
    }
}
