package dk.nikolajbrinch.assembler.parser.statements;

import dk.nikolajbrinch.assembler.scanner.Token;

public record GlobalStatement(Token identifier) implements Statement {

  @Override
  public <R> R accept(StatementVisitor<R> visitor) {
    return visitor.visitGlobalStatement(this);
  }
}
