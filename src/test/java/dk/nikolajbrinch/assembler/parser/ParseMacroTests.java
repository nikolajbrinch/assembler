package dk.nikolajbrinch.assembler.parser;

import dk.nikolajbrinch.assembler.ast.statements.Statement;
import dk.nikolajbrinch.assembler.compiler.Compiler;
import dk.nikolajbrinch.assembler.compiler.ExpressionEvaluator;
import dk.nikolajbrinch.assembler.compiler.MacroResolver;
import dk.nikolajbrinch.assembler.scanner.AssemblerScanner;
import dk.nikolajbrinch.assembler.util.AstPrinter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class ParseMacroTests {

//  @Disabled
  @Test
  void testParse() throws IOException {
    try (ByteArrayInputStream inputStream =
            new ByteArrayInputStream(
                """
            mx = 0
            .macro macro1 param1=0, param2=4, param3='\\0'



            L:
            ld a, param1
            ld b, param2
            ld c, param3
            .endm
            mx = mx + 1

            macro1(1, 2, 4+3)
            """
                    .getBytes(StandardCharsets.UTF_8));
        AssemblerScanner scanner = new AssemblerScanner(inputStream)) {

      List<Statement> statements = new AssemblerParser(scanner).parse();

      for (Statement statement : statements) {
        System.out.println(new AstPrinter().print(statement));
      }

      System.out.println("-----");

      List<Statement> resolved = new MacroResolver(new ExpressionEvaluator()).resolve(statements);
      for (Statement statement : resolved) {
        System.out.println(new AstPrinter().print(statement));
      }

      System.out.println("-----");

      new Compiler().compile(statements);
    }
  }
}
