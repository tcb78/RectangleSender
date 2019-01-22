package com.example.taichiabe.rectanglesender;
/*=========================================================*
 * システム：矩形波送信処理
 *==========================================================*/
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements OnCheckedChangeListener {

    int SENDFREQ;
    int VOLUME;
    int SendSR;
    int SendBufSize;
    int musicVolume = 0;

    AudioManager audioManager;
    AudioTrack audioTrack = null;
    private List<SoundDto> soundList = new ArrayList<SoundDto>();
    Thread send;
    boolean bIsPlaying = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView SendfreqText = findViewById(R.id.SendfreqText);
        SendfreqText.setText(R.string.SendfreqText);
        TextView volumeText = findViewById(R.id.volumeText);
        volumeText.setText(R.string.volumeText);
        Switch switch1 = findViewById(R.id.Switch);
        switch1.setOnCheckedChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if(isChecked) {
            EditText SendfreqEdit = findViewById(R.id.SendfreqEdit);
            EditText volumeEdit = findViewById(R.id.volumeEdit);

            SENDFREQ = Integer.parseInt(SendfreqEdit.getText().toString());
            VOLUME = Integer.parseInt(volumeEdit.getText().toString());

            SendSR = 4 * SENDFREQ;
            SendBufSize = 4 * SENDFREQ;

            audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

            audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    SendSR,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_8BIT,
                    SendBufSize,
                    AudioTrack.MODE_STREAM);

            soundList.add(new SoundDto(createWaves(SendSR)));

            musicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, VOLUME, 0);

            audioTrack.play();
            bIsPlaying = true;
            send = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(bIsPlaying) {
                        for(SoundDto sound : soundList) {
                            audioTrack.write(sound.getSound(), 0, sound.getSound().length);
                        }
                        Log.d("audioTrack","audioTrack");
                    }
                    audioTrack.stop();
                    audioTrack.release();
                }
            });
            send.start();
        } else {

            Log.d("value","SENDFREQ：" + String.valueOf(SENDFREQ));
            Log.d("value","VOLUME：" + String.valueOf(VOLUME));

            if(audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                audioTrack.stop();
                //audioTrack.release();
                bIsPlaying = false;
            }
            //soundList.clear();
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, musicVolume, 0);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if(audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.stop();
            bIsPlaying = false;
        }
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, musicVolume, 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.stop();
            audioTrack.release();
            bIsPlaying = false;
        }
    }

    //波形データ生成
    public byte[] createWaves(int sampleRate) {
        byte[] data = new byte[sampleRate];
        int flag = 0;

        for(int i = 0; i < sampleRate; i = i + 2) {
            if(flag == 0) {
                data[i] = (byte)0xff;
                data[i + 1] = (byte)0xff;
                flag++;
            } else {
                data[i] = (byte)0x00;
                data[i + 1] = (byte)0x00;
                flag--;
            }
        }
        return data;
    }

}
