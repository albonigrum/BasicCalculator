package BasicCalculator;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

record BasicCalculator<NumberType extends Evaluable<NumberType>>(NumberType parser) {
    static final String spaceSymbols = " \t";
    static final String regexSpaceSymbols = "[" + spaceSymbols + "]";
    //operator '-' must be last in string.
    static final String binaryOperators = "()+*/-";
    static final String unaryOperators = "-";
    static final String regexBinaryOperator = "[" + binaryOperators + "]";
    static final String regexUnaryOperators = "[" + unaryOperators + "]";
    static final String regexNumber = "[1-9]\\d*" + "|" + "0\\.\\d*[1-9]" + "|" + "0";
    static final String regexToken = regexNumber + '|' + regexBinaryOperator + "|" + regexUnaryOperators;
    static final String regexExpression =
            '(' + regexSpaceSymbols + '*' + '(' + regexToken + ')' + '+' + regexSpaceSymbols + '*' + ')' + '+';
    static final Pattern patternElement = Pattern.compile(regexToken);


    static class ExpressionFormatException extends IllegalArgumentException {
        public ExpressionFormatException(String s) {
            super(s);
        }
    }

    public Token TokenFabric(String s, Token last) {
        try {
            switch (s.charAt(0)) {
                //TODO: rewrite with array operators
                case '-':
                    if (last == null || last instanceof OperatorOpenParenthesis)
                        return new OperatorUnaryMinus();
                    else
                        return new OperatorMinus();
                case '+':
                    return new OperatorPlus();
                case '*':
                    return new OperatorAsterisk();
                case '/':
                    return new OperatorSlash();
                case ')':
                    return new OperatorCloseParenthesis();
                case '(':
                    return new OperatorOpenParenthesis();
                default:
                    if ('0' <= s.charAt(0) && s.charAt(0) <= '9')
                        return new Operand(parser.from(s));
                    throw new ExpressionFormatException("Invalid expression token");
            }
        } catch (NumberFormatException exception) {
            throw new ExpressionFormatException("Bad parsing number in expression: " + exception.getMessage());
        }
    }

    abstract class Token {
        public abstract String toString();
    }

    class Operand extends Token {
        public NumberType value;

        protected Operand(NumberType value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return (value == null ? null : value.toString());
        }
    }

    enum RankOperator {
        PARENTHESES(0),
        PLUS(1),
        ASTERISK(2),
        UNARY_PLUS(3);

        final int rank;

        RankOperator(int rank) {
            this.rank = rank;
        }
    }

    abstract class Operator extends Token {
        abstract RankOperator getRank();
    }

    class OperatorOpenParenthesis extends Operator {
        static char strRepresentation = '(';
        static RankOperator rankOperator = RankOperator.PARENTHESES;

        @Override
        public String toString() {
            return Character.toString(strRepresentation);
        }

        @Override
        RankOperator getRank() {
            return rankOperator;
        }
    }

    class OperatorCloseParenthesis extends Operator {
        static char strRepresentation = ')';
        static RankOperator rankOperator = RankOperator.PARENTHESES;

        @Override
        public String toString() {
            return Character.toString(strRepresentation);
        }

        @Override
        RankOperator getRank() {
            return rankOperator;
        }
    }

    abstract class ArithmeticOperator extends Operator {}

    abstract class UnaryOperator extends ArithmeticOperator {
        abstract Operand evaluate(Operand operand);
    }

    class OperatorUnaryMinus extends UnaryOperator {
        static final char strRepresentation = '-';
        static final RankOperator rankOperator = RankOperator.UNARY_PLUS;

        @Override
        RankOperator getRank() {
            return rankOperator;
        }

        @Override
        Operand evaluate(Operand operand) {
            return new Operand(operand.value.negate());
        }

        @Override
        public String toString() {
            return Character.toString(strRepresentation);
        }
    }

    abstract class BinaryOperator extends ArithmeticOperator {
        abstract Operand evaluate(Operand first, Operand second);
    }

    class OperatorPlus extends BinaryOperator {
        static char strRepresentation = '+';
        static RankOperator rankOperator = RankOperator.PLUS;

        @Override
        Operand evaluate(Operand first, Operand second) {
            return new Operand(first.value.add(second.value));
        }

        @Override
        public String toString() {
            return Character.toString(strRepresentation);
        }

        @Override
        RankOperator getRank() {
            return rankOperator;
        }
    }

    class OperatorMinus extends BinaryOperator {
        static char strRepresentation = '-';
        static RankOperator rankOperator = RankOperator.PLUS;

        @Override
        Operand evaluate(Operand first, Operand second) {
            return new Operand(first.value.subtract(second.value));
        }

        @Override
        public String toString() {
            return Character.toString(strRepresentation);
        }

        @Override
        RankOperator getRank() {
            return rankOperator;
        }
    }

    class OperatorAsterisk extends BinaryOperator {
        static char strRepresentation = '*';
        static RankOperator rankOperator = RankOperator.ASTERISK;

        @Override
        Operand evaluate(Operand first, Operand second) {
            return new Operand(first.value.multiply(second.value));
        }

        @Override
        public String toString() {
            return Character.toString(strRepresentation);
        }

        @Override
        RankOperator getRank() {
            return rankOperator;
        }
    }

    class OperatorSlash extends BinaryOperator {
        static char strRepresentation = '/';
        static RankOperator rankOperator = RankOperator.ASTERISK;

        @Override
        Operand evaluate(Operand first, Operand second) {
            return new Operand(first.value.divide(second.value));
        }

        @Override
        public String toString() {
            return Character.toString(strRepresentation);
        }

        @Override
        RankOperator getRank() {
            return rankOperator;
        }
    }

    Queue<Token> generateExpression(String s) {
        if (!Pattern.matches(regexExpression, s))
            throw new ExpressionFormatException("No calculable expression");

        for (char c : spaceSymbols.toCharArray())
            s = s.replace(Character.toString(c), "");

        Matcher matcher = patternElement.matcher(s);
        Queue<Token> expression = new ArrayDeque<>();
        Token last = null;
        int prevEnd = 0;
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            assert prevEnd == start;

            Token temp = TokenFabric(s.substring(start, end), last);
            expression.add(temp);

            last = temp;
            prevEnd = end;
        }
        return expression;
    }

    Queue<Token> generateRPN(Queue<Token> expression) {
        Queue<Token> rpn = new ArrayDeque<>();
        Stack<Operator> operators = new Stack<>();
        while (!expression.isEmpty()) {
            var curElem = expression.remove();
            if (curElem instanceof Operand) {
                rpn.add(curElem);
            } else if (curElem instanceof OperatorOpenParenthesis) {
                operators.push((Operator) curElem);
            } else if (curElem instanceof OperatorCloseParenthesis) {
                while (!operators.empty() && !(operators.peek() instanceof OperatorOpenParenthesis))
                    rpn.add(operators.pop());
                if (operators.empty())
                    throw new ExpressionFormatException("Invalid parentheses sequence");
                operators.pop();
            } else if (curElem instanceof ArithmeticOperator) {
                Operator curOperator = (Operator) curElem;
                while (!operators.empty() && operators.peek().getRank().rank >= curOperator.getRank().rank)
                    rpn.add(operators.pop());
                operators.push(curOperator);
            } else {
                throw new ExpressionFormatException("Invalid token: " + curElem.toString());
            }
        }
        while (!operators.empty()) {
            if (operators.peek() instanceof OperatorOpenParenthesis)
                throw new ExpressionFormatException("Invalid parentheses sequence");
            rpn.add(operators.pop());
        }
        return rpn;
    }

    NumberType calculateRPN(Queue<Token> rpn) {
        Stack<Operand> operands = new Stack<>();
        while (!rpn.isEmpty()) {
            var curElem = rpn.remove();
            if (curElem instanceof Operand) {
                operands.push((Operand) curElem);
            } else if (curElem instanceof BinaryOperator) {
                Operand operand1, operand2;
                try {
                    operand2 = operands.pop();
                    operand1 = operands.pop();
                } catch (EmptyStackException exception) {
                    throw new ExpressionFormatException("Invalid RPN");
                }
                operands.push(((BinaryOperator) curElem).evaluate(operand1, operand2));
            } else if (curElem instanceof UnaryOperator) {
                Operand operand;
                try {
                    operand = operands.pop();
                } catch (EmptyStackException exception) {
                    throw new ExpressionFormatException("Invalid RPN");
                }
                operands.push(((UnaryOperator) curElem).evaluate(operand));
            } else {
                assert false;
            }
        }
        if (operands.size() != 1)
            throw new ExpressionFormatException("Invalid RPN");

        return operands.peek().value;
    }

    public NumberType calculate(String s) {
        return calculateRPN(generateRPN(generateExpression(s)));
    }
}

