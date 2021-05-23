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
    var lines = Files.readAllLines(Path.of("../input/day07.txt"));
    var connections = lines.stream()
      .map(ConnectionParser::parse)
      .map(o -> o.orElseThrow())
      .collect(toList());

    System.out.println("Part 1:");
    /* By topologically sorting all the wires, we can simply calculate the value for all wires
       in that order, because all inputs values will have been calculated before. */
    var sortedWires = topologicalSort(connections).orElseThrow();
    var wireValues = calculateWireValues(connections, sortedWires);
    var wireA = new Wire("a");
    var part1 = wireValues.get(wireA);
    System.out.println(part1);

    System.out.println("Part 2:");
    var wireB = new Wire("b");
    var wireValues2 = calculateWireValues(
      /* just scan for, and replace, the connection that has b on the right side. */
      connections.stream()
        .map(c -> c.rhs.equals(wireB) ? new Connection(new Literal(part1), wireB) : c)
        .collect(toList()),
      sortedWires
    );
    var part2 = wireValues2.get(wireA);
    System.out.println(part2);
  }

  record Edge(Wire from, Wire to) { };

  static Map<Wire, Integer> calculateWireValues(
    List<Connection> connections, List<Wire> sortedWires
  ) {
    var connectionsByRhs = connections.stream().collect(toMap(c -> c.rhs, c -> c));
    var result = new HashMap<Wire, Integer>();
    for(var wire: sortedWires) {
      var lhs = connectionsByRhs.get(wire).lhs;
      result.put(wire, lhs.calculateValue(result::get));
    }
    return result;
  }

  /* Kahn's algorithm: https://en.wikipedia.org/wiki/Topological_sorting#Kahn's_algorithm */
  static Optional<List<Wire>> topologicalSort(List<Connection> connections) {
    var wires = connections.stream().map(c -> c.rhs).collect(toList());
    var edges = connections.stream().flatMap(c -> getEdges(c)).collect(toList());
    var edgesByTo = edges.stream().collect(groupingBy(e -> e.to));
    var edgesByFrom = edges.stream().collect(groupingBy(e -> e.from));

    var nodeQueue = new LinkedList<>(
      wires.stream().filter(w -> !edgesByTo.containsKey(w)).collect(toList())
    );
    var removedEdges = new HashSet<Edge>();
    var sorted = new ArrayList<Wire>(wires.size());

    while (0 < nodeQueue.size()) {
      var node = nodeQueue.poll();
      sorted.add(node);
      if (!edgesByFrom.containsKey(node)) { continue; }
      for(var edge: edgesByFrom.get(node)) {
        removedEdges.add(edge);
        var nodeEdges = edgesByTo.getOrDefault(edge.to, List.of());
        if (nodeEdges.stream().allMatch(e -> removedEdges.contains(e))) {
          nodeQueue.add(edge.to);
        }
      }
    }
    if (edges.stream().anyMatch(e -> !removedEdges.contains(e))) {
      /* There was a cycle */
      return Optional.empty();
    }
    return Optional.of(sorted);
  }

  static Stream<Edge> getEdges(Connection connection) {
    return connection.lhs.getWires().map(w -> new Edge(w, connection.rhs));
  }

  record Connection(Connectable lhs, Wire rhs) { }
  interface Connectable { 
    Stream<Wire> getWires();
    Integer calculateValue(Function<Wire, Integer> getWireValue);
  }
  record Literal(int value) implements Connectable {
    public Stream<Wire> getWires() { return Stream.empty(); }
    public Integer calculateValue(Function<Wire, Integer> getWireValue) { return value; }
  }
  record Wire(String wire) implements Connectable {
    public Stream<Wire> getWires() { return Stream.of(this); }
    public Integer calculateValue(Function<Wire, Integer> getWireValue) {
      return getWireValue.apply(this);
    }
  }
  record BinaryOp(Connectable left, IntBinaryOperator operator, Connectable right) implements Connectable {
    public Stream<Wire> getWires() { return Stream.concat(left.getWires(), right.getWires()); }
    public Integer calculateValue(Function<Wire, Integer> getWireValue) {
      return operator.applyAsInt(left.calculateValue(getWireValue), right.calculateValue(getWireValue));
    }
  }
  record NotOp(Connectable connectable) implements Connectable {
    public Stream<Wire> getWires() { return connectable.getWires(); }
    public Integer calculateValue(Function<Wire, Integer> getWireValue) {
      return ~connectable.calculateValue(getWireValue).intValue() & 65535;
    }
  }

  static class ConnectionParser {
    static Pattern connection = Pattern.compile("^([a-zA-Z0-9 ]+) -> ([a-z]+)$");
    static Pattern literal = Pattern.compile("^\\d+$");
    static Pattern wire = Pattern.compile("^[a-z]+$");
    static Pattern notOp = Pattern.compile("^NOT ([a-z]+)$");
    static Pattern binaryOp = Pattern.compile("^([a-z0-9]+) (LSHIFT|RSHIFT|AND|OR) ([a-z0-9]+)$");

    public static Optional<Connection> parse(String s) {
      var matcher = connection.matcher(s);
      if (!matcher.matches()) { return Optional.empty(); }
      var lhs = matcher.group(1);
      var wire = matcher.group(2);
      return parseConnectable(lhs).map(c -> new Connection(c, new Wire(wire)));
    }

    static Optional<Connectable> parseConnectable(String s) {
      if (literal.matcher(s).matches()) {
        return tryParse(s).flatMap(i -> Optional.of(new Literal(i.intValue())));
      }
      if (wire.matcher(s).matches()) { return Optional.of(new Wire(s)); }
      var notMatcher = notOp.matcher(s);
      if (notMatcher.matches()) {
        return parseConnectable(notMatcher.group(1)).map(c -> new NotOp(c));
      }
      var binaryOpMatcher = binaryOp.matcher(s);
      if (binaryOpMatcher.matches()) {
        return parseConnectable(binaryOpMatcher.group(1))
          .flatMap(l -> parseConnectable(binaryOpMatcher.group(3))
            .flatMap(r -> getOperator(binaryOpMatcher.group(2))
              .map(op -> new BinaryOp(l, op, r))
            )
          );
      }
      return Optional.empty();
    }

    static Optional<IntBinaryOperator> getOperator(String s) {
      return Optional.<IntBinaryOperator>ofNullable(switch (s) {
        case "AND" -> (leftVal, rightVal) -> leftVal & rightVal;
        case "OR" -> (leftVal, rightVal) -> leftVal | rightVal;
        case "LSHIFT" -> (leftVal, rightVal) -> leftVal << rightVal;
        case "RSHIFT" -> (leftVal, rightVal) -> leftVal >>> rightVal;
        default -> null;
      });
    }

    static Optional<Integer> tryParse(String s) {
      try { return Optional.of(Integer.valueOf(s)); }
      catch (NumberFormatException n) { return Optional.empty(); }
    }
  }

}
