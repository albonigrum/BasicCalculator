package BasicCalculator;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class BasicCalculatorTest {

    @Nested
    class InInteger {
        @ParameterizedTest(name = "{0}")
        @CsvFileSource(files = "src/test/resources/basic_expressions.csv", numLinesToSkip = 1)
        void calculateBasicExpressionsInInteger(String name, String expression, int answer) {
            BasicCalculator<EInteger> calc = new BasicCalculator<EInteger>(new EInteger(0));
            assertEquals(answer, calc.calculate(expression).value.intValue());
        }
    }

    @Nested
    class InLong {
        @ParameterizedTest(name = "{0}")
        @CsvFileSource(files = "src/test/resources/basic_expressions.csv", numLinesToSkip = 1)
        void calculateBasicExpressionsInLong(String name, String expression, long answer) {
            BasicCalculator<ELong> calc = new BasicCalculator<ELong>(new ELong(0L));
            assertEquals(answer, calc.calculate(expression).value.intValue());
        }
    }

    @Nested
    class InBigInteger {
        @ParameterizedTest(name = "{0}")
        @CsvFileSource(files = "src/test/resources/basic_expressions.csv", numLinesToSkip = 1)
        void calculateBasicExpressionsInBigInteger(String name, String expression, String answer) {
            BasicCalculator<EBigInteger> calc =
                    new BasicCalculator<EBigInteger>(new EBigInteger(new BigInteger("0")));
            assertEquals(answer, calc.calculate(expression).value.toString());
        }
    }

}