package com.example.taichiabe.rectanglesender;
/*=========================================================*
 * システム：矩形波送信処理
 * http://tongarism.com/1861
 * https://dev.classmethod.jp/smartphone/andoid_sound_generator_xmas/
 *==========================================================*/
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements OnCheckedChangeListener {

    //取得するデフォルト音量
    public static int DEFAULT_VOLUME;

    AudioManager audioManager;
    AudioTrack audioTrack = null;
    private List<SoundDto> soundList = new ArrayList<>();
    Thread send;
    boolean isPlaying = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView sendingFreqText = findViewById(R.id.sendingFreqText);
        sendingFreqText.setText(R.string.sendingFreqText);
        TextView volumeText = findViewById(R.id.volumeText);
        volumeText.setText(R.string.volumeText);
        Switch sendingSwitch = findViewById(R.id.Switch);
        sendingSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if(isChecked) {
            EditText sendingFreqEdit = findViewById(R.id.sendingFreqEdit);
            EditText volumeEdit = findViewById(R.id.volumeEdit);

            final int SENDING_FREQ = Integer.parseInt(sendingFreqEdit.getText().toString());
            final int SENDING_VOLUME = Integer.parseInt(volumeEdit.getText().toString());

            final int SAMPLING_RATE = 4 * SENDING_FREQ;
            final int SEND_BUFFER_SIZE = 4 * SENDING_FREQ;

            audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

            audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    SAMPLING_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_8BIT,
                    SEND_BUFFER_SIZE,
                    AudioTrack.MODE_STREAM);

            soundList.add(new SoundDto(createWaves(SAMPLING_RATE)));

            DEFAULT_VOLUME = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, SENDING_VOLUME, 0);

            audioTrack.play();
            isPlaying = true;
            send = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(isPlaying) {
                        for(SoundDto sound : soundList) {
                            audioTrack.write(sound.getSound(), 0, sound.getSound().length);
                        }
                    }
                    audioTrack.stop();
                    audioTrack.release();
                }
            });
            send.start();
        } else {

            if(audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                audioTrack.stop();
                //audioTrack.release();
                isPlaying = false;
            }
            //soundList.clear();
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, DEFAULT_VOLUME, 0);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if(audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.stop();
            isPlaying = false;
        }
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, DEFAULT_VOLUME, 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.stop();
            audioTrack.release();
            isPlaying = false;
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
