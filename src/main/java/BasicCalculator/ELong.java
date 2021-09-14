package BasicCalculator;

class ELong implements Evaluable<ELong> {
    Long value;
    ELong(Long value) {
        this.value = value;
    }
    @Override
    public ELong from(String s) {
        return new ELong(Long.parseLong(s));
    }

    @Override
    public ELong add(ELong second) {
        return new ELong(this.value + second.value);
    }

    @Override
    public ELong subtract(ELong second) {
        return new ELong(this.value - second.value);
    }

    @Override
    public ELong negate() {
        return new ELong(-this.value);
    }
}
