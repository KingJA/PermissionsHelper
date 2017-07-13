package sample.kingja.permissionshelper;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.kingja.permissionshelper.PermissionRequest;

import kingja.permissionshelper.annotations.OnNeverAskAgain;
import kingja.permissionshelper.annotations.OnPermissionDenied;
import kingja.permissionshelper.annotations.OnShowRationale;
import kingja.permissionshelper.annotations.OnPermissionGranted;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @OnPermissionGranted(Manifest.permission.CAMERA)
    public void openCamera() {
        Log.e(TAG, "openCamera: ");
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    public void onNeverAskOpenCamera() {
        showSettingdDialog("相机");
    }

    @OnShowRationale(Manifest.permission.CAMERA)
    public void onShowRationaleCamera(PermissionRequest request) {
        showRationaleDialog("需要打开相机权限", request);
    }


    @OnPermissionDenied(Manifest.permission.CAMERA)
    public void onDeniedCamera() {
        Log.e(TAG, "onDeniedCamera: ");
    }


    @OnPermissionGranted({Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS})
    public void getContacts() {
        Log.e(TAG, "getContacts: ");
    }

    @OnShowRationale({Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS})
    public void onShowRationaleContacts(PermissionRequest request) {
        Log.e(TAG, "onShowRationaleContacts: ");

    }

//    @OnPermissionDenied({Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS})
//    public void onDeniedContacts() {
//        Log.e(TAG, "onDeniedContacts: ");
//    }

    public void onCamera(View view) {
        sample.kingja.permissionshelper.MainActivityPermissionsHelper.openCameraCheckPermission(this);
    }

    public void showSettingdDialog(String message) {
        new AlertDialog.Builder(this)
                .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAppSettings();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setCancelable(false)
                .setMessage(String.format("当前应用缺少%s权限\n请点击\"设置\"-\"权限\"打开全选\n最后点击两次返回按钮即可返回应用", message))
                .show();
    }

    private void showRationaleDialog(String message, final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage(message)
                .show();
    }

    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        sample.kingja.permissionshelper.MainActivityPermissionsHelper.onRequestPermissionsResult(this, requestCode,
                grantResults);
    }
}
