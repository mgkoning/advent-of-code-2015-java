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
    var inputLines = Files.readAllLines(Path.of("../input/day15.txt"));
    var ingredients = inputLines.stream()
      .map(IngredientParser::parse)
      .map(Optional::orElseThrow)
      .collect(toList());
    //ingredients = Arrays.asList(new Ingredient("Butterscotch", -1, -2, 6, 3, 8), new Ingredient("Cinnamon", 2, 3, -2, -1, 3));
    System.out.println("Part 1:");
    var mixes = getPossibleMixes(FList.wrap(ingredients), 100L, 0L);
    var scores = mixes.map(Program::toScore);
    System.out.println(scores.map(s -> s.first()).foldLeft(0L, Math::max));
    System.out.println("Part 2:");
    System.out.println(scores.filter(s -> s.second().longValue() == 500L).map(s -> s.first()).foldLeft(0L, Math::max));
  }

  static Tuple<Long, Long> toScore(FList<Tuple<Ingredient, Long>> list) {
    var summedScore = list.map(t -> {
      var count = t.second();
      var ingredient = t.first();
      return new Score(
        count * ingredient.capacity(), count * ingredient.durability(), count * ingredient.flavor(),
        count * ingredient.texture(), count * ingredient.calories()
      );
    })
      .foldLeft(new Score(0L, 0L, 0L, 0L, 0L), Score::add);
    return new Tuple<>(
      Math.max(0L, summedScore.capacity()) * Math.max(0L, summedScore.durability()) * 
      Math.max(0L, summedScore.flavor()) * Math.max(0L, summedScore.texture()),
      summedScore.calories()
    );
  }

  static FList<FList<Tuple<Ingredient, Long>>> getPossibleMixes(FList<Ingredient> ingredients, long desiredTotal, long soFar) {
    return ingredients.match(
      nil -> FList.singleton(FList.empty()),
      cons ->
        cons.tail().match(
          nil -> FList.singleton(FList.singleton(new Tuple<>(cons.head(), desiredTotal - soFar))),
          cons2 -> FList.wrap(LongStream.rangeClosed(0L, desiredTotal - soFar).mapToObj(i -> i).collect(toList()))
            .flatMap(i -> getPossibleMixes(cons.tail(), desiredTotal, soFar + i).map(m -> FList.cons(new Tuple<>(cons.head(), i), m)))
        )
         
    );
  }
}

interface FList<T> {
  <U> U match(Function<Nil<T>, U> matchNil, Function<Cons<T>, U> matchCons);
  public static <T> FList<T> wrap(List<T> list) {
    FList<T> result = new Nil<T>();
    for(var i = list.size() - 1; i >= 0; i--) {
      result = new Cons<>(list.get(i), result);
    }
    return result;
  }

  public static <T> FList<T> empty() { return new Nil<T>(); }

  public static <T> FList<T> singleton(T value) { return FList.cons(value, FList.empty()); }

  public static <T> FList<T> cons(T head, FList<T> tail) { return new Cons<>(head, tail); }

  default public <U> FList<U> map(Function<T, U> f) {
    return this.match(
      nil -> new Nil<U>(),
      cons -> new Cons<U>(f.apply(cons.head()), cons.tail().map(f))
    );
  }

  default public FList<T> concat(FList<T> other) {
    return this.match(
      nil -> other,
      cons -> FList.cons(cons.head(), cons.tail().concat(other))
    );
  }
  
  default public <U> FList<U> flatMap(Function<T, FList<U>> f) {
    return this.match(
      nil -> new Nil<>(),
      cons -> f.apply(cons.head()).concat(cons.tail().flatMap(f))
    );
  }

  default public <U> U foldLeft(U start, BiFunction<T, U, U> next) {
    return this.match(
      nil -> start,
      cons -> next.apply(cons.head(), cons.tail().foldLeft(start, next))
    );
  }

  default public FList<T> filter(Predicate<T> predicate) {
    return this.match(
      nil -> FList.empty(),
      cons -> {
        var rest = cons.tail().filter(predicate);
        return predicate.test(cons.head()) ? FList.cons(cons.head(), rest) : rest;
      }
    );
  }
}

record Nil<T>() implements FList<T> {
  public <U> U match(Function<Nil<T>, U> matchNil, Function<Cons<T>, U> matchCons) {
    return matchNil.apply(this);
  }
  public String toString() { return "[]"; }
}
record Cons<T>(T head, FList<T> tail) implements FList<T> {
  public <U> U match(Function<Nil<T>, U> matchNil, Function<Cons<T>, U> matchCons) {
    return matchCons.apply(this);
  }
  public String toString() { return String.format("%s:%s", head, tail); }
}

record Tuple<T,U>(T first, U second) {}

record Score(long capacity, long durability, long flavor, long texture, long calories) {
  public static Score add(Score one, Score other) {
    return new Score(
      one.capacity + other.capacity, one.durability + other.durability, one.flavor + other.flavor,
      one.texture + other.texture, one.calories + other.calories
    );
  }
}

record Ingredient(
  String name, long capacity, long durability, long flavor, long texture, long calories
) { }

class IngredientParser {
  static Pattern ingredientPattern = Pattern.compile(
    "(\\p{IsLetter}+): capacity (-?[0-9]+), durability (-?[0-9]+), " +
    "flavor (-?[0-9]+), texture (-?[0-9]+), calories (-?[0-9]+)"
  );
  public static Optional<Ingredient> parse(String line) {
    var matcher = ingredientPattern.matcher(line);
    if (!matcher.matches()) { return empty(); }
    return OptionalBinder.bind(
      of(matcher.group(1)), parseLong(matcher.group(2)), parseLong(matcher.group(3)),
      parseLong(matcher.group(4)), parseLong(matcher.group(5)), parseLong(matcher.group(6)),
      Ingredient::new
    );
  }

  static Optional<Long> parseLong(String number) {
    try {
      return of(Long.parseLong(number));
    } catch (NumberFormatException n) {
      return empty();
    }
  }
}

class OptionalBinder {
  static <A,B,C,D,E,F,G> Optional<G> bind(
    Optional<A> optA, Optional<B> optB, Optional<C> optC, Optional<D> optD,
    Optional<E> optE, Optional<F> optF, HexFunc<A,B,C,D,E,F,G> func
  ) {
    return optA.flatMap(a ->
      optB.flatMap(b -> 
      optC.flatMap(c -> 
      optD.flatMap(d ->
      optE.flatMap(e ->
      optF.map(f -> func.apply(a, b, c, d, e, f)))))));
  }
}

@FunctionalInterface
interface HexFunc<A,B,C,D,E,F,G> {
  G apply(A a, B b, C c, D d, E e, F f);
}