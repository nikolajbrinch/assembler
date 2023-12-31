package dk.nikolajbrinch.assembler.compiler;

import dk.nikolajbrinch.assembler.ast.expressions.AddressExpression;
import dk.nikolajbrinch.assembler.ast.expressions.AssignExpression;
import dk.nikolajbrinch.assembler.ast.expressions.BinaryExpression;
import dk.nikolajbrinch.assembler.ast.expressions.ConditionExpression;
import dk.nikolajbrinch.assembler.ast.expressions.Expression;
import dk.nikolajbrinch.assembler.ast.expressions.ExpressionVisitor;
import dk.nikolajbrinch.assembler.ast.expressions.GroupingExpression;
import dk.nikolajbrinch.assembler.ast.expressions.IdentifierExpression;
import dk.nikolajbrinch.assembler.ast.expressions.LiteralExpression;
import dk.nikolajbrinch.assembler.ast.expressions.RegisterExpression;
import dk.nikolajbrinch.assembler.ast.expressions.UnaryExpression;
import dk.nikolajbrinch.assembler.compiler.values.BinaryMath;
import dk.nikolajbrinch.assembler.compiler.values.IntegerMath;
import dk.nikolajbrinch.assembler.compiler.values.Logic;
import dk.nikolajbrinch.assembler.compiler.values.NumberValue;
import dk.nikolajbrinch.assembler.compiler.values.StringValue;
import dk.nikolajbrinch.assembler.parser.Environment;
import dk.nikolajbrinch.assembler.scanner.AssemblerTokenType;

public class ExpressionEvaluator implements ExpressionVisitor<Object> {

  private Environment environment;
  private Address currentAddress;

  public Object visitBinaryExpression(BinaryExpression expression) {
    Object left = evaluate(expression.left());
    Object right = evaluate(expression.right());

    return switch (expression.operator().type()) {
      case PLUS -> IntegerMath.add(left, right);
      case MINUS -> IntegerMath.subtract(left, right);
      case STAR -> IntegerMath.multiply(left, right);
      case SLASH -> IntegerMath.divide(left, right);
      case EQUAL_EQUAL -> Logic.compare(left, right);
      case AND -> BinaryMath.and(left, right);
      case AND_AND -> Logic.and(left, right);
      case PIPE -> BinaryMath.or(left, right);
      case PIPE_PIPE -> Logic.or(left, right);
      case CARET -> BinaryMath.xor(left, right);
      case GREATER_GREATER -> BinaryMath.shiftRight(left, right);
      case LESS_LESS -> BinaryMath.shiftLeft(left, right);
      case GREATER_GREATER_GREATER -> Logic.shiftRight(left, right);
      default -> throw new IllegalStateException("Unknown binary expression");
    };
  }

  @Override
  public Object visitUnaryExpression(UnaryExpression expression) {
    Object value = evaluate(expression.expression());

    return switch (expression.operator().type()) {
      case MINUS -> IntegerMath.negate(value);
      case PLUS -> value;
      case BANG -> Logic.not(value);
      case TILDE -> BinaryMath.not(value);
      default -> throw new IllegalStateException("Unknown unary expression");
    };
  }

  @Override
  public Object visitGroupingExpression(GroupingExpression expression) {
    return expression.expression().accept(this);
  }

  @Override
  public Object visitLiteralExpression(LiteralExpression expression) {
    return switch (expression.token().type()) {
      case DECIMAL_NUMBER, HEX_NUMBER, OCTAL_NUMBER, BINARY_NUMBER -> NumberValue.create(
          expression.token());
      case STRING, CHAR -> StringValue.create(expression.token());
      default -> throw new IllegalStateException("Unknown literal expression");
    };
  }

  @Override
  public Object visitIdentifierExpression(IdentifierExpression expression) {
    return environment.get(expression.token().text());
  }

  @Override
  public Void visitAssignExpression(AssignExpression expression) {
    environment.assign(expression.identifier().text(), evaluate(expression.expression()));

    throw new IllegalStateException("Unknown assign expression");
  }

  @Override
  public Object visitRegisterExpression(RegisterExpression expression) {
    return expression.register();
  }

  @Override
  public Object visitConditionExpression(ConditionExpression conditionExpression) {
    return conditionExpression.condition();
  }

  @Override
  public Object visitAddressExpression(AddressExpression expression) {
    if (expression.token().type() == AssemblerTokenType.DOLLAR) {
      return currentAddress.logicalAddress();
    }

    if (expression.token().type() == AssemblerTokenType.DOLLAR_DOLLAR) {
      return currentAddress.physicalAddress();
    }

    throw new IllegalStateException("Unknown address expression");
  }

  private Object evaluate(Expression expression) {
    return expression.accept(this);
  }

  public Object evaluate(
      Expression expression, Environment environment, Address currentAddress) {
    this.environment = environment;
    this.currentAddress = currentAddress;

    return expression.accept(this);
  }
}
