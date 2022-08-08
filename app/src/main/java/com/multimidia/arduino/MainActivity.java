package com.multimidia.arduino;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;
import com.multimidia.arduino.uploader.ArduinoUploader;
import com.multimidia.arduino.uploader.UsbSerialException;
import com.multimidia.arduino.uploader.UsbSerialManager;

import java.io.IOException;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    Button abrirBotao;
    String deviceKeyName;
    Button uploadHex;
    UsbSerialManager usbSerialManager;
    TextView status;

    private final BroadcastReceiver mUsbNotifyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                //Get intent
                case UsbSerialManager.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB permission granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbSerialManager.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission denied", Toast.LENGTH_SHORT).show();
                    break;
                case UsbSerialManager.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbSerialManager.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    //usbConnectChange(UsbConnectState.DISCONNECTED);
                    break;
                case UsbSerialManager.ACTION_USB_CONNECT: // USB DISCONNECTED
                    Toast.makeText(context, "USB connected", Toast.LENGTH_SHORT).show();
                    //usbConnectChange(UsbConnectState.CONNECT);
                    break;
                case UsbSerialManager.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
                case UsbSerialManager.ACTION_USB_READY:
                    Toast.makeText(context, "Usb device ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbSerialManager.ACTION_USB_DEVICE_NOT_WORKING:
                    Toast.makeText(context, "USB device not working", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private final BroadcastReceiver mUsbHardwareReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UsbSerialManager.ACTION_USB_PERMISSION_REQUEST)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) // User accepted our USB connection. Try to open the device as a serial port
                {
                    UsbDevice grantedDevice = intent.getExtras().getParcelable(UsbManager.EXTRA_DEVICE);
                    usbPermissionGranted(grantedDevice.getDeviceName());
                    Intent it = new Intent(UsbSerialManager.ACTION_USB_PERMISSION_GRANTED);
                    context.sendBroadcast(it);

                } else // User not accepted our USB connection. Send an Intent to the Main Activity
                {
                    Intent it = new Intent(UsbSerialManager.ACTION_USB_PERMISSION_NOT_GRANTED);
                    context.sendBroadcast(it);
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                Intent it = new Intent(UsbSerialManager.ACTION_USB_CONNECT);
                context.sendBroadcast(it);

            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                // Usb device was disconnected. send an intent to the Main Activity
                Intent it = new Intent(UsbSerialManager.ACTION_USB_DISCONNECTED);
                context.sendBroadcast(it);

            }
        }
    };

    public void usbPermissionGranted(String usbKey) {
        deviceKeyName = usbKey;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usbSerialManager = new UsbSerialManager(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbSerialManager.ACTION_USB_PERMISSION_REQUEST);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        registerReceiver(mUsbHardwareReceiver, filter);
        abrirBotao = findViewById(R.id.abrirBotao);
        uploadHex = findViewById(R.id.uploadHex);
        status = findViewById(R.id.status);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter usbFilter = new IntentFilter();
        usbFilter.addAction(UsbSerialManager.ACTION_USB_PERMISSION_GRANTED);
        usbFilter.addAction(UsbSerialManager.ACTION_NO_USB);
        usbFilter.addAction(UsbSerialManager.ACTION_USB_DISCONNECTED);
        usbFilter.addAction(UsbSerialManager.ACTION_USB_CONNECT);
        usbFilter.addAction(UsbSerialManager.ACTION_USB_NOT_SUPPORTED);
        usbFilter.addAction(UsbSerialManager.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbNotifyReceiver, usbFilter);
    }

    public void compilar(View view) throws IOException {
        status.setText("Iniciando compilação...");
        String teste = Terminal.exec("echo teste");
        Log.e("teste", teste);
        status.setText(teste);
    }

    public void abrir(View view) {
        try {
            Map<String, UsbDevice> deviceList = usbSerialManager.getUsbDeviceList();

            Map.Entry<String, UsbDevice> entry = deviceList.entrySet().iterator().next();
            String keySelect = entry.getKey();
            boolean hasPem = usbSerialManager.checkDevicePermission(keySelect);
            if (hasPem) {
                //portSelect.setText(keySelect);
                if(deviceKeyName == null) {
                    uploadHex.setEnabled(true);
                    abrirBotao.setEnabled(false);
                    Snackbar.make(this, view, "Arduino setado", Snackbar.LENGTH_LONG).show();
                    deviceKeyName = keySelect;
                } else {
                    Snackbar.make(this, view, "Arduino não setado", Snackbar.LENGTH_LONG).show();
                }
                //if (fab != null) fab.show();
            } else {
                usbSerialManager.getDevicePermission(keySelect);
            }

        } catch (UsbSerialException e) {
            Snackbar.make(this, view, "Não foi detectado nenhum arduino conectado", Snackbar.LENGTH_LONG).show();
            return;
        }
    }

    public void enviarArquivo(View view) {
        if(deviceKeyName == null) {
            Snackbar.make(this, view, "Você não plugou o arduino", Snackbar.LENGTH_LONG).show();
            return;
        }
        try {
            ArduinoUploader.makeUpload(getResources().getAssets().open("piscar.hex"), this, deviceKeyName);
        } catch (RuntimeException | IOException e) {
            Log.e("teste", e.toString());
        }
    }
}