package sample.kingja.permissionshelper;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import kingja.permissionshelper.annotations.NeedPermissions;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @NeedPermissions("abc")
    public void openCamera() {

    }
}
