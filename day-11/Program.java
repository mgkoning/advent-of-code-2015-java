import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.util.function.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import static java.util.stream.Collectors.*;
import static java.lang.Math.*;

public class Program {
  public static void main(String... args) throws java.io.IOException {
    var input = Files.readString(Path.of("../input/day11.txt"));
    System.out.println("Part 1:");
    var part1 = nextPassword(input);
    System.out.println(part1);
    System.out.println("Part 2:");
    System.out.println(nextPassword(part1));
  }

  static String nextPassword(String current) {
    return Stream.iterate(current, Program::incrementPassword)
      .skip(1)
      .filter(Program::isValidPassword)
      .findFirst().orElseThrow();
  }

  static String incrementPassword(String current) {
    var incremented = foldLeft(
      new StringBuilder(current).reverse().chars().mapToObj(c -> (char)c),
      new Tuple<>(new ArrayList<Character>(), true),
      (acc, next) -> {
        var toAppend = acc.snd ? incrementChar(next) : next;
        acc.fst.add(toAppend);
        return new Tuple<>(acc.fst, toAppend < next);
      }
    ).fst;
    return incremented.stream()
      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
      .reverse()
      .toString();
  }

  static boolean isValidPassword(String candidate) {
    return !hasBlacklistedLetters(candidate) &&
      hasStraight(candidate) &&
      hasTwoPairs(candidate);
  }

  static String blacklist = "iol";
  static boolean hasBlacklistedLetters(String candidate) {
    return blacklist.chars().anyMatch(b -> candidate.chars().anyMatch(c -> c == b));
  }

  static boolean hasStraight(String candidate) {
    return substringsOfLength(candidate, 3)
      .map(t -> t.snd)
      .anyMatch(Program::isStraight);
  }

  static boolean isStraight(String s) {
    return pairwise(s).allMatch(t -> t.fst + 1 == t.snd);
  }

  static Stream<Tuple<Integer, Integer>> pairwise(String s) {
    return StreamSupport.stream(
      zip(
        s.chars().mapToObj(i -> i).collect(toList()),
        s.substring(1).chars().mapToObj(i -> i).collect(toList())
      ).spliterator(),
      false
    );
  }

  static Stream<Tuple<Integer, String>> substringsOfLength(String s, int length) {
    return IntStream.rangeClosed(0, s.length() - length)
      .mapToObj(i -> new Tuple<>(i, s.substring(i, i + length)));
  }

  static boolean hasTwoPairs(String candidate) {
    var allPairIndices = substringsOfLength(candidate, 2)
      .filter(s -> pairwise(s.snd).allMatch(t -> t.fst == t.snd))
      .map(s -> s.fst)
      .collect(toList());
    return 1 < allPairIndices.size() &&
      allPairIndices.stream().anyMatch(a -> allPairIndices.stream().anyMatch(b -> a + 1 < b));
  }

  static char incrementChar(char current) {
    return (char)((current + 1 - 'a') % ('z' - 'a' + 1) + 'a');
  }

  record Tuple<T, U>(T fst, U snd) { }
  record Triple<T, U, V>(T fst, U snd, V trd) { }

  static <T, U> U foldLeft(Stream<T> stream, U seed, BiFunction<U, ? super T, U> accumulator) {
    var acc = seed;
    var iterator = stream.iterator();
    while(iterator.hasNext()) {
      acc = accumulator.apply(acc, iterator.next());
    }
    return acc;
  }

  static <T, U> Iterable<Tuple<T, U>> zip(Iterable<T> left, Iterable<U> right) {
    return zip(left, right, (l, r) -> new Tuple<>(l, r));
  }

  static <T, U, R> Iterable<R> zip(
    Iterable<T> left, Iterable<U> right, BiFunction<? super T, ? super U, R> combine
  ) {
    return () -> new ZipIterator<>(left, right, combine);
  }
}

class ZipIterator<T, U, R> implements Iterator<R> {
  public ZipIterator(Iterable<T> ts, Iterable<U> us, BiFunction<? super T, ? super U, R> combine) {
    this.ts = ts.iterator();
    this.us = us.iterator();
    this.combine = combine;
  }

  Iterator<T> ts;
  Iterator<U> us;
  BiFunction<? super T, ? super U, R> combine;

  public boolean hasNext() { return ts.hasNext() && us.hasNext(); }

  public R next() { return combine.apply(ts.next(), us.next()); }
}