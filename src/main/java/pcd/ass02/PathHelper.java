package pcd.ass02;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.declarations.ResolvedEnumConstantDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class PathHelper {

    public static final String FILE_EXTENSION = ".java";
    public static final String SRC_FOLDER = "java";

    /**
     * Get the JavaParser instance with the correct configuration.
     * @param classSrcFile the class file to analyze
     * @return the JavaParser instance
     */
    public static JavaParser getJavaParser(String classSrcFile) {
        Path sourceRoot = findSourceRoot(Paths.get(classSrcFile));
        if (sourceRoot == null) {
            throw new IllegalArgumentException("Source root not found. Check the file path contains 'java' folder.");
        }

        CombinedTypeSolver solver = new CombinedTypeSolver(
                new ReflectionTypeSolver(false),
                new JavaParserTypeSolver(sourceRoot.toFile())
        );

        ParserConfiguration config = new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21)
                .setSymbolResolver(new JavaSymbolSolver(solver));

        return new JavaParser(config);
    }

    private static Path findSourceRoot(Path path) {
        while (path != null && path.getFileName() != null && !path.getFileName().toString().equals(SRC_FOLDER)) {
            path = path.getParent();
        }
        return path;
    }

    /**
     * Collect the dependencies of a class.
     * @param cu the compilation unit
     * @param sourceFilePath the path of the source file
     * @return the list of dependencies
     */
    public static List<String> collectDependencies(CompilationUnit cu, Path sourceFilePath) {
        List<String> deps = new ArrayList<>();

        String packageName = cu.getPackageDeclaration()
                .map(pd -> pd.getName().toString())
                .orElse("");
        String className = sourceFilePath.getFileName().toString().replace(FILE_EXTENSION, "");
        String fullyQualifiedClassName = packageName.isEmpty()
                ? className
                : packageName + "." + className;

        cu.findAll(ClassOrInterfaceType.class).forEach(t -> tryResolve(() ->
                deps.add(t.resolve().asReferenceType().getQualifiedName())));

        cu.findAll(MethodCallExpr.class).forEach(e -> tryResolve(() ->
                deps.add(e.resolve().declaringType().getQualifiedName())));

        cu.findAll(FieldAccessExpr.class).forEach(e -> tryResolve(() -> {
            ResolvedValueDeclaration r = e.resolve();
            if (r instanceof ResolvedFieldDeclaration f)
                deps.add(f.declaringType().getQualifiedName());
            else if (r instanceof ResolvedEnumConstantDeclaration en)
                deps.add(en.getType().asReferenceType().getQualifiedName());
        }));

        cu.findAll(ObjectCreationExpr.class).forEach(e -> tryResolve(() ->
                deps.add(e.resolve().declaringType().getQualifiedName())));

        return deps.stream()
                .filter(dep -> !dep.equals(fullyQualifiedClassName))
                .distinct().sorted().toList();
    }

    private static void tryResolve(Runnable r) {
        try { r.run(); } catch (RuntimeException ignored) {}
    }

}
