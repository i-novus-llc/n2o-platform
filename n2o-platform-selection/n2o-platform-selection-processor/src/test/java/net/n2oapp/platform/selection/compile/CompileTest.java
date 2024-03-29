package net.n2oapp.platform.selection.compile;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.JavaFileObjects;
import net.n2oapp.platform.selection.integration.model.*;
import net.n2oapp.platform.selection.processor.SelectionProcessor;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.google.testing.compile.Compiler.javac;

class CompileTest {

    @Test
    void testCompileNoErrors() throws IOException {
        List<String> classes = List.of(
            Address.class.getSimpleName(),
            AddressModel.class.getSimpleName(),
            AModel.class.getSimpleName(),
            BaseModel.class.getSimpleName(),
            BModel.class.getSimpleName(),
            Contact.class.getSimpleName(),
            Employee.class.getSimpleName(),
            Model.class.getSimpleName(),
            Organisation.class.getSimpleName(),
            Passport.class.getSimpleName(),
            Project.class.getSimpleName(),
            TestModel.class.getSimpleName()
        );
        final String packageQualified = "net.n2oapp.platform.selection.integration.model";
        List<JavaFileObject> sources = new ArrayList<>();
        for (String clazz : classes) {
            sources.add(
                JavaFileObjects.forSourceString(
                    packageQualified + "." + clazz,
                    getModelSourceCode(clazz)
                )
            );
        }
        Compilation compilation = javac().withProcessors(new SelectionProcessor()).compile(sources);
        CompilationSubject.assertThat(compilation).succeeded();
    }

    private String getModelSourceCode(String sourceFile) throws IOException {
        Path path = Path.of("src", "test", "java", "net", "n2oapp", "platform", "selection", "integration", "model", sourceFile + ".java");
        return Files.readString(path);
    }

}
