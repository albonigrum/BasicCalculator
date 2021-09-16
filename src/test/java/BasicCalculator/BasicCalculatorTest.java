package BasicCalculator;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class BasicCalculatorTest {

    @Nested
    class InInteger {
        @ParameterizedTest(name = "{0}")
        @CsvFileSource(files = "src/test/resources/basic_expressions.csv", numLinesToSkip = 1)
        void calculateBasicExpressions(String name, String expression, int answer) {
            BasicCalculator<EInteger> calc = new BasicCalculator<EInteger>(new EInteger(0));
            assertEquals(answer, calc.calculate(expression).value);
        }

        @ParameterizedTest(name = "{0}")
        @CsvFileSource(files = "src/test/resources/slashExpressionForIntegerTypes.csv")
        void calculateSlash(String name, String expression, int answer) {
            BasicCalculator<EInteger> calc = new BasicCalculator<EInteger>(new EInteger(0));
            assertEquals(answer, calc.calculate(expression).value);
        }
    }

    @Nested
    class InLong {
        @ParameterizedTest(name = "{0}")
        @CsvFileSource(files = "src/test/resources/basic_expressions.csv", numLinesToSkip = 1)
        void calculateBasicExpressions(String name, String expression, long answer) {
            BasicCalculator<ELong> calc = new BasicCalculator<ELong>(new ELong(0L));
            assertEquals(answer, calc.calculate(expression).value.intValue());
        }
        @ParameterizedTest(name = "{0}")
        @CsvFileSource(files = "src/test/resources/slashExpressionForIntegerTypes.csv")
        void calculateSlash(String name, String expression, long answer) {
            BasicCalculator<ELong> calc = new BasicCalculator<ELong>(new ELong(0L));
            assertEquals(answer, calc.calculate(expression).value);
        }
    }

    @Nested
    class InBigInteger {
        @ParameterizedTest(name = "{0}")
        @CsvFileSource(files = "src/test/resources/basic_expressions.csv", numLinesToSkip = 1)
        void calculateBasicExpressions(String name, String expression, String answer) {
            BasicCalculator<EBigInteger> calc =
                    new BasicCalculator<EBigInteger>(new EBigInteger(new BigInteger("0")));
            assertEquals(answer, calc.calculate(expression).value.toString());
        }
        @ParameterizedTest(name = "{0}")
        @CsvFileSource(files = "src/test/resources/slashExpressionForIntegerTypes.csv")
        void calculateSlash(String name, String expression, String answer) {
            BasicCalculator<EBigInteger> calc =
                    new BasicCalculator<EBigInteger>(new EBigInteger(new BigInteger("0")));
            assertEquals(answer, calc.calculate(expression).value.toString());
        }
    }

    @Nested
    class InDouble {
        @ParameterizedTest(name = "{0}")
        @CsvFileSource(files = "src/test/resources/basic_expressions.csv", numLinesToSkip = 1)
        void calculateBasicExpressions(String name, String expression, double answer) {
            BasicCalculator<EDouble> calc = new BasicCalculator<EDouble>(new EDouble(0D));
            assertEquals(answer, calc.calculate(expression).value);
        }

        @ParameterizedTest(name = "{0}")
        @CsvSource(
                value = {
                        "ToOne1, 0.001 * 1000, 1",
                        "ToOne2, 0.0000001 * 10000000, 1",
                        "ToMinusOne, 1000000 * (-0.000001), -1"
                }
        )
        void calculate(String name, String expression, double answer) {
            BasicCalculator<EDouble> calc = new BasicCalculator<EDouble>(new EDouble(0D));
            assertEquals(answer, calc.calculate(expression).value);
        }

        @ParameterizedTest(name = "{0}")
        @CsvFileSource(files = "src/test/resources/slashExpressionForFloatTypes.csv")
        void calculateSlash(String name, String expression, double answer) {
            BasicCalculator<EDouble> calc = new BasicCalculator<EDouble>(new EDouble(0D));
            assertEquals(answer, calc.calculate(expression).value);
        }

    }

}