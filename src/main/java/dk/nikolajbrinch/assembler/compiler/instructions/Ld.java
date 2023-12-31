package dk.nikolajbrinch.assembler.compiler.instructions;

import dk.nikolajbrinch.assembler.compiler.Address;
import dk.nikolajbrinch.assembler.compiler.ByteSource;
import dk.nikolajbrinch.assembler.compiler.IllegalSizeException;
import dk.nikolajbrinch.assembler.compiler.operands.Registers;
import dk.nikolajbrinch.assembler.compiler.values.NumberValue;
import dk.nikolajbrinch.assembler.compiler.values.NumberValue.Size;
import dk.nikolajbrinch.assembler.parser.Register;

public class Ld implements InstructionGenerator {

  @Override
  public ByteSource generateRegisterToRegister(
      Address currentAddress, Register targetRegister, Register sourceRegister) {
    if (sourceRegister == Register.I && targetRegister == Register.A) {
      return ByteSource.of(0xED, 0x57);
    }

    if (sourceRegister == Register.R && targetRegister == Register.A) {
      return ByteSource.of(0xED, 0x5F);
    }

    if (sourceRegister == Register.A && targetRegister == Register.I) {
      return ByteSource.of(0xED, 0x47);
    }

    if (sourceRegister == Register.A && targetRegister == Register.R) {
      return ByteSource.of(0xED, 0x4F);
    }

    if (sourceRegister == Register.HL) {
      return switch (targetRegister) {
        case SP -> ByteSource.of(0xF9);
        case IX -> ByteSource.of(0xDD, 0xF9);
        case IY -> ByteSource.of(0xFD, 0xF9);
        default -> null;
      };
    }

    return ByteSource.of(
        0b01000000 | (Registers.r.get(targetRegister) << 3) | (Registers.r.get(sourceRegister)));
  }

  @Override
  public ByteSource generateImmediateToRegister(
      Address currentAddress, Register targetRegister, NumberValue numberValue) {
    return ByteSource.of(0b00000110 | (Registers.r.get(targetRegister) << 3), numberValue.value());
  }

  @Override
  public ByteSource generateImmediateExtendedToRegister(
      Address currentAddress, Register targetRegister, NumberValue numberValue) {

    if (targetRegister.size() == Size.BYTE) {
      throw new IllegalSizeException(
          "Register "
              + targetRegister.name()
              + " is an 8 bit register, and can not hold a 16 bit value");
    }

    return switch (targetRegister) {
      case IX -> ByteSource.of(0xDD, 0x21, numberValue.lsb().value(), numberValue.msb().value());
      case IY -> ByteSource.of(0xFD, 0x21, numberValue.lsb().value(), numberValue.msb().value());
      default -> ByteSource.of(
          0b00000001 | (Registers.dd.get(targetRegister) << 3),
          numberValue.lsb().value(),
          numberValue.msb().value());
    };
  }

  @Override
  public ByteSource generateRegisterIndirectToRegister(
      Address currentAddress, Register targetRegister, Register sourceRegister) {
    if (sourceRegister == Register.HL) {
      return ByteSource.of(0b01000110 | (Registers.r.get(targetRegister) << 3));
    }

    if (targetRegister == Register.A) {
      return switch (sourceRegister) {
        case BC -> ByteSource.of(0x0A);
        case DE -> ByteSource.of(0x1A);
        default -> null;
      };
    }

    return null;
  }

  @Override
  public ByteSource generateIndexedToRegister(
      Address currentAddress,
      Register targetRegister,
      Register sourceRegister,
      long displacement) {
    return switch (sourceRegister) {
      case IX -> ByteSource.of(
          0xDD, 0b01000110 | (Registers.r.get(targetRegister) << 3), displacement);
      case IY -> ByteSource.of(
          0xFD, 0b01000110 | (Registers.r.get(targetRegister) << 3), displacement);
      default -> null;
    };
  }

  @Override
  public ByteSource generateRegisterToRegisterIndirect(
      Address currentAddress, Register targetRegister, Register sourceRegister) {
    if (targetRegister == Register.HL) {
      return ByteSource.of(0b1110000 | Registers.r.get(sourceRegister));
    }

    if (sourceRegister == Register.A) {
      return switch (targetRegister) {
        case BC -> ByteSource.of(0x02);
        case DE -> ByteSource.of(0x12);
        default -> null;
      };
    }

    return null;
  }

  @Override
  public ByteSource generateRegisterToIndexed(
      Address currentAddress,
      Register targetRegister,
      long displacement,
      Register sourceRegister) {
    return switch (targetRegister) {
      case IX -> ByteSource.of(0xDD, 0b01110000 | Registers.r.get(sourceRegister));
      case IY -> ByteSource.of(0xFD, 0b01110000 | Registers.r.get(sourceRegister));
      default -> null;
    };
  }

  @Override
  public ByteSource generateImmediateToRegisterIndirect(
      Address currentAddress, Register register, NumberValue numberValue) {
    if (register == Register.HL) {
      return ByteSource.of(0x36, numberValue.value());
    }

    return null;
  }

  @Override
  public ByteSource generateImmediateToIndexed(
      Address currentAddress,
      Register targetRegister,
      long displacement,
      NumberValue numberValue) {
    return switch (targetRegister) {
      case IX -> ByteSource.of(0xDD, 0x36, displacement, numberValue.value());
      case IY -> ByteSource.of(0xFD, 0x36, displacement, numberValue.value());
      default -> null;
    };
  }

  @Override
  public ByteSource generateExtendedToRegister(
      Address currentAddress, Register targetRegister, NumberValue numberValue) {
    return switch (targetRegister) {
      case A -> ByteSource.of(0x3A, numberValue.lsb().value(), numberValue.msb().value());
      case HL -> ByteSource.of(0x2A, numberValue.lsb().value(), numberValue.msb().value());
      default -> ByteSource.of(
          0xED,
          0b01001011 | (Registers.dd.get(targetRegister) << 4),
          numberValue.lsb().value(),
          numberValue.msb().value());
    };
  }

  @Override
  public ByteSource generateRegisterToExtended(
      Address currentAddress, NumberValue numberValue, Register sourceRegister) {
    return switch (sourceRegister) {
      case A -> ByteSource.of(0x32, numberValue.lsb().value(), numberValue.msb().value());
      case HL -> ByteSource.of(0x22, numberValue.lsb().value(), numberValue.msb().value());
      case IX -> ByteSource.of(0xDD, 0x22, numberValue.lsb().value(), numberValue.msb().value());
      case IY -> ByteSource.of(0xDD, 0x22, numberValue.lsb().value(), numberValue.msb().value());
      default -> ByteSource.of(
          0xED,
          0b01000011 | (Registers.dd.get(sourceRegister) << 4),
          numberValue.lsb().value(),
          numberValue.msb().value());
    };
  }

  @Override
  public ByteSource generateExtendedToIndexed(
      Address currentAddress,
      Register targetRegister,
      long displacement,
      NumberValue numberValue) {
    return switch (targetRegister) {
      case IX -> ByteSource.of(0xDD, 0x2A, numberValue.lsb().value(), numberValue.msb().value());
      case IY -> ByteSource.of(0xFD, 0x2A, numberValue.lsb().value(), numberValue.msb().value());
      default -> null;
    };
  }
}
