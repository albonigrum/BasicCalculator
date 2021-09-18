package BasicCalculator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class BasicCalculator {
    static final String spaceSymbols = " \t";
    static final String regexSpaceSymbols = "[" + spaceSymbols + "]";
    static final String binaryOperators = "()+*/-";
    static final String unaryOperators = "-";
    static final String regexBinaryOperator = "[" + binaryOperators + "]";
    static final String regexUnaryOperators = "[" + unaryOperators + "]";
    static final String regexNumber = "(?:[1-9]\\d*|0)(?:\\.\\d*[1-9])?";
    static final String regexToken = regexNumber + '|' + regexBinaryOperator + "|" + regexUnaryOperators;
    static final String regexExpression =
            '(' + regexSpaceSymbols + '*' + '(' + regexToken + ')' + '+' + regexSpaceSymbols + '*' + ')' + '+';
    static final Pattern patternElement = Pattern.compile(regexToken);
    static final Map<String, Operator> mapUnaryOperators = new HashMap<>();
    static final Map<String, Operator> mapBinaryOperators = new HashMap<>();
    static final Map<String, Operator> mapOperatorsParentheses = new HashMap<>();

    static {
        try {
            mapUnaryOperators.put("-",
                    new UnaryOperator(
                            RankOperator.UNARY_PLUS,
                            "-",
                            Operand.class.getDeclaredMethod("negate"))
            );
            mapBinaryOperators.put("-",
                    new BinaryOperator(
                            RankOperator.PLUS,
                            "-",
                            Operand.class.getDeclaredMethod("subtract", Operand.class))
            );
            mapBinaryOperators.put("+",
                    new BinaryOperator(
                            RankOperator.PLUS,
                            "+",
                            Operand.class.getDeclaredMethod("add", Operand.class))
            );
            mapBinaryOperators.put("*",
                    new BinaryOperator(
                            RankOperator.ASTERISK,
                            "*",
                            Operand.class.getDeclaredMethod("multiply", Operand.class))
            );
            mapBinaryOperators.put("/",
                    new BinaryOperator(
                            RankOperator.ASTERISK,
                            "/",
                            Operand.class.getDeclaredMethod("divide", Operand.class))
            );
            mapOperatorsParentheses.put("(", new OperatorParenthesis("(", true));
            mapOperatorsParentheses.put(")", new OperatorParenthesis(")", false));
        } catch (NoSuchMethodException e) {
            throw new ReflectAPIUsingException("No found method. Method: " + e);
        }
    }

    boolean INTEGER_MODE_FLAG;

    static class ExpressionFormatException extends IllegalArgumentException {
        public ExpressionFormatException(String s) {
            super(s);
        }
    }

    static class ReflectAPIUsingException extends RuntimeException {
        public ReflectAPIUsingException(String s) {
            super(s);
        }
    }

    public Token TokenFabric(String s, Token last) {
        try {
            boolean isParenthesis = mapOperatorsParentheses.containsKey(s);
            boolean isUnary = mapUnaryOperators.containsKey(s);
            boolean isBinary = mapBinaryOperators.containsKey(s);
            if (isParenthesis)
                return mapOperatorsParentheses.get(s);
            if (isBinary && !isUnary)
                return mapBinaryOperators.get(s);
            if (isUnary && !isBinary)
                return mapUnaryOperators.get(s);
            if (isUnary && isBinary){
                if (last != null && !(last instanceof OperatorParenthesis && ((OperatorParenthesis) last).isOpen))
                    return mapBinaryOperators.get(s);
                else
                    return mapUnaryOperators.get(s);
            }
            if ('0' <= s.charAt(0) && s.charAt(0) <= '9')
                return new Operand(s, INTEGER_MODE_FLAG);
            throw new ExpressionFormatException("Invalid expression token");
        } catch (NumberFormatException exception) {
            throw new ExpressionFormatException("Bad parsing number in expression: " + exception.getMessage());
        }
    }

    static abstract class Token {
        public abstract String toString();
    }

    private static class Value {
        public final boolean isInteger;
        public final Double fValue;
        public final Long iValue;

        private Value(Double fValue, Long iValue) {
            if (iValue == null && fValue == null)
                throw new IllegalArgumentException("Value cannot be null");
            assert iValue == null || fValue == null;

            isInteger = iValue != null;

            this.fValue = fValue;
            this.iValue = iValue;
        }

        public Value(Long iValue) {
            this(null, iValue);
        }
        public Value(Double fValue) {
            this(fValue, null);
        }

        public Value(String s, boolean isInteger) {
            this((!isInteger ? Double.parseDouble(s) : null), (isInteger ? Long.parseLong(s) : null));
        }

        private void isSameType(Value value) {
            if (isInteger != value.isInteger)
                throw new IllegalArgumentException(this + " not same type with " + value);
        }

        Value add(Value value) {
            isSameType(value);
            if (isInteger)
                return new Value(iValue + value.iValue);
            else
                return new Value(fValue + value.fValue);
        }

        Value subtract(Value value) {
            isSameType(value);
            if (isInteger)
                return new Value(iValue - value.iValue);
            else
                return new Value(fValue - value.fValue);
        }

        Value multiply(Value value) {
            isSameType(value);
            if (isInteger)
                return new Value(iValue * value.iValue);
            else
                return new Value(fValue * value.fValue);
        }

        Value divide(Value value) {
            isSameType(value);
            if (isInteger)
                return new Value(iValue / value.iValue);
            else
                return new Value(fValue / value.fValue);
        }

        Value negate() {
            if (isInteger)
                return new Value(-iValue);
            else
                return new Value(-fValue);
        }

        @Override
        public String toString() {
            return (isInteger ? iValue.toString() : fValue.toString());
        }
    }

    static class Operand extends Token {
        public Value value;

        Operand(Value value) {
            this.value = value;
        }

        Operand(String s, boolean isInteger) {
            value = new Value(s, isInteger);
        }

        Operand add(Operand operand) {
            return new Operand(value.add(operand.value));
        }

        Operand subtract(Operand operand) {
            return new Operand(value.subtract(operand.value));
        }

        Operand multiply(Operand operand) {
            return new Operand(value.multiply(operand.value));
        }

        Operand divide(Operand operand) {
            return new Operand(value.divide(operand.value));
        }

        Operand negate() {
            return new Operand(value.negate());
        }

        @Override
        public String toString() {
            return value.toString();
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

    static abstract class Operator extends Token {
        final RankOperator rankOperator;
        final String strRepresentation;

        Operator(RankOperator rankOperator, String strRepresentation) {
            this.rankOperator = rankOperator;
            this.strRepresentation = strRepresentation;
        }

        RankOperator getRank() {
            return rankOperator;
        }
        public String toString() {
            return strRepresentation;
        }
    }

    static class OperatorParenthesis extends Operator {
        final boolean isOpen;
        OperatorParenthesis(String strRepresentation, boolean isOpen) {
            super(RankOperator.PARENTHESES, strRepresentation);
            this.isOpen = isOpen;
        }
    }

    static abstract class ArithmeticOperator extends Operator {
        final Method evaluateMethod;

        ArithmeticOperator(RankOperator rankOperator, String strRepresentation, Method evaluateMethod) {
            super(rankOperator, strRepresentation);
            this.evaluateMethod = evaluateMethod;
        }
    }

    static class UnaryOperator extends ArithmeticOperator {
        UnaryOperator(RankOperator rankOperator, String strRepresentation, Method evaluateMethod) {
            super(rankOperator, strRepresentation, evaluateMethod);
        }

        Operand evaluate(Operand operand) {
            try {
                return (Operand) evaluateMethod.invoke(operand);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                throw new ReflectAPIUsingException("Bad method invoke. Method: " + evaluateMethod);
            }
        }
    }

    static class BinaryOperator extends ArithmeticOperator {
        BinaryOperator(RankOperator rankOperator, String strRepresentation, Method evaluateMethod) {
            super(rankOperator, strRepresentation, evaluateMethod);
        }
        Operand evaluate(Operand operand1, Operand operand2) {
            try {
                return (Operand) evaluateMethod.invoke(operand1, operand2);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                throw new ReflectAPIUsingException("Bad method invoke. Method: " + evaluateMethod);
            }
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
            } else if (curElem instanceof OperatorParenthesis) {
                if (((OperatorParenthesis) curElem).isOpen) {
                    operators.push((Operator) curElem);
                } else {
                    while (!operators.empty()) {
                        Operator curOperator = operators.peek();
                        boolean isCurOperatorOpenParenthesis =
                                curOperator instanceof OperatorParenthesis && ((OperatorParenthesis) curOperator).isOpen;
                        assert !(curOperator instanceof OperatorParenthesis && !isCurOperatorOpenParenthesis);
                        if (isCurOperatorOpenParenthesis)
                            break;
                        rpn.add(operators.pop());
                    }
                    if (operators.empty())
                        throw new ExpressionFormatException("Invalid parentheses sequence");
                    operators.pop();
                }
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
            if (!(operators.peek() instanceof ArithmeticOperator))
                throw new ExpressionFormatException("Invalid operator " + operators.peek());
            rpn.add(operators.pop());
        }
        return rpn;
    }

    Value calculateRPN(Queue<Token> rpn) {
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

    private Value calculateValue(String s) {
        return calculateRPN(generateRPN(generateExpression(s)));
    }

    public double calculateFloat(String s) {
        INTEGER_MODE_FLAG = false;
        return calculateValue(s).fValue;
    }

    public long calculateInt(String s) {
        INTEGER_MODE_FLAG = true;
        return calculateValue(s).iValue;
    }
}

