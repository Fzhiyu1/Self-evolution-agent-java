package com.quine.utils;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

public class CompilerUtils {

    /**
     * 编译 Java 源码字符串并加载为 Class
     * @param className 完全限定类名 (如 "com.quine.sandbox.TargetSubject")
     * @param sourceCode Java 源代码字符串
     * @return 编译后的 Class 对象
     */
    public static Class<?> compileAndLoad(String className, String sourceCode) throws Exception {
        // 获取系统编译器
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new RuntimeException("无法获取 Java 编译器，请确保使用 JDK 而非 JRE");
        }

        // 使用 target/classes 作为输出目录（已在 classpath 中）
        Path outputDir = Paths.get("target/classes");
        Files.createDirectories(outputDir);

        // 创建源文件对象
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        // 设置输出目录
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, List.of(outputDir.toFile()));

        // 获取当前 classpath 并添加 target/classes
        String classPath = System.getProperty("java.class.path");
        String fullClassPath = outputDir.toAbsolutePath() + File.pathSeparator + classPath;
        List<String> options = List.of("-classpath", fullClassPath);

        // 创建内存中的源文件
        JavaFileObject sourceFile = new StringSourceJavaFileObject(className, sourceCode);

        // 编译
        JavaCompiler.CompilationTask task = compiler.getTask(
            null,
            fileManager,
            diagnostics,
            options,
            null,
            List.of(sourceFile)
        );

        boolean success = task.call();

        if (!success) {
            StringBuilder errors = new StringBuilder("编译失败:\n");
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                errors.append(diagnostic.getMessage(null)).append("\n");
            }
            throw new RuntimeException(errors.toString());
        }

        fileManager.close();

        // 直接加载类（已在当前 classpath 中）
        return Class.forName(className);
    }

    /**
     * 内存中的 Java 源文件对象
     */
    private static class StringSourceJavaFileObject extends SimpleJavaFileObject {
        private final String code;

        public StringSourceJavaFileObject(String className, String code) {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension),
                  Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }
}
