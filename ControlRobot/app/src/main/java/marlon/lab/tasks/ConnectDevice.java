package marlon.lab.tasks;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.UUID;

import marlon.lab.controlrobotarms.MainActivity;
import marlon.lab.controlrobotarms.R;

public class ConnectDevice extends AsyncTask {

    private String address;

    private static final UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public ConnectDevice(String address) {
        this.address = address;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        try {
            BluetoothSocket btSocket;
            BluetoothAdapter myBluetooth = BluetoothAdapter.getDefaultAdapter();

            if (myBluetooth.isDiscovering())
                myBluetooth.cancelDiscovery();

            BluetoothDevice device = myBluetooth.getRemoteDevice(address);
            btSocket = device.createRfcommSocketToServiceRecord(SERIAL_UUID);

            try {
                btSocket.connect();
                MainActivity.device = device;
                MainActivity.btSocket = btSocket;
            } catch (IOException e) {
                try {
                    btSocket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device, 1);
                    btSocket.connect();
                    MainActivity.device = device;
                    MainActivity.btSocket = btSocket;
                } catch (Exception e1) {
                    try {
                        btSocket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, 2);
                        btSocket.connect();
                        MainActivity.device = device;
                        MainActivity.btSocket = btSocket;
                    } catch (Exception e2) { e2.printStackTrace(); }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
