package kingja.permissionshelper.compiler;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import kingja.permissionshelper.annotations.OnNeverAskAgain;
import kingja.permissionshelper.annotations.OnPermissionDenied;
import kingja.permissionshelper.annotations.OnPermissionGranted;
import kingja.permissionshelper.annotations.OnShowRationale;

import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Description:TODO
 * Create Time:2017/7/11 17:27
 * Author:KingJA
 * Email:kingjavip@gmail.com
 */
public class PermissionsProcessor extends AbstractProcessor {

    private Messager mMessager;
    private Map<String, GeneratedBody> generatedBodys = new HashMap<>();
    private Elements mElementUtils;
    private Filer mFiler;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mMessager = processingEnvironment.getMessager();
        mElementUtils = processingEnvironment.getElementUtils();
        mFiler = processingEnvironment.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        generatedBodys.clear();
        processAnimation(roundEnvironment, OnPermissionGranted.class);
        processAnimation(roundEnvironment, OnShowRationale.class);
        processAnimation(roundEnvironment, OnPermissionDenied.class);
        processAnimation(roundEnvironment, OnNeverAskAgain.class);


        for (String key : generatedBodys.keySet()) {
            GeneratedBody generatedBody = generatedBodys.get(key);
            generatedBody.generateBody(mFiler);
        }
        return true;
    }

    private boolean processAnimation(RoundEnvironment roundEnvironment, Class<? extends Annotation>
            clazz) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(clazz);
        for (Element element : elements) {
            if (!validateElement(element, clazz)) {
                return false;
            }
            ExecutableElement methodElement = (ExecutableElement) element;
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();
            String className = typeElement.getQualifiedName().toString();
            GeneratedBody generatedBody = generatedBodys.get(className);
            if (generatedBody == null) {
                generatedBody = new GeneratedBody(mElementUtils, typeElement);
                generatedBodys.put(className, generatedBody);
            }
            putAnnoatationElements(generatedBody, clazz, methodElement);
        }
        return true;
    }


    private void putAnnoatationElements(GeneratedBody generatedBody, Class<? extends Annotation> clazz,
                                        ExecutableElement methodElement) {
        Annotation annotation = methodElement.getAnnotation(clazz);
        if (annotation instanceof OnPermissionGranted) {
            generatedBody.putGrantMethod(getStringFromArr(((OnPermissionGranted) annotation).value()), methodElement);
        } else if (annotation instanceof OnShowRationale) {
            generatedBody.putRationaleMethod(getStringFromArr(((OnShowRationale) annotation).value()), methodElement);
        } else if (annotation instanceof OnPermissionDenied) {
            generatedBody.putDeniedMethod(getStringFromArr(((OnPermissionDenied) annotation).value()), methodElement);
        } else if (annotation instanceof OnNeverAskAgain) {
            generatedBody.putNeverAskMethod(getStringFromArr(((OnNeverAskAgain) annotation).value()), methodElement);
        } else {
            printError("%s is a  unknown Annotation type", annotation.getClass().getSimpleName());
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> animationTypes = new LinkedHashSet<>();
        animationTypes.add(OnPermissionGranted.class.getCanonicalName());
        animationTypes.add(OnShowRationale.class.getCanonicalName());
        animationTypes.add(OnPermissionDenied.class.getCanonicalName());
        return animationTypes;
    }


    private String getStringFromArr(String[] arr) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < arr.length; i++) {
            sb.append("\"");
            sb.append(arr[i]);
            sb.append("\"");
            if (i != arr.length - 1) {
                sb.append(",");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private void printError(String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        mMessager.printMessage(Diagnostic.Kind.NOTE, message);
    }

    private boolean validateElement(Element element, Class<? extends Annotation> clazz) {
        if (element.getKind() != ElementKind.METHOD) {
            printError("%s must be eclared on method ", clazz.getClass().getSimpleName());
            return false;
        }

        if (!element.getModifiers().contains(PUBLIC)) {
            printError("the modifier of %s() must be public", element.getSimpleName());
            return false;
        }
        return true;
    }
}
