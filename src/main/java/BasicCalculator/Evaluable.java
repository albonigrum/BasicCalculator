package BasicCalculator;

public interface Evaluable<T> {
    T from(String s);
    T add(T second);
    T subtract(T second);
    T multiply(T second);
    T negate();
}
