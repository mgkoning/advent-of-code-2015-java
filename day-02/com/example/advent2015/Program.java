package com.example.advent2015;

import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.util.function.*;
import static java.util.stream.Collectors.*;
import static java.lang.Math.*;

public class Program {
  public static void main(String... args) throws java.io.IOException {
    var presents = Files.lines(Path.of("../input/day02.txt"))
      .map(Program::toPresent).collect(toList());
    
    System.out.println("Part 1:");
    System.out.println(getAnswer(presents, Present::requiredPaper));
    System.out.println("Part 2:");
    System.out.println(getAnswer(presents, Present::requiredRibbon));
  }

  private static Present toPresent(String spec) {
    var dimensions = Arrays.stream(spec.split("x")).mapToInt(Integer::parseInt).toArray();
    return new Present(dimensions);
  }

  private static int getAnswer(List<Present> presents, ToIntFunction<Present> selector) {
    return presents.stream().mapToInt(selector).sum();
  }
}

class Present {
  Present(int[] sides) {
    l = sides[0];
    w = sides[1];
    h = sides[2];
    sortedSides = IntStream.of(sides).sorted().toArray();
  }
  private final int l, w, h;
  private final int[] sortedSides;
  public int smallestSide() { return sortedSides[0] * sortedSides[1]; }
  public int requiredPaper() {
    return 2*l*w + 2*w*h + 2*h*l + smallestSide();
  }
  public int volume() { return w*h*l; }
  public int requiredRibbon() { 
    return volume() + 2*sortedSides[0] + 2*sortedSides[1];
  }
}