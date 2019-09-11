package online.forgottenbit.wifidatatransfer;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Enumeration;

public class ServerActivity extends AppCompatActivity {

    public static final int SERVER_PORT = 8185;
    public static String SERVER_IP = "";
    ServerSocket serverSocket;
    Thread Thread1 = null;
    TextView tvIP, tvPort;
    EditText editMessage;
    TextView tvMessage;
    Button btnSend;
    String message;
    Socket socket;
    private PrintWriter output;
    private BufferedReader input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        tvIP = findViewById(R.id.tvIP);
        tvPort = findViewById(R.id.tvPort);
        tvMessage = findViewById(R.id.tvMessages);
        editMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        try {
            SERVER_IP = getLocalIpAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }


        Thread1 = new Thread(new Thread1());
        Thread1.start();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = editMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    new Thread(new Thread3(message)).start();

                }
            }
        });

    }

    private String getLocalIpAddress() throws UnknownHostException {

        if (ApManager.isApOn(this)) {

            try {
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                     en.hasMoreElements(); ) {
                    NetworkInterface intf = en.nextElement();
                    if (intf.getName().contains("wlan")) {

                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && (inetAddress.getAddress().length == 4)) {
                                Log.e("error", inetAddress.getHostAddress());
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                }
            } catch (SocketException ex) {
                Log.e("error", ex.toString());
            }

        } else {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

            if (wifiManager != null) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int ipInt = wifiInfo.getIpAddress();
                Log.e("IP address : ", InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).getHostAddress());
                return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).getHostAddress();
            }
        }

        return null;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    class Thread1 implements Runnable {
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvMessage.setText("Not connected");
                        tvIP.setText("IP: " + SERVER_IP);
                        tvPort.setText("Port: " + String.valueOf(SERVER_PORT));
                    }
                });
                try {
                    socket = serverSocket.accept();
                    socket.setKeepAlive(true);
                    output = new PrintWriter(socket.getOutputStream());

                    input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvMessage.setText("Connected\n");
                        }
                    });

                    new Thread(new Thread2(new BufferedReader(new InputStreamReader(socket.getInputStream())))).start();
                } catch (IOException e) {
                    Log.e("In theard 1", e.getMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class Thread2 implements Runnable {

        private BufferedReader ipObj;

        Thread2(BufferedReader ipObj) {
            this.ipObj = ipObj;
        }

        @Override
        public void run() {
            while (true) {
                try {

                    if (ipObj == null) {
                        Log.e("Err", "null obj is passed");
                    }
                    final String message = ipObj.readLine();

                    Log.e("msgServerReceive",":  "+message);

                    if (message != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvMessage.append("client:" + message + "\n");
                            }
                        });
                    } else {

                        Log.e("msgServerReceive","Error hai thread 2 server");

                        Thread1 = new Thread(new Thread1());
                        Thread1.start();
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class Thread3 implements Runnable {
        private String message;

        Thread3(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            output.write(message);
            output.flush();
            Log.e("msgServerSend",message);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvMessage.append("server: " + message + "\n");
                    editMessage.setText("");
                }
            });
        }
    }


}
