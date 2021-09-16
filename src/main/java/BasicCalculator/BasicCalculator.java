package BasicCalculator;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class BasicCalculator <NumberType extends Evaluable<NumberType>> {
    static final String spaceSymbols = " \t";
    static final String regexSpaceSymbols = "[" + spaceSymbols + "]";
    //operator '-' must be last in string.
    static final String operators = "()+*/-";
    static final String regexOperator = "[" + operators + "]";
    //TODO: rewrite logic for unary operators
    static final String regexNumber = "\\(-[1-9]\\d*\\)|[1-9]\\d*|\\(-0\\.\\d*[1-9]\\)|0\\.\\d*[1-9]|0";
    static final String regexToken = regexNumber + '|' + regexOperator;
    static final String regexExpression =
            '(' + regexSpaceSymbols + '*' + '(' + regexToken + ')' + '+' + regexSpaceSymbols + '*' + ')' + '+';
    static final Pattern patternElement = Pattern.compile(regexToken);


    NumberType parser;

    BasicCalculator(NumberType parser) { this.parser = parser; }


    static class ExpressionFormatException extends IllegalArgumentException {
        public ExpressionFormatException(String s) { super(s); }
    }

    public Token TokenFabric(String s) {
        try {
            switch (s.charAt(0)) {
                case '-':
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
                    if (s.length() > 1) {
                        assert s.charAt(s.length() - 1) == ')';
                        //TODO:rewrite to unary operators
                        assert s.charAt(1) == '-';
                        return new Operand(parser.from(s.substring(2, s.length() - 1)).negate());
                    } else {
                        return new OperatorOpenParenthesis();
                    }
                default:
                    return new Operand(parser.from(s));
            }
        } catch(NumberFormatException exception) {
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

    enum OperatorRank {
        PARENTHESES(0),
        PLUS_MINUS(1),
        ASTERISK(2);

        final int rank;

        OperatorRank(int rank) {
            this.rank = rank;
        }
    }

    abstract class Operator extends Token {
        abstract OperatorRank getRank();
    }

    class OperatorOpenParenthesis extends Operator {
        static char strRepresentation = '(';
        static OperatorRank operatorRank = OperatorRank.PARENTHESES;
        OperatorOpenParenthesis() {}

        @Override
        public String toString() {
            return Character.toString(strRepresentation);
        }

        @Override
        OperatorRank getRank() {
            return operatorRank;
        }
    }
    class OperatorCloseParenthesis extends Operator {
        static char strRepresentation = ')';
        static OperatorRank operatorRank = OperatorRank.PARENTHESES;
        OperatorCloseParenthesis() {}

        @Override
        public String toString() {
            return Character.toString(strRepresentation);
        }

        @Override
        OperatorRank getRank() {
            return operatorRank;
        }
    }

    abstract class ArithmeticOperator extends Operator {
        abstract Operand evaluate(Operand pos1, Operand pos2);
    }

    class OperatorPlus extends ArithmeticOperator {
        static char strRepresentation = '+';
        static OperatorRank operatorRank = OperatorRank.PLUS_MINUS;
        OperatorPlus() {}
        @Override
        Operand evaluate(Operand pos1, Operand pos2) {
            return new Operand(pos1.value.add(pos2.value));
        }

        @Override
        public String toString() {
            return Character.toString(strRepresentation);
        }

        @Override
        OperatorRank getRank() {
            return operatorRank;
        }
    }
    class OperatorMinus extends ArithmeticOperator {
        static char strRepresentation = '-';
        static OperatorRank operatorRank = OperatorRank.PLUS_MINUS;
        OperatorMinus() {}
        @Override
        Operand evaluate(Operand pos1, Operand pos2) {
            return new Operand(pos1.value.subtract(pos2.value));
        }

        @Override
        public String toString() {
            return Character.toString(strRepresentation);
        }

        @Override
        OperatorRank getRank() {
            return operatorRank;
        }
    }
    class OperatorAsterisk extends ArithmeticOperator {
        static char strRepresentation = '*';
        static OperatorRank operatorRank = OperatorRank.ASTERISK;
        @Override
        Operand evaluate(Operand pos1, Operand pos2) {
            return new Operand(pos1.value.multiply(pos2.value));
        }

        @Override
        public String toString() {
            return Character.toString(strRepresentation);
        }

        @Override
        OperatorRank getRank() {
            return operatorRank;
        }
    }
    class OperatorSlash extends ArithmeticOperator {
        static char strRepresentation = '/';
        static OperatorRank operatorRank = OperatorRank.ASTERISK;
        @Override
        Operand evaluate(Operand pos1, Operand pos2) {
            return new Operand(pos1.value.divide(pos2.value));
        }

        @Override
        public String toString() {
            return Character.toString(strRepresentation);
        }

        @Override
        OperatorRank getRank() {
            return operatorRank;
        }
    }

    Queue<Token> generateExpression(String s) {
        if (!Pattern.matches(regexExpression, s))
            throw new ExpressionFormatException("No calculable expression");

        for (char c : spaceSymbols.toCharArray())
            s = s.replace(Character.toString(c), "");

        //TODO: rewrite with unary operators
        if (s.charAt(0) == '-')
            s = '0' + s;

        Matcher matcher = patternElement.matcher(s);
        Queue<Token> expression = new ArrayDeque<>();
        int prev_end = 0;
        while(matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            assert prev_end == start;

            String part_expression = s.substring(start, end);
            expression.add(TokenFabric(part_expression));

            prev_end = end;
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
                    throw new ExpressionFormatException("Bad parentheses");
                operators.pop();
            } else if (curElem instanceof ArithmeticOperator) {
                Operator curOperator = (Operator) curElem;
                while (!operators.empty() && operators.peek().getRank().rank >= curOperator.getRank().rank)
                    rpn.add(operators.pop());
                operators.push(curOperator);
            } else {
                assert false;
            }
        }
        while (!operators.empty()) {
            if (operators.peek() instanceof OperatorOpenParenthesis)
                throw new ExpressionFormatException("Bad parentheses");
            rpn.add(operators.pop());
        }
        return rpn;
    }

    NumberType calculateRPN(Queue<Token> rpn) {
        if (rpn.isEmpty())
            return null;

        Stack<Operand> operands = new Stack<>();
        while (!rpn.isEmpty()) {
            var curElem = rpn.remove();
            if (curElem instanceof Operand) {
                operands.push((Operand) curElem);
            } else if (curElem instanceof ArithmeticOperator) {
                Operand oper2 = operands.pop();
                Operand oper1 = operands.pop();
                operands.push(((ArithmeticOperator) curElem).evaluate(oper1, oper2));
            } else {
                assert false;
            }
        }
        assert operands.size() == 1;

        return operands.peek().value;
    }

    public NumberType calculate(String s) {
        return calculateRPN(generateRPN(generateExpression(s)));
    }
}

