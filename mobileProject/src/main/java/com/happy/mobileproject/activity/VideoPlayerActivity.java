package com.happy.mobileproject.activity;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.happy.mobileproject.R;
import com.happy.mobileproject.domain.MediaItem;
import com.happy.mobileproject.download.DownloadManagerPro;
import com.happy.mobileproject.utils.Utils;
import com.happy.mobileproject.view.VideoView;

import java.io.File;
import java.util.ArrayList;

/**
 * 视频播放器
 *
 * @author 阿福
 */
public class VideoPlayerActivity extends BaseActivity {

    /**
     * 进度更新消息
     */
    protected static final int PROGRESS = 0;

    /**
     * 延迟6秒隐藏控制面板的消息
     */
    protected static final int DELAYED_HIDE_MESSAGE = 1;
    /**
     * 关闭播放器的消息
     */

    private static final int FINISH = 2;

    /**
     * 视频为最大，全屏
     */
    protected static final int SCREEN_FULL = 0;
    /**
     * 视频为默认
     */
    protected static final int SCREEN_DEFAULT = 1;


    /**
     * 是否是全屏
     * true:全屏显示
     * false:默认显示
     */
    private boolean isScreenFull = false;


    private VideoView videoview;

    private TextView tv_videoname;
    private ImageView iv_battery;
    private TextView tv_systemtime;

    private Button btn_voice;
    private SeekBar seekBar_voice;
    private Button btn_switch_player;

    private TextView tv_video_current_time;
    private SeekBar seekBar_video;
    private TextView tv_video_duration;

    private Button btn_exit;
    private Button btn_pre;
    private Button btn_play_pause;
    private Button btn_next;
    private Button btn_full_default;


    private FrameLayout fl_control;
    private LinearLayout ll_loading;
    private LinearLayout ll_vido_buffering;

    /**
     * 是否播放状态
     * true:播放中
     * false:暂停中
     */
    private boolean isPlaying = false;
    private Utils utils;

    private String mTitle;

    /**
     * true:当前Activity已经销毁
     * false:Activity还存在
     */
    private boolean isStoped = false;
    /**
     * 监听电量的变化
     */
    private MyBroadcastReceiver receiver;

    /**
     * 当前电量变量；电量范围：0~100之间
     */
    private int level;
    /**
     * 视频列表
     */
    private ArrayList<MediaItem> videoItems;

    //1.定义手势识别器
    private GestureDetector detector;
    /**
     * 声音管理
     */
    private AudioManager am;

    /**
     * 是否是静音
     * true:静音
     * false：非静音
     */
    private boolean isMute = false;
    /**
     * 是否是缓存中
     * true:正在缓冲，该显示卡效果
     * false:缓存结束，不该显示卡效果
     */
    private boolean isBuffering = false;

    /**
     * true:网络资源
     * false:本地资源
     */
    private boolean isNewUrl = false;

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case PROGRESS://更新进度

                    //得到当前的播放进度-毫秒
                    int currentPosition = videoview.getCurrentPosition();
                    tv_video_current_time.setText(utils.stringForTime(currentPosition));

                    //seeKbar跟新进度
                    seekBar_video.setProgress(currentPosition);

                    //设置电量的状态
                    setBetteryStatus();

                    //显示系统时间
                    tv_systemtime.setText(utils.getSystemCurrentTime());

                    if (isNewUrl) {
                        //设置视频网络缓存进度
                        //0~100;
                        int bufferPercentage = videoview.getBufferPercentage();

                        int buffer = bufferPercentage * seekBar_video.getMax();

                        int secondaryProgress = buffer / 100;

                        seekBar_video.setSecondaryProgress(secondaryProgress);
                    } else {
                        seekBar_video.setSecondaryProgress(0);
                    }


                    //每一秒钟发一下-死循环
                    if (!isStoped) {
                        handler.sendEmptyMessageDelayed(PROGRESS, 1000);
                    }


                    break;
                case DELAYED_HIDE_MESSAGE:
                    //隐藏控制面板
                    hideControl();

                    break;
                case FINISH:
                    videoview.stopPlayback();
                    finish();

                default:
                    break;
            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//		Toast.makeText(getApplicationContext(), "当前系统播放器播放视频", 0).show();
        setTitleBar(View.GONE);
        initData();
        getDataFromLocal();

        System.out.println("视频播放地址是：" + uri);
        initView();
        setPlayData();
        setListener();
        //设置显示系统的控制面板
//		videoview.setMediaController(new MediaController(this));

        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadManagerPro = new DownloadManagerPro(downloadManager);

        downloadObserver = new DownloadChangeObserver();
        completeReceiver = new CompleteReceiver();
        getContentResolver().registerContentObserver(DownloadManagerPro.CONTENT_URI, true, downloadObserver);
        registerReceiver(completeReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }

    private void startDownLoad() {
        File folder = Environment.getExternalStoragePublicDirectory(DOWNLOAD_FOLDER_NAME);
        if (!folder.exists() || !folder.isDirectory()) {
            folder.mkdirs();
        }
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(APK_URL));
        request.setDestinationInExternalPublicDir(DOWNLOAD_FOLDER_NAME, DOWNLOAD_FILE_NAME);
        request.setTitle("");
        request.setDescription("");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setVisibleInDownloadsUi(false);
        request.setMimeType("application/com.happy.download.file");
        downloadId = downloadManager.enqueue(request);
    }

    private void setPlayData() {
        if (uri != null) {
            videoview.setVideoURI(uri);
            isNewUrl = utils.isNetUrl(uri.toString());
            tv_videoname.setText(mTitle);
//            tv_videoname.setText(uri.toString());
        } else {
            //从播放列表来的
            MediaItem item = videoItems.get(position);
            //设置视频的标题
            tv_videoname.setText(item.getTitle());
            //设置播放地址
            videoview.setVideoPath(item.getData());
            isNewUrl = utils.isNetUrl(item.getData());

        }
        seekBar_voice.setMax(maxVolume);
        seekBar_voice.setProgress(currentVolume);
        setNextOrPreButtonState();

    }

    private void getDataFromLocal() {
        //播放地址,通常是用来得到：浏览器和本地文件夹管理器，第三方应用
//        uri = getIntent().getData();
        String url = getIntent().getStringExtra("URI");
        mTitle = getIntent().getStringExtra("TITLE");
        if (!TextUtils.isEmpty(url)) {
            uri = Uri.parse(url);
        }
//        uri = Uri.parse("http://video.weibo.com/show?fid=1034:5847cb932ebe04daefd5dfe73d740997");
        //得到传入的播放列表和位置
        videoItems = (ArrayList<MediaItem>) getIntent().getSerializableExtra("videolist");
        position = getIntent().getIntExtra("position", 0);
    }

    /**
     * 设置电量的状态
     */
    protected void setBetteryStatus() {
        if (level <= 0) {
            //没有电了
            iv_battery.setImageResource(R.drawable.ic_battery_0);
        } else if (level > 0 && level <= 10) {
            //10
            iv_battery.setImageResource(R.drawable.ic_battery_10);
        } else if (level > 10 && level <= 20) {
            //20
            iv_battery.setImageResource(R.drawable.ic_battery_20);
        } else if (level > 20 && level <= 40) {
            //40
            iv_battery.setImageResource(R.drawable.ic_battery_40);
        } else if (level > 40 && level <= 60) {
            //60
            iv_battery.setImageResource(R.drawable.ic_battery_60);
        } else if (level > 60 && level <= 80) {
            //80
            iv_battery.setImageResource(R.drawable.ic_battery_80);
        } else if (level > 80 && level <= 100) {
            //100
            iv_battery.setImageResource(R.drawable.ic_battery_100);
        }
    }

    /**
     * 是否显示控件面板默认是隐藏
     * true:显示
     * false隐藏
     */
    private boolean isShowControl = false;


    /**
     * 初始化数据
     */
    private void initData() {
        utils = new Utils();
        isPlaying = false;
        //监听电量变化
        receiver = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);//监听电量的动作
        registerReceiver(receiver, filter);

        //设置当前的手机不锁屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //得到屏幕的宽和高
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeight = wm.getDefaultDisplay().getHeight();


        //实例化声音管理
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);

        maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);


        //2.实例化手势识别器
        detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public void onLongPress(MotionEvent e) {
//				Toast.makeText(getApplicationContext(), " 哥，你按痛我了-长按了", 1).show();
                playAndPause();
                super.onLongPress(e);
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
//				Toast.makeText(getApplicationContext(), " 哥，你双击我", 1).show();
                if (isScreenFull) {
                    setVideoType(SCREEN_DEFAULT);
                } else {
                    setVideoType(SCREEN_FULL);
                }
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
//				Toast.makeText(getApplicationContext(), " 哥，单击我了", 1).show();
                if (isShowControl) {
                    //隐藏
                    hideControl();
                    removeHideMessage();
                } else {
                    //显示
                    showControl();

                    //6秒后隐藏
                    sendHideMessage();

                }
                return super.onSingleTapConfirmed(e);
            }

        });
    }

    /**
     * 显示控制面板
     */
    protected void showControl() {
        fl_control.setVisibility(View.VISIBLE);
        isShowControl = true;

    }

    /**
     * 隐藏控制面板
     */
    protected void hideControl() {
        fl_control.setVisibility(View.GONE);
        isShowControl = false;

    }

    /**
     * 移除隐藏控制面板的消息
     */
    private void removeHideMessage() {
        handler.removeMessages(DELAYED_HIDE_MESSAGE);
    }

    /**
     * 移除隐藏控制面板的消息
     */
    private void sendHideMessage() {
        handler.sendEmptyMessageDelayed(DELAYED_HIDE_MESSAGE, 6000);
    }

    class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            level = intent.getIntExtra("level", 0);

        }

    }

    private float startY = 0;

    private int mVol;
    /**
     * 在屏幕上滑动的区域
     */
    private int audioTouchRang;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        super.onTouchEvent(event);//执行父类的方法
        //3.把事件传入给手势识别器
        detector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN://按下
                //1.记录坐标
                startY = event.getRawY();
                mVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                audioTouchRang = Math.min(screenHeight, screenWidth);
                removeHideMessage();

                break;
            case MotionEvent.ACTION_MOVE://滑动
                //2.来到新的坐标
                float endY = event.getRawY();
                //3.计算偏移量
                float distanceY = startY - endY;
                //4.滑动距离和声音变化的关系
                float deta = distanceY / audioTouchRang;

                //5.改变的音量
                float volume = deta * maxVolume;
                //6.屏蔽非法值

                int realVolume = (int) Math.min(Math.max(volume + mVol, 0), maxVolume);

                if (deta != 0) {
                    //改变声音的大小
                    updateVolume(realVolume);
                }


                break;
            case MotionEvent.ACTION_UP://离开
                startY = event.getRawY();
                sendHideMessage();
                break;

            default:
                break;
        }
        return true;
    }

    /**
     * 改变音量
     *
     * @param volume:0~15
     */
    private void updateVolume(int volume) {

        if (isMute) {
            //设置为静音
            am.setStreamVolume(am.STREAM_MUSIC, 0, 0);
            seekBar_voice.setProgress(0);
        } else {
            am.setStreamVolume(am.STREAM_MUSIC, volume, 0);
            seekBar_voice.setProgress(volume);
        }


        currentVolume = volume;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isStoped = true;
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        getContentResolver().unregisterContentObserver(downloadObserver);
        unregisterReceiver(completeReceiver);
    }

    private void setListener() {
        //设置播放和暂停按钮的点击事件
        btn_play_pause.setOnClickListener(mOnClickListener);
        btn_next.setOnClickListener(mOnClickListener);
        btn_pre.setOnClickListener(mOnClickListener);
        btn_full_default.setOnClickListener(mOnClickListener);
        btn_voice.setOnClickListener(mOnClickListener);
        btn_exit.setOnClickListener(mOnClickListener);
        btn_switch_player.setOnClickListener(mOnClickListener);


        seekBar_video.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            //当手指离开当前控件的时候
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendHideMessage();
            }

            //当手指一触摸的时候执行
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                removeHideMessage();
            }

            //当状态发送变化的时候执行
            //我们要拖拽到的进度progress
            //fromUser:默认是为false,用户按下true
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (fromUser) {
                    videoview.seekTo(progress);
                }
            }
        });

        //设置声音改变的监听
        seekBar_voice.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendHideMessage();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                removeHideMessage();
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (fromUser) {
                    updateVolume(progress);
                }

            }
        });
        //设置准备监听
        videoview.setOnPreparedListener(new OnPreparedListener() {

            //当视频可以播放的时候
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoview.start();//视频播放
                ll_loading.setVisibility(View.GONE);
                isPlaying = true;
                isStoped = false;
                //得到视频的总时长-毫秒
                int duration = videoview.getDuration();
                tv_video_duration.setText(utils.stringForTime(duration));

                //设置seeKBar的总的等分,和总时长关联起来
                seekBar_video.setMax(duration);

                //隐藏控制面板
                hideControl();

                setVideoType(SCREEN_DEFAULT);

                handler.sendEmptyMessage(PROGRESS);


            }
        });

        //设置监听播放完成,当一个视频播放完成后，会回调这个接口
        videoview.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                setPlayNext();

            }
        });

        //设置监听错误，当播放视频出错的时候回调这个方法
        videoview.setOnErrorListener(new OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                // TODO Auto-generated method stub
//				Toast.makeText(getApplicationContext(), "视频播放出错了..", 1).show();
                //1.播放的格式不支持的时候，会出错。--集成万能播放器
                startVitamioPlayer();
                //2.文件下载不完整，中间有空白。-播放器没法解决
                //3.播放网络视频，突然没有网络了-段段续续-重新播放器-重新播放三次-出错了再弹出对话框

                return true;
            }
        });

        //设置监听卡
        videoview.setOnInfoListener(new OnInfoListener() {

            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {

                switch (what) {
                    //当视频播放卡的时候和视频拖拽的时候回调这个what
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                        ll_vido_buffering.setVisibility(View.VISIBLE);
//					Toast.makeText(getApplicationContext(), "视频卡了或者拖拽了", 0).show();
                        isBuffering = true;
                        break;
                    //当视频播放卡结束的时候和视频拖拽结束的时候回调这个what
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        ll_vido_buffering.setVisibility(View.GONE);
                        isBuffering = false;
//					Toast.makeText(getApplicationContext(), "视频卡结束了或者拖拽结束了", 0).show();
                        break;

                    default:
                        break;
                }


                return true;
            }
        });


        //设置监听拖拽缓存完成监听
        videoview.setOnSeekCompleteListener(new OnSeekCompleteListener() {

            @Override
            public void onSeekComplete(MediaPlayer mp) {

                if (isBuffering) {
                    ll_vido_buffering.setVisibility(View.GONE);
                }

            }
        });


    }

    /**
     * 跳转到万能播放器里面去
     */
    protected void startVitamioPlayer() {
        //当点击的时候传播放列表和当前点击的位置
        Intent intent = new Intent(this, VitamioPlayerActivity.class);
        if (uri != null) {
            intent.setData(uri);
        } else {
            //传入播放列表的初级
            Bundle bundle = new Bundle();
            bundle.putSerializable("videolist", videoItems);//视频列表
            intent.putExtras(bundle);
            intent.putExtra("position", position);
        }

        startActivity(intent);
        handler.sendEmptyMessageDelayed(FINISH, 2000);

    }

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.btn_play_pause://播放和暂停
                    playAndPause();
                    break;
                case R.id.btn_next://下一个视频
                    setPlayNext();
                    break;
                case R.id.btn_pre://上一个视频
                    setPlayPre();
                    break;
                case R.id.btn_full_default://视频大小的切换

                    if (isScreenFull) {
                        setVideoType(SCREEN_DEFAULT);
                    } else {
                        setVideoType(SCREEN_FULL);
                    }
                    break;
                case R.id.btn_voice:

                    isMute = !isMute;
                    updateVolume(currentVolume);

                    break;
                case R.id.btn_exit:
                    finish();
                    break;
                case R.id.btn_switch_player://切换播放器
                    //当前是系统播放器—要切换到万能播放器


                    new AlertDialog.Builder(VideoPlayerActivity.this)
                            .setTitle("提示")
                            .setMessage("当前是系统播放器播放视频，是否要切换到万能播放器播放")
                            .setNegativeButton("取消", null)
                            .setPositiveButton("切换",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                /* If we get here, there is no onError listener, so
                                 * at least inform them that the video is over.
                                 */
//                               finish();
                                            startVitamioPlayer();
                                        }
                                    })

                            .setCancelable(false)
                            .show();
                    break;
                default:
                    break;
            }

            removeHideMessage();
            sendHideMessage();

        }
    };

    /**
     * 播放地址
     */
    private Uri uri;

    /**
     * 视频列表中的位置
     */
    private int position;

    private int screenWidth;

    private int screenHeight;

    /**
     * 当前手机的音量
     */
    private int currentVolume;

    /**
     * 当前手机的最大音量值：0~15之间
     */
    private int maxVolume;

    /**
     * 初始化View
     */
    private void initView() {
        videoview = (VideoView) findViewById(R.id.videoview);
        tv_videoname = (TextView) findViewById(R.id.tv_videoname);
        iv_battery = (ImageView) findViewById(R.id.iv_battery);
        tv_systemtime = (TextView) findViewById(R.id.tv_systemtime);

        btn_voice = (Button) findViewById(R.id.btn_voice);
        seekBar_voice = (SeekBar) findViewById(R.id.seekBar_voice);
        btn_switch_player = (Button) findViewById(R.id.btn_switch_player);

        tv_video_current_time = (TextView) findViewById(R.id.tv_video_current_time);
        seekBar_video = (SeekBar) findViewById(R.id.seekBar_video);
        tv_video_duration = (TextView) findViewById(R.id.tv_video_duration);

        btn_exit = (Button) findViewById(R.id.btn_exit);
        btn_pre = (Button) findViewById(R.id.btn_pre);
        btn_play_pause = (Button) findViewById(R.id.btn_play_pause);
        btn_next = (Button) findViewById(R.id.btn_next);
        btn_full_default = (Button) findViewById(R.id.btn_full_default);

        fl_control = (FrameLayout) findViewById(R.id.fl_control);
        ll_loading = (LinearLayout) findViewById(R.id.ll_loading);

        ll_vido_buffering = (LinearLayout) findViewById(R.id.ll_vido_buffering);
    }

    /**
     * 设置视频的模式：全屏和默认
     *
     * @param screenDefault
     */
    protected void setVideoType(int screenDefault) {
        switch (screenDefault) {
            case SCREEN_FULL://全屏显示
                int videowidth = screenWidth;
                int videoheight = screenHeight;
                videoview.setVideoSize(videowidth, videoheight);
                isScreenFull = true;
                //把标题栏隐藏
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

                btn_full_default.setBackgroundResource(R.drawable.screen_switch_defualt_btn_bg);

                break;
            case SCREEN_DEFAULT://默认显示
                //当前视频默认拥有的宽的和高度
                int mVideoWidth = videoview.getVideoWidth();
                int mVideoHeight = videoview.getVideoHeight();

                //结合屏幕计算什么样的大小合适显示在当前屏幕上-默认
                int width = screenWidth;
                int height = screenHeight;

                if (mVideoWidth > 0 && mVideoHeight > 0) {
                    if (mVideoWidth * height > width * mVideoHeight) {
                        //Log.i("@@@", "image too tall, correcting");
                        height = width * mVideoHeight / mVideoWidth;
                    } else if (mVideoWidth * height < width * mVideoHeight) {
                        //Log.i("@@@", "image too wide, correcting");
                        width = height * mVideoWidth / mVideoHeight;
                    } else {
                        //Log.i("@@@", "aspect ratio is correct: " +
                        //width+"/"+height+"="+
                        //mVideoWidth+"/"+mVideoHeight);
                    }
                }

                videoview.setVideoSize(width, height);
                isScreenFull = false;
                btn_full_default.setBackgroundResource(R.drawable.screen_switch_full_btn_bg);
                //把标题栏隐藏
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

                break;

            default:
                break;
        }

    }

    /**
     * 播放下一个视频
     */
    protected void setPlayPre() {
        ll_loading.setVisibility(View.VISIBLE);
        if (videoItems != null) {
            if (videoItems.size() > 0) {

                position--;//下一个视频
                if (position >= 0) {
                    MediaItem item = videoItems.get(position);
                    tv_videoname.setText(item.getTitle());
                    videoview.setVideoPath(item.getData());
                    isNewUrl = utils.isNetUrl(item.getData());
                    setNextOrPreButtonState();

                } else {

                    Toast.makeText(VideoPlayerActivity.this, "已经是第一个视频了", Toast.LENGTH_SHORT).show();

                }


            }
        } else if (uri != null) {
            finish();
            Toast.makeText(VideoPlayerActivity.this, "已经是第一个视频了", Toast.LENGTH_SHORT).show();
        }


    }

    /**
     * 播放下一个视频
     */
    protected void setPlayNext() {
        if (videoItems != null) {
            if (videoItems.size() > 0) {

                position++;//下一个视频
                if (position <= videoItems.size() - 1) {
                    MediaItem item = videoItems.get(position);
                    tv_videoname.setText(item.getTitle());
                    videoview.setVideoPath(item.getData());
                    isNewUrl = utils.isNetUrl(item.getData());
                    setNextOrPreButtonState();

                } else {

                    Toast.makeText(VideoPlayerActivity.this, "已经是最后一个视频", Toast.LENGTH_SHORT).show();
                    finish();//退出播放器

                }

            }
        } else if (uri != null) {
            finish();
            Toast.makeText(VideoPlayerActivity.this, "已经是最后一个视频", Toast.LENGTH_SHORT).show();
        }


    }

    private void setNextOrPreButtonState() {

        if (uri != null) {
            //从文件夹发起的播放
            btn_pre.setEnabled(false);
            btn_pre.setBackgroundResource(R.drawable.btn_pre_gray);
            btn_next.setEnabled(false);
            btn_next.setBackgroundResource(R.drawable.btn_next_gray);
        } else {
            //本地的播放列表发起的播放
            if (position == 0) {
                //当前是第一个视频，上一个视频按钮不可用点击
                btn_pre.setEnabled(false);
                btn_pre.setBackgroundResource(R.drawable.btn_pre_gray);
            } else if (position == videoItems.size() - 1) {
                //当前是最后一个视频，下一个视频按钮不可用点击
                btn_next.setEnabled(false);
                btn_next.setBackgroundResource(R.drawable.btn_next_gray);
            } else {
                btn_pre.setEnabled(true);
                btn_pre.setBackgroundResource(R.drawable.pre_btn_bg);
                btn_next.setEnabled(true);
                btn_next.setBackgroundResource(R.drawable.next_btn_bg);

            }
        }

    }

    @Override
    public void rightButtonClick() {

    }

    @Override
    public void leftButtonClick() {

    }

    @Override
    public View setContentView() {
        return View.inflate(this, R.layout.activity_videoplayer, null);
    }

    /**
     * 播放和暂停
     */
    private void playAndPause() {
        if (isPlaying) {

            //暂停
            isPlaying = false;
            videoview.pause();
            //设置按钮的状态-播放
            btn_play_pause.setBackgroundResource(R.drawable.play_btn_bg);
        } else {
            //播放
            isPlaying = true;
            videoview.start();
            //设置按钮的状态-暂停
            btn_play_pause.setBackgroundResource(R.drawable.pause_btn_bg);
        }
    }

    public static final String DOWNLOAD_FOLDER_NAME = "fastloan";
    public static final String DOWNLOAD_FILE_NAME = "jisudai.apk";

    public static final String APK_URL = "http://img.meilishuo.net/css/images/AndroidShare/Meilishuo_3.6.1_10006.apk";
    public static final String KEY_NAME_DOWNLOAD_ID = "downloadId";

    private DownloadManager downloadManager;
    private DownloadManagerPro downloadManagerPro;
    private long downloadId = 0;

    private DownloadChangeObserver downloadObserver;
    private CompleteReceiver completeReceiver;

    private RefreshHandler mHandler;

    static class RefreshHandler extends Handler {

        VideoPlayerActivity mParent;

        public RefreshHandler(VideoPlayerActivity parent) {
            mParent = parent;
        }

    }

    class DownloadChangeObserver extends ContentObserver {

        public DownloadChangeObserver() {
            super(mHandler);
        }

        @Override
        public void onChange(boolean selfChange) {
        }

    }

    class CompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (completeDownloadId == downloadId) {
                if (downloadManagerPro.getStatusById(downloadId) == DownloadManager.STATUS_SUCCESSFUL) {
                    Toast.makeText(getApplication(), "下载完成", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


}
