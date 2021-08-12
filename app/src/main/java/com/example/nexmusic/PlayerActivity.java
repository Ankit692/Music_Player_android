package com.example.nexmusic;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import java.util.ArrayList;
import java.util.Random;


import static com.example.nexmusic.AlbumDetailsAdapter.albumFiles;
import static com.example.nexmusic.MainActivity.musicFiles;
import static com.example.nexmusic.MainActivity.repeatBoolean;
import static com.example.nexmusic.MainActivity.shuffleBoolean;
import static com.example.nexmusic.MusicAdapter.mFiles;

public class PlayerActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {
TextView song_name,artist_name,duration_played,duration_total;
ImageView cover_art,next_btn,prev_btn,shuffle_btn,repeat_btn;
FloatingActionButton playPauseBtn;
SeekBar seekbar;
int position=-1;
static ArrayList<MusicFiles> listSongs=new ArrayList<>();
static Uri uri;
static MediaPlayer mediaPlayer;
private Handler handler=new Handler();
private Thread playThread,prevThread,nextThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initViews();
        getIntentMethod();
        song_name.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());
        mediaPlayer.setOnCompletionListener(this);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mediaPlayer!=null && fromUser){
                    mediaPlayer.seekTo(progress*1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer!=null){
                    int currentPosition=mediaPlayer.getCurrentPosition()/1000;
                    seekbar.setProgress(currentPosition);
                    duration_played.setText(formattedTime(currentPosition));
                }
                handler.postDelayed(this,1000);
            }
        });
        shuffle_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shuffleBoolean){
                    shuffleBoolean=false;
                    shuffle_btn.setImageResource(R.drawable.suffle_off);
                }else{
                    shuffleBoolean=true;
                    shuffle_btn.setImageResource(R.drawable.shuffle_on);
                }
            }
        });
        repeat_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatBoolean){
                    repeatBoolean=false;
                    repeat_btn.setImageResource(R.drawable.repeat_off);
                }
                else{
                    repeatBoolean=true;
                    repeat_btn.setImageResource(R.drawable.repeat_on);
                }
            }
        });
    }

    @Override
    protected void onResume() {

        super.onResume();
        playThreadBtn();
        prevThreadBtn();
        nextThreadBtn();


    }

    private void playThreadBtn() {
        playThread=new Thread(){
            @Override
            public void run() {
                super.run();
                playPauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPauseBtnClicked();
                    }
                });
            }
        };
        playThread.start();
    }

    private void playPauseBtnClicked() {
        if(mediaPlayer.isPlaying()){
            playPauseBtn.setImageResource(R.drawable.play);
            mediaPlayer.pause();
            seekbar.setMax(mediaPlayer.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer!=null){
                        int currentPosition=mediaPlayer.getCurrentPosition()/1000;
                        seekbar.setProgress(currentPosition);

                    }

                    handler.postDelayed(this,1000);
                }
            });

        }else{
            playPauseBtn.setImageResource(R.drawable.pause);
            mediaPlayer.start();
            seekbar.setMax(mediaPlayer.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer!=null){
                        int currentPosition=mediaPlayer.getCurrentPosition()/1000;
                        seekbar.setProgress(currentPosition);

                    }

                    handler.postDelayed(this,1000);
                }
            });

        }
    }

    private void prevThreadBtn() {
        prevThread=new Thread(){
            @Override
            public void run() {
                super.run();
                prev_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prevBtnClicked();
                    }
                });
            }
        };
        prevThread.start();
    }

    private void prevBtnClicked() {
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
            if(shuffleBoolean &&!repeatBoolean){
                position=getRandom(listSongs.size()-1);
            }
            else if(!shuffleBoolean && !repeatBoolean){
                position=((position-1)<0?(listSongs.size()-1):(position-1));
            }

            uri=Uri.parse(listSongs.get(position).getPath());
            mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            seekbar.setMax(mediaPlayer.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer!=null){
                        int currentPosition=mediaPlayer.getCurrentPosition()/1000;
                        seekbar.setProgress(currentPosition);

                    }

                    handler.postDelayed(this,1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            playPauseBtn.setBackgroundResource(R.drawable.pause);
            mediaPlayer.start();
        }else{
            mediaPlayer.stop();
            mediaPlayer.release();
            if(shuffleBoolean &&!repeatBoolean){
                position=getRandom(listSongs.size()-1);
            }
            else if(!shuffleBoolean && !repeatBoolean){
                position=((position-1)<0?(listSongs.size()-1):(position-1));
            }

            uri=Uri.parse(listSongs.get(position).getPath());
            mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            seekbar.setMax(mediaPlayer.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer!=null){
                        int currentPosition=mediaPlayer.getCurrentPosition()/1000;
                        seekbar.setProgress(currentPosition);

                    }

                    handler.postDelayed(this,1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            playPauseBtn.setBackgroundResource(R.drawable.play);

        }
    }

    private void nextThreadBtn() {
        nextThread=new Thread(){
            @Override
            public void run() {
                super.run();
                next_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextBtnClicked();
                    }
                });
            }
        };
        nextThread.start();
    }

    private void nextBtnClicked() {
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
            if(shuffleBoolean &&!repeatBoolean){
                position=getRandom(listSongs.size()-1);
            }
            else if(!shuffleBoolean && !repeatBoolean){
                position=((position+1)%listSongs.size());
            }

            uri=Uri.parse(listSongs.get(position).getPath());
            mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            seekbar.setMax(mediaPlayer.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer!=null){
                        int currentPosition=mediaPlayer.getCurrentPosition()/1000;
                        seekbar.setProgress(currentPosition);

                    }

                    handler.postDelayed(this,1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            playPauseBtn.setBackgroundResource(R.drawable.pause);
            mediaPlayer.start();
        }else{
            mediaPlayer.stop();
            mediaPlayer.release();
            if(shuffleBoolean &&!repeatBoolean){
                position=getRandom(listSongs.size()-1);
            }
            else if(!shuffleBoolean && !repeatBoolean){
                position=((position+1)%listSongs.size());
            }
            uri=Uri.parse(listSongs.get(position).getPath());
            mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            seekbar.setMax(mediaPlayer.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer!=null){
                        int currentPosition=mediaPlayer.getCurrentPosition()/1000;
                        seekbar.setProgress(currentPosition);

                    }

                    handler.postDelayed(this,1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            playPauseBtn.setBackgroundResource(R.drawable.play);

        }
    }

    private int getRandom(int i) {
        Random random=new Random();
        return random.nextInt(i+1);
    }

    private String formattedTime(int currentPosition) {
        String totalOut="";
        String totalNew="";
        String seconds=String.valueOf(currentPosition % 60);
        String minutes=String.valueOf(currentPosition / 60);
        totalOut=minutes + ":" + seconds;
        totalNew=minutes + ":" + "0" + seconds;

        if(seconds.length()==1){
            return totalNew;

        }
        else{
            return totalOut;
        }
    }

    private void getIntentMethod() {
             position=getIntent().getIntExtra("position",-1);
             String sender=getIntent().getStringExtra("Sender");
             if(sender!=null && sender.equals("albumDetails")){
                 listSongs=albumFiles;
             }
             else {
                 listSongs = mFiles;
             }

            if(listSongs!=null){
              playPauseBtn.setImageResource(R.drawable.pause);
              uri= Uri.parse(listSongs.get(position).getPath());
            }
           if(mediaPlayer!=null){
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
                mediaPlayer.start();
            }
          else{
               mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
               mediaPlayer.start();
             }
           seekbar.setMax(mediaPlayer.getDuration()/1000);
          metaData(uri);
    }

    private void initViews() {
        song_name=findViewById(R.id.song_name);

        artist_name=findViewById(R.id.song_artist);
        duration_played=findViewById(R.id.durationPlayed);
        duration_total=findViewById(R.id.durationTotal);
        cover_art=findViewById(R.id.cover_art);
        next_btn=findViewById(R.id.id_next);
        prev_btn=findViewById(R.id.id_prev);
        shuffle_btn=findViewById(R.id.suffle);
        repeat_btn=findViewById(R.id.repeat);
        playPauseBtn=findViewById(R.id.play_pause);
        seekbar=findViewById(R.id.seekBar);
    }

    private void metaData(Uri uri){
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durationTotal=Integer.parseInt(listSongs.get(position).getDuration())/1000;
        duration_total.setText(formattedTime(durationTotal));
        byte[] art=retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if(art!=null){

            bitmap= BitmapFactory.decodeByteArray(art,0,art.length);
            ImageAnimation(this,cover_art,bitmap);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    Palette.Swatch swatch=palette.getDominantSwatch();
                    if(swatch!=null){
                        ImageView gradient=findViewById(R.id.imageViewGradient);
                        RelativeLayout mContainer=findViewById(R.id.mContainer);
                        gradient.setBackgroundResource(R.drawable.gradient_bg);
                        mContainer.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable=new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(),0x00000000});
                        gradient.setBackground(gradientDrawable);

                        GradientDrawable gradientDrawableBg=new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(),swatch.getRgb()});
                        mContainer.setBackground(gradientDrawableBg);
                        song_name.setTextColor(swatch.getTitleTextColor());
                        artist_name.setTextColor(swatch.getBodyTextColor());
                    }else{
                        ImageView gradient=findViewById(R.id.imageViewGradient);
                        RelativeLayout mContainer=findViewById(R.id.mContainer);
                        gradient.setBackgroundResource(R.drawable.gradient_bg);
                        mContainer.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable=new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000,0x00000000});
                        gradient.setBackground(gradientDrawable);

                        GradientDrawable gradientDrawableBg=new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000,0xff000000});
                        mContainer.setBackground(gradientDrawableBg);
                        song_name.setTextColor(Color.WHITE);
                        artist_name.setTextColor(Color.DKGRAY);
                    }
                }
            });
        }
        else{
            Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.dow)
                    .into(cover_art);


            ImageView gradient=findViewById(R.id.imageViewGradient);
            RelativeLayout mContainer=findViewById(R.id.mContainer);
            gradient.setBackgroundResource(R.drawable.gradient_bg);
            mContainer.setBackgroundResource(R.drawable.main_bg);
            song_name.setTextColor(Color.WHITE);
            artist_name.setTextColor(Color.DKGRAY);
        }
    }
    public void ImageAnimation(final Context context,final ImageView imageView,final Bitmap bitmap){
        Animation animOut= AnimationUtils.loadAnimation(context,android.R.anim.fade_out);
       final Animation animIn=AnimationUtils.loadAnimation(context,android.R.anim.fade_in);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                 Glide.with(context).load(bitmap).into(imageView);
                 animIn.setAnimationListener(new Animation.AnimationListener() {
                     @Override
                     public void onAnimationStart(Animation animation) {

                     }

                     @Override
                     public void onAnimationEnd(Animation animation) {

                     }

                     @Override
                     public void onAnimationRepeat(Animation animation) {

                     }
                 });
                 imageView.startAnimation(animIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animOut);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        nextBtnClicked();
        if(mediaPlayer!=null){
            mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(this);
        }
    }
}