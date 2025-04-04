package com.example.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlueControl extends AppCompatActivity {
    private ImageButton btnTb1, btnTb2, btnDis;
    private TextView txt1, txtMAC;
    private BluetoothAdapter myBluetooth = null;
    private BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private String address = null;
    private ProgressDialog mProgressDialog;
    private int flaglamp1 = 0;
    private int flaglamp2 = 0;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;
    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        address = intent.getStringExtra(MainActivity.EXTRA_ADDRESS);
        setContentView(R.layout.activity_control);
        initializeViews();
        requestBluetoothPermissions();
        setupButtonListeners();
    }

    private void initializeViews() {
        btnTb1 = findViewById(R.id.btnTb1);
        btnTb2 = findViewById(R.id.btnTb2);
        btnDis = findViewById(R.id.bnDisc);
        txt1 = findViewById(R.id.textV1);
        txtMAC = findViewById(R.id.textMac);
    }

    private void setupButtonListeners() {
        btnTb1.setOnClickListener(v -> thiettbi1());
        btnTb2.setOnClickListener(v -> thiettbi2());
        btnDis.setOnClickListener(v -> disconnect());
    }

    private void requestBluetoothPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                        REQUEST_BLUETOOTH_PERMISSIONS);
            } else {
                connectBluetooth();
            }
        } else {
            connectBluetooth();
        }
    }

    @SuppressLint("MissingPermission")
    private void connectBluetooth() {
        mProgressDialog = ProgressDialog.show(BlueControl.this, "Đang kết nối...", "Vui lòng đợi!");

        executorService.execute(() -> {
            boolean connectSuccess = true;

            try {
                myBluetooth = BluetoothAdapter.getDefaultAdapter();
                if (myBluetooth == null) {
                    mainHandler.post(() -> msg("Thiết bị không hỗ trợ Bluetooth"));
                    connectSuccess = false;
                    return;
                }

                if (address == null) {
                    mainHandler.post(() -> msg("Địa chỉ MAC bị null"));
                    connectSuccess = false;
                    return;
                }

                // Kiểm tra quyền trước khi dùng
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    mainHandler.post(() -> msg("Thiếu quyền BLUETOOTH_CONNECT"));
                    connectSuccess = false;
                    return;
                }

                BluetoothDevice device = myBluetooth.getRemoteDevice(address);
                btSocket = device.createRfcommSocketToServiceRecord(myUUID);
                myBluetooth.cancelDiscovery();
                btSocket.connect();
            } catch (IOException e) {
                connectSuccess = false;
                Log.e("Bluetooth", "Lỗi khi connect: " + e.getMessage());
                e.printStackTrace();
            }

            boolean finalConnectSuccess = connectSuccess;
            mainHandler.post(() -> {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                if (!finalConnectSuccess) {
                    msg("Kết nối thất bại. Hãy thử lại!");
                    finish();
                } else {
                    msg("Đã kết nối thành công");
                    isBtConnected = true;
                    updateDeviceInfo();
                }
            });
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateDeviceInfo() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            BluetoothDevice device = myBluetooth.getRemoteDevice(address);
            txtMAC.setText(device.getName() + " - " + device.getAddress());
        } catch (Exception e) {
            txtMAC.setText("Địa chỉ MAC: " + address);
        }
    }

    private void thiettbi1() {
        if (btSocket != null) {
            try {
                if (flaglamp1 == 0) {
                    btnTb1.setBackgroundResource(R.drawable.tb1_on);
                    flaglamp1 = 1;
                    btSocket.getOutputStream().write("1".getBytes());
                    txt1.setText("Thiết bị 1 đang BẬT");
                } else {
                    btnTb1.setBackgroundResource(R.drawable.tb1_off);
                    flaglamp1 = 0;
                    btSocket.getOutputStream().write("A".getBytes());
                    txt1.setText("Thiết bị 1 đang TẮT");
                }
            } catch (IOException e) {
                msg("Lỗi khi điều khiển thiết bị 1");
            }
        } else {
            msg("Vui lòng kết nối Bluetooth trước");
        }
    }

    private void thiettbi2() {
        if (btSocket != null) {
            try {
                if (flaglamp2 == 0) {
                    btnTb2.setBackgroundResource(R.drawable.tb2_on);
                    flaglamp2 = 1;
                    btSocket.getOutputStream().write("7".getBytes());
                    txt1.setText("Thiết bị 2 đang BẬT");
                } else {
                    btnTb2.setBackgroundResource(R.drawable.tb2_off);
                    flaglamp2 = 0;
                    btSocket.getOutputStream().write("G".getBytes());
                    txt1.setText("Thiết bị 2 đang TẮT");
                }
            } catch (IOException e) {
                msg("Lỗi khi điều khiển thiết bị 2");
            }
        } else {
            msg("Vui lòng kết nối Bluetooth trước");
        }
    }

    private void disconnect() {
        if (btSocket != null) {
            try {
                btSocket.close();
                isBtConnected = false;
                msg("Đã ngắt kết nối");
            } catch (IOException e) {
                msg("Lỗi khi ngắt kết nối");
            }
        }
        finish();
    }

    private void msg(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }
}
