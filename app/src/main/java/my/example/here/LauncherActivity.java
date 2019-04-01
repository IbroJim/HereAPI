package my.example.here;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class LauncherActivity extends AppCompatActivity implements View.OnClickListener {

    private Button pharmacyBtn, hospitalBtn;
    private static final String EXTRA_SEARCH_REQUEST="search";
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] RUNTIME_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
         setypButton();
        if (hasPermissions(this, RUNTIME_PERMISSIONS)) {

        } else {
            ActivityCompat
                    .requestPermissions(this, RUNTIME_PERMISSIONS, REQUEST_CODE_ASK_PERMISSIONS);
        }
    }
    private static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    private void setypButton(){
        pharmacyBtn=(Button) findViewById(R.id.pharmacy);
        hospitalBtn=(Button) findViewById(R.id.hospital);
        pharmacyBtn.setOnClickListener(this);
        hospitalBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case  R.id.hospital:
                Intent intent=new Intent(this, MapActivity.class);
                intent.putExtra(EXTRA_SEARCH_REQUEST,"hospital");
                startActivity(intent);
                break;
            case R.id.pharmacy:
                Intent intent1=new Intent(this,MapActivity.class);
                intent1.putExtra(EXTRA_SEARCH_REQUEST,"pharmacy");
                startActivity(intent1);
                break;
        }
    }
}
