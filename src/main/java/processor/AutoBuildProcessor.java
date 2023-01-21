package processor;

import annotations.AutoBuilder;
import utils.Check;
import utils.Pair;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AutoBuildProcessor {
    public static boolean process(RoundEnvironment environment) {
        var annotatedElements = environment.getElementsAnnotatedWith(AutoBuilder.class);
        var annotatedElementsIsNotEmpty = !annotatedElements.isEmpty();
        annotatedElements.parallelStream()
                .map(AutoBuildProcessor::mapToBuilderData)
                .filter(Optional::isPresent)
                .forEach(optional -> BuilderWriter.write(MainProcessor.filer, optional.get()));
        return annotatedElementsIsNotEmpty;
    }

    private static Optional<BuilderData> mapToBuilderData(Element annotatedElement) {
        var constructor = (ExecutableElement) annotatedElement;
        var parentElement = (TypeElement) constructor.getEnclosingElement();
        var grandParentElement = parentElement.getEnclosingElement();
        var packageName = parentElement.getQualifiedName().toString();
        if (grandParentElement instanceof QualifiedNameable qualifiedNameable) {
            packageName = qualifiedNameable.getQualifiedName().toString();
        }
        var modifiers = constructor.getModifiers();
        @SuppressWarnings("unchecked")
        List<TypeParameterElement> typeParameterElements = (List<TypeParameterElement>) parentElement.getTypeParameters();
        if (Check.check(modifiers.contains(Modifier.PUBLIC), new Element[]{constructor},"Construtor anotado com @AutoBuilder deve ser público")) {
            var properties = constructor.getParameters()
                    .stream()
                    .map(parameter -> new Pair<>(parameter.getSimpleName().toString(), parameter.asType()))
                    .collect(Collectors.toList());
            var classNameTypePair = new Pair<>(parentElement.getSimpleName().toString(), parentElement.asType());
            return Optional.of(new BuilderData(packageName, typeParameterElements, properties, classNameTypePair));
        }
        return Optional.empty();
    }
}
