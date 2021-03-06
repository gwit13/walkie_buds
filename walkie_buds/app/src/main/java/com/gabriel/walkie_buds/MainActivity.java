package com.gabriel.walkie_buds;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "switchboard";

    public Button playback;
    public Button btcheck;
    public ExtendedFloatingActionButton share;
    public TextView text;
    public TextView btstatus;
    public TextView recstatus;
    public TextView audiorec;
    public TextView audioperm;
    private Activity mActivity;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int REQUEST_MODIFY_AUDIO_SETTINGS = 300;
    private static final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 400;

    private static String fileName = null;

    private boolean mStartRecording = true;
    private boolean mStartPlaying = true;

    private MediaRecorder recorder = null;
    private MediaPlayer player = null;

    //request permission
    private boolean permissionToRecord = false;
    private boolean permissionToModify = false;
    private String[] permissions = {Manifest.permission.MODIFY_AUDIO_SETTINGS};
    //Manifest.permission.RECORD_AUDIO
    
    //bluetooth
    public AudioManager am;


    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return super.registerReceiver(receiver, filter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        System.out.println("hello");
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if(permissionToRecord)
                    audiorec.setText("Audio Permissions granted: " + permissionToRecord);
                else
                    audiorec.setText("Audio Permissions denied: " + permissionToRecord);
                requestSecondPermission();
                break;
            case REQUEST_MODIFY_AUDIO_SETTINGS:
                permissionToModify = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if(permissionToModify)
                    audioperm.setText("Audio Settings granted: " + permissionToModify);
                else
                    audioperm.setText("Audio Settings denied: " + permissionToModify);
                System.out.println("Hello?");
                break;
        }
        if(!permissionToRecord)
            System.out.println("HIIIIIII");
    }

    public void requestSecondPermission(){
        ActivityCompat.requestPermissions(this, permissions, REQUEST_MODIFY_AUDIO_SETTINGS);
    }

    //call methods
    private void onRecord(boolean start) {
        if(start){
            startRecord();
        }
        else{
            stopRecord();
        }
    }

    private void onPlay(boolean start) {
        if(start){
            startPlay();
        }
        else{
            stopPlay();
        }
    }

    //actionable methods
    private void startRecord(){
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioSamplingRate(8000);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        am.startBluetoothSco();
        try{
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() on record failed");
        }

//        //debug
//        AudioManager audioManager = (AudioManager) WalkieBuds.getAppContext().getSystemService(Context.AUDIO_SERVICE);
//        System.out.println(audioManager.getMode());

        recstatus.setText("Recording");
        recorder.start();
    }

    private void stopRecord(){
        recstatus.setText("Not Recording");
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    private void startPlay(){
        player = new MediaPlayer();
        try{
            player.setDataSource(fileName);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() on play failed");
        }
    }

    private void stopPlay(){
        player.release();
        player = null;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/audiorecordtest.3gp";

        audiorec = findViewById(R.id.audiorec);
        audioperm = findViewById(R.id.audioperm);

        requestSecondPermission();
        //ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        //ActivityCompat.requestPermissions(this, permissions, REQUEST_MODIFY_AUDIO_SETTINGS);

//        requestPermissions(new String[]{
//                        Manifest.permission.RECORD_AUDIO,
//                        Manifest.permission.MODIFY_AUDIO_SETTINGS},
//                ASK_MULTIPLE_PERMISSION_REQUEST_CODE);

        mActivity = MainActivity.this;
        playback = findViewById(R.id.button);
        text = findViewById(R.id.text);
        share = findViewById(R.id.floatingActionButton);
        btstatus = findViewById(R.id.btstatus);
        btcheck = findViewById(R.id.bt_check);
        recstatus = findViewById(R.id.recstatus);

        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        bluetoothConnect();

    }

    public void bluetoothConnect(){
        registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
                Log.d(LOG_TAG, "Audio SCO state: " + state);

                if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {
                    Log.d(LOG_TAG, "bluetooth connected");
                    btstatus.setText("Bluetooth Connected");
                    /*
                     * Now the connection has been established to the bluetooth device.
                     * Record audio or whatever (on another thread).With AudioRecord you can record with an object created like this:
                     * new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                     * AudioFormat.ENCODING_PCM_16BIT, audioBufferSize);
                     *
                     * After finishing, don't forget to unregister this receiver and
                     * to stop the bluetooth connection with am.stopBluetoothSco();
                     */
                    unregisterReceiver(this);
                }
                else{
                    //maybe a text prompt saying connected?
                    Log.d(LOG_TAG, "bluetooth connection failed");
                }

            }
        }, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED));
    }


    //final implementations - to do
    public void shareAudio(View view) {
        onRecord(mStartRecording);
        mStartRecording = !mStartRecording;
    }

    public void playback(View view) { //WRONG, FIX IT
        onPlay(mStartPlaying);
        if(mStartPlaying){
            playback.setText("Stop");
        }
        else{
            playback.setText("Playback");
        }
        mStartPlaying = !mStartPlaying;
    }

    public void onStop(){
        super.onStop();
        if(recorder != null){
            recorder.release();
            recorder = null;
        }

        if(player != null){
            player.release();
            player = null;
        }
    }

    public void btcheck(View view) {
        bluetoothConnect();
    }

//    protected void checkPermission(){
//        if(ContextCompat.checkSelfPermission(mActivity,Manifest.permission.CAMERA)
//                + ContextCompat.checkSelfPermission(
//                mActivity,Manifest.permission.READ_CONTACTS)
//                + ContextCompat.checkSelfPermission(
//                mActivity,Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED){
//
//            // Do something, when permissions not granted
//            if(ActivityCompat.shouldShowRequestPermissionRationale(
//                    mActivity,Manifest.permission.CAMERA)
//                    || ActivityCompat.shouldShowRequestPermissionRationale(
//                    mActivity,Manifest.permission.READ_CONTACTS)
//                    || ActivityCompat.shouldShowRequestPermissionRationale(
//                    mActivity,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
//                // If we should give explanation of requested permissions
//
//                // Show an alert dialog here with request explanation
//                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
//                builder.setMessage("Camera, Read Contacts and Write External" +
//                        " Storage permissions are required to do the task.");
//                builder.setTitle("Please grant those permissions");
//                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        ActivityCompat.requestPermissions(
//                                mActivity,
//                                new String[]{
//                                        Manifest.permission.CAMERA,
//                                        Manifest.permission.READ_CONTACTS,
//                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
//                                },
//                                MY_PERMISSIONS_REQUEST_CODE
//                        );
//                    }
//                });
//                builder.setNeutralButton("Cancel",null);
//                AlertDialog dialog = builder.create();
//                dialog.show();
//            }else{
//                // Directly request for required permissions, without explanation
//                ActivityCompat.requestPermissions(
//                        mActivity,
//                        new String[]{
//                                Manifest.permission.CAMERA,
//                                Manifest.permission.READ_CONTACTS,
//                                Manifest.permission.WRITE_EXTERNAL_STORAGE
//                        },
//                        MY_PERMISSIONS_REQUEST_CODE
//                );
//            }
//        }else {
//            // Do something, when permissions are already granted
//            Toast.makeText(mContext,"Permissions already granted",Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
//        switch (requestCode){
//            case MY_PERMISSIONS_REQUEST_CODE:{
//                // When request is cancelled, the results array are empty
//                if(
//                        (grantResults.length >0) &&
//                                (grantResults[0]
//                                        + grantResults[1]
//                                        + grantResults[2]
//                                        == PackageManager.PERMISSION_GRANTED
//                                )
//                ){
//                    // Permissions are granted
//                    Toast.makeText(mContext,"Permissions granted.",Toast.LENGTH_SHORT).show();
//                }else {
//                    // Permissions are denied
//                    Toast.makeText(mContext,"Permissions denied.",Toast.LENGTH_SHORT).show();
//                }
//                return;
//            }
//        }
//    }
//}
}