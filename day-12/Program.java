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
    var input = Files.readString(Path.of("../input/day12.txt"));
    System.out.println("Part 1:");
    var value = BasicJsonParser.parseJsonValue(input).orElseThrow();
    System.out.println(getSum(value));
    System.out.println("Part 2:");
    System.out.println(getNonRedSum(value));
  }

  static long getSum(JsonValue value) {
    return value.match(
      i -> i.value(),
      s -> 0L,
      array -> array.contents().stream().mapToLong(Program::getSum).sum(),
      object -> object.properties().values().stream().mapToLong(Program::getSum).sum()
    );
  }

  static long getNonRedSum(JsonValue value) {
    return value.match(
      i -> i.value(),
      s -> 0L,
      array -> array.contents().stream().mapToLong(Program::getNonRedSum).sum(),
      object -> {
        var values = object.properties().values();
        return values.stream().anyMatch(Program::isRed) ?
          0L :
          values.stream().mapToLong(Program::getNonRedSum).sum();
      }
    );
  }

  static boolean isRed(JsonValue value) {
    return value.match(i -> false, s -> s.value().equals("red"), array -> false, object -> false);
  }
}

interface JsonValue {
  <U> U match(
    Function<JsonInteger, U> matchInteger, Function<JsonString, U> matchString,
    Function<JsonArray, U> matchArray, Function<JsonObject, U> matchObject
  );
}
record JsonInteger(long value) implements JsonValue { 
  public <U> U match(
    Function<JsonInteger, U> matchInteger, Function<JsonString, U> matchString,
    Function<JsonArray, U> matchArray, Function<JsonObject, U> matchObject
  ) {
    return matchInteger.apply(this);
  }
}
record JsonString(String value) implements JsonValue {
  public <U> U match(
    Function<JsonInteger, U> matchInteger, Function<JsonString, U> matchString,
    Function<JsonArray, U> matchArray, Function<JsonObject, U> matchObject
  ) {
    return matchString.apply(this);
  }
}
record JsonArray(List<JsonValue> contents) implements JsonValue {
    public <U> U match(
    Function<JsonInteger, U> matchInteger, Function<JsonString, U> matchString,
    Function<JsonArray, U> matchArray, Function<JsonObject, U> matchObject
  ) {
    return matchArray.apply(this);
  }
 }
record JsonObject(Map<String, JsonValue> properties) implements JsonValue {
  public <U> U match(
    Function<JsonInteger, U> matchInteger, Function<JsonString, U> matchString,
    Function<JsonArray, U> matchArray, Function<JsonObject, U> matchObject
  ) {
    return matchObject.apply(this);
  }
}

class SimplifiedJsonParser {
  public static Optional<JsonValue> parseJsonValue(String input) {
    return parseJsonValue(new LinkedList<Character>(input.chars().mapToObj(c -> (char)c).collect(toList())));
  }

  static Optional<JsonValue> parseJsonValue(Deque<Character> input) {
    var next = input.peek();
    return (switch (next) {
      case '-' -> consume(input, '-')
        .flatMap(minus -> parsePositiveInteger(input).map(i -> new JsonInteger(-1 * i.value())));
      case '"' -> parseString(input);
      case '[' -> parseArray(input);
      case '{' -> parseObject(input);
      default -> Character.isDigit(next) ? parsePositiveInteger(input) : empty();
    }).map(result -> (JsonValue)result);
  }

  static Optional<Character> consume(Deque<Character> input, char target) {
    if (input.peek() == target) { return of(input.pop()); }
    return empty();
  }
  
  static Optional<JsonInteger> parsePositiveInteger(Deque<Character> input) {
    if (!Character.isDigit(input.peek())) { return empty(); }
    var value = 0L;
    while(0 < input.size() && Character.isDigit(input.peek())) {
      value = value * 10 + Character.digit(input.pop(), 10);
    }
    return of(new JsonInteger(value));
  }

  static Optional<JsonString> parseString(Deque<Character> input) {
    return consume(input, '"')
      .map(quote -> {
        var builder = new StringBuilder();
        var head = input.pop();
        while(head != '"') {
          builder.append(head);
          head = input.pop();
        }
        return builder.toString();
      })
      .map(JsonString::new);
  }

  static Optional<JsonArray> parseArray(Deque<Character> input) {
    return consume(input, '[')
      .flatMap(openBracket -> {
        var elements = new ArrayList<JsonValue>();
        var next = input.peek();
        while(next != ']') {
          var element = parseJsonValue(input);
          if (!element.isPresent()) { return empty(); }
          elements.add(element.orElseThrow());
          next = input.peek();
          if (next == ',') {
            input.pop();
            next = input.peek();
          } else if (next != ']') { 
            return empty();
          }
        }
        return consume(input, ']').map(closeBracket -> elements);
      })
      .map(JsonArray::new);
  }
  
  static Optional<JsonObject> parseObject(Deque<Character> input) {
    return consume(input, '{')
      .flatMap(openBrace -> {
        var properties = new HashMap<String, JsonValue>();
        var next = input.peek();
        while (next != '}') {
          var maybeEntry = parseString(input)
            .flatMap(key -> consume(input, ':')
              .flatMap(colon -> parseJsonValue(input)
                .map(value -> new AbstractMap.SimpleEntry<>(key, value))
              )
            );
          if (!maybeEntry.isPresent()) { return empty(); }
          var entry = maybeEntry.orElseThrow();
          properties.put(entry.getKey().value(), entry.getValue());
          next = input.peek();
          if (next == ',') {
            input.pop();
            next = input.peek();
          } else if (next != '}') { 
            return empty();
          }
        }
        return consume(input, '}').map(closeBracket -> properties);
      })
      .map(JsonObject::new);
  }
}