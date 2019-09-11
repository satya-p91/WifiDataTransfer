package online.forgottenbit.wifidatatransfer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientActivity extends AppCompatActivity {


    Thread Thread1 = null;
    EditText etIP, etPort;
    TextView tvMessages;
    EditText etMessage;
    Button btnSend;
    String SERVER_IP;
    int SERVER_PORT;
    Socket socket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);


        etIP = findViewById(R.id.etIP);
        etPort = findViewById(R.id.etPort);
        tvMessages = findViewById(R.id.tvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        Button btnConnect = findViewById(R.id.btnConnect);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvMessages.setText("Not connected");
                SERVER_IP = etIP.getText().toString().trim();
                SERVER_PORT = Integer.parseInt(etPort.getText().toString().trim());
                Thread1 = new Thread(new Thread1());
                Thread1.start();
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = etMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    new Thread(new Thread3(message)).start();
                }
            }
        });

    }



    private PrintWriter output;
    private BufferedReader input;


    class Thread1 implements Runnable {
        @Override
        public void run() {

            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {

                socket.setKeepAlive(true);
                output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvMessages.setText("Connected\n");
                    }
                });
                new Thread(new Thread2(new BufferedReader(new InputStreamReader(socket.getInputStream())))).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    class Thread2 implements Runnable {

        private BufferedReader ipObj;

        Thread2(BufferedReader ipObj){
            this.ipObj = ipObj;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    final String message = ipObj.readLine();
                    if (message != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvMessages.append("server: " + message + "\n");
                            }
                        });
                    } else {
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

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvMessages.append("client: " + message + "\n");
                    etMessage.setText("");
                }
            });
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(socket !=null){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
