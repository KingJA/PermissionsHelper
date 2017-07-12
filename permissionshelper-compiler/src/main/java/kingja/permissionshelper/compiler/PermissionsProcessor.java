package kingja.permissionshelper.compiler;

import java.io.IOException;
import java.io.Writer;
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
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import kingja.permissionshelper.annotations.NeedPermissions;
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
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(NeedPermissions.class);
        for (Element element : elements) {
            ExecutableElement executableElement = (ExecutableElement) element;
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();
            String className = typeElement.getQualifiedName().toString();
            mMessager.printMessage(Diagnostic.Kind.NOTE, "className:" + className);
            GeneratedBody generatedBody = generatedBodys.get(className);
            if (generatedBody == null) {
                generatedBody = new GeneratedBody(mElementUtils, typeElement);
                generatedBodys.put(className, generatedBody);
            }
            String[] permissions = executableElement.getAnnotation(NeedPermissions.class).value();
            String[] onShowRationales = executableElement.getAnnotation(OnShowRationale.class).value();
            String[] onPermissionDenieds = executableElement.getAnnotation(OnPermissionDenied.class).value();
            generatedBody.putExecutableElement(permissions, executableElement);
            generatedBody.putShowRationaleElement(onShowRationales, executableElement);
            generatedBody.putPermissionDeniedElements(onPermissionDenieds, executableElement);

        }

        Set<? extends Element> showRationaleElements = roundEnvironment.getElementsAnnotatedWith(OnShowRationale.class);
        for (Element element : elements) {
            ExecutableElement executableElement = (ExecutableElement) element;
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();
            String className = typeElement.getQualifiedName().toString();
            mMessager.printMessage(Diagnostic.Kind.NOTE, "className:" + className);
            GeneratedBody generatedBody = generatedBodys.get(className);
            if (generatedBody == null) {
                generatedBody = new GeneratedBody(mElementUtils, typeElement);
                generatedBodys.put(className, generatedBody);
            }
            String[] onShowRationales = executableElement.getAnnotation(OnShowRationale.class).value();
            generatedBody.putShowRationaleElement(onShowRationales, executableElement);
        }


        for (String key : generatedBodys.keySet()) {
            GeneratedBody generatedBody = generatedBodys.get(key);

            try {
                JavaFileObject sourceFile = mFiler.createSourceFile(generatedBody.getFullClassName());
                Writer writer = sourceFile.openWriter();
                writer.append(generatedBody.getGeneratedCode());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        return true;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> animationTypes = new LinkedHashSet<>();
        animationTypes.add(NeedPermissions.class.getCanonicalName());
        return animationTypes;
    }
}
