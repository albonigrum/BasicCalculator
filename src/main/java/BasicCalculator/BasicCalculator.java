package BasicCalculator;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class BasicCalculator <IntegerType extends Evaluable<IntegerType>> {
    IntegerType parser;

    BasicCalculator(IntegerType parser) { this.parser = parser; }

    static class ExpressionFormatException extends IllegalArgumentException {
        public ExpressionFormatException(String s) { super(s); }
    }

    public ExpressionElement ExpressionElementFabric(String expression) {
        try {
            switch (expression.charAt(0)) {
                case '-':
                    return new OperatorMinus();
                case '+':
                    return new OperatorPlus();
                case ')':
                    return new OperatorCloseParenthesis();
                case '(':
                    if (expression.length() > 1) {
                        if (expression.charAt(expression.length() - 1) != ')')
                            throw new NumberFormatException();
                        else
                            expression = expression.substring(1, expression.length() - 1);
                        return new Operand(parser.from(expression));
                    } else {
                        return new OperatorOpenParenthesis();
                    }
                default:
                    return new Operand(parser.from(expression));
            }
        } catch(NumberFormatException exception) {
            throw new BasicCalculator.BasicCalculator.ExpressionFormatException("Bad parsing expression");
        }
    }

    abstract class ExpressionElement {}

    class Operand extends ExpressionElement {
        public IntegerType value;
        protected Operand(IntegerType value) {
            this.value = value;
        }
    }

    abstract class Operator extends ExpressionElement {}

    class OperatorOpenParenthesis extends Operator {
        OperatorOpenParenthesis() {}
    }
    class OperatorCloseParenthesis extends Operator {
        OperatorCloseParenthesis() {}
    }

    abstract class ArithmeticOperator extends Operator {
        abstract Operand evaluate(Operand pos1, Operand pos2);
    }

    class OperatorPlus extends ArithmeticOperator {
        OperatorPlus() {}
        @Override
        Operand evaluate(Operand pos1, Operand pos2) {
            return new Operand(pos1.value.add(pos2.value));
        }
    }
    class OperatorMinus extends ArithmeticOperator {
        OperatorMinus() {}
        @Override
        Operand evaluate(Operand pos1, Operand pos2) {
            return new Operand(pos1.value.subtract(pos2.value));
        }
    }

    Queue<ExpressionElement> generateExpression(String s) {
        s = s.replace(" ", "");
        if (s.charAt(0) == '-') {
            s = '0' + s;
        }
        Pattern pattern = Pattern.compile("\\(-[1-9]\\d*\\)|[1-9]\\d*|0|[()+-]");
        Matcher matcher = pattern.matcher(s);
        Queue<ExpressionElement> expression = new ArrayDeque<>();
        while(matcher.find()) {
            int start = matcher.start();
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
                    throw new IllegalArgumentException("Bad expression");
                } else {
                    expression.remove();
                    operators.pop();
                    if (!operators.empty() && operators.peek() instanceof ArithmeticOperator)
                        rpn.add(operators.pop());
                }
            } else if (expression.element() instanceof ArithmeticOperator) {
                operators.push((ArithmeticOperator) expression.remove());
            } else {
                throw new IllegalArgumentException("Bad expression");
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
                throw new IllegalArgumentException("Bad RPN expression");
            }
        }
        if (operands.size() != 1) {
            throw new IllegalArgumentException("Bad RPN expression");
        }
        return operands.peek().value;
    }

    public IntegerType calculate(String s) {
        return calculateRPN(generateRPN(generateExpression(s)));
    }
}

