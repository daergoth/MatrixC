package com.fordprog.matrix;

import static java.util.stream.Collectors.joining;

import com.fordprog.matrix.interpreter.Interpreter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

  public static void main(String[] args) throws IOException {

    String path = "src/main/resources/input.mxc";

    String inputCode = Files.readAllLines(Paths.get(path)).stream().collect(joining("\n"));

    Interpreter interpreter = new Interpreter(inputCode);

    interpreter.interpret();
  }

}
