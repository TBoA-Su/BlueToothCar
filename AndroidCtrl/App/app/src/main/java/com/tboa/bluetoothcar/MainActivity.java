package com.tboa.bluetoothcar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;
    private String mConnectedDeviceAddress = null;
    private int isSelect = 1;

    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;

    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<String> bluetoothEquipment_name = new ArrayList<String>();//搜索到的蓝牙的列表名字
    private ArrayList<String> bluetoothEquipment_mac = new ArrayList<String>();//搜索到的蓝牙的列表地址
    private BluetoothDevice bluetoothDevice;
    private ArrayAdapter<String> listAdapter;
    boolean isConnected = false;

    Button left_move_forward, move_forward, right_move_forward, left_move, stop_move, right_move, left_move_backward, move_backward, right_move_backward, fs, btn_blue_seek;

    TextView textView;
    Spinner spi_blue_tooth;
    TextView txt_blue_status;
    EditText et;
    ScrollView scrollView;

    Switch hexShowSwitch;

    boolean isOnLongClick = false;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(MainActivity.this, mHandler);


        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }

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
        scrollView = findViewById(R.id.scrollView);

        applypermission();
        //获取蓝牙适配器

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
        //获取蓝牙列表适配器
        listAdapter = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, bluetoothEquipment_name);
        spi_blue_tooth.setAdapter(listAdapter);
        bluetoothEquipment_name.add("关闭");
        bluetoothEquipment_mac.add("关闭");
        listAdapter.notifyDataSetChanged();
        //注册广播
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);//注册广播接收信号
        registerReceiver(bluetoothReceiver, intentFilter);//用BroadcastReceiver 来取得结果
        Btn_Operation();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        hexShowSwitch = menu.findItem(R.id.hexShow).getActionView().findViewById(R.id.hexShowSwitch);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exit:
                isConnected = false;
                unregisterReceiver(bluetoothReceiver);
                if (mChatService != null) {
                    mChatService.stop();
                }
                finish();
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
        isConnected = false;
        unregisterReceiver(bluetoothReceiver);
        if (mChatService != null) {
            mChatService.stop();
        }
    }


    @SuppressLint({"ClickableViewAccessibility", "MissingPermission"})
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
            @SuppressLint("MissingPermission")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isSelect == 1) {
                    String selectItem_name = bluetoothEquipment_name.get(position);
                    String selectItem_mac = bluetoothEquipment_mac.get(position);
                    if ("关闭".equals(selectItem_name)) {
                        if (bluetoothDevice != null) {
                            bluetoothDevice = null;
                        }
                        txt_blue_status.setText(R.string.unConnected);
                    } else {
                        txt_blue_status.setText(R.string.onConnecting);
                        bluetoothAdapter.cancelDiscovery();//关闭蓝牙扫描
                        btn_blue_seek.setText(R.string.search);
                        Toast.makeText(MainActivity.this, "选择连接：" + selectItem_name, Toast.LENGTH_SHORT).show();
                        if (bluetoothDevice == null) {
                            bluetoothDevice = bluetoothAdapter.getRemoteDevice(selectItem_mac);
                            mChatService.connect(bluetoothDevice);
                        }
                    }
                } else isSelect = 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        fs.setOnClickListener(v -> {
            sendMessage(et.getText().toString().getBytes());
        });

        left_move_forward.setOnTouchListener((v, event) -> {
            btnClick(event, "A");
            return true;
        });
        move_forward.setOnTouchListener((v, event) -> {
            btnClick(event, "B");
            return true;
        });
        right_move_forward.setOnTouchListener((v, event) -> {
            btnClick(event, "C");
            return true;
        });

        left_move.setOnTouchListener((v, event) -> {
            btnClick(event, "D");
            return true;
        });
        stop_move.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                sendMessage("E".getBytes());
            }
            return true;
        });
        right_move.setOnTouchListener((v, event) -> {
            btnClick(event, "F");
            return true;
        });

        left_move_backward.setOnTouchListener((v, event) -> {
            btnClick(event, "G");
            return true;
        });
        move_backward.setOnTouchListener((v, event) -> {
            btnClick(event, "H");
            return true;
        });
        right_move_backward.setOnTouchListener((v, event) -> {
            btnClick(event, "I");
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


    public void btnClick(MotionEvent event, String d) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                sendMessage(d.getBytes());
                checkIsOnLongClick(true);
                break;
            case MotionEvent.ACTION_UP:
                if (isOnLongClick) {
                    sendMessage("E".getBytes());
                }
                checkIsOnLongClick(false);
                break;
        }
    }

    private final Handler mHandler = new Handler() {
        @SuppressLint("MissingPermission")
        @Override
        public void handleMessage(Message msg) {
            //FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    StringBuffer writeMessage = new StringBuffer();
                    if (hexShowSwitch.isChecked()) {
                        for (int i = 0; i < writeBuf.length - 1; i++) {
                            writeMessage.append(toHexString(Integer.toHexString((int) writeBuf[i])) + ",");
                        }
                        writeMessage.append(toHexString(Integer.toHexString((int) writeBuf[writeBuf.length - 1])));
                    } else
                        writeMessage.append(new String(writeBuf));
                    textView.append("\n发：\n" + writeMessage);
                    post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    textView.append("\n收：\n" + readMessage);
                    post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    mConnectedDeviceAddress = msg.getData().getString(Constants.DEVICE_ADDRESS);
                    isSelect = msg.getData().getInt(Constants.SELECT_WAY);
                    Toast.makeText(MainActivity.this, "已连接到 " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    if (isSelect == 0) {
                        if (bluetoothEquipment_name.contains(mConnectedDeviceName) && bluetoothEquipment_mac.contains(mConnectedDeviceAddress))
                            spi_blue_tooth.setSelection(bluetoothEquipment_mac.indexOf(mConnectedDeviceAddress));
                        else {
                            bluetoothEquipment_name.add(mConnectedDeviceName);
                            bluetoothEquipment_mac.add(mConnectedDeviceAddress);
                            listAdapter.notifyDataSetChanged();
                            spi_blue_tooth.setSelection(bluetoothEquipment_mac.indexOf(mConnectedDeviceAddress));
                        }
                    }
                    txt_blue_status.setText(R.string.connected);
                    if (bluetoothAdapter.isDiscovering()) {
                        //判断蓝牙是否正在扫描，如果是调用取消扫描方法；如果不是，则开始扫描
                        bluetoothAdapter.cancelDiscovery();
                    }
                    btn_blue_seek.setText(R.string.search);
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(MainActivity.this, msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                    spi_blue_tooth.setSelection(0);
                    txt_blue_status.setText(R.string.unConnected);
                    break;
                case Constants.UI_BUTTON:
                    btn_blue_seek.setText(R.string.search);
                    break;
            }
        }
    };

    private String toHexString(String writeBuf) {
        String writeMessage;
        if (writeBuf.length() == 1) {
            writeMessage = "0x0" + writeBuf;
        } else {
            writeMessage = "0x" + writeBuf;
        }
        return writeMessage;
    }

    //创建一个接受 ACTION_FOUND 的 BroadcastReceiver
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // 当 Discovery 发现了一个设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 从 Intent 中获取发现的 BluetoothDevice
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 将名字和地址放入要显示的适配器中
                bluetoothEquipment_name.add(device.getName() + " ");
                bluetoothEquipment_mac.add(device.getAddress());
                listAdapter.notifyDataSetChanged();
            }
        }
    };

    //发送要传输的数据

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(byte[] message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(MainActivity.this, R.string.unConnected, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (message.length > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            mChatService.write(message);
            et.setText("");
        }
    }

    @SuppressLint("MissingPermission")
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
            new Thread(() -> {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while (bluetoothAdapter.isDiscovering()) ;
                mHandler.sendEmptyMessage(Constants.UI_BUTTON);
            }).start();
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
