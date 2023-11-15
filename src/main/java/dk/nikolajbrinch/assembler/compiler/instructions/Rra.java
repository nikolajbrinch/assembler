package dk.nikolajbrinch.assembler.compiler.instructions;

import dk.nikolajbrinch.assembler.compiler.ByteSource;
import dk.nikolajbrinch.assembler.compiler.values.NumberValue;

public class Rra implements InstructionGenerator {

  @Override
  public ByteSource generate(NumberValue currentAddress) {
    return ByteSource.of(0x1F);
  }
}
