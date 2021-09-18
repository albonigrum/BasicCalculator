package BasicCalculator;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class BasicCalculatorTest {

    @Nested
    class InInteger {
        @ParameterizedTest(name = "{0}")
        @CsvFileSource(files = "src/test/resources/basic_expressions.csv", numLinesToSkip = 1)
        void calculateBasicExpressions(String name, String expression, long answer) {
            BasicCalculator calc = new BasicCalculator();
            assertEquals(answer, calc.calculateInt(expression));
        }

        @ParameterizedTest(name = "{0}")
        @CsvFileSource(files = "src/test/resources/slashExpressionForIntegerTypes.csv")
        void calculateSlash(String name, String expression, long answer) {
            BasicCalculator calc = new BasicCalculator();
            assertEquals(answer, calc.calculateInt(expression));
        }
    }

    @Nested
    class InFloat {
        @ParameterizedTest(name = "{0}")
        @CsvFileSource(files = "src/test/resources/basic_expressions.csv", numLinesToSkip = 1)
        void calculateBasicExpressions(String name, String expression, double answer) {
            BasicCalculator calc = new BasicCalculator();
            assertEquals(answer, calc.calculateFloat(expression));
        }

        @ParameterizedTest(name = "{0}")
        @CsvSource(
                value = {
                        "ToOne1, 0.001 * 1000, 1",
                        "ToOne2, 0.0000001 * 10000000, 1",
                        "ToMinusOne, 1000000 * (-0.000001), -1",
                        "ToTen, 10.1 + (-0.1), 10"
                }
        )
        void calculateFractional(String name, String expression, double answer) {
            BasicCalculator calc = new BasicCalculator();
            assertEquals(answer, calc.calculateFloat(expression));
        }

        @ParameterizedTest(name = "{0}")
        @CsvFileSource(files = "src/test/resources/slashExpressionForFloatTypes.csv")
        void calculateSlash(String name, String expression, double answer) {
            BasicCalculator calc = new BasicCalculator();
            assertEquals(answer, calc.calculateFloat(expression));
        }

    }

}