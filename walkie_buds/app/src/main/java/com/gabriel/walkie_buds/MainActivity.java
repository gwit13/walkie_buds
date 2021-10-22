package com.gabriel.walkie_buds;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
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

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = null;

    private boolean mStartRecording = true;
    private boolean mStartPlaying = true;

    private MediaRecorder recorder = null;
    private MediaPlayer player = null;

    //request permission
    private boolean permissionToRecord = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if(!permissionToRecord)
            finish();
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

        try{
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() on record failed");
        }

//        //debug
//        AudioManager audioManager = (AudioManager) WalkieBuds.getAppContext().getSystemService(Context.AUDIO_SERVICE);
//        System.out.println(audioManager.getMode());

        recorder.start();
    }

    private void stopRecord(){
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

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        playback = findViewById(R.id.button);
        share = findViewById(R.id.floatingActionButton);
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