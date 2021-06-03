import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.util.function.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import static java.util.stream.Collectors.*;
import static java.lang.Math.*;
import static java.util.Optional.*;

public class Program {
  public static void main(String... args) throws java.io.IOException {
    var inputLines = Files.readAllLines(Path.of("../input/day17.txt"));
    var volumes = inputLines.stream()
      .map(Program::tryParse).map(OptionalInt::orElseThrow).collect(toList());
    var target = 150;
    System.out.println("Part 1:");
    var exactFills = getExactFills(target, FList.wrap(volumes));
    System.out.println(exactFills.stream().count());
    System.out.println("Part 2:");
    var sortedSizes = exactFills.stream().map(f -> f.stream().count()).sorted().collect(toList());
    var smallest = sortedSizes.get(0);
    System.out.println(sortedSizes.stream().takeWhile(s -> s == smallest).count());
  }

  static OptionalInt tryParse(String val) {
    try { return OptionalInt.of(Integer.parseInt(val)); }
    catch(NumberFormatException n) { return OptionalInt.empty(); }
  }

  static FList<FList<Integer>> getExactFills(int target, FList<Integer> volumes) {
    if (target < 0) { return FList.empty(); }
    return target == 0 ?
      FList.singleton(FList.empty()) :
      volumes.match(
        nil -> FList.empty(),
        cons -> {
          var head = cons.head();
          var tail = cons.tail();
          var includeHead = getExactFills(target - head, tail)
            .map(subfill -> FList.cons(head, subfill));
          var excludeHead = getExactFills(target, tail);
          return includeHead.concat(excludeHead);
        }
      );
  }
}

/* Singly-linked list commonly available in functional languages, hence F(unctional)List. */
interface FList<T> extends Iterable<T> {
  <U> U match(Function<Nil<T>, U> matchNil, Function<Node<T>, U> matchNode);

  public static <T> FList<T> wrap(List<T> list) {
    FList<T> result = new Nil<T>();
    for(var i = list.size() - 1; i >= 0; i--) {
      result = new Node<>(list.get(i), result);
    }
    return result;
  }

  public default Stream<T> stream() { return StreamSupport.stream(spliterator(), false); }

  public default Iterator<T> iterator() {
    var flist = this;
    return new Iterator<T>() {
      FList<T> remaining = flist;
      public boolean hasNext() { return !remaining.isEmpty(); }
      public T next() {
        var flist = (Node<T>)remaining;
        remaining = flist.tail();
        return flist.head();
      }
    };
  }

  public static <T> FList<T> empty() { return new Nil<T>(); }

  public static <T> FList<T> singleton(T value) { return FList.cons(value, FList.empty()); }
 
  public static <T> FList<T> cons(T head, FList<T> tail) { return new Node<>(head, tail); }

  default public boolean isEmpty() { return match(nil -> true, node -> false); }

  default public <U> FList<U> map(Function<T, U> f) {
    return this.match(
      nil -> new Nil<U>(),
      node -> new Node<U>(f.apply(node.head()), node.tail().map(f))
    );
  }

  default public FList<T> concat(FList<T> other) {
    return this.match(
      nil -> other,
      node -> FList.cons(node.head(), node.tail().concat(other))
    );
  }
}

record Nil<T>() implements FList<T> {
  public <U> U match(Function<Nil<T>, U> matchNil, Function<Node<T>, U> matchNode) {
    return matchNil.apply(this);
  }
  public String toString() { return "[]"; }
}

record Node<T>(T head, FList<T> tail) implements FList<T> {
  public <U> U match(Function<Nil<T>, U> matchNil, Function<Node<T>, U> matchNode) {
    return matchNode.apply(this);
  }
  public String toString() { return String.format("%s:%s", head, tail); }
}