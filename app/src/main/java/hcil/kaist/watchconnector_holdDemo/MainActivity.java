package hcil.kaist.watchconnector_holdDemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import Event.WatchTouchEvent_Ext;
import Views.KeyboardView;
import comm.bluetooth.BluetoothChatService;
import comm.bluetooth.DeviceListActivity;
import comm.usb.Constants;
import comm.usb.SerialConnector;
import util.ExperimentManager;
import util.Measure;
import util.PhraseSets.ExamplePhrases;
import util.ScreenInfo;

import static hcil.kaist.watchconnector_holdDemo.R.id.result;


public class MainActivity extends Activity {

    private final int COMM_METHOD_BLUETOOTH = 1;
    private final int COMM_METHOD_SERIAL = 2;

    private int commMethod = COMM_METHOD_BLUETOOTH;

    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_CONNECT = 6;
    public static final int MESSAGE_VIBRATE = 7;
    public static final int MESSAGE_END = 8;
    public static final int MESSAGE_TOAST2 = 9;
    public static final int MESSAGE_PHASE = 10;
    public static final int MESSAGE_PHASE_SET_2 = 20;
    public static final int MESSAGE_TASK = 11;
    public static final int MESSAGE_EYE_CONDITION = 12;
    public static final int MESSAGE_INPUT = 20;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private KeyboardView gv;
    private CountDownTimer cdt;
    private CountDownTimer cdtBlock, cdtVib;
    private int vibCount = 0;
    private boolean end = false;

    private long current, prev;
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;


    private UsbSerialHandler mUsbHandler = null;

    private SerialListener mListener = null;
    private SerialConnector mSerialConn = null;

    private Button setting, mode;

    private Vibrator vib;
    int actions[];
    int point[];
    int x, y, e;
    int minSize;

    //watchDispPx is the dimesion for used smartwatch (G-Watch).
    // if you use another smartwatch for this app, you should replace the value below as same to the dimension of your smartwatch
    private final int watchDispPx_width = 280;
    private final int watchDispPx_height = 280;

    private WatchTouchEvent_Ext event;
    private TextView inputText, exampleText, resultView;

    //Experiment
    Measure measure;
    boolean isFirstInput;
    boolean doneActivate;
    boolean isWait;
    private final int waitTime =10000; //ms scale
    ExamplePhrases examples;
    ExperimentManager experimentManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                        | android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);


        measure = new Measure();
        isFirstInput = true;
        doneActivate = false;
        isWait = false;
        examples = new ExamplePhrases();
        examples.readTxt(getResources().openRawResource(R.raw.phrases));
        experimentManager = new ExperimentManager(3, 5); //block, trial

        actions = new int[5];
        point = new int[4];

        setting = (Button)findViewById(R.id.setting);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serverIntent = null;
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            }
        });
        mode = (Button)findViewById(R.id.mode);
        mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gv.changeKeyboardMode();
            }
        });
        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(200);
        inputText = (TextView)findViewById(R.id.input);
        exampleText = (TextView)findViewById(R.id.example);
        resultView = (TextView)findViewById(result);

        Display mdisp = getWindowManager().getDefaultDisplay();

        Point mdispSize = new Point();
        mdisp.getSize(mdispSize);

        ScreenInfo.displayMetrics = new DisplayMetrics();

        minSize = mdispSize.y < mdispSize.x ? mdispSize.y : mdispSize.x;

        ScreenInfo.screenHeight = mdispSize.y;
        ScreenInfo.screenWidth = mdispSize.x;

//        ScreenInfo.screenHeight = 540;
//        ScreenInfo.screenWidth = 960;


        // Initialize
        mListener = new SerialListener();
        mUsbHandler = new UsbSerialHandler();

        // Initialize Serial connector and starts Serial monitoring thread.
        mSerialConn = new SerialConnector(this, mListener, mUsbHandler);
        mSerialConn.initialize();

        ScreenInfo.heightRatio = (float) ScreenInfo.screenHeight / watchDispPx_height;
        ScreenInfo.widthRatio = (float) ScreenInfo.screenWidth / watchDispPx_width;

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        gv = (KeyboardView)findViewById(R.id.gv);
        gv.initialize(this, mHandler);
        event = new WatchTouchEvent_Ext();

        cdtVib  = new CountDownTimer(50*1000 - waitTime, 50*1000 - waitTime) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                Toast.makeText(MainActivity.this, "ready", Toast.LENGTH_SHORT).show();
                vib.vibrate(200);


                cdtBlock.start();
            }
        };
        int time = 50*1000 - waitTime;
        time = 500;
        cdtVib  = new CountDownTimer(time, time) {
            @Override
            public void onTick(long millisUntilFinished) {
                vib.vibrate(200);
            }

            @Override
            public void onFinish() {
                vibCount = 0;
                vib.vibrate(100);
                Log.d("exampleText", "new");
                exampleText.setText(examples.getRandomPhrase());
                measure.setBlock(experimentManager.getBlock());
                measure.setTrial(experimentManager.getTrial());
                measure.recordExample(exampleText.getText().toString());
                exampleText.setTextColor(Color.GREEN);
                exampleText.postInvalidate();
                experimentManager.setBlockDone(false);
                cdt.start();
            }
        };
        cdt = new CountDownTimer(waitTime, waitTime) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if(!experimentManager.isBlockDone()){
                    isWait = false;
                    exampleText.setTextColor(Color.WHITE);
                    exampleText.postInvalidate();
                    cdt.cancel();

                }else {
                    cdtVib.start();
                }
            }
        };
    }


    @Override
    public void onStart() {
        super.onStart();
        if (D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }

    }


    @Override
    public synchronized void onResume() {
        super.onResume();
        if (D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the compose field with a listener for the return key


        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        try {
            cdtBlock.cancel();
        }catch (Exception e){

        }
        if (D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            cdtBlock.cancel();
        }catch (Exception e){

        }
        if (D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            cdtBlock.cancel();
        }catch (Exception e){

        }
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();

        if (D) Log.e(TAG, "--- ON DESTROY ---");
        mSerialConn.finalize();
    }


    private void ensureDiscoverable() {
        if (D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            //mOutEditText.setText(mOutStringBuffer);
        }
    }


    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_CONNECT:
                    switch (commMethod) {
                        case COMM_METHOD_SERIAL:
                            break;
                        case COMM_METHOD_BLUETOOTH:
                            Intent serverIntent = null;
                            // Launch the DeviceListActivity to see devices and do scan
                            serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                            break;
                    }
                    break;
                case MESSAGE_STATE_CHANGE:
                    if (D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
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
                case MESSAGE_WRITE:
                    //byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = String.valueOf(msg.arg1) + "\r\n";
                    Log.d("MainActivity", String.valueOf(msg.arg1));
                    mChatService.write(writeMessage.getBytes());
                    break;
                case MESSAGE_READ:
                    //long start = System.currentTimeMillis();
                    interpretPacket((byte[]) msg.obj, msg.arg1);
                    //Log.d("interpret", ""+ (System.currentTimeMillis() - start));
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();

                    break;
                case MESSAGE_TOAST:
                    if (!end)
                        Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                                Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST2:
                    Toast.makeText(getApplicationContext(), (String) msg.obj,
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_VIBRATE:
                    vib.vibrate(msg.arg2);
                    break;
                case MESSAGE_END:
                    end = true;
                    vib.vibrate(100);
                    Toast.makeText(getApplicationContext(), "End!",
                            Toast.LENGTH_SHORT).show();
                    MainActivity.this.finish();
                    break;
                case MESSAGE_PHASE:
                    //gv.changeKeyboard();
                    break;
                case MESSAGE_INPUT:
                    String inputed = (String)msg.obj;
                    Log.d("input", inputed);
                    if(isFirstInput){
                        inputStart(System.currentTimeMillis());
                    }
                    inputText.append(inputed);
                    inputText.postInvalidate();
                    measure.done(inputed.charAt(0), System.currentTimeMillis());
                    break;
            }
        }
    };

    public void inputStart(long current){
        measure.start(current);
        isFirstInput = false;
    }

    public void inputDone(){
        if(!isWait) {
            isWait = true;
            measure.startRecording();
            String example = exampleText.getText().toString();
            String inputed = inputText.getText().toString();
            Log.d("example", example);
            Log.d("inputed", inputed);
            exampleText.setText("(file write)");
            inputText.setText("");
            exampleText.postInvalidate();
            inputText.postInvalidate();

            isFirstInput = true;
            int numOfInput = inputed.length();
            if (numOfInput > 0) {
                int eLen = example.length();
                int maxLen = eLen > numOfInput ? eLen : numOfInput;
                int MSD = measure.getMSD(example, inputed);
                float ER = (float) MSD * 100 / maxLen;

                measure.userInputAnalyse(maxLen);

                String result = String.format(" WPM: %.2f / TER: %.2f (CER: %.2f, UER: %.2f)", measure.getWPM(numOfInput), measure.getTER(), measure.getCER(), measure.getUER());
                resultView.setText(result);
                resultView.postInvalidate();
                //isRecording = true;

            } else {
                resultView.setText("Cannot Measure performance..");
                resultView.postInvalidate();
                //isRecording = true;
            }


            measure.recordResult(inputed);
            measure.recordSideTouchEvent();
            measure.recordUserInput();
            measure.recordingDone();

            if (experimentManager.addTrial()) {
                //block is done
                exampleText.setTextColor(Color.YELLOW);
                exampleText.setText("Block " + (experimentManager.getBlock() - 1) + " is done! please wait for next block");
                exampleText.postInvalidate();
                if (experimentManager.isDone()) {
                    measure.fileClose();
                    Toast.makeText(this, "Experiment is done!", Toast.LENGTH_SHORT).show();
                    exampleText.setText("Experiment is done!");
                    exampleText.postInvalidate();
                }
            } else {
                Log.d("exampleText", "new");
                exampleText.setText(examples.getRandomPhrase());
                measure.setBlock(experimentManager.getBlock());
                measure.setTrial(experimentManager.getTrial());
                measure.recordExample(exampleText.getText().toString());
                exampleText.setTextColor(Color.GREEN);
                exampleText.postInvalidate();
            }

            cdt.start();
        }


    }

    public int positionRemappingX(int raw) {
        int result = -1;
        return (int)(raw * ScreenInfo.widthRatio);
    }

    public int positionRemappingY(int raw) {
        int result = -1;
        return (int)(raw * ScreenInfo.heightRatio);
    }

    public void interpretPacket(byte[] readBuf, int byteLength) {
        if(!isWait) {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);

            event.eventTime = System.currentTimeMillis();
            event.selected = (char)0;
            String readMessage = new String(readBuf, 0, byteLength);

            Log.d("msg", readMessage);
            try {
                String[] str = readMessage.split(" ");
                //public SideTouchEvent(int actions[], int point[], int x, int y)
                //Log.d("get", str[0] + ", " + str[1] + ", " + str[2]);
                switch (str[0].charAt(0)) {
                    case 'v':
                        doneActivate = false;
                        event.state_screen = WatchTouchEvent_Ext.SCREEN_STATE_UP;
                        event.gesture = -1;
                        //Toast.makeText(this, "v",Toast.LENGTH_SHORT).show();
                        break;
                    case 'x':
                        gv.isGesture = false;
                        event.state_screen = WatchTouchEvent_Ext.SCREEN_STATE_DOWN;
                        event.xPos = positionRemappingX(Integer.parseInt(str[1]));
                        event.yPos = positionRemappingY(Integer.parseInt(str[2]));
                        event.gesture = -1;

                        break;

                    case 's':
                        gv.gestureInput();
                        int what = (int) ((Integer.parseInt(str[1])));
                        //Log.d("inter", readMessage);
                        switch (what) {
                            case 0: //left
                                measure.done('-', System.currentTimeMillis());
                                measure.userInputBackspace();
                                event.gesture = 0;

                                inputText.setText(inputText.getText().subSequence(0, inputText.getText().length() - 1));
                                event.selected = '-';
                                inputText.postInvalidate();
                                doneActivate = false;
                                break;
                            case 2: //right
                                event.gesture = 2;
                                if (isFirstInput) {
                                    inputStart(System.currentTimeMillis());
                                }
                                measure.done(' ', System.currentTimeMillis());
                                inputText.append(" ");
                                event.selected = '>';
                                inputText.postInvalidate();
                                doneActivate = false;
                                break;
                            case 9: //end
                                event.state_screen = WatchTouchEvent_Ext.SCREEN_STATE_UP;
                                event.xPos = -1;
                                event.yPos = -1;
                                event.gesture = 9;
                                gv.gestureInput();
                                if (inputText.getText().toString().length() > 0) {
                                    measure.addWatchTouchEvent(event);
                                    inputDone();
                                }
                                break;
                        }
                        break;
                    case 'z':
                        event.state_screen = WatchTouchEvent_Ext.SCREEN_STATE_MOVE;
                        event.xPos = positionRemappingX(Integer.parseInt(str[1]));
                        event.yPos = positionRemappingY(Integer.parseInt(str[2]));
                        event.gesture = -1;
                        break;
                }
                measure.addWatchTouchEvent(gv.OnWatchTouchEvent(event));
            } catch (NumberFormatException e) {
                Log.e("Exception", e.toString());
                Log.e("Exception", readMessage);
            } catch (Exception ee) {
                Log.e("Exception", ee.toString());
                Log.e("Exception", readMessage);

            }
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BLuetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
            case R.id.secure_connect_scan:
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            case R.id.insecure_connect_scan:
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            case R.id.discoverable:
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
        }
        return false;
    }


    public class SerialListener {
        public void onReceive(int msg, int arg0, int arg1, String arg2, Object arg3) {
            switch(msg) {
                case Constants.MSG_DEVICD_INFO:
                    //mTextSerial.append("\nMSG_DEVICD_INFO / " + (String)arg2);
                    break;
                case Constants.MSG_DEVICE_COUNT:
                    //mTextSerial.append("\nMSG_DEVICE_COUNT / " + Integer.toString(arg0) + " device(s) found \n");
                    break;
                case Constants.MSG_READ_DATA_COUNT:
                    //mTextSerial.append("\nMSG_READ_DATA_COUNT / " + Integer.toString(arg0) + " buffer received \n");
                    break;
                case Constants.MSG_READ_DATA:
                    //mTextSerial.append("\nMSG_READ_DATA / " + (String)arg3);
                    break;
                case Constants.MSG_SERIAL_ERROR:
                    //mTextSerial.append("\nMSG_SERIAL_ERROR / " + (String)arg2);
                    break;
                case Constants.MSG_FATAL_ERROR_FINISH_APP:
                    finish();
                    break;
            }
        }
    }

    public class UsbSerialHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case Constants.MSG_DEVICD_INFO:
                    Log.d("MSG_DEVICD_INFO",(String)msg.obj);
                    break;
                case Constants.MSG_DEVICE_COUNT:
                    Log.d("MSG_DEVICE_COUNT",Integer.toString(msg.arg1) + " device(s) found \n");
                    break;
                case Constants.MSG_READ_DATA_COUNT:
                    //mTextSerial.append((String)msg.obj );
//                    if(
//                    //mTextSerial.getLineCount() > 40){
//                        //mTextSerial.setText((String)msg.obj);
//                    }
                    try {
                        event.eventTime = System.currentTimeMillis();
                        String temp = (String) msg.obj;

                        e = Integer.parseInt(temp);
                        event.ePos = e * 169 + 42;
                        event.gesture = -3;
                        if(event.state_screen > WatchTouchEvent_Ext.SCREEN_STATE_UP){
                            gv.onEdgeTouchEvent(event.ePos);
                            event.state_screen = WatchTouchEvent_Ext.SCREEN_STATE_DOWN;
                            measure.addWatchTouchEvent(gv.OnWatchTouchEvent(event));
                        }else {
                            event.selected = (char) 0;
                            gv.onEdgeTouchEvent(event.ePos);
                            measure.addWatchTouchEvent(event);
                        }

                        Log.d("UsbSerialHandler", "e: " + event.ePos);
                    }catch(NumberFormatException ee){
                        Log.d("UsbSerialHandler", ee.getMessage());
                        Log.d("UsbSerialHandler", (String) msg.obj);
                    }
                    //gv.OnWatchTouchEvent(event);
                    break;
                case Constants.MSG_READ_DATA:
                    //mTextSerial.append("\nMSG_READ_DATA / " + (String)msg.obj );
                    break;
                case Constants.MSG_SERIAL_ERROR:
                    Log.d("MSG_SERIAL_ERROR",(String)msg.obj);
                    break;
            }
        }
    }
}



