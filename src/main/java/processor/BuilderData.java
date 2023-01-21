package processor;

import utils.Pair;

import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

public record BuilderData(
        String packageName,
        List<TypeParameterElement> typeParameterNames,
        List<Pair<String, TypeMirror>> properties,
        Pair<String, TypeMirror> classNameTypePair
) { }