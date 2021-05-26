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
    var input = Files.readString(Path.of("../input/day10.txt"));
    System.out.println("Part 1:");
    var part1 = Stream.iterate(input, Program::lookAndSay).skip(40).findFirst().orElseThrow();
    System.out.println(part1.length());
    System.out.println("Part 2:");
    System.out.println(Stream.iterate(part1, Program::lookAndSay).skip(10).findFirst().orElseThrow().length());
  }

  private static String lookAndSay(String input) {
    return groups(input).map(g -> g.length() + g.substring(0, 1)).collect(joining());
  }

  private static Stream<String> groups(String s) {
    Iterable<String> iterable = () -> new StringGroupIterator(s);
    return StreamSupport.stream(iterable.spliterator(), false);
  }
}

class StringGroupIterator implements Iterator<String> {
  public StringGroupIterator(String input) {
    chars = input.toCharArray();
  }
  char[] chars;
  int index = 0;

  public boolean hasNext() { return index < chars.length; }

  public String next() {
    var group = new StringBuilder();
    char current;
    do {
      current = chars[index];
      group.append(current);
      index += 1;
    } while(index < chars.length && chars[index] == current);
    return group.toString();
  }
}