package kingja.permissionshelper.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import kingja.permissionshelper.annotations.OnPermissionGranted;


/**
 * Description:TODO
 * Create Time:2017/7/12 14:37
 * Author:KingJA
 * Email:kingjavip@gmail.com
 */
public class GeneratedBody {
    private final String SUFFIX = "PermissionsHelper";
    private static final String REQUEST_ = "REQUEST_";
    private static final String PERMISSIONS_ = "PERMISSIONS_";
    private static final String CheckPermission = "CheckPermission";
    private static final String PermissionRequest = "PermissionRequest";
    private Map<String, ExecutableElement> grantMethods = new LinkedHashMap<>();
    private Map<String, ExecutableElement> rationaleMethods = new LinkedHashMap<>();
    private Map<String, ExecutableElement> deniedMethods = new LinkedHashMap<>();
    private Map<String, ExecutableElement> neverAskMethods = new LinkedHashMap<>();
    private final String fullName;
    private final String packageName;
    private TypeElement typeElement;
    private ClassName PERMISSION_UTILS = ClassName.get("com.kingja.permissionshelper", "PermissionUtils");
    private ClassName PERMISSION_REQUEST = ClassName.get("com.kingja.permissionshelper", "PermissionRequest");
    private ClassName WEAKREFERENCE = ClassName.get("java.lang.ref", "WeakReference");

    private TypeName getClassName(TypeElement typeElement) {
        return ClassName.get(typeElement.asType());
    }

    public GeneratedBody(Elements mElementUtils, TypeElement typeElement) {
        this.typeElement = typeElement;
        PackageElement packageElement = mElementUtils.getPackageOf(typeElement);
        String className = typeElement.getSimpleName().toString();
        packageName = packageElement.getQualifiedName().toString();
        fullName = className + SUFFIX;

    }

    public void generateBody(Filer filer) {
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(fullName);
        typeBuilder.addModifiers(Modifier.FINAL);
        int requestCode = 0;
        /*=================*/
        for (String key : grantMethods.keySet()) {
            ExecutableElement element = grantMethods.get(key);
            typeBuilder.addField(createRequestCodeField(REQUEST_ + element.getSimpleName().toString().toUpperCase(),
                    requestCode++));
        }
        for (String key : grantMethods.keySet()) {
            ExecutableElement element = grantMethods.get(key);
            typeBuilder.addField(createPermissionField(PERMISSIONS_ + element.getSimpleName().toString().toUpperCase(),
                    getPermissions(element)));
        }

        /*========Inner Class=========*/
        for (String key : grantMethods.keySet()) {
            ExecutableElement element = grantMethods.get(key);
            typeBuilder.addType(createPermissionsRequest(element.getSimpleName().toString(), key));
        }

        /*=================*/
        for (String key : grantMethods.keySet()) {
            ExecutableElement element = grantMethods.get(key);
            typeBuilder.addMethod(createCheckPermission(element.getSimpleName().toString(), key, hasRationaleMethod
                    (key)));
        }

        typeBuilder.addMethod(createResult());

        /*=================*/
        JavaFile javaFile = JavaFile.builder(packageName, typeBuilder.build())
                .addFileComment("Generated code from PermissionsHelper. Do not modify!")
                .build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MethodSpec createCheckPermission(String method, String key, boolean hasRationale) {
        String permissionField = PERMISSIONS_ + method.toUpperCase();
        String requestField = REQUEST_ + method.toUpperCase();
        MethodSpec.Builder builder = MethodSpec.methodBuilder(method + CheckPermission);
        builder.returns(TypeName.VOID);
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        builder.addParameter(getClassName(typeElement), "target");
        builder.beginControlFlow("if ($T.hasSelfPermissions(target, " + permissionField + "))", PERMISSION_UTILS);
        builder.addStatement("target." + method + "()");
        builder.nextControlFlow("else");
        builder.beginControlFlow("if($T.shouldShowRequestPermissionRationale(target, " + permissionField + "))",
                PERMISSION_UTILS);
        if (hasRationale) {
            builder.addStatement("target." + getMethodName(rationaleMethods, key) + "(new " +
                    "" + getUpperCamelCase(method) + "(target))");
        }
        builder.nextControlFlow("else");
        builder.addStatement("$T.requestPermissions(target, " + permissionField + ", " + requestField + ")",
                PERMISSION_UTILS);
        builder.endControlFlow();
        builder.endControlFlow();
        return builder.build();
    }

    private MethodSpec createResult() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("onRequestPermissionsResult");
        builder.returns(TypeName.VOID);
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        builder.addParameter(getClassName(typeElement), "target");
        builder.addParameter(int.class, "requestCode");
        builder.addParameter(int[].class, "grantResults");
        builder.beginControlFlow("switch (requestCode)");

        for (String key : grantMethods.keySet()) {
            ExecutableElement element = grantMethods.get(key);
            String method = element.getSimpleName().toString();
            String permissionField = PERMISSIONS_ + method.toUpperCase();
            String requestField = REQUEST_ + method.toUpperCase();

            builder.addCode("case " + requestField + ":\n");

            builder.beginControlFlow("if($T.verifyPermissions(grantResults))", PERMISSION_UTILS);
            builder.addStatement("target." + method + "()");
            builder.nextControlFlow("else");
            builder.beginControlFlow("if(!$T.shouldShowRequestPermissionRationale(target, " + permissionField + "))",
                    PERMISSION_UTILS);
            if (hasNeverAskMethod(key)) {
                builder.addStatement("target." + getMethodName(neverAskMethods, key) + "()");
            }
            if (hasDeniedMethod(key)) {
                builder.nextControlFlow("else");
                builder.addStatement("target." + getMethodName(deniedMethods, key) + "()");
            }
            builder.endControlFlow();
            builder.endControlFlow();
            builder.addStatement("break");

        }
        builder.addCode("default:\n");
        builder.addStatement("break");
        builder.endControlFlow();
        return builder.build();
    }

    private FieldSpec createRequestCodeField(String name, int requestCode) {
        return FieldSpec.builder(int.class, name, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer
                ("$L", requestCode++).build();
    }

    private FieldSpec createPermissionField(String name, String arrStr) {
        return FieldSpec.builder(String[].class, name, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer
                (CodeBlock.of(arrStr)).build();
    }

    private TypeSpec createPermissionsRequest(String name, String key) {
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(getUpperCamelCase(name));
        typeBuilder.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
        typeBuilder.addSuperinterface(PERMISSION_REQUEST);

        String permissionField = PERMISSIONS_ + name.toUpperCase();
        String requestField = REQUEST_ + name.toUpperCase();

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(getClassName(typeElement), "target")
                .addStatement("this.weakTarget = new WeakReference<$T>(target)", getClassName(typeElement))
                .build();

        MethodSpec.Builder proceedBuilder = MethodSpec.methodBuilder("proceed");
        proceedBuilder.returns(TypeName.VOID);
        proceedBuilder.addModifiers(Modifier.PUBLIC);
        proceedBuilder.addAnnotation(Override.class);
        proceedBuilder.addStatement("$T target = weakTarget.get()", getClassName(typeElement));
        proceedBuilder.addStatement("if (target == null) return");
        proceedBuilder.addStatement("$T.requestPermissions(target, " + permissionField + ", " + requestField + ")",
                PERMISSION_UTILS);
        MethodSpec processMethodSpec = proceedBuilder.build();

        MethodSpec.Builder cancelBuilder = MethodSpec.methodBuilder("cancel");
        cancelBuilder.returns(TypeName.VOID);
        cancelBuilder.addModifiers(Modifier.PUBLIC);
        cancelBuilder.addAnnotation(Override.class);

        if (hasDeniedMethod(key)) {
            cancelBuilder.addStatement("$T target = weakTarget.get()", getClassName(typeElement));
            cancelBuilder.addStatement("if (target == null) return");
            cancelBuilder.addStatement("target." + getMethodName(deniedMethods, key) + "()");
        }

        MethodSpec cancelMethodSpec = cancelBuilder.build();


        FieldSpec weakTarget = FieldSpec.builder(ParameterizedTypeName.get(WEAKREFERENCE, getClassName
                (typeElement)), "weakTarget", Modifier.PRIVATE, Modifier.FINAL)
                .build();

        typeBuilder.addMethod(constructor);
        typeBuilder.addMethod(processMethodSpec);
        typeBuilder.addMethod(cancelMethodSpec);
        typeBuilder.addField(weakTarget);


        return typeBuilder.build();
    }


    public void putGrantMethod(String vlaues, ExecutableElement element) {
        grantMethods.put(vlaues, element);
    }

    public void putRationaleMethod(String vlaues, ExecutableElement element) {
        rationaleMethods.put(vlaues, element);
    }

    public void putDeniedMethod(String vlaues, ExecutableElement element) {
        deniedMethods.put(vlaues, element);
    }

    public void putNeverAskMethod(String vlaues, ExecutableElement element) {
        neverAskMethods.put(vlaues, element);
    }


    private boolean hasDeniedMethod(String key) {
        return deniedMethods.get(key) != null;
    }

    private boolean hasRationaleMethod(String key) {
        return rationaleMethods.get(key) != null;
    }

    private boolean hasNeverAskMethod(String key) {
        return neverAskMethods.get(key) != null;
    }

    private String getMethodName(Map<String, ExecutableElement> methodMap, String key) {
        ExecutableElement grantedMethod = methodMap.get(key);
        return grantedMethod.getSimpleName().toString();
    }


    private String getPermissions(ExecutableElement executableElement) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        String[] permissions = executableElement.getAnnotation(OnPermissionGranted.class).value();
        for (int i = 0; i < permissions.length; i++) {
            sb.append("\"");
            sb.append(permissions[i]);
            sb.append("\"");
            if (i != permissions.length - 1) {
                sb.append(",");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private String getUpperCamelCase(String className) {
        String firstLetter = (className.charAt(0) + "").toUpperCase();
        return firstLetter + className.substring(1) + PermissionRequest;
    }

}
