package es.upm.grise.profundizacion.grupo.aa.mapping;

// Adapted from https://github.com/TestSmells/TestFileMapping/blob/master/src/main/java/edu/rit/se/testsmells/MappingDetector.java
// The original jar had some big pitfalls and assumed many things, including, for example, that paths are always split windows style.


import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import es.upm.grise.profundizacion.grupo.aa.UnitTestPair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class MappingDetector {
    public UnitTestPair detectMapping(File testFilePath, File basedir) throws IOException {
        // UnitTestPair testFile = new UnitTestPair(testFilePath, basedir);
        String productionFileName;
        String testFileName = testFilePath.getName();

        int index = testFileName.toLowerCase().lastIndexOf("test");
        if (index == 0) {
            //the name of the test file starts with the name 'test'
            productionFileName = testFileName.substring(4);
        } else {
            //the name of the test file ends with the name 'test'
            productionFileName = testFileName.substring(0, index) + ".java";
        }

        Path startDir = Paths.get(basedir.getAbsolutePath());
        FindJavaTestFilesVisitor v = new FindJavaTestFilesVisitor(productionFileName);
        Files.walkFileTree(startDir, v);

        File productionFilePath = v.getProductionFilePath();

        if (isFileSyntacticallyValid(productionFilePath))
            return new UnitTestPair(testFilePath, productionFilePath);
        else
            return null;
    }

    /**
     * Determines if the identified production file is syntactically correct by parsing it and generating its AST
     *
     * @param filePath of the production file
     */
    private boolean isFileSyntacticallyValid(File filePath) {
        boolean valid = false;

        if (filePath.length() != 0) {
            try {
                FileInputStream fTemp = new FileInputStream(filePath);
                CompilationUnit compilationUnit = JavaParser.parse(fTemp);
                MappingDetector.ClassVisitor classVisitor;
                classVisitor = new MappingDetector.ClassVisitor();
                classVisitor.visit(compilationUnit, null);
                valid = !classVisitor.isIgnoreFile();
            } catch (Exception ignored) {
            }

        }

        return valid;
    }

    public static class FindJavaTestFilesVisitor extends SimpleFileVisitor<Path> {
        private final String productionFileName;

        private File productionFilePath = null;

        public FindJavaTestFilesVisitor(String productionFileName) {
            this.productionFileName = productionFileName;
        }

        public File getProductionFilePath() {
            return productionFilePath;
        }

        @Override
        public FileVisitResult visitFile(Path file,
                                         BasicFileAttributes attrs)
                throws IOException {
            if (file.getFileName().toString().equalsIgnoreCase(productionFileName)) {
                productionFilePath = file.toFile();
                return FileVisitResult.TERMINATE;
            }
            return FileVisitResult.CONTINUE;
        }
    }

    /**
     * Visitor class
     */
    private static class ClassVisitor extends VoidVisitorAdapter<Void> {
        private boolean ignoreFile = false;

        public boolean isIgnoreFile() {
            return ignoreFile;
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Void arg) {
            ignoreFile = n.isInterface();
            super.visit(n, arg);
        }

        @Override
        public void visit(AnnotationDeclaration n, Void arg) {
            ignoreFile = true;
            super.visit(n, arg);
        }
    }

}