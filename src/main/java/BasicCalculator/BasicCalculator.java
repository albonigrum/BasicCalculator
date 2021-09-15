package BasicCalculator;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class BasicCalculator <IntegerType extends Evaluable<IntegerType>> {
    static final String spaceSymbols = " \t";
    static final String regexSpaceSymbols = "[" + spaceSymbols + "]";
    //operator '-' must be last in string.
    static final String operators = "()+*-";
    static final String regexOperator = "[" + operators + "]";
    //TODO: rewrite logic for unary operators
    static final String regexNumber = "\\(-[1-9]\\d*\\)|[1-9]\\d*|0";
    static final String regexElement = regexNumber + "|" + regexOperator;
    static final String regexExpression =
            regexSpaceSymbols + '*' + '(' + regexElement + ')' + '+' + regexSpaceSymbols + '*';
    static final Pattern patternElement = Pattern.compile(regexElement);


    IntegerType parser;

    BasicCalculator(IntegerType parser) { this.parser = parser; }

    static class ExpressionFormatException extends IllegalArgumentException {
        public ExpressionFormatException(String s) { super(s); }
    }

    public ExpressionElement ExpressionElementFabric(String s) {
        try {
            switch (s.charAt(0)) {
                case '-':
                    return new OperatorMinus();
                case '+':
                    return new OperatorPlus();
                case ')':
                    return new OperatorCloseParenthesis();
                case '(':
                    if (s.length() > 1) {
                        assert s.charAt(s.length() - 1) == ')';
                        return new Operand(parser.from(s.substring(1, s.length() - 1)));
                    } else {
                        return new OperatorOpenParenthesis();
                    }
                default:
                    return new Operand(parser.from(s));
            }
        } catch(NumberFormatException exception) {
            throw new ExpressionFormatException("Bad parsing expression");
        }
    }

    abstract class ExpressionElement {
        public abstract String toString();
    }

    class Operand extends ExpressionElement {
        public IntegerType value;
        protected Operand(IntegerType value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return (value == null ? null : value.toString());
        }
    }

    abstract class Operator extends ExpressionElement {}

    class OperatorOpenParenthesis extends Operator {
        static char strRepresentation = '(';
        OperatorOpenParenthesis() {}

        @Override
        public String toString() {
            return Character.toString(strRepresentation);
        }
    }
    class OperatorCloseParenthesis extends Operator {
        static char strRepresentation = ')';
        OperatorCloseParenthesis() {}

        @Override
        public String toString() {
            return Character.toString(strRepresentation);
        }
    }

    abstract class ArithmeticOperator extends Operator {
        abstract Operand evaluate(Operand pos1, Operand pos2);
    }

    //TODO: ranks for operators and set down them in generateRPN
    class OperatorPlus extends ArithmeticOperator {
        static char strRepresentation = '+';
        OperatorPlus() {}
        @Override
        Operand evaluate(Operand pos1, Operand pos2) {
            return new Operand(pos1.value.add(pos2.value));
        }

        @Override
        public String toString() {
            return Character.toString(strRepresentation);
        }
    }
    class OperatorMinus extends ArithmeticOperator {
        static char strRepresentation = '-';
        OperatorMinus() {}
        @Override
        Operand evaluate(Operand pos1, Operand pos2) {
            return new Operand(pos1.value.subtract(pos2.value));
        }

        @Override
        public String toString() {
            return Character.toString(strRepresentation);
        }
    }
    class OperatorAsterisk extends ArithmeticOperator {
        static char strRepresentation = '*';
        @Override
        Operand evaluate(Operand pos1, Operand pos2) {
            return new Operand(pos1.value.multiply(pos2.value));
        }

        @Override
        public String toString() {
            return Character.toString(strRepresentation);
        }
    }

    Queue<ExpressionElement> generateExpression(String s) {
        if (!Pattern.matches(regexExpression, s))
            throw new ExpressionFormatException("No calculable expression");

        for (char c : spaceSymbols.toCharArray()) {
            s = s.replace(Character.toString(c), "");
        }

        if (s.charAt(0) == '-')
            s = '0' + s;

        Matcher matcher = patternElement.matcher(s);
        Queue<ExpressionElement> expression = new ArrayDeque<>();
        int prev_end = 0;
        while(matcher.find()) {
            int start = matcher.start();
            assert prev_end == start;
            int end = matcher.end();
            String part_expression = s.substring(start, end);
            expression.add(ExpressionElementFabric(part_expression));
        }
        return expression;
    }

    Queue<ExpressionElement> generateRPN(Queue<ExpressionElement> expression) {
        Queue<ExpressionElement> rpn = new ArrayDeque<>();
        Stack<Operator> operators = new Stack<>();
        while (!expression.isEmpty()) {
            if (expression.element() instanceof Operand) {
                rpn.add(expression.remove());
                if (!operators.empty() && operators.peek() instanceof ArithmeticOperator)
                    rpn.add(operators.pop());
            } else if (expression.element() instanceof OperatorOpenParenthesis) {
                operators.push((OperatorOpenParenthesis) expression.remove());
            } else if (expression.element() instanceof OperatorCloseParenthesis) {
                if (!(operators.peek() instanceof OperatorOpenParenthesis)) {
                    throw new ExpressionFormatException("Bad expression");
                } else {
                    expression.remove();
                    operators.pop();
                    if (!operators.empty() && operators.peek() instanceof ArithmeticOperator)
                        rpn.add(operators.pop());
                }
            } else if (expression.element() instanceof ArithmeticOperator) {
                operators.push((ArithmeticOperator) expression.remove());
            } else {
                throw new ExpressionFormatException("Bad expression");
            }
        }
        return rpn;
    }

    IntegerType calculateRPN(Queue<ExpressionElement> rpn) {
        if (rpn.isEmpty())
            return null;

        Stack<Operand> operands = new Stack<>();
        while (!rpn.isEmpty()) {
            if (rpn.element() instanceof Operand) {
                operands.push((Operand) rpn.remove());
            } else if (rpn.element() instanceof ArithmeticOperator) {
                Operand oper2 = operands.pop();
                Operand oper1 = operands.pop();
                operands.push(((ArithmeticOperator) rpn.remove()).evaluate(oper1, oper2));
            } else {
                throw new ExpressionFormatException("Bad RPN expression");
            }
        }
        if (operands.size() != 1) {
            throw new ExpressionFormatException("Bad RPN expression");
        }
        return operands.peek().value;
    }

    public IntegerType calculate(String s) {
        return calculateRPN(generateRPN(generateExpression(s)));
    }
}

