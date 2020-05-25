/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.example.android.bluetoothchat;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothHealth;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ViewAnimator;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.android.common.activities.SampleActivityBase;
import com.example.android.common.logger.Log;
import com.example.android.common.logger.LogFragment;
import com.example.android.common.logger.LogWrapper;
import com.example.android.common.logger.MessageOnlyLogFilter;

/**
 * A simple launcher activity containing a summary sample description, sample log and a custom
 * {@link Fragment} which can display a view.
 * <p>
 * For devices with displays with a width of 720dp or greater, the sample log is always visible,
 * on other devices it's visibility is controlled by an item on the Action Bar.
 */
public class MainActivity extends SampleActivityBase {

    public static final String TAG = "MainActivity";

    // Whether the Log Fragment is currently shown
    private boolean mLogShown;

    private void initBluetooth() {
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);


        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()
                && mBluetoothAdapter.getProfileConnectionState(BluetoothA2dp.A2DP) == BluetoothA2dp.STATE_CONNECTED) {
            Log.d(TAG, "Connection to Bluetooth A2DP");
        }

//        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()
//                && mBluetoothAdapter.getProfileConnectionState(BluetoothGatt.HEADSET) == BluetoothGatt.STATE_CONNECTED) {
//            Log.d(TAG, "Connection to Bluetooth Headset");
//        }
//
//        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()
//                && mBluetoothAdapter.getProfileConnectionState(BluetoothGattServer.HEADSET) == BluetoothGattServer.STATE_CONNECTED) {
//            Log.d(TAG, "Connection to Bluetooth Headset");
//        }

        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()
                && mBluetoothAdapter.getProfileConnectionState(BluetoothHealth.HEALTH) == BluetoothHealth.STATE_CONNECTED) {
            Log.d(TAG, "Connection to Bluetooth health");
        }

        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()
                && mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED) {
            Log.d(TAG, "Connection to Bluetooth Headset");
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            BluetoothChatFragment fragment = new BluetoothChatFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }

        initBluetooth();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem logToggle = menu.findItem(R.id.menu_toggle_log);
        logToggle.setVisible(findViewById(R.id.sample_output) instanceof ViewAnimator);
        logToggle.setTitle(mLogShown ? R.string.sample_hide_log : R.string.sample_show_log);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_toggle_log:
                mLogShown = !mLogShown;
                ViewAnimator output = findViewById(R.id.sample_output);
                if (mLogShown) {
                    output.setDisplayedChild(1);
                } else {
                    output.setDisplayedChild(0);
                }
                invalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Create a chain of targets that will receive log data
     */
    @Override
    public void initializeLogging() {
        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper);

        // Filter strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);

        // On screen logging via a fragment with a TextView.
        LogFragment logFragment = (LogFragment) getSupportFragmentManager()
                .findFragmentById(R.id.log_fragment);
        msgFilter.setNext(logFragment.getLogView());

        Log.i(TAG, "Ready");
    }


    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private final void printDeviceInfo(final BluetoothDevice device) {

        if (device.getBluetoothClass().hasService(BluetoothClass.Service.AUDIO)) {
            Log.d(TAG, "Has AUDIO Service)");
        }
        if (device.getBluetoothClass().hasService(BluetoothClass.Service.LIMITED_DISCOVERABILITY)) {
            Log.d(TAG, "Has LIMITED_DISCOVERABILITY Service)");
        }
        if (device.getBluetoothClass().hasService(BluetoothClass.Service.POSITIONING)) {
            Log.d(TAG, "Has POSITIONING Service)");
        }
        if (device.getBluetoothClass().hasService(BluetoothClass.Service.NETWORKING)) {
            Log.d(TAG, "Has NETWORKING Service)");
        }
        if (device.getBluetoothClass().hasService(BluetoothClass.Service.RENDER)) {
            Log.d(TAG, "Has RENDER Service)");
        }
        if (device.getBluetoothClass().hasService(BluetoothClass.Service.CAPTURE)) {
            Log.d(TAG, "Has CAPTURE Service)");
        }
        if (device.getBluetoothClass().hasService(BluetoothClass.Service.OBJECT_TRANSFER)) {
            Log.d(TAG, "Has OBJECT_TRANSFER Service)");
        }
        if (device.getBluetoothClass().hasService(BluetoothClass.Service.TELEPHONY)) {
            Log.d(TAG, "Has TELEPHONY Service)");
        }
        if (device.getBluetoothClass().hasService(BluetoothClass.Service.INFORMATION)) {
            Log.d(TAG, "Has INFORMATION Service)");
        }

        switch (device.getBluetoothClass().getMajorDeviceClass()) {
            case BluetoothClass.Device.Major.AUDIO_VIDEO: Log.d(TAG, "Major device class AUDIO_VIDEO)"); break;
            case BluetoothClass.Device.Major.COMPUTER: Log.d(TAG, "Major device class COMPUTER)"); break;
            case BluetoothClass.Device.Major.HEALTH: Log.d(TAG, "Major device class HEALTH)"); break;
            case BluetoothClass.Device.Major.IMAGING: Log.d(TAG, "Major device class IMAGING)"); break;
            case BluetoothClass.Device.Major.MISC: Log.d(TAG, "Major device class MISC)"); break;
            case BluetoothClass.Device.Major.NETWORKING: Log.d(TAG, "Major device class NETWORKING)"); break;
            case BluetoothClass.Device.Major.PERIPHERAL: Log.d(TAG, "Major device class PERIPHERAL)"); break;
            case BluetoothClass.Device.Major.PHONE: Log.d(TAG, "Major device class PHONE)"); break;
            case BluetoothClass.Device.Major.TOY: Log.d(TAG, "Major device class TOY)"); break;
            case BluetoothClass.Device.Major.UNCATEGORIZED: Log.d(TAG, "Major device class UNCATEGORIZED)"); break;
            case BluetoothClass.Device.Major.WEARABLE: Log.d(TAG, "Major device class WEARABLE)"); break;
            default: Log.d(TAG, "Major device class UNKNOWN!!!)"); break;
        }

        switch (device.getBluetoothClass().getDeviceClass()) {
            case BluetoothClass.Device.AUDIO_VIDEO_CAMCORDER:
                Log.d(TAG, "Device class AUDIO_VIDEO_CAMCORDER");
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO:
                Log.d(TAG, "Device class AUDIO_VIDEO_CAR_AUDIO");
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE:
                Log.d(TAG, "Device class AUDIO_VIDEO_HANDSFREE");
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES:
                Log.d(TAG, "Device class AUDIO_VIDEO_HEADPHONES");
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_HIFI_AUDIO:
                Log.d(TAG, "Device class AUDIO_VIDEO_HIFI_AUDIO");
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER:
                Log.d(TAG, "Device class AanUDIO_VIDEO_LOUDSPEAKER");
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_MICROPHONE:
                Log.d(TAG, "Device class AUDIO_VIDEO_MICROPHONE");
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_PORTABLE_AUDIO:
                Log.d(TAG, "Device class AUDIO_VIDEO_PORTABLE_AUDIO");
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_SET_TOP_BOX:
                Log.d(TAG, "Device class AUDIO_VIDEO_SET_TOP_BOX");
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_UNCATEGORIZED:
                Log.d(TAG, "Device class AUDIO_VIDEO_UNCATEGORIZED");
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_VCR:
                Log.d(TAG, "Device class AUDIO_VIDEO_VCR");
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_VIDEO_CAMERA:
                Log.d(TAG, "Device class AUDIO_VIDEO_VIDEO_CAMERA");
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_VIDEO_CONFERENCING:
                Log.d(TAG, "Device class AUDIO_VIDEO_VIDEO_CONFERENCING");
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET:
                Log.d(TAG, "Device class AUDIO_VIDEO_WEARABLE_HEADSET");
                break;
            case BluetoothClass.Device.COMPUTER_DESKTOP:
                Log.d(TAG, "Device class COMPUTER_DESKTOP");
                break;
            case BluetoothClass.Device.COMPUTER_SERVER:
                Log.d(TAG, "Device class COMPUTER_SERVER");
                break;
            case BluetoothClass.Device.COMPUTER_LAPTOP:
                Log.d(TAG, "Device class COMPUTER_LAPTOP");
                break;
            case BluetoothClass.Device.COMPUTER_HANDHELD_PC_PDA:
                Log.d(TAG, "Device class COMPUTER_HANDHELD_PC_PDA");
                break;
            case BluetoothClass.Device.COMPUTER_PALM_SIZE_PC_PDA:
                Log.d(TAG, "Device class COMPUTER_PALM_SIZE_PC_PDA");
                break;
            case BluetoothClass.Device.COMPUTER_WEARABLE:
                Log.d(TAG, "Device class COMPUTER_WEARABLE");
                break;
            case BluetoothClass.Device.COMPUTER_UNCATEGORIZED:
                Log.d(TAG, "Device class COMPUTER_UNCATEGORIZED");
                break;
        }

        Log.d(TAG, "Bluetooth Device class: " + String.format("0x%04X", device.getBluetoothClass().getDeviceClass()));

//
//
//            // Devices in the COMPUTER major class
//        public static final int COMPUTER_UNCATEGORIZED = 0x0100;
//        public static final int COMPUTER_DESKTOP = 0x0104;
//        public static final int COMPUTER_SERVER = 0x0108;
//        public static final int COMPUTER_LAPTOP = 0x010C;
//        public static final int COMPUTER_HANDHELD_PC_PDA = 0x0110;
//        public static final int COMPUTER_PALM_SIZE_PC_PDA = 0x0114;
//        public static final int COMPUTER_WEARABLE = 0x0118;
//
//        // Devices in the PHONE major class
//        public static final int PHONE_UNCATEGORIZED = 0x0200;
//        public static final int PHONE_CELLULAR = 0x0204;
//        public static final int PHONE_CORDLESS = 0x0208;
//        public static final int PHONE_SMART = 0x020C;
//        public static final int PHONE_MODEM_OR_GATEWAY = 0x0210;
//        public static final int PHONE_ISDN = 0x0214;
//
//        // Minor classes for the AUDIO_VIDEO major class
//        public static final int AUDIO_VIDEO_UNCATEGORIZED = 0x0400;
//        public static final int AUDIO_VIDEO_WEARABLE_HEADSET = 0x0404;
//        public static final int AUDIO_VIDEO_HANDSFREE = 0x0408;
//        //public static final int AUDIO_VIDEO_RESERVED              = 0x040C;
//        public static final int AUDIO_VIDEO_MICROPHONE = 0x0410;
//        public static final int AUDIO_VIDEO_LOUDSPEAKER = 0x0414;
//        public static final int AUDIO_VIDEO_HEADPHONES = 0x0418;
//        public static final int AUDIO_VIDEO_PORTABLE_AUDIO = 0x041C;
//        public static final int AUDIO_VIDEO_CAR_AUDIO = 0x0420;
//        public static final int AUDIO_VIDEO_SET_TOP_BOX = 0x0424;
//        public static final int AUDIO_VIDEO_HIFI_AUDIO = 0x0428;
//        public static final int AUDIO_VIDEO_VCR = 0x042C;
//        public static final int AUDIO_VIDEO_VIDEO_CAMERA = 0x0430;
//        public static final int AUDIO_VIDEO_CAMCORDER = 0x0434;
//        public static final int AUDIO_VIDEO_VIDEO_MONITOR = 0x0438;
//        public static final int AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER = 0x043C;
//        public static final int AUDIO_VIDEO_VIDEO_CONFERENCING = 0x0440;
//        //public static final int AUDIO_VIDEO_RESERVED              = 0x0444;
//        public static final int AUDIO_VIDEO_VIDEO_GAMING_TOY = 0x0448;
//
//        // Devices in the WEARABLE major class
//        public static final int WEARABLE_UNCATEGORIZED = 0x0700;
//        public static final int WEARABLE_WRIST_WATCH = 0x0704;
//        public static final int WEARABLE_PAGER = 0x0708;
//        public static final int WEARABLE_JACKET = 0x070C;
//        public static final int WEARABLE_HELMET = 0x0710;
//        public static final int WEARABLE_GLASSES = 0x0714;
//
//        // Devices in the TOY major class
//        public static final int TOY_UNCATEGORIZED = 0x0800;
//        public static final int TOY_ROBOT = 0x0804;
//        public static final int TOY_VEHICLE = 0x0808;
//        public static final int TOY_DOLL_ACTION_FIGURE = 0x080C;
//        public static final int TOY_CONTROLLER = 0x0810;
//        public static final int TOY_GAME = 0x0814;
//
//        // Devices in the HEALTH major class
//        public static final int HEALTH_UNCATEGORIZED = 0x0900;
//        public static final int HEALTH_BLOOD_PRESSURE = 0x0904;
//        public static final int HEALTH_THERMOMETER = 0x0908;
//        public static final int HEALTH_WEIGHING = 0x090C;
//        public static final int HEALTH_GLUCOSE = 0x0910;
//        public static final int HEALTH_PULSE_OXIMETER = 0x0914;
//        public static final int HEALTH_PULSE_RATE = 0x0918;
//        public static final int HEALTH_DATA_DISPLAY = 0x091C;
//
//        // Devices in PERIPHERAL major class
//        /**
//         * @hide
//         */
//        public static final int PERIPHERAL_NON_KEYBOARD_NON_POINTING = 0x0500;
//        /**
//         * @hide
//         */
//        public static final int PERIPHERAL_KEYBOARD = 0x0540;
//        /**
//         * @hide
//         */
//        public static final int PERIPHERAL_POINTING = 0x0580;
//        /**
//         * @hide
//         */
//        public static final int PERIPHERAL_KEYBOARD_POINTING = 0x05C0;
//
//
//
//        public static class Major {
//            private static final int BITMASK = 0x1F00;
//
//            public static final int MISC = 0x0000;
//            public static final int COMPUTER = 0x0100;
//            public static final int PHONE = 0x0200;
//            public static final int NETWORKING = 0x0300;
//            public static final int AUDIO_VIDEO = 0x0400;
//            public static final int PERIPHERAL = 0x0500;
//            public static final int IMAGING = 0x0600;
//            public static final int WEARABLE = 0x0700;
//            public static final int TOY = 0x0800;
//            public static final int HEALTH = 0x0900;
//            public static final int UNCATEGORIZED = 0x1F00;
//        }
//
//
//        public int getMajorDeviceClass() {
//            return (mClass & BluetoothClass.Device.Major.BITMASK);
//        }
    }



    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String deviceId = "";


            if (device != null) {
                deviceId  = device.getName() + ":" + device.getAddress();
            }

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d(TAG, "Action Found (" + deviceId + ")");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "Discovery Finished (" + deviceId + ")");
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Log.d(TAG, "Device Connected (" + deviceId + ")");
                printDeviceInfo(device);
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "Discovery finished (" + deviceId + ")");
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                Log.d(TAG, "Device Disconnect Request (" + deviceId + ")");
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Log.d(TAG, "Device Disconnected (" + deviceId + ")");
            }

            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()
                    && mBluetoothAdapter.getProfileConnectionState(BluetoothA2dp.A2DP) == BluetoothA2dp.STATE_CONNECTED) {
                Log.d(TAG, "BluetoothAdapter Connection to Bluetooth A2DP");
            }

            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()
                    && mBluetoothAdapter.getProfileConnectionState(BluetoothHealth.HEALTH) == BluetoothHealth.STATE_CONNECTED) {
                Log.d(TAG, "BluetoothAdapter Connection to Bluetooth health");
            }

            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()
                    && mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED) {
                Log.d(TAG, "BluetoothAdapter Connection to Bluetooth Headset");
            }
        }
    };
}
