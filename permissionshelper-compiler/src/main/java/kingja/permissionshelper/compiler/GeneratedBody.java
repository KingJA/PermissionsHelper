package kingja.permissionshelper.compiler;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;
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
    private Map<String, ExecutableElement> grantMethods = new LinkedHashMap<>();
    private Map<String, ExecutableElement> rationaleMethods = new LinkedHashMap<>();
    private Map<String, ExecutableElement> deniedMethods = new LinkedHashMap<>();
    private Map<String, ExecutableElement> neverAskMethods = new LinkedHashMap<>();
    private final String fullName;
    private final String packageName;
    private TypeElement typeElement;

    public GeneratedBody(Elements mElementUtils, TypeElement typeElement) {
        this.typeElement = typeElement;
        PackageElement packageElement = mElementUtils.getPackageOf(typeElement);
        String className = typeElement.getSimpleName().toString();
        packageName = packageElement.getQualifiedName().toString();
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
        builder.append("// Generated code from PermissionsHelper. Do not modify!");
        builder.append("\npackage ").append(packageName).append(";");
        builder.append("\n\nimport ").append(packageName).append(".*").append(";");
        builder.append("\n\nimport com.kingja.permissionshelper.*;");
        builder.append("\n\nfinal class ").append(fullName).append(" {");
        builder.append(getVariable());
        builder.append(getGrantedMethod());
        builder.append(getRequestPermissionsResult());
        builder.append(getPermissionRequest());
        builder.append("\n}");
        return builder.toString();
    }


    private String getVariable() {
        StringBuilder builder = new StringBuilder();
        int requestCode = 0;
        for (String key : grantMethods.keySet()) {
            ExecutableElement executableElement = grantMethods.get(key);
            String methodName = executableElement.getSimpleName().toString().toUpperCase();
            builder.append("\n\tprivate static final int ").append(REQUEST_).append(methodName).append(" = ").append
                    (requestCode++).append(";");
            builder.append("\n\tprivate static final String[]").append(PERMISSIONS_).append(methodName).append(" = ")
                    .append
                            (getPermissions(executableElement)).append(";");
        }
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

            builder.append("\n\t\t\tcase ").append(REQUEST_).append(grantedName.toUpperCase()).append(":");
            builder.append("\n\t\t\t\tif (PermissionUtils.verifyPermissions(grantResults)) {");
            builder.append("\n\t\t\t\t\ttarget.").append(grantedName).append("();");
            builder.append("\n\t\t\t\t} else {");

            builder.append("\n\t\t\t\t\tif (!PermissionUtils.shouldShowRequestPermissionRationale(target, ").append
                    (PERMISSIONS_).append(grantedName.toUpperCase()).append(")) {");
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

    //new ShowContactsPermissionRequest(target
    public String getGrantedMethod() {
        StringBuilder builder = new StringBuilder();
        for (String key : grantMethods.keySet()) {
            ExecutableElement element = grantMethods.get(key);
            String methodUpperCase = element.getSimpleName().toString().toUpperCase();
            String requestVar = REQUEST_ + methodUpperCase;
            String permissionsVar = PERMISSIONS_ + methodUpperCase;
            builder.append("\n\tpublic static void ").append(element.getSimpleName()).append("CheckPermission (").append
                    (typeElement.getSimpleName().toString()).append(" target) {");
            builder.append("\n\t\tif (PermissionUtils.hasSelfPermissions(target, ").append(permissionsVar).append("))" +
                    " {");
            builder.append("\n\t\t\ttarget.").append(element.toString()).append(";");
            builder.append("\n\t\t} else {");
            builder.append("\n\t\t\tif (PermissionUtils.shouldShowRequestPermissionRationale(target, ").append
                    (permissionsVar).append(")) {");
            builder.append("\n\t\t\t\ttarget.").append(rationaleMethods.get(key).getSimpleName()).append("(new ")
                    .append(element.getSimpleName()).append("PermissionRequest (target));");
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

    private String getPermissionRequest() {
        StringBuilder builder = new StringBuilder();
        for (String key : grantMethods.keySet()) {
            ExecutableElement element = grantMethods.get(key);
            String methodUpperCase = element.getSimpleName().toString().toUpperCase();
            String requestVar = REQUEST_ + methodUpperCase;
            String permissionsVar = PERMISSIONS_ + methodUpperCase;
            builder.append("\n\tpublic static final class ").append(element.getSimpleName()).append
                    ("PermissionRequest implements PermissionRequest {");

            builder.append("\n\t\tprivate final java.lang.ref.WeakReference<MainActivity> weakTarget;");
            builder.append("\n\n\t\tprivate ").append(element.getSimpleName()).append("PermissionRequest(MainActivity" +
                    " target) {");

            builder.append("\n\t\t\tthis.weakTarget = new java.lang.ref.WeakReference<MainActivity>(target);");

            builder.append("\n\t\t}");
            builder.append("\n\n\t\t@Override");
            builder.append("\n\t\tpublic void proceed() {");

            builder.append("\n\t\t\tMainActivity target = weakTarget.get();");
            builder.append("\n\t\t\tif (target == null) return;");
            builder.append("\n\t\t\tPermissionUtils.requestPermissions(target, ").append(permissionsVar).append(",")
                    .append
                            (requestVar).append(");");

            builder.append("\n\t\t}");

            builder.append("\n\n\t\t@Override");
            builder.append("\n\t\tpublic void cancel() {");

            if (hasDeniedMethod(key)) {
                builder.append("\n\t\t\tMainActivity target = weakTarget.get();");
                builder.append("\n\t\t\tif (target == null) return;");
                builder.append("\n\t\t\ttarget.").append(deniedMethods.get(key)).append(";");
            }

            builder.append("\n\t\t}");

            builder.append("\n\t}");
        }

        return builder.toString();
    }
}
