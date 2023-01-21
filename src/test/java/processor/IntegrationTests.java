package processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.hamcrest.CoreMatchers.is;

public class IntegrationTests {

    @Test
    public void autoBuilder_AnnotatedElementIsMethod_Throws() {
        Compilation compilation =
                javac()
                        .withProcessors(new MainProcessor())
                        .compile(JavaFileObjects.forSourceLines(
                                "Test",
                                "import annotations.AutoBuilder;",
                                "class Test {",
                                "   public @AutoBuilder void method() {}",
                                "}"
                        ));
        assertThat(compilation).hadErrorContainingMatch("annotation type not applicable to this kind of declaration");
    }

    @Test
    public void autoBuilder_AnnotatedElementIsClass_Throws() {
        Compilation compilation =
                javac()
                        .withProcessors(new MainProcessor())
                        .compile(JavaFileObjects.forSourceLines(
                                "Test",
                                "import annotations.AutoBuilder;",
                                "@AutoBuilder class Test {",
                                "   public void method() {}",
                                "}"
                        ));
        assertThat(compilation).hadErrorContainingMatch("annotation type not applicable to this kind of declaration");
    }

    @Test
    public void autoBuilder_AnnotatedConstructorNotPublic_Throws() {
        Compilation compilation =
                javac()
                        .withProcessors(new MainProcessor())
                        .compile(JavaFileObjects.forSourceLines(
                                "Test",
                                "import annotations.AutoBuilder;",
                                "class Test {",
                                "   private @AutoBuilder Test() {}",
                                "}"
                        ));
        assertThat(compilation).hadErrorContainingMatch("Construtor anotado com @AutoBuilder deve ser público");
    }

    @Test
    public void autoBuilder_EmptyConstructor_Success() throws IOException {
        Compilation compilation =
                javac()
                        .withProcessors(new MainProcessor())
                        .compile(JavaFileObjects.forSourceLines(
                                "Test",
                                "import annotations.AutoBuilder;",
                                "class Test {",
                                "   public @AutoBuilder Test() {}",
                                "}"
                        ));
        assertThat(compilation).succeeded();
        String expectedGeneratedFile = """
               public class TestBuilder {
                 Test build() {
                   return new Test();
                 }
               }
               """;
        String actualGeneratedFile =  compilation.generatedSourceFile("TestBuilder")
                .orElseThrow().getCharContent(false).toString();
        assertThat(actualGeneratedFile, is(expectedGeneratedFile));

    }

    @Test
    public void autoBuilder_NonGenericArgumentConstructor_Success() throws IOException {
        Compilation compilation =
                javac()
                        .withProcessors(new MainProcessor())
                        .compile(JavaFileObjects.forSourceLines(
                                "Test",
                                "import annotations.AutoBuilder;",
                                "import java.util.Locale;",
                                "class Test {",
                                "   public @AutoBuilder Test(boolean bool, String string, Locale locale) {}",
                                "}"
                        ));
        assertThat(compilation).succeeded();
        String expectedClass = """
                    import java.lang.String;
                    import java.util.Locale;
                            
                    public class TestBuilder {
                      private boolean _bool;
                            
                      private String _string;
                            
                      private Locale _locale;
                            
                      public TestBuilder bool(boolean bool) {
                        this._bool = bool;
                        return this;
                      }
                            
                      public TestBuilder string(String string) {
                        this._string = string;
                        return this;
                      }
                            
                      public TestBuilder locale(Locale locale) {
                        this._locale = locale;
                        return this;
                      }
                            
                      Test build() {
                        return new Test(this._bool,this._string,this._locale);
                      }
                    }
                    """;
        String actualGeneratedFile =  compilation.generatedSourceFile("TestBuilder")
                .orElseThrow().getCharContent(false).toString();
        assertThat(actualGeneratedFile, is(expectedClass));
    }

    @Test
    public void autoBuilder_GenericArgumentConstructor_Success() throws IOException {
        Compilation compilation =
                javac()
                        .withProcessors(new MainProcessor())
                        .compile(JavaFileObjects.forSourceLines(
                                "Test",
                                "import annotations.AutoBuilder;",
                                "import java.util.List;",
                                "import java.util.Map;",
                                "class Test {",
                                "   public @AutoBuilder Test(List<Boolean> list, Map<String, Boolean> map) {}",
                                "}"
                        ));
        assertThat(compilation).succeeded();
        String expectedClass = """
                    import java.lang.Boolean;
                    import java.lang.String;
                    import java.util.List;
                    import java.util.Map;
                    
                    public class TestBuilder {
                      private List<Boolean> _list;
                    
                      private Map<String, Boolean> _map;
                    
                      public TestBuilder list(List<Boolean> list) {
                        this._list = list;
                        return this;
                      }
                    
                      public TestBuilder map(Map<String, Boolean> map) {
                        this._map = map;
                        return this;
                      }
                    
                      Test build() {
                        return new Test(this._list,this._map);
                      }
                    }
                    """;
        String actualGeneratedFile =  compilation.generatedSourceFile("TestBuilder")
                .orElseThrow().getCharContent(false).toString();
        assertThat(actualGeneratedFile, is(expectedClass));
    }

    @Test
    public void autoBuilder_WithGenericArguments_Success() throws IOException {
        Compilation compilation =
                javac()
                        .withProcessors(new MainProcessor())
                        .compile(JavaFileObjects.forSourceLines(
                                "Test",
                                "import annotations.AutoBuilder;",
                                "import java.util.List;",
                                "class Test<T> {",
                                "   public @AutoBuilder Test(List<T> list) {}",
                                "}"
                        ));
        assertThat(compilation).succeeded();
        String expectedClass = """
                    import java.util.List;
                    
                    public class TestBuilder<T> {
                      private List<T> _list;
                    
                      public TestBuilder list(List<T> list) {
                        this._list = list;
                        return this;
                      }
                    
                      Test<T> build() {
                        return new Test<T>(this._list);
                      }
                    }
                    """;
        String actualGeneratedFile =  compilation.generatedSourceFile("TestBuilder")
                .orElseThrow().getCharContent(false).toString();
        assertThat(actualGeneratedFile, is(expectedClass));
    }

    @Test
    public void autoBuilder_WithBoundedGenericArguments_Success() throws IOException {
        Compilation compilation =
                javac()
                        .withProcessors(new MainProcessor())
                        .compile(JavaFileObjects.forSourceLines(
                                "Test",
                                "import annotations.AutoBuilder;",
                                "import java.lang.Boolean;",
                                "import java.util.List;",
                                "class Test<T extends Boolean> {",
                                "   public @AutoBuilder Test(List<T> list) {}",
                                "}"
                        ));
        assertThat(compilation).succeeded();
        String expectedClass = """
                    import java.lang.Boolean;
                    import java.util.List;
                    
                    public class TestBuilder<T extends Boolean> {
                      private List<T> _list;
                    
                      public TestBuilder list(List<T> list) {
                        this._list = list;
                        return this;
                      }
                    
                      Test<T> build() {
                        return new Test<T>(this._list);
                      }
                    }
                    """;
        String actualGeneratedFile =  compilation.generatedSourceFile("TestBuilder")
                .orElseThrow().getCharContent(false).toString();
        assertThat(actualGeneratedFile, is(expectedClass));
    }

    @Test
    public void autoBuilder_WithUnboundedGenericArguments_Success() throws IOException {
        Compilation compilation =
                javac()
                        .withProcessors(new MainProcessor())
                        .compile(JavaFileObjects.forSourceLines(
                                "Test",
                                "import annotations.AutoBuilder;",
                                "import java.lang.Boolean;",
                                "import java.util.List;",
                                "class Test<T extends List<?>> {",
                                "   public @AutoBuilder Test(T list) {}",
                                "}"
                        ));
        assertThat(compilation).succeeded();
        String expectedClass = """
                    import java.util.List;
                    
                    public class TestBuilder<T extends List<?>> {
                      private T _list;
                    
                      public TestBuilder list(T list) {
                        this._list = list;
                        return this;
                      }
                    
                      Test<T> build() {
                        return new Test<T>(this._list);
                      }
                    }
                    """;
        String actualGeneratedFile =  compilation.generatedSourceFile("TestBuilder")
                .orElseThrow().getCharContent(false).toString();
        assertThat(actualGeneratedFile, is(expectedClass));
    }
}
