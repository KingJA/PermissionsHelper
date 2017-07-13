package sample.kingja.permissionshelper;

import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import kingja.permissionshelper.annotations.OnPermissionDenied;
import kingja.permissionshelper.annotations.onPermissionGranted;
import kingja.permissionshelper.annotations.OnShowRationale;
import sample.kingja.permissionshelper.MainActivityPermissionsHelper;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @onPermissionGranted(Manifest.permission.CAMERA)
    public void openCamera() {
        Log.e(TAG, "openCamera: ");
    }

    @OnShowRationale(Manifest.permission.CAMERA)
    public void onShowRationaleCamera() {
        new AlertDialog.Builder(this)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
//                        ActivityCompat.requestPermissions(PermissionActivity.this, new String[]{Manifest.permission
//                                .CAMERA}, REQUEST_CAMERA);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                    }
                })
                .setCancelable(false)
                .setMessage("需要打开相机权限")
                .show();
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    public void onDeniedCamera() {
        Log.e(TAG, "onDeniedCamera: ");
    }


    @onPermissionGranted({Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS})
    public void getContacts() {
        Log.e(TAG, "getContacts: ");
    }

    @OnShowRationale({Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS})
    public void onShowRationaleContacts() {
        Log.e(TAG, "onShowRationaleContacts: ");

    }

    @OnPermissionDenied({Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS})
    public void onDeniedContacts() {
        Log.e(TAG, "onDeniedContacts: ");
    }

    public void onCamera(View view) {
        MainActivityPermissionsHelper.openCameraCheckPermission(this);
    }
}
