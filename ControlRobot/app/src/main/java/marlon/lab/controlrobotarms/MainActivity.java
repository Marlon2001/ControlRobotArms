package marlon.lab.controlrobotarms;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import marlon.lab.model.Bluetooth;
import marlon.lab.tasks.ConnectDevice;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private ImageButton btnOpenSettings;
    private ImageView btnUpBlue, btnDownBlue;
    private ImageView btnUpGrey, btnDownGrey;
    private ImageView btnUpGreen, btnDownGreen;
    private ImageButton btnRotateR, btnRotateL, btnRotateC, btnRotateV;
    private ImageButton btnMachineOpen, btnMachineClose;
    private TextView txtDeviceName;
    private BluetoothAdapter myBluetooth = null;
    public static BluetoothSocket btSocket = null;
    private static AlphaAnimation buttonClick = new AlphaAnimation(0.7F, 2.5F);
    private static final int REQUEST_ENABLE_BLUETOOTH = 2152;
    private static boolean BLUETOOTH_IS_ON = false;
    private ArrayList<BluetoothDevice> mDeviceList;
    private ProgressBar loadingConnect;

    public static BluetoothDevice device;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOpenSettings = findViewById(R.id.menu_configuracoes);

        btnUpBlue = findViewById(R.id.btn_up_blue);
        btnDownBlue = findViewById(R.id.btn_down_blue);

        btnUpGrey = findViewById(R.id.btn_up_grey);
        btnDownGrey = findViewById(R.id.btn_down_grey);

        btnUpGreen = findViewById(R.id.btn_up_green);
        btnDownGreen = findViewById(R.id.btn_down_green);

        btnRotateR = findViewById(R.id.btn_rotate_r);
        btnRotateL = findViewById(R.id.btn_rotate_l);
        btnRotateC = findViewById(R.id.btn_rotate_c);
        btnRotateV = findViewById(R.id.btn_rotate_v);

        btnMachineOpen = findViewById(R.id.btn_machine_open);
        btnMachineClose = findViewById(R.id.btn_machine_closed);

        txtDeviceName = findViewById(R.id.txt_device_name);

        loadingConnect = findViewById(R.id.loading_connect);

        btnOpenSettings.setOnClickListener(this);
        txtDeviceName.setOnClickListener(this);
        btnUpBlue.setOnTouchListener(this);
        btnDownBlue.setOnTouchListener(this);
        btnUpGrey.setOnTouchListener(this);
        btnDownGrey.setOnTouchListener(this);
        btnUpGreen.setOnTouchListener(this);
        btnDownGreen.setOnTouchListener(this);
        btnRotateR.setOnTouchListener(this);
        btnRotateL.setOnTouchListener(this);
        btnRotateC.setOnTouchListener(this);
        btnRotateV.setOnTouchListener(this);
        btnMachineOpen.setOnTouchListener(this);
        btnMachineClose.setOnTouchListener(this);

        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        txtDeviceName.setText(Html.fromHtml("<i>No device</i>"));

        loadingConnect.setVisibility(View.INVISIBLE);

        if(!myBluetooth.isEnabled()){
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, REQUEST_ENABLE_BLUETOOTH);
        } else
            BLUETOOTH_IS_ON = true;

        IntentFilter filter = new IntentFilter();

        mDeviceList = new ArrayList<>();

        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiver, filter);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_ON) {

                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mDeviceList = new ArrayList<>();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d("DEVICE LIST", mDeviceList.toString());
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDeviceList.add(device);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ENABLE_BLUETOOTH) {
            BLUETOOTH_IS_ON = resultCode == RESULT_OK;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View view) {
        view.startAnimation(buttonClick);
        switch (view.getId()){
            case R.id.txt_device_name:
                if(!txtDeviceName.getText().toString().equals("No device")){
                    new AlertDialog.Builder(this)
                        .setTitle("Desconectar dispositivo.")
                        .setMessage("Deseja desfazer a conexão com o dispositivo "+txtDeviceName.getText().toString()+"?")
                        .setPositiveButton("Desconectar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                btSocket = null;
                                device = null;
                                txtDeviceName.setText(Html.fromHtml("<i>No device</i>"));
                            }
                        })
                        .setNegativeButton("Fechar", null)
                        .show();
                }
                break;
            case R.id.menu_configuracoes: {
                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                @SuppressLint("InflateParams") final View mView = getLayoutInflater().inflate(R.layout.dialog_menu, null);

                LinearLayout layoutConnectBluetooth = mView.findViewById(R.id.view_connect_bluetooth);
                LinearLayout linearSettings = mView.findViewById(R.id.view_settings);
                LinearLayout linearCloseApp = mView.findViewById(R.id.view_close_app);

                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();

                layoutConnectBluetooth.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!myBluetooth.isEnabled()) {
                            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBluetooth, REQUEST_ENABLE_BLUETOOTH);
                        } else {
                            final AlertDialog.Builder mBuilder2 = new AlertDialog.Builder(MainActivity.this);
                            @SuppressLint("InflateParams") final View mView2 = getLayoutInflater().inflate(R.layout.dialog_bluetooth, null);

                            final ListView listPairedDevices = mView2.findViewById(R.id.list_paired_devices);
                            final Button btnScanDevices = mView2.findViewById(R.id.btn_scan_devices);
                            final ProgressBar loadingScan = mView2.findViewById(R.id.loading_scan);
                            loadingScan.setVisibility(View.INVISIBLE);

                            Set<BluetoothDevice> pairedDevices = myBluetooth.getBondedDevices();
                            ArrayList<Bluetooth> listBluetooth = new ArrayList<>();

                            for (BluetoothDevice bt : pairedDevices) {
                                Bluetooth bluetooth = new Bluetooth();
                                bluetooth.setName(bt.getName());
                                bluetooth.setAddress(bt.getAddress());
                                listBluetooth.add(bluetooth);
                            }

                            final ArrayAdapter<Bluetooth> arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, listBluetooth);
                            listPairedDevices.setAdapter(arrayAdapter);

                            mBuilder2.setView(mView2);
                            final AlertDialog dialog2 = mBuilder2.create();
                            dialog.dismiss();
                            dialog2.show();

                            listPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    dialog2.dismiss();
                                    Bluetooth bluetooth = arrayAdapter.getItem(i);
                                    String address = bluetooth.getAddress();

                                    try {
                                        if (connectDevice(address))
                                            Toast.makeText(MainActivity.this, "Conectado ao dispositivo  " + bluetooth.getName() + ".", Toast.LENGTH_LONG).show();
                                        else
                                            Toast.makeText(MainActivity.this, "Não foi possível se conectar ao dispositivo " + bluetooth.getName() + ". Tente novamente mais tarde.", Toast.LENGTH_LONG).show();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            btnScanDevices.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    loadingScan.setVisibility(View.VISIBLE);
                                    btnScanDevices.setEnabled(false);
                                    Toast.makeText(MainActivity.this, "Scanning...", Toast.LENGTH_SHORT).show();
                                    myBluetooth.startDiscovery();
                                }
                            });
                        }
                    }
                });

                linearSettings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final AlertDialog.Builder mBuilder2 = new AlertDialog.Builder(MainActivity.this);
                        @SuppressLint("InflateParams") final View mView2 = getLayoutInflater().inflate(R.layout.dialog_settings, null);

                        final Button btnClose = mView2.findViewById(R.id.btn_close);

                        mBuilder2.setView(mView2);
                        final AlertDialog dialog2 = mBuilder2.create();
                        dialog.dismiss();
                        dialog2.show();

                        btnClose.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog2.dismiss();
                            }
                        });
                    }
                });

                linearCloseApp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });
                break;
            }
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();

        if(action == MotionEvent.ACTION_DOWN && btSocket != null){
            switch (view.getId()){
                case R.id.btn_up_blue:
                    sendFlag("U");
                    break;
                case R.id.btn_down_blue:
                    sendFlag("D");
                    break;
                case R.id.btn_up_grey:
                    sendFlag("Q");
                    break;
                case R.id.btn_down_grey:
                    sendFlag("W");
                    break;
                case R.id.btn_up_green:
                    sendFlag("Z");
                    break;
                case R.id.btn_down_green:
                    sendFlag("X");
                    break;
                case R.id.btn_rotate_r:
                    sendFlag("R");
                    break;
                case R.id.btn_rotate_l:
                    sendFlag("L");
                    break;
                case R.id.btn_rotate_c:
                    sendFlag("C");
                    break;
                case R.id.btn_rotate_v:
                    sendFlag("V");
                    break;
                case R.id.btn_machine_open:
                    sendFlag("A");
                    break;
                case R.id.btn_machine_closed:
                    sendFlag("F");
                    break;
            }
        }else if(action == MotionEvent.ACTION_UP){
            sendFlag("P");
        }else if(action == MotionEvent.ACTION_CANCEL){
            sendFlag("P");
        }
        return false;
    }

    private boolean connectDevice(String address) throws ExecutionException, InterruptedException {
        loadingConnect.setVisibility(View.VISIBLE);

        ConnectDevice connectDevice = new ConnectDevice(address);
        connectDevice.execute().get();

        if(device != null && btSocket != null) {
            txtDeviceName.setText(Html.fromHtml("<i>"+ device.getName() +"</i>"));
            loadingConnect.setVisibility(View.INVISIBLE);
            return true;
        }
        loadingConnect.setVisibility(View.INVISIBLE);
        return false;
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendFlag(String flag) {
        try {
            if (btSocket != null) {
                OutputStream out = btSocket.getOutputStream();
                out.write(flag.getBytes());
                out.flush();
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Não foi possível enviar a flag. Erro na conexão.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        if (myBluetooth != null) {
            if (myBluetooth.isDiscovering()) {
                myBluetooth.cancelDiscovery();
            }
        }
        super.onPause();
    }

    @Override
    protected void onDestroy(){
        if((btSocket != null && btSocket.isConnected()) && myBluetooth.isEnabled())
            try { btSocket.close(); } catch (IOException e) { e.printStackTrace(); }
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
}
