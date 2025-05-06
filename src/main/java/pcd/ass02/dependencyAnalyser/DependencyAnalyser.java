package pcd.ass02.dependencyAnalyser;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.declarations.*;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

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

    public ClassDependency parseClassDependencies(Path javaFile) { //TODO: fix
        CombinedTypeSolver typeSolver = new CombinedTypeSolver(new ReflectionTypeSolver(false));
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        ParserConfiguration config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
        config.setSymbolResolver(symbolSolver);
        StaticJavaParser.setConfiguration(config);
        try {
            CompilationUnit cu = StaticJavaParser.parse(javaFile.toString());
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
        }

    }
}
