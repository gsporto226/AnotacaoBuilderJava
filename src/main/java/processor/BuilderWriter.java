package processor;

import com.squareup.javapoet.*;
import utils.Pair;

import javax.annotation.processing.Filer;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

class BuilderWriter {
    private static final String SUFFIX = "Builder";
    private static final String FIELD_PREFIX = "_";
    public static void write(Filer filer, BuilderData builderData) {
        JavaFile javaFile = JavaFile.builder(builderData.packageName(), buildBuilderClass(builderData)).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException exception) {}
    }

    private static String prefixFieldName(String fieldName) {
        return FIELD_PREFIX + fieldName;
    }
    private static TypeSpec buildBuilderClass(BuilderData builderData) {
        var builtClassName = builderData.classNameTypePair().getFirst();
        var builderName = builtClassName + SUFFIX;
        var builderSpecBuilder = TypeSpec.classBuilder(builderName)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariables(builderData.typeParameterNames()
                        .stream()
                        .map(TypeVariableName::get)
                        .collect(Collectors.toList())
                );
        var fieldsAndMethodsPair = buildFieldsAndMethods(builderData, builderName);
        builderSpecBuilder.addFields(fieldsAndMethodsPair.getFirst());
        builderSpecBuilder.addMethods(fieldsAndMethodsPair.getSecond());
        builderSpecBuilder.addMethod(buildBuildMethodSpec(builderData));
        return builderSpecBuilder.build();
    }

    private static Pair<List<FieldSpec>, List<MethodSpec>> buildFieldsAndMethods(BuilderData builderData, String builderName) {
        List<FieldSpec> fieldSpecs = new ArrayList<>();
        List<MethodSpec> methodSpecs = new ArrayList<>();
        for (Pair<String, TypeMirror> pair : builderData.properties()) {
            var fieldAndSetterPair = buildFieldAndSetter(builderData, builderName, pair);
            fieldSpecs.add(fieldAndSetterPair.getFirst());
            methodSpecs.add(fieldAndSetterPair.getSecond());
        }
        return new Pair<>(fieldSpecs, methodSpecs);
    }

    private static Pair<FieldSpec, MethodSpec> buildFieldAndSetter(
            BuilderData builderData,
            String builderName,
            Pair<String, TypeMirror> pair
    ) {
        var methodName = pair.getFirst();
        var fieldName = prefixFieldName(methodName);
        var fieldTypeName = TypeName.get(pair.getSecond());
        var builderClassName = ClassName.get(builderData.packageName(), builderName);
        var fieldSpec = FieldSpec.builder(fieldTypeName, fieldName, Modifier.PRIVATE).build();
        var methodSpec = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(fieldTypeName, pair.getFirst())
                .addStatement("this.$L = $L", fieldName, methodName)
                .addStatement("return this")
                .returns(builderClassName)
                .build();
        return new Pair(fieldSpec, methodSpec);
    }

    private static MethodSpec buildBuildMethodSpec(BuilderData builderData) {
        var builtClassTypeName = TypeName.get(builderData.classNameTypePair().getSecond());
        var constructorArgumentsJoined = builderData.properties()
                .stream()
                .map(pair -> String.format("this.%s", prefixFieldName(pair.getFirst())))
                .collect(Collectors.joining(","));
        var buildMethodSpec = MethodSpec.methodBuilder("build")
                .returns(builtClassTypeName)
                .addStatement("return new $T($L)", builtClassTypeName, constructorArgumentsJoined);
        return buildMethodSpec.build();
    }
}