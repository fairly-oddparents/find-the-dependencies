package pcd.ass02.dependencyAnalyser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.declarations.*;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pcd.ass02.dependencyAnalyserLib.DependencyAnalyserLib.SRC_FOLDER;

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
        Path sourceRoot = findSourceRoot(javaFile);
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

            // Tipi usati direttamente
            cu.findAll(ClassOrInterfaceType.class).forEach(type -> {
                try {
                    ResolvedType resolved = type.resolve();
                    if (resolved.isReferenceType()) {
                        String qualifiedName = resolved.asReferenceType().getQualifiedName();
                        dependencies.add(qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1));
                    }
                } catch (RuntimeException ignored) {}
            });

            // Chiamate a metodi (anche statici)
            cu.findAll(MethodCallExpr.class).forEach(expr -> {
                try {
                    ResolvedMethodDeclaration method = expr.resolve();
                    dependencies.add(method.declaringType().getClassName());
                } catch (RuntimeException ignored) {}
            });

            // Accessi a campi (statici o meno)
            cu.findAll(FieldAccessExpr.class).forEach(expr -> {
                try {
                    ResolvedValueDeclaration resolved = expr.resolve();
                    if (resolved instanceof ResolvedFieldDeclaration field) {
                        dependencies.add(field.declaringType().getClassName());
                    } else if (resolved instanceof ResolvedEnumConstantDeclaration enumConst) {
                        String qualifiedName = enumConst.getType().asReferenceType().getQualifiedName();
                        dependencies.add(qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1));
                    }
                } catch (RuntimeException ignored) {}
            });

            // Istanziazione oggetti con new
            cu.findAll(ObjectCreationExpr.class).forEach(expr -> {
                try {
                    ResolvedConstructorDeclaration constructor = expr.resolve();
                    dependencies.add(constructor.getClassName());
                } catch (RuntimeException ignored) {}
            });

            return new ClassDependency(className, dependencies);

        } catch (ParseProblemException e) {
            System.out.println("Error parsing file: " + javaFile + ", " + e.getMessage());
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static Path findSourceRoot(Path path) {
        while (path != null && !path.getFileName().toString().equals(SRC_FOLDER)) {
            path = path.getParent();
        }
        return path;
    }
}
