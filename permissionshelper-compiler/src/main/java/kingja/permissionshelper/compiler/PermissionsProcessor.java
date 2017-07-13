package kingja.permissionshelper.compiler;

import java.io.IOException;
import java.io.Writer;
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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import kingja.permissionshelper.annotations.OnNeverAskAgain;
import kingja.permissionshelper.annotations.onPermissionGranted;
import kingja.permissionshelper.annotations.OnPermissionDenied;
import kingja.permissionshelper.annotations.OnShowRationale;


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
        mMessager.printMessage(Diagnostic.Kind.NOTE, "Begin process...");
        processAnimation(roundEnvironment, onPermissionGranted.class);
        processAnimation(roundEnvironment, OnShowRationale.class);
        processAnimation(roundEnvironment, OnPermissionDenied.class);
        processAnimation(roundEnvironment, OnNeverAskAgain.class);


        for (String key : generatedBodys.keySet()) {
            GeneratedBody generatedBody = generatedBodys.get(key);
            try {
                JavaFileObject sourceFile = mFiler.createSourceFile(generatedBody.getFullClassName());
                Writer writer = sourceFile.openWriter();
                writer.append(generatedBody.getGeneratedCode());
                writer.flush();
                writer.close();
            } catch (IOException e) {
//                e.printStackTrace();
            }
        }
        return true;
    }

    private Set<? extends Element> processAnimation(RoundEnvironment roundEnvironment, Class<? extends Annotation>
            clazz) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(clazz);
        for (Element element : elements) {
            ExecutableElement methodElement = (ExecutableElement) element;
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();
            String className = typeElement.getQualifiedName().toString();
            GeneratedBody generatedBody = generatedBodys.get(className);
            if (generatedBody == null) {
                generatedBody = new GeneratedBody(mElementUtils, typeElement, mMessager);
                generatedBodys.put(className, generatedBody);
            }

            putAnnoatationElements(generatedBody, clazz, methodElement);
        }
        return elements;
    }

    private void putAnnoatationElements(GeneratedBody generatedBody, Class<? extends Annotation> clazz,
                                        ExecutableElement methodElement) {
        Annotation annotation = methodElement.getAnnotation(clazz);
        if (annotation instanceof onPermissionGranted) {
            generatedBody.putGrantMethod(getStringFromArr(((onPermissionGranted) annotation).value()), methodElement);
        } else if (annotation instanceof OnShowRationale) {
            generatedBody.putRationaleMethod(getStringFromArr(((OnShowRationale) annotation).value()), methodElement);
        } else if (annotation instanceof OnPermissionDenied) {
            generatedBody.putDeniedMethod(getStringFromArr(((OnPermissionDenied) annotation).value()), methodElement);
        } else if (annotation instanceof OnNeverAskAgain) {
            generatedBody.putNeverAskMethod(getStringFromArr(((OnNeverAskAgain) annotation).value()), methodElement);
        } else {
            error("%s process error", annotation.getClass().getSimpleName());
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> animationTypes = new LinkedHashSet<>();
        animationTypes.add(onPermissionGranted.class.getCanonicalName());
        animationTypes.add(OnShowRationale.class.getCanonicalName());
        animationTypes.add(OnPermissionDenied.class.getCanonicalName());
        return animationTypes;
    }

    private void error(String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
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
}
