package processor;

import annotations.AutoBuilder;
import com.google.auto.service.AutoService;
import processor.AutoBuildProcessor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Set;

@AutoService(Processor.class)
public class MainProcessor extends AbstractProcessor {
    public static Filer filer;
    public static Elements elements;
    public static Messager messager;
    public static Types types;

    @Override
    public synchronized void init(ProcessingEnvironment environment) {
        super.init(environment);
        types = environment.getTypeUtils();
        elements = environment.getElementUtils();
        filer = environment.getFiler();
        messager = environment.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment environment) {
        return AutoBuildProcessor.process(environment);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(
            AutoBuilder.class.getCanonicalName()
        );
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
