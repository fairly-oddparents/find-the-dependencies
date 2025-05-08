package pcd.ass02.reactive;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import pcd.ass02.asynchronous.DependencyAnalyserLib;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DependencyAnalyser {
    public Set<Path> getJavaFiles(Path root) {
        try (Stream<Path> files = Files.walk(root)) {
            return files
                    .filter(f -> f.toString().endsWith(".java"))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            return Set.of();
        }
    }

    public ClassDependency parseClassDependencies(Path javaFile) {
        JavaParser parser = DependencyAnalyserLib.getJavaParser(javaFile.toString());

        try {
            String fileContent = Files.readString(javaFile);
            CompilationUnit cu = parser.parse(fileContent).getResult()
                    .orElseThrow(() -> new RuntimeException("Parsing failed"));
            String fileName = javaFile.getFileName().toString();
            String className = cu.getPrimaryTypeName().orElse(
                    fileName.endsWith(".java") ? fileName.substring(0, fileName.length() - ".java".length()) : fileName
            );

            Set<String> dependencies = new HashSet<>();
            DependencyAnalyserLib.collectDependencies(cu, javaFile).forEach(dep ->
                    dependencies.add(dep.substring(dep.lastIndexOf('.') + 1))
            );
            return new ClassDependency(className, dependencies);

        } catch (ParseProblemException e) {
            System.out.println("Error parsing file: " + javaFile + ", " + e.getMessage());
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
