package cn.sharedream.game;

import java.io.IOException;

import org.cocos2dx.lua.AppActivity;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

/**
 * 
 * @author Yichou
 *
 * create data:2013-4-22 22:19:49
 */
public class VideoView extends SurfaceView implements 
			SurfaceHolder.Callback, 
			View.OnTouchListener, 
			MediaPlayer.OnPreparedListener, 
			MediaPlayer.OnErrorListener, 
			MediaPlayer.OnInfoListener,
			MediaPlayer.OnCompletionListener {
	private static final String TAG = "VideoView";
	
	private MediaPlayer mPlayer; // MediaPlayer对象
	private Activity gameActivity;
	private AssetFileDescriptor fd;
	private boolean surfaceCreated;
	ViewGroup group;
	int luaOnFinishCallback;
	

	public VideoView(Activity context) {
		super(context);

		this.gameActivity = context;
		group = (ViewGroup)context.getWindow().getDecorView();

		final SurfaceHolder holder = getHolder();
		holder.addCallback(this); // 设置回调接口
		setOnTouchListener(this);

		mPlayer = new MediaPlayer();
		mPlayer.setScreenOnWhilePlaying(true);

		mPlayer.setOnPreparedListener(this);
		mPlayer.setOnCompletionListener(this);
		mPlayer.setOnErrorListener(this);
		mPlayer.setOnInfoListener(this);
	}
	
	public void setVideo(AssetFileDescriptor fd) {
		this.fd = fd;
		try {
			mPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceCreated(final SurfaceHolder holder) {
		Log.i(TAG, "surfaceCreated");

		surfaceCreated = true;

		mPlayer.setDisplay(holder); // 指定SurfaceHolder
		try {
			mPlayer.prepare();
		} catch (Exception e1) {
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(TAG, "surfaceDestroyed");
		surfaceCreated = false;
		
		if(mPlayer != null){
			mPlayer.stop();
			mPlayer.reset();
		}
	}

	@Override
	public void onPrepared(MediaPlayer player) {
		Log.i(TAG, "onPrepared");

		int wWidth = getWidth();
		int wHeight = getHeight();

		/* 获得视频宽长 */
		int vWidth = mPlayer.getVideoWidth();
		int vHeight = mPlayer.getVideoHeight();

		/* 最适屏幕 */
		float wRatio = (float) vWidth / (float) wWidth; // 宽度比
		float hRatio = (float) vHeight / (float) wHeight; // 高度比
		float ratio = Math.max(wRatio, hRatio); // 较大的比
		vWidth = (int) Math.ceil((float) vWidth / ratio); // 新视频宽度
		vHeight = (int) Math.ceil((float) vHeight / ratio); // 新视频高度

		// 改变SurfaceHolder大小
		getHolder().setFixedSize(vWidth, vHeight);
		mPlayer.seekTo(posttion);
		mPlayer.start();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.i(TAG, "onCompletion");

		onVideoFinish();
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		return true;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		return true;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			//onVideoFinish();
		}
		return true;
	}
	
	int posttion;
	public void pause() {
		posttion = mPlayer.getCurrentPosition();
		mPlayer.pause();
	}
	
	/**
	 * 暂停的时候，系统会销毁 SurfaceView ，所以在resume的时候相对于重新设置MediaPlayer
	 */
	public void resume() {
		if(surfaceCreated){
			mPlayer.seekTo(posttion);
			mPlayer.start();
		}else {
			try {
				if (fd != null) {
					mPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
				}
			} catch (Exception e) {
			}
		}
	}
	
	public void onVideoFinish()
	{
		if(mPlayer == null)
			return;
		
		mPlayer.stop();
		mPlayer.release();
		mPlayer = null;
		if (fd != null) {
			try {
				fd.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			fd = null;
		}
		
		ViewGroup group = (ViewGroup)gameActivity.getWindow().getDecorView();
		group.removeView(this);
		
		if(luaOnFinishCallback != 0)
			doLuaFinishCallback(luaOnFinishCallback);
			
	}
	
	public void setLuaOnFinishCallback(int luaOnFinishCallback) {
		this.luaOnFinishCallback = luaOnFinishCallback;
	}

	public static void playVideo(final String name,final int luaCallback) {
		final AppActivity instance = AppActivity.instance;
		if (instance != null) {
			instance.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					VideoView videoView = new VideoView(instance);
					videoView.setLuaOnFinishCallback(luaCallback);
					try {
						AssetFileDescriptor afd = instance.getAssets().openFd(name);
						videoView.setVideo(afd);
						ViewGroup group = (ViewGroup)instance.getWindow().getDecorView();
						group.addView(videoView);
						videoView.setZOrderMediaOverlay(true);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
	
	public native void doLuaFinishCallback(int luaCallback);
}
