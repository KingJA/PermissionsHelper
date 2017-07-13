package kingja.permissionshelper.compiler;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import kingja.permissionshelper.annotations.onPermissionGranted;

/**
 * Description:TODO
 * Create Time:2017/7/12 14:37
 * Author:KingJA
 * Email:kingjavip@gmail.com
 */
public class GeneratedBody {
    private final String SUFFIX = "PermissionsHelper";
    private Map<String, ExecutableElement> grantMethods = new LinkedHashMap<>();
    private Map<String, ExecutableElement> rationaleMethods = new LinkedHashMap<>();
    private Map<String, ExecutableElement> deniedMethods = new LinkedHashMap<>();
    private Map<String, ExecutableElement> neverAskMethods = new LinkedHashMap<>();
    private final String fullName;
    private final String packageName;
    private TypeElement typeElement;
    private Messager mMessager;

    public GeneratedBody(Elements mElementUtils, TypeElement typeElement, Messager mMessager) {
        this.typeElement = typeElement;
        this.mMessager = mMessager;
        PackageElement packageElement = mElementUtils.getPackageOf(typeElement);
        packageName = packageElement.getQualifiedName().toString();
        String className = typeElement.getSimpleName().toString();
        fullName = className + SUFFIX;
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

    public String getFullClassName() {
        return fullName;
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    public String getGeneratedCode() {
        StringBuilder builder = new StringBuilder();
        builder.append("// Generated code. Do not modify!");
        builder.append("\npackage ").append(packageName).append(";");
        builder.append("\n\nimport ").append(packageName).append(".*").append(";");
        builder.append("\n\nimport com.kingja.permissionshelper.*;");
        builder.append("\n\nfinal class ").append(fullName).append(" {");
        builder.append(getGrantedMethod());

        builder.append(getRequestPermissionsResult());

        builder.append("\n}");
        return builder.toString();
    }

    private boolean hasDeniedMethod(String key) {
        return deniedMethods.get(key) != null;
    }

    private boolean hasNeverAskMethod(String key) {
        return neverAskMethods.get(key) != null;
    }

    private String getMethodName(Map<String, ExecutableElement> methodMap, String key) {
        ExecutableElement grantedMethod = methodMap.get(key);
        return grantedMethod.getSimpleName().toString();
    }

    private String getRequestPermissionsResult() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n\tpublic static void onRequestPermissionsResult(MainActivity target, int requestCode, int[]" +
                " grantResults) {");
        builder.append("\n\t\tswitch (requestCode) {");

        for (String key : grantMethods.keySet()) {
            ExecutableElement grantedMethod = grantMethods.get(key);
            String grantedName = grantedMethod.getSimpleName().toString();

            builder.append("\n\t\t\tcase REQUEST_").append(grantedName.toUpperCase()).append(":");
            builder.append("\n\t\t\t\tif (PermissionUtils.verifyPermissions(grantResults)) {");
            builder.append("\n\t\t\t\t\ttarget.").append(grantedName).append("();");
            builder.append("\n\t\t\t\t} else {");

            builder.append("\n\t\t\t\t\tif (!PermissionUtils.shouldShowRequestPermissionRationale(target, ").append
                    ("PERMISSIONS_").append(grantedName.toUpperCase()).append(")) {");
            if (hasNeverAskMethod(key)) {
                builder.append("\n\t\t\t\t\t\ttarget.").append(getMethodName(neverAskMethods, key)).append("();");
            }

            builder.append("\n\t\t\t\t\t} else {");
            if (hasDeniedMethod(key)) {
                builder.append("\n\t\t\t\t\t\ttarget.").append(getMethodName(deniedMethods, key)).append("();");
            }

            builder.append("\n\t\t\t\t\t}");

            builder.append("\n\t\t\t\t}");
            builder.append("\n\t\t\t\tbreak;");


        }


        builder.append("\n\t\t\tdefault:");
        builder.append("\n\t\t\t\tbreak;");
        builder.append("\n\t\t\t}");
        builder.append("\n\t}");

        return builder.toString();
    }

    public String getGrantedMethod() {
        StringBuilder builder = new StringBuilder();
        int requestCode = 0;
        for (String key : grantMethods.keySet()) {
            ExecutableElement executableElement = grantMethods.get(key);
            String methodName = executableElement.getSimpleName().toString();
            String requestVar = "REQUEST_" + methodName.toUpperCase();
            String permissionsVar = "PERMISSIONS_" + methodName.toUpperCase();
            builder.append("\nprivate static final int ").append(requestVar).append(" = ").append
                    (requestCode++).append(";");
            builder.append("\nprivate static final String[]").append(permissionsVar).append(" = ").append
                    (getPermissions(executableElement)).append(";");
            builder.append("\n\tpublic static void ").append(methodName).append("CheckPermission (").append
                    (typeElement.getSimpleName().toString()).append(" target) {");
            builder.append("\n\t\tif (PermissionUtils.hasSelfPermissions(target, ").append(permissionsVar).append("))" +
                    " {");
            builder.append("\n\t\t\ttarget.").append(methodName).append("();");
            builder.append("\n\t\t} else {");
            builder.append("\n\t\t\tif (PermissionUtils.shouldShowRequestPermissionRationale(target, ").append
                    (permissionsVar).append(")) {");
            builder.append("\n\t\t\t\ttarget.").append(rationaleMethods.get(key).getSimpleName()).append("();");
            builder.append("\n\t\t\t} else {");
            builder.append("\n\t\t\t\tPermissionUtils.requestPermissions(target, ").append(permissionsVar).append("," +
                    "").append(requestVar).append(");");
            builder.append("\n\t\t\t}");
            builder.append("\n\t\t}");
            builder.append("\n\t}");
        }
        return builder.toString();
    }

    private String getPermissions(ExecutableElement executableElement) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        String[] permissions = executableElement.getAnnotation(onPermissionGranted.class).value();
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


}
