package sample.kingja.permissionshelper;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import kingja.permissionshelper.annotations.NeedPermissions;
import kingja.permissionshelper.annotations.OnShowRationale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @NeedPermissions(Manifest.permission.CAMERA)
    public void openCamera() {
        Log.e(TAG, "openCamera: ");
    }

    @OnShowRationale(Manifest.permission.CAMERA)
    public void onShowRationaleCamera() {
        Log.e(TAG, "onShowRationaleCamera: ");
    }


    @NeedPermissions({Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS})
    public void getContacts() {
        Log.e(TAG, "getContacts: ");
    }
}
