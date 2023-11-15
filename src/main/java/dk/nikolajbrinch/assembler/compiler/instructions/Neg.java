package dk.nikolajbrinch.assembler.compiler.instructions;

import dk.nikolajbrinch.assembler.compiler.ByteSource;
import dk.nikolajbrinch.assembler.compiler.NumberValue;

public class Neg implements InstructionGenerator {

  @Override
  public ByteSource generate(NumberValue currentAddress) {
    return ByteSource.of(0xED, 0x44);
  }
}
