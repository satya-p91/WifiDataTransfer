package online.forgottenbit.wifidatatransfer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {

    Button server,client;

    int permission_req_code = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!defaultPermissionCheck()){
            askForPermission();
        }

        server = findViewById(R.id.serverActivity);
        client = findViewById(R.id.clientActivity);

        server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ServerActivity.class));
            }
        });


        client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ClientActivity.class));
            }
        });

    }

    private void askForPermission() {

        //asking  for storage permission from user at runtime

        ActivityCompat.requestPermissions(this,new String[] {
                Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_NETWORK_STATE
        },permission_req_code);


    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //checking if user granted the permissions or not

        if(requestCode == permission_req_code){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission granted :)", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(this, "App will not work without permissions, Grant these permissions from settings. :|", Toast.LENGTH_LONG).show();
                askForPermission();
            }
        }
    }

    private boolean defaultPermissionCheck() {
        //checking if permissions is already granted
        int external_storage_write = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE);
        int a = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_WIFI_STATE);
        int b = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_NETWORK_STATE);
        return external_storage_write == PackageManager.PERMISSION_GRANTED && a==PackageManager.PERMISSION_GRANTED && b==PackageManager.PERMISSION_GRANTED;
    }



}
