package com.fu.mediaplayer;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    private Button start, pause, stop;
    MediaPlayer mPlayer;
    SeekBar mSeekBar;
    private TextView elapsedtimelabel, remaininglabel;
    private boolean isChanging = false;

    private Handler mHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start = findViewById(R.id.start);
        pause = findViewById(R.id.pause);
        stop = findViewById(R.id.stop);
        mSeekBar = findViewById(R.id.seekbar);
        elapsedtimelabel = findViewById(R.id.elapsedtime);
        remaininglabel = findViewById(R.id.remainingtime);



        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if song haven't start, press button begin to start
                if (mPlayer == null) {
                    mPlayer = MediaPlayer.create(MainActivity.this, R.raw.dream);
                    mPlayer.seekTo(0);
                    //set the song length
                    int totaltime = mPlayer.getDuration() / 1000;
                    mSeekBar.setMax(totaltime);
                    // TODO:When the music start and seekBar UI move with music time
                    //update UI
                    Thread thread = new Thread(seekrunnable);
                    thread.start();
                    timerunnable.run();
                    //The song finished to stop music
                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            stopPlayer();
                        }
                    });
                }
                mPlayer.start();
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer != null) {
                    mPlayer.pause();

                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlayer();
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(final SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    timerunnable.run();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //prevent from thread  seekbar  drag  conflict
                isChanging = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPlayer.seekTo(seekBar.getProgress() * 1000);
                isChanging = false;
                Thread thread = new Thread(seekrunnable);
                thread.start();
            }
        });

    }


    private void stopPlayer() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    private String createTime(int time) {
        String timeLabel = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;
        timeLabel = min + ":";
        if (sec < 10) {
            timeLabel = timeLabel + "0";
        }
        timeLabel = timeLabel + sec;
        return timeLabel;
    }

    Runnable timerunnable = new Runnable() {
        @Override
        public void run() {
            if (mPlayer != null) {
                int elapsedtime = mSeekBar.getProgress() * 1000;
                elapsedtimelabel.setText(createTime(elapsedtime));
                String remaining = createTime(mPlayer.getDuration());
                remaininglabel.setText(remaining);
            } else {
                elapsedtimelabel.setText("0:00");
                remaininglabel.setText("0:00");
            }
            mHandler.postDelayed(this, 1000);
        }
    };

    Runnable seekrunnable = new Runnable() {
        @Override
        public void run() {
            while (mPlayer != null && !isChanging) {
                int currentime = mPlayer.getCurrentPosition();
                mSeekBar.setProgress(currentime / 1000);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPlayer();
        mHandler.removeCallbacks(timerunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPlayer();
    }
}
