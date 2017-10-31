package org.jenkinsci.plugins.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.jenkinsci.plugins.entity.AnnotatedTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JavaSourceUtils is a util class for inspecting and parsing Java source code file
 * in order to extract test method info. It returns information only for method
 * annotated both @Test and @JIRATestKey annotations. JIRATestKey annotation must
 * contain specified 'key' value.
 */
public class JavaSourceUtils {

    public static List<AnnotatedTest> inspectWorkspace(String workspace) throws IOException {
        List<AnnotatedTest> tests = new ArrayList<>();
        Files.walkFileTree(Paths.get(workspace), new FileVisitor(tests));
        return tests;
    }

    private static List<AnnotatedTest> inspectJavaFile(File javaFile, List<AnnotatedTest> tests) {
        CompilationUnit compilationUnit;
        try (FileInputStream in = new FileInputStream(javaFile)) {
            compilationUnit = JavaParser.parse(in);
            new MethodVisitor().visit(compilationUnit, tests);
        } catch (IOException e) {
            System.out.println(javaFile.getAbsolutePath());
            e.printStackTrace();
        }
        return tests;
    }

    private static class FileVisitor extends SimpleFileVisitor<Path> {

        private List<AnnotatedTest> tests;

        FileVisitor(List<AnnotatedTest> tests) {
            this.tests = tests;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (file.getFileName().toString().endsWith(".java")) {
                inspectJavaFile(file.toFile(), tests);
            }

            return FileVisitResult.CONTINUE;
        }
    }

    private static class MethodVisitor extends VoidVisitorAdapter<List<AnnotatedTest>> {

        @Override
        public void visit(MethodDeclaration method, List<AnnotatedTest> list) {

            if (method.getAnnotations() != null) {
                List<AnnotationExpr> annotations = method.getAnnotations();
                AnnotationExpr testAnnotation = null;
                AnnotationExpr keyAnnotation = null;
                Map<String, String> testParams = new HashMap<>();
                String jiraKey = null;

                for(AnnotationExpr annotation : annotations) {
                    String annotationName = annotation.getNameAsString();
                    if ("Test".equals(annotationName)) {
                        testAnnotation = annotation;
                    } else if ("JIRATestKey".equals(annotationName) && annotation instanceof NormalAnnotationExpr) {
                        keyAnnotation = annotation;
                    }
                }
                if (testAnnotation == null || keyAnnotation == null) return;

                if (testAnnotation instanceof NormalAnnotationExpr) {
                    for (MemberValuePair pair : ((NormalAnnotationExpr) testAnnotation).getPairs()) {
                        testParams.put(pair.getNameAsString(), pair.getValue().toString());
                    }
                }

                for (MemberValuePair pair : ((NormalAnnotationExpr) keyAnnotation).getPairs()) {
                    if (pair.getNameAsString().equals("key")) {
                        jiraKey = pair.getValue().toString();
                        break;
                    }
                }

                if (jiraKey != null) {
                    list.add(new AnnotatedTest(method.getNameAsString(), jiraKey, testParams));
                }
            }
        }
    }
}
