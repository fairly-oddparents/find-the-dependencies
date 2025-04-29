package pcd.ass02.dependencyAnalyser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        try {
            JavaParser parser = new JavaParser();
            CompilationUnit cu = parser.parse(javaFile).getResult().orElseThrow();
            String className = cu.getPrimaryTypeName().orElse(javaFile.getFileName().toString());
            Set<String> deps = cu.findAll(ClassOrInterfaceType.class)
                    .stream()
                    .map(NodeWithSimpleName::getNameAsString)
                    .collect(Collectors.toSet());
            return new ClassDependency(className, deps);
        } catch (IOException e) {
            return new ClassDependency("Unknown", Set.of());
        }
    }
}
