package com.multimidia.arduino.uploader;

import android.content.Context;
import android.util.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;

import ArduinoUploader.Config.Arduino;
import CSharpStyle.IProgress;
import IntelHexFormatReader.Utils.LineReader;
import ArduinoUploader.IArduinoUploaderLogger;
import ArduinoUploader.ArduinoSketchUploader;
import ArduinoUploader.Config.Arduino;

public class ArduinoUploader implements Runnable {

    static public void makeUpload(InputStream input, Context context, String deviceKeyName) {
        new Thread(new ArduinoUploader(input, context, deviceKeyName)).start();
    }

    InputStream input;
    Context context;
    String deviceKeyName;

    ArduinoUploader(InputStream input, Context context, String deviceKeyName) {
        this.input = input;
        this.context = context;
        this.deviceKeyName = deviceKeyName;
    }

    @Override
    public void run() {
        class LogOutput implements IArduinoUploaderLogger {
            final String TAG = "teste";

            @Override
            public void Error(String message, Exception exception) {
                Log.e(TAG, message);
            }

            @Override
            public void Warn(String message) {
                Log.w(TAG, message);
            }

            @Override
            public void Info(String message) {
                Log.i(TAG, message);
            }

            @Override
            public void Debug(String message) {
                Log.d(TAG, message);
            }

            @Override
            public void Trace(String message) {
                Log.wtf(TAG, message);
            }
        }

        try {
            IProgress progress = new IProgress<Double>() {
                @Override
                public void Report(Double value) {
                    String result = String.format("Upload progress: %1$,3.2f%%", value * 100);
                    Log.d("teste", result);
                    //logUI("Procees:" + result);

                }
            };

            Boards board = Boards.ARDUINO_UNO;

            Arduino arduinoBoard = new Arduino(board.name, board.chipType, board.uploadBaudrate, board.uploadProtocol);

            arduinoBoard.setSleepAfterOpen(250);

            Reader reader = new InputStreamReader(input);
            Collection<String> hexFileContents = new LineReader(reader).readLines();
            ArduinoSketchUploader<SerialPortStreamImpl> uploader = new ArduinoSketchUploader<SerialPortStreamImpl>(context, SerialPortStreamImpl.class, null, new LogOutput(), progress) {};
            uploader.UploadSketch(hexFileContents, arduinoBoard, deviceKeyName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
