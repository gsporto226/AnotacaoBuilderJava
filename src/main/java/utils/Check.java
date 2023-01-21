package utils;

import processor.MainProcessor;

import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

public class Check {

    public static boolean check(boolean value, Element[] elements, String message, Object... args) {
        if (!value) {
            printErrorMessage(elements, message, args);
        }
        return value;
    }

    private static void printErrorMessage(Element[] elements, String message, Object... args) {
        for (Element element : elements) {
            MainProcessor.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    String.format(message, args),
                    element
            );
        }
    }
}
