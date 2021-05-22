import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.util.function.*;
import static java.util.stream.Collectors.*;
import static java.lang.Math.*;

public class Program {
  public static void main(String... args) throws java.io.IOException {
    var lines = Files.readAllLines(Path.of("../input/day05.txt"));
    System.out.println("Part 1:");
    System.out.println(lines.stream().filter(Program::isNiceLegacy).count());
    System.out.println("Part 2:");
    System.out.println(lines.stream().filter(Program::isNiceModern).count());
  }

  static List<String> _substringBlacklist = Arrays.asList("ab", "cd", "pq", "xy");
  static List<Character> _vowels = Arrays.asList('a', 'i', 'u', 'e', 'o');

  static boolean isNiceLegacy(String s) {
    if (_substringBlacklist.stream().anyMatch(b -> s.contains(b))) { return false; }
    if (asCharStream(s).filter(c -> _vowels.contains(c)).count() < 3) { return false; }
    return has2Palindrome(s);
  }

  static boolean has2Palindrome(String s) {
    if (s.length() < 1) { return false; }
    return toStream(zip(asIterable(s), asIterable(s.substring(1))))
      .anyMatch(t -> t.fst == t.snd);

    // if (s.length() < 1) { return false; }
    // var previous = s.charAt(0);
    // for(char c: s.substring(1).toCharArray()) {
    //   if (c == previous) { return true; }
    //   previous = c;
    // }
    // return false;
  }

  static boolean isNiceModern(String s) {
    return has3Palindrome(s) && hasRepeated2String(s);
  }

  static boolean has3Palindrome(String s) {
    if (s.length() < 2) { return false; }
    return toStream(zip3(
      asIterable(s), asIterable(s.substring(1)), asIterable(s.substring(2))
    )).anyMatch(t -> t.fst == t.trd);
  }

  static boolean hasRepeated2String(String str) {
    return IntStream.range(0, str.length())
      .mapToObj(i -> str.substring(i))
      .anyMatch(s -> prefixRepeats(s, 2));
  }

  static boolean prefixRepeats(String s, int prefixLength) {
    return prefixLength <= s.length() && 
      s.substring(prefixLength).contains(s.substring(0, prefixLength));
  }

  static Stream<Character> asCharStream(CharSequence s) {
    return s.chars().mapToObj(c -> (char)c);
  }

  static Iterable<Character> asIterable(CharSequence s) {
    return asCharStream(s).collect(toList());
  }

  static <T> Stream<T> toStream(Iterable<T> iterable) {
    return StreamSupport.stream(iterable.spliterator(), false);
  }

  record Tuple<T, U>(T fst, U snd) { }
  record Tuple3<T, U, V>(T fst, U snd, V trd) { }

  static <T, U> Iterable<Tuple<T, U>> zip(Iterable<T> left, Iterable<U> right) {
    return zip(left, right, (l, r) -> new Tuple<>(l, r));
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

  @FunctionalInterface
  interface TriFunction<T, U, V, R> { R apply(T t, U u, V v); }

  static <T, U, V> Iterable<Tuple3<T, U, V>> zip3(Iterable<T> ts, Iterable<U> us, Iterable<V> vs) {
    return zip3(ts, us, vs, (t, u, v) -> new Tuple3<>(t, u, v));
  }

  static <T, U, V, R> Iterable<R> zip3(
    Iterable<T> ts, Iterable<U> us, Iterable<V> vs,
    TriFunction<? super T, ? super U, ? super V, R> combine
  ) {
    var zip3Iterator = new Iterator<R>() {
      Iterator<T> t = ts.iterator();
      Iterator<U> u = us.iterator();
      Iterator<V> v = vs.iterator();
      public boolean hasNext() { return t.hasNext() && u.hasNext() && v.hasNext(); }
      public R next() { return combine.apply(t.next(), u.next(), v.next()); }
    };
    return () -> zip3Iterator;
  }

}