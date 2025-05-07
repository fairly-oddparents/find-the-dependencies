package pcd.ass02.dependencyAnalyser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import pcd.ass02.dependencyAnalyserLib.DependencyAnalyserLib;

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
        Path sourceRoot = DependencyAnalyserLib.findSourceRoot(javaFile);
        if (sourceRoot == null) {
            throw new IllegalArgumentException("Cannot locate 'java' folder in the path");
        }

        CombinedTypeSolver solver = new CombinedTypeSolver(
                new ReflectionTypeSolver(false),
                new JavaParserTypeSolver(sourceRoot.toFile())
        );
        ParserConfiguration config = new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21)
                .setSymbolResolver(new JavaSymbolSolver(solver));
        JavaParser parser = new JavaParser(config);

        try {
            String fileContent = Files.readString(javaFile);
            CompilationUnit cu = parser.parse(fileContent).getResult()
                    .orElseThrow(() -> new RuntimeException("Parsing failed"));
            String className = cu.getPrimaryTypeName().orElse(javaFile.getFileName().toString());
            Set<String> dependencies = new HashSet<>();
            DependencyAnalyserLib.collectDependencies(cu).forEach(dep ->
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
