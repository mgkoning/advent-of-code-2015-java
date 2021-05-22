import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.util.function.*;
import static java.util.stream.Collectors.*;
import static java.lang.Math.*;

public class Program {
  public static void main(String... args) throws java.io.IOException {
    var lines = Files.lines(Path.of("../input/day05.txt"));
    System.out.println("Part 1:");
    System.out.println(lines.filter(Program::isNiceLegacy).count());
  }

  static List<String> _substringBlacklist = Arrays.asList("ab", "cd", "pq", "xy");
  static List<Character> _vowels = Arrays.asList('a', 'i', 'u', 'e', 'o');

  static boolean isNiceLegacy(String s) {
    if (_substringBlacklist.stream().anyMatch(b -> s.contains(b))) { return false; }
    if (asCharStream(s).filter(c -> _vowels.contains(c)).count() < 3) { return false; }
    return hasRepeat(s);
  }

  static Stream<Character> asCharStream(CharSequence s) {
    return s.chars().mapToObj(c -> (char)c);
  }

  static Iterable<Character> asIterable(CharSequence s) {
    return asCharStream(s).collect(toList());
  }

  static boolean hasRepeat(String s) {
    if (s.length() < 1) { return false; }
    return StreamSupport.stream(
      zip(asIterable(s), asIterable(s.substring(1)), (l, r) -> l == r).spliterator(),
      true
    ).anyMatch(c -> c);

    // if (s.length() < 1) { return false; }
    // var previous = s.charAt(0);
    // for(char c: s.substring(1).toCharArray()) {
    //   if (c == previous) { return true; }
    //   previous = c;
    // }
    // return false;
  }

  static <T, U, R> Iterable<R> zip(
    Iterable<T> left, Iterable<U> right, BiFunction<? super T, ? super U, R> combine
  ) {
    var zipIterator = new Iterator<R>() {
      Iterator<T> l = left.iterator();
      Iterator<U> r = right.iterator();
      public boolean hasNext() { return l.hasNext() && r.hasNext(); }
      public R next() { return combine.apply(l.next(), r.next()); }
    };
    return () -> zipIterator;
  }

}