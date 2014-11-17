package cn.sharedream.game;

import java.io.IOException;

import org.cocos2dx.lib.Cocos2dxLuaJavaBridge;
import org.cocos2dx.lua.AppActivity;

import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Debug;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

public class VideoView extends SurfaceView implements SurfaceHolder.Callback,
		MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener,
		MediaPlayer.OnCompletionListener {
	private static final String TAG = "VideoView";

	private MediaPlayer mPlayer; // MediaPlayer对象
	private AssetFileDescriptor fd;
	int luaOnFinishCallback;
	private boolean surfaceCreated = false;

	private TextView skipButton = null;
	final private AppActivity appActivity ;

	public VideoView(AppActivity appActivity) {

		super(appActivity);
		this.appActivity = appActivity;
		Log.i(TAG, "new VideoView");
		final SurfaceHolder holder = getHolder();
		holder.addCallback(this);

		this.addSkipButton();
	}
	
	private void addSkipButton(){
		skipButton = new TextView(this.appActivity);
		skipButton.setText("跳过 >>");
		skipButton.setTextColor(Color.argb(180, 255, 255, 255));
		skipButton.setTextSize(20);

		skipButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onVideoFinish();
			}
		});
		
		((ViewGroup) this.appActivity.getWindow().getDecorView()).addView(
				skipButton);
	}

	public void setVideo(AssetFileDescriptor fd) {
		this.fd = fd;
		try {
			mPlayer = new MediaPlayer();
			mPlayer.setScreenOnWhilePlaying(true);

			mPlayer.setOnCompletionListener(this);
			mPlayer.setOnErrorListener(this);
			mPlayer.setOnInfoListener(this);
			mPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(),
					fd.getLength());
			mPlayer.prepare();
			posttion = 0;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void fixSzie(SurfaceHolder holder)
	{
		int wWidth = getWidth();
		int wHeight = getHeight();

		/* 获得视频宽长 */
		int vWidth = mPlayer.getVideoWidth();
		int vHeight = mPlayer.getVideoHeight();

		Log.i(TAG, "fixSzie:"+vWidth+","+vHeight);
		
		/* 最适屏幕 */
		float wRatio = (float) vWidth / (float) wWidth; // 宽度比
		float hRatio = (float) vHeight / (float) wHeight; // 高度比
		float ratio = Math.max(wRatio, hRatio); // 较大的比
		vWidth = (int) Math.ceil((float) vWidth / ratio); // 新视频宽度
		vHeight = (int) Math.ceil((float) vHeight / ratio); // 新视频高度

		// 改变SurfaceHolder大小
		holder.setFixedSize(vWidth, vHeight);
		
		if (skipButton != null) {
			
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
					skipButton.getWidth(), skipButton.getHeight());
			params.leftMargin = (int) (getWidth() * 0.8);
			params.topMargin = (int) (getHeight() * 0.86);
			skipButton.setLayoutParams(params);
			
			skipButton.bringToFront();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.i(TAG, "surfaceChanged:"+width+","+height);
		this.fixSzie(holder);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i(TAG, "surfaceCreated");
		try {

			this.fixSzie(holder);

			mPlayer.setDisplay(holder); // 指定SurfaceHolder
			mPlayer.seekTo(posttion);
			mPlayer.start();

		} catch (Exception e) {
			e.printStackTrace();
			onVideoFinish();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

		try {
			Log.i(TAG, "surfaceDestroyed");
			if (mPlayer != null) {
				posttion = mPlayer.getCurrentPosition();
				mPlayer.pause();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

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
		System.out.println("play error:" + what + "," + extra);
		// return false 会走到onCompletion处理
		return false;
	}

	int posttion;

	public void onVideoFinish() {
		Log.i(TAG, "onVideoFinish");
		try {
			if (mPlayer != null) {
				mPlayer.stop();
				mPlayer.release();
				mPlayer = null;
			}
			if (fd != null) {
				fd.close();
				fd = null;
			}
		} catch (Exception e) {
			Log.i(TAG, "onVideoFinish error");
			e.printStackTrace();
		}
		try {
			final AppActivity instance = this.appActivity;
			instance.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					try {

						ViewGroup group = (ViewGroup) instance.getWindow()
								.getDecorView();
						group.removeView(VideoView.this);

						if (skipButton != null) {
							group.removeView(skipButton);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			if (luaOnFinishCallback != 0) {
				instance.runOnGLThread(new Runnable() {
					@Override
					public void run() {
						try {
							Cocos2dxLuaJavaBridge.callLuaFunctionWithString(
									luaOnFinishCallback, "FINISH");
							Cocos2dxLuaJavaBridge
									.releaseLuaFunction(luaOnFinishCallback);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setLuaOnFinishCallback(int luaOnFinishCallback) {
		this.luaOnFinishCallback = luaOnFinishCallback;
	}

	public static void playVideo(final String name, final int luaCallback) {
		final AppActivity instance = AppActivity.instance;
		if (instance != null) {
			instance.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					final VideoView videoView = new VideoView(instance);
					videoView.setLuaOnFinishCallback(luaCallback);
					try {
						AssetFileDescriptor afd = instance.getAssets().openFd(
								name);
						videoView.setVideo(afd);
						final ViewGroup group = (ViewGroup) instance
								.getWindow().getDecorView();
						group.addView(videoView);
						videoView.setZOrderMediaOverlay(true);

					} catch (IOException e) {
						e.printStackTrace();
						videoView.onVideoFinish();
					}
				}
			});
		}
	}

	public native void doLuaFinishCallback(int luaCallback);
}
