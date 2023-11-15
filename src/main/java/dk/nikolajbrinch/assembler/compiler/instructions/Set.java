package dk.nikolajbrinch.assembler.compiler.instructions;

import dk.nikolajbrinch.assembler.compiler.ByteSource;
import dk.nikolajbrinch.assembler.compiler.NumberValue;
import dk.nikolajbrinch.assembler.compiler.operands.Operand;
import dk.nikolajbrinch.assembler.compiler.operands.Registers;
import dk.nikolajbrinch.assembler.parser.Register;

public class Set implements InstructionGenerator {

  /**
   * SET b, m
   *
   * @param currentAddress
   * @param numberValue
   * @param register
   * @return
   */
  @Override
  public ByteSource generateRegisterToImmediate(
      NumberValue currentAddress, NumberValue numberValue, Register register) {
    return ByteSource.of(
        0xCB, 0b11000000 | ((numberValue.value() & 0b00000111) << 3) | Registers.r.get(register));
  }

  /**
   * SET b, (HL)
   *
   * @param currentAddress
   * @param numberValue
   * @param register
   * @return
   */
  @Override
  public ByteSource generateRegisterIndirectToImmediate(
      NumberValue currentAddress, NumberValue numberValue, Register register) {
    if (register == Register.HL) {
      return ByteSource.of(0xCB, 0b11000110 | ((numberValue.value() & 0b00000111) << 3));
    }

    return null;
  }

  /**
   * SET b, (IX+d), SET b, (IY+d)
   *
   * @param currentAddress
   * @param numberValue
   * @param sourceIndex
   * @return
   */
  @Override
  public ByteSource generateIndexedToImmediate(
      NumberValue currentAddress, NumberValue numberValue, Operand sourceIndex) {
    Register sourceRegister = sourceIndex.asRegister();

    if (sourceRegister == Register.IX) {
      return ByteSource.of(
          0xDD,
          0xCB,
          sourceIndex.displacementD(),
          0b11000110 | ((numberValue.value() & 0b00000111) << 3));
    } else if (sourceRegister == Register.IY) {
      return ByteSource.of(
          0xFD,
          0xCB,
          sourceIndex.displacementD(),
          0b11000110 | ((numberValue.value() & 0b00000111) << 3));
    }

    return null;
  }
}