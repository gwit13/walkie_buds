package com.gabriel.walkie_buds;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
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
    public ExtendedFloatingActionButton share;
    public TextView text;
    public TextView btstatus;
    public TextView recstatus;

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
    private String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS};

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
                ActivityCompat.requestPermissions(this, permissions, REQUEST_MODIFY_AUDIO_SETTINGS);
                break;
            case REQUEST_MODIFY_AUDIO_SETTINGS:
                permissionToModify = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                System.out.println("Hello?");
                break;
        }
        if(!permissionToRecord)
            System.out.println("HIIIIIII");
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

        //ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        //ActivityCompat.requestPermissions(this, permissions, REQUEST_MODIFY_AUDIO_SETTINGS);

        requestPermissions(new String[]{
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.MODIFY_AUDIO_SETTINGS},
                ASK_MULTIPLE_PERMISSION_REQUEST_CODE);

        playback = findViewById(R.id.button);
        text = findViewById(R.id.text);
        share = findViewById(R.id.floatingActionButton);
        btstatus = findViewById(R.id.btstatus);
        recstatus = findViewById(R.id.recstatus);

        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

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
}