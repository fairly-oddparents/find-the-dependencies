package pcd.ass02.reactive;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import pcd.ass02.PathHelper;

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
            return files.filter(f -> f.toString().endsWith(PathHelper.FILE_EXTENSION))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            return Set.of();
        }
    }

    public ClassDependency parseClassDependencies(Path javaFile) {
        JavaParser parser = PathHelper.getJavaParser(javaFile.toString());
        try {
            String fileContent = Files.readString(javaFile);
            CompilationUnit cu = parser.parse(fileContent).getResult()
                    .orElseThrow(() -> new RuntimeException("Parsing failed"));
            String className = extractClassName(javaFile, cu);
            Set<String> dependencies = new HashSet<>(PathHelper.collectDependencies(cu, javaFile));
            return new ClassDependency(className, dependencies);
        } catch (ParseProblemException e) {
            System.err.println("Error parsing " + javaFile + ": " + e.getMessage());
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String extractClassName(Path javaFile, CompilationUnit cu) {
        String fileName = javaFile.getFileName().toString();
        String packageName = cu.getPackageDeclaration()
                .map(pd -> pd.getName().toString())
                .orElse("");
        String simpleName = cu.getPrimaryTypeName().orElse(
                fileName.endsWith(PathHelper.FILE_EXTENSION)
                        ? fileName.substring(0, fileName.length() - PathHelper.FILE_EXTENSION.length())
                        : fileName
        );
        return packageName.isEmpty() ? simpleName : packageName + "." + simpleName;
    }

}