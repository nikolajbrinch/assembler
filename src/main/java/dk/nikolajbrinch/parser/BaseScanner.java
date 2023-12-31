package dk.nikolajbrinch.parser;

import dk.nikolajbrinch.parser.impl.CharReaderImpl;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class BaseScanner<E extends TokenType, T extends Token>
    implements Scanner<T>, Iterable<T>, AutoCloseable, Closeable {

  final CharReaderImpl charReader;

  private final List<T> buffer = new LinkedList<>();

  public BaseScanner(InputStream inputStream) {
    this.charReader = new CharReaderImpl(inputStream);
  }

  protected CharReaderImpl getCharReader() {
    return charReader;
  }

  public T next() {
    if (ensureBuffer(1)) {
      return buffer.removeFirst();
    }

    return null;
  }

  public T peek() {
    return peek(1);
  }

  public T peek(int position) {
    if (ensureBuffer(position)) {
      return buffer.get(position - 1);
    }

    return null;
  }

  private boolean hasNext() {
    if (ensureBuffer(1)) {
      return !buffer.isEmpty();
    }

    return false;
  }

  protected abstract T createToken() throws IOException;

  protected abstract T createEofToken(int line, int position) throws IOException;

  protected abstract T createToken(E tokenType, int lineNumber, int start, int end, String text);

  protected Char nextChar() throws IOException {
    return charReader.next();
  }

  protected Char peekChar() throws IOException {
    return charReader.peek();
  }

  protected Char appendChar(StringBuilder buffer) throws IOException {
    return appendChar(buffer, charReader.next());
  }

  protected Char appendChar(StringBuilder buffer, Char ch) {
    buffer.append(ch.toString());

    return ch;
  }

  protected T createCharsToken(E tokenType, Char... chars) {
    Char first = chars[0];
    Char last = chars[chars.length - 1];

    return createToken(
        tokenType,
        first.line(),
        first.position(),
        last.position(),
        String.valueOf(Arrays.stream(chars).map(Char::toString).collect(Collectors.joining())));
  }

  protected boolean checkNextChar(char ch) throws IOException {
    return checkNextChar(nextChar -> nextChar == ch);
  }

  protected boolean checkNextChar(Predicate<Character> predicate) throws IOException {
    Char nextChar = charReader.peek();

    return nextChar != null && predicate.test(nextChar.character());
  }

  @Override
  public Iterator<T> iterator() {

    return new Iterator<T>() {

      private boolean hasNext = true;

      @Override
      public boolean hasNext() {
        return hasNext;
      }

      @Override
      public T next() {
        if (!hasNext) {
          throw new NoSuchElementException("No more elements!");
        }

        T token = BaseScanner.this.next();

        if (isEofToken(token)) {
          hasNext = false;
        }

        return token;
      }
    };
  }

  protected abstract boolean isEofToken(T token);

  public Stream<T> stream() {
    return StreamSupport.stream(spliterator(), false);
  }

  @Override
  public void close() throws IOException {
    charReader.close();
  }

  private boolean ensureBuffer(int size) {
    while (buffer.size() < size) {
      T read = read();

      if (read != null) {
        buffer.add(read);
      } else {
        break;
      }
    }

    return buffer.size() >= size;
  }

  private T read() {
    T token = null;

    try {
      while (token == null) {
        if (!charReader.hasNext()) {
          token = createEofToken(charReader.getLine(), charReader.getPosition());
        } else {
          token = createToken();
        }
      }
    } catch (IOException e) {
      throw new ScanException(e);
    }

    return token;
  }
}
