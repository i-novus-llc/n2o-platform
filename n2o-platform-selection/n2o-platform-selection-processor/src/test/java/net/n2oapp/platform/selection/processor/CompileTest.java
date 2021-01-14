package net.n2oapp.platform.selection.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import static com.google.testing.compile.Compiler.javac;

public class CompileTest {

    private static final String TARGET_PACKAGE = CompileTest.class.getPackageName();

    @Test
    public void testCompileNoErrors() {
        Compilation compilation = javac().
                withProcessors(new SelectionProcessor()).
                compile(JavaFileObjects.forResource("TestModel.java"));
        CompilationSubject.assertThat(compilation).succeeded();
        for (int i = 1; i <= 12; i++) {
            CompilationSubject.assertThat(compilation).generatedSourceFile(source("Test" + i + "Selection"));
            CompilationSubject.assertThat(compilation).generatedSourceFile(source("Test" + i + "Mapper"));
            CompilationSubject.assertThat(compilation).generatedSourceFile(source("DefaultTest" + i + "Selection"));
        }
    }

    private String source(String simpleName) {
        return TARGET_PACKAGE + "/" + simpleName;
    }

}
