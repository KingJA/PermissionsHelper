package kingja.permissionshelper.compiler;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import kingja.permissionshelper.annotations.NeedPermissions;

/**
 * Description:TODO
 * Create Time:2017/7/12 14:37
 * Author:KingJA
 * Email:kingjavip@gmail.com
 */
public class GeneratedBody {
    private final String SUFFIX = "PermissionsHelper";
    private Map<String[], ExecutableElement> permissionElements = new HashMap<>();
    private Map<String[], ExecutableElement> showRationaleElements = new HashMap<>();
    private Map<String[], ExecutableElement> permissionDeniedElements = new HashMap<>();
    private final String fullName;
    private final String packageName;
    private TypeElement typeElement;

    public GeneratedBody(Elements mElementUtils, TypeElement typeElement) {
        this.typeElement = typeElement;
        PackageElement packageElement = mElementUtils.getPackageOf(typeElement);
        packageName = packageElement.getQualifiedName().toString();
        String className = typeElement.getSimpleName().toString();
        fullName = className + SUFFIX;
    }

    public void putExecutableElement(String[] vlaues, ExecutableElement element) {
        permissionElements.put(vlaues, element);
    }

    public void putShowRationaleElement(String[] vlaues, ExecutableElement element) {
        showRationaleElements.put(vlaues, element);
    }

    public void putPermissionDeniedElements(String[] vlaues, ExecutableElement element) {
        permissionDeniedElements.put(vlaues, element);
    }

    public String getFullClassName() {
        return fullName;
    }

    public String getGeneratedCode() {
        StringBuilder builder = new StringBuilder();
        builder.append("// Generated code. Do not modify!");
        builder.append("\npackage ").append(packageName).append(";");
        builder.append("\n\nimport ").append(packageName).append(".*").append(";");
        builder.append("\n\nimport com.kingja.permissionshelper.*;");
        builder.append("\n\nfinal class ").append(fullName).append(" {");
        builder.append(getCheckPermissionsMethod());

        builder.append("\n}");
        return builder.toString();
    }

    public String getCheckPermissionsMethod() {
        StringBuilder builder = new StringBuilder();
        int requestCode = 0;
        for (String[] key : permissionElements.keySet()) {
            ExecutableElement executableElement = permissionElements.get(key);
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
            //TODO
//            if (PermissionUtils.shouldShowRequestPermissionRationale(target, PERMISSION_SHOWCAMERA)) {
//                target.showRationaleForCamera(new ShowCameraPermissionRequest(target));
//            } else {
//                ActivityCompat.requestPermissions(target, PERMISSION_SHOWCAMERA, REQUEST_SHOWCAMERA);
//            }
            builder.append("\n\t\t}");
            builder.append("\n\t}");
        }
        return builder.toString();
    }

    private String getPermissions(ExecutableElement executableElement) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        String[] permissions = executableElement.getAnnotation(NeedPermissions.class).value();
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

//    public void onCamera(View view) {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager
// .PERMISSION_GRANTED) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {//
//                showAllowDialog();
//            } else {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
//            }
//        } else {
//            openCamera();
//        }
//    }
}
