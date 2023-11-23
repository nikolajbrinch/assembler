package dk.nikolajbrinch.assembler.ast.statements;

import dk.nikolajbrinch.assembler.scanner.AssemblerToken;
import java.util.List;

public record MacroCallStatement(AssemblerToken name, List<Statement> arguments)
    implements Statement {

  @Override
  public <R> R accept(StatementVisitor<R> visitor) {
    return visitor.visitMacroCallStatement(this);
  }
}
