package dk.nikolajbrinch.assembler.ast.expressions;

public interface ExpressionVisitor<R> {

  R visitBinaryExpression(BinaryExpression expression);

  R visitUnaryExpression(UnaryExpression expression);

  R visitGroupingExpression(GroupingExpression expression);

  R visitLiteralExpression(LiteralExpression expression);

  R visitIdentifierExpression(IdentifierExpression expression);

  R visitAddressExpression(AddressExpression addressExpression);

  R visitAssignExpression(AssignExpression assignExpression);

  R visitRegisterExpression(RegisterExpression registerExpression);

  R visitConditionExpression(ConditionExpression conditionExpression);
}
