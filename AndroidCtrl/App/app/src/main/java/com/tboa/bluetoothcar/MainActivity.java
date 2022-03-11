package com.tboa.bluetoothcar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<String> bluetoothEquipment_name = new ArrayList<String>();//搜索到的蓝牙的列表名字
    private ArrayList<String> bluetoothEquipment_mac = new ArrayList<String>();//搜索到的蓝牙的列表地址
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private OutputStream os;
    private InputStream is;
    private ArrayAdapter<String> listAdapter;
    boolean isConnected = false;
    private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    @SuppressLint("UseSwitchCompatOrMaterialCode")

    Button left_move_forward, move_forward, right_move_forward, left_move, stop_move, right_move, left_move_backward, move_backward, right_move_backward, fs, btn_blue_seek;

    TextView textView;
    Spinner spi_blue_tooth;
    TextView txt_blue_status;
    EditText et;
    boolean isOnLongClick = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        left_move_forward = findViewById(R.id.left_move_forward);
        move_forward = findViewById(R.id.move_forward);
        right_move_forward = findViewById(R.id.right_move_forward);

        left_move = findViewById(R.id.left_move);
        stop_move = findViewById(R.id.stop_move);
        right_move = findViewById(R.id.right_move);

        left_move_backward = findViewById(R.id.left_move_backward);
        move_backward = findViewById(R.id.move_backward);
        right_move_backward = findViewById(R.id.right_move_backward);

        textView = findViewById(R.id.textView3);
        btn_blue_seek = findViewById(R.id.bluetooth_seek);
        fs = findViewById(R.id.fs);
        et = findViewById(R.id.et);
        spi_blue_tooth = findViewById(R.id.bluetooth_Spinner);
        txt_blue_status = findViewById(R.id.blue_tooth_status);

        applypermission();
        //获取蓝牙适配器
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
        //获取蓝牙列表适配器
        listAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, bluetoothEquipment_name);
        spi_blue_tooth.setAdapter(listAdapter);
        bluetoothEquipment_name.add("关闭");
        bluetoothEquipment_mac.add("关闭");
        listAdapter.notifyDataSetChanged();
        //注册广播
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);//注册广播接收信号
        registerReceiver(bluetoothReceiver, intentFilter);//用BroadcastReceiver 来取得结果
        Btn_Operation();

    }

    class SendTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                if (bluetoothSocket == null) return "";
                os = bluetoothSocket.getOutputStream();
                os.write(params[0].getBytes());
                os.flush();
            } catch (IOException e) {
                Log.e("error", "ON RESUME: Exception during write.", e);
            }
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exit:
                finish();
                break;
            case R.id.onoff:
                bluetoothO();
                break;
            case R.id.clear:
                textView.setText(R.string.context);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        isConnected = false;
        unregisterReceiver(bluetoothReceiver);
    }


    @SuppressLint("ClickableViewAccessibility")
    private void Btn_Operation() {
        //搜索蓝牙
        btn_blue_seek.setOnClickListener(v -> {
            //判断是否打开蓝牙
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
            }
            if (!bluetoothAdapter.isDiscovering()) {
                bluetoothEquipment_name.clear();
                bluetoothEquipment_mac.clear();
                bluetoothEquipment_name.add("关闭");
                bluetoothEquipment_mac.add("关闭");
                listAdapter.notifyDataSetChanged();
            }
            doDiscovry();
        });

        //选择蓝牙或者关闭
        spi_blue_tooth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectItem_name = bluetoothEquipment_name.get(position);
                String selectItem_mac = bluetoothEquipment_mac.get(position);
//                System.out.println(selectItem_mac);
                if ("关闭".equals(selectItem_name)) {
                    if (bluetoothDevice != null) {
                        bluetoothDevice = null;
                    }
                    if (bluetoothSocket != null) {
                        try {
                            bluetoothSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    txt_blue_status.setText(R.string.unConnected);
                } else {
                    txt_blue_status.setText(R.string.onConnecting);
                    bluetoothAdapter.cancelDiscovery();//关闭蓝牙扫描
                    Toast.makeText(MainActivity.this, "选择连接：" + selectItem_name, Toast.LENGTH_SHORT).show();
                    if (bluetoothDevice == null) {
                        bluetoothDevice = bluetoothAdapter.getRemoteDevice(selectItem_mac);
                    }
                    if (bluetoothSocket == null) {
                        try {
                            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                            bluetoothSocket.connect();
                            os = bluetoothSocket.getOutputStream();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (os != null) {
//                            os.write("OK".getBytes(StandardCharsets.UTF_8));
                        isConnected = true;
                        txt_blue_status.setText(R.string.connected);
                    }
                    Toast.makeText(MainActivity.this, "发送信息成功", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        fs.setOnClickListener(v -> {
            send(et.getText().toString(), true);
        });

        left_move_forward.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    send("A", false);
                    checkIsOnLongClick(true);
                    break;
                case MotionEvent.ACTION_UP:
                    if (isOnLongClick) {
                        send("E", false);
                    }
                    checkIsOnLongClick(false);
                    break;
            }
            return true;
        });
        move_forward.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    send("B", false);
                    checkIsOnLongClick(true);
                    break;
                case MotionEvent.ACTION_UP:
                    if (isOnLongClick) {
                        send("E", false);
                    }
                    checkIsOnLongClick(false);
                    break;
            }
            return true;
        });
        right_move_forward.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    send("C", false);
                    checkIsOnLongClick(true);
                    break;
                case MotionEvent.ACTION_UP:
                    if (isOnLongClick) {
                        send("E", false);
                    }
                    checkIsOnLongClick(false);
                    break;
            }
            return true;
        });

        left_move.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    send("D", false);
                    checkIsOnLongClick(true);
                    break;
                case MotionEvent.ACTION_UP:
                    if (isOnLongClick) {
                        send("E", false);
                    }
                    checkIsOnLongClick(false);
                    break;
            }
            return true;
        });
        stop_move.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    send("E", false);
                    break;
            }
            return true;
        });
        right_move.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    send("F", false);
                    checkIsOnLongClick(true);
                    break;
                case MotionEvent.ACTION_UP:
                    if (isOnLongClick) {
                        send("E", false);
                    }
                    checkIsOnLongClick(false);
                    break;
            }
            return true;
        });

        left_move_backward.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    send("G", false);
                    checkIsOnLongClick(true);
                    break;
                case MotionEvent.ACTION_UP:
                    if (isOnLongClick) {
                        send("E", false);
                    }
                    checkIsOnLongClick(false);
                    break;
            }
            return true;
        });
        move_backward.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    send("H", false);
                    checkIsOnLongClick(true);
                    break;
                case MotionEvent.ACTION_UP:
                    if (isOnLongClick) {
                        send("E", false);
                    }
                    checkIsOnLongClick(false);
                    break;
            }
            return true;
        });
        right_move_backward.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    send("I", false);
                    checkIsOnLongClick(true);
                    break;
                case MotionEvent.ACTION_UP:
                    if (isOnLongClick) {
                        send("E", false);
                    }
                    checkIsOnLongClick(false);
                    break;
            }
            return true;
        });
    }

    public void checkIsOnLongClick(boolean a) {
        new Thread(() -> {
            try {
                Thread.sleep(500);
                isOnLongClick = a;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void bluetoothO() {
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            Toast.makeText(getApplicationContext(), R.string.on, Toast.LENGTH_LONG).show();
        } else {
            bluetoothAdapter.disable();
            isConnected = false;
            txt_blue_status.setText(R.string.unConnected);
            Toast.makeText(getApplicationContext(), R.string.off, Toast.LENGTH_LONG).show();
        }
    }

    //创建一个接受 ACTION_FOUND 的 BroadcastReceiver
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // 当 Discovery 发现了一个设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 从 Intent 中获取发现的 BluetoothDevice
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 将名字和地址放入要显示的适配器中
                bluetoothEquipment_name.add(device.getName() + "   ");
                bluetoothEquipment_mac.add(device.getAddress());
                listAdapter.notifyDataSetChanged();
            }
        }
    };

    //发送要传输的数据
    @SuppressLint("SetTextI18n")
    public void send(String s, boolean a) {
        if (a) {
            try {
                if (os != null) {
                    os.write(s.getBytes(StandardCharsets.UTF_8));
                    textView.setText(textView.getText().toString() + "\n我：" + s);
                    et.setText("");
                } else Toast.makeText(this, R.string.connectBluetooth, Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (os != null) {
                new SendTask().execute(s);
                textView.setText(textView.getText().toString() + "\n我：" + s);
            } else Toast.makeText(this, R.string.connectBluetooth, Toast.LENGTH_LONG).show();
        }
    }

    public void doDiscovry() {
        if (bluetoothAdapter.isDiscovering()) {
            //判断蓝牙是否正在扫描，如果是调用取消扫描方法；如果不是，则开始扫描
            bluetoothAdapter.cancelDiscovery();
            btn_blue_seek.setText(R.string.search);
            Toast.makeText(this, R.string.cancelSearch, Toast.LENGTH_SHORT).show();
        } else {
            bluetoothAdapter.startDiscovery();
            btn_blue_seek.setText(R.string.stop);
            Toast.makeText(this, R.string.onSearch, Toast.LENGTH_LONG).show();
        }
    }


    /*
    检查是否已经给了权限
     */
    public void applypermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            //检查是否已经给了权限
            int checkpermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
            if (checkpermission != PackageManager.PERMISSION_GRANTED) {//没有给权限
                Log.e("permission", "动态申请");
                //参数分别是当前活动，权限字符串数组，requestcode
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    /*
    动态授权
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "已授权", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "拒绝授权", Toast.LENGTH_SHORT).show();
        }

    }

}
