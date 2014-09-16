package cn.sharedream.game;

import java.io.IOException;

import org.cocos2dx.lib.Cocos2dxLuaJavaBridge;
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

public class VideoView extends SurfaceView implements SurfaceHolder.Callback,
		View.OnTouchListener,
		MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener,
		MediaPlayer.OnCompletionListener {
	private static final String TAG = "VideoView";

	private MediaPlayer mPlayer; // MediaPlayer对象
	private AssetFileDescriptor fd;
	int luaOnFinishCallback;
	private static long videoStartTime = 0;
	private static long videoFinishLimit = 5 *1000;
	private boolean surfaceCreated = false;

	public VideoView(Activity context) {
		
		super(context);
		Log.i(TAG, "new VideoView");

		final SurfaceHolder holder = getHolder();
		holder.addCallback(this); // 设置回调接口
		setOnTouchListener(this);

		
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

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(final SurfaceHolder holder) {
		Log.i(TAG, "surfaceCreated");
		try {
			
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
			holder.setFixedSize(vWidth, vHeight);
			
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
			if (mPlayer != null){
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

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			long now =System.currentTimeMillis();
			System.out.println("key down: nowSecns=" + now + ",limitSecns"
					+ (videoStartTime + videoFinishLimit));
			if (now > (videoStartTime + videoFinishLimit)) {
				onVideoFinish();
			}
		}
		return true;
	}

	int posttion;

	public void onVideoFinish() {
		Log.i(TAG, "onVideoFinish");
		try {
			if (mPlayer != null){
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
			final AppActivity instance = AppActivity.instance;
			instance.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					try{
						ViewGroup group = (ViewGroup) instance.getWindow()
								.getDecorView();
						group.removeView(VideoView.this);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			});
			
			if (luaOnFinishCallback != 0) {
				instance.runOnGLThread(new Runnable() {
					@Override
					public void run() {
						try{
							Cocos2dxLuaJavaBridge.callLuaFunctionWithString(
									luaOnFinishCallback, "FINISH");
							Cocos2dxLuaJavaBridge
									.releaseLuaFunction(luaOnFinishCallback);
						}catch(Exception e){
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
		videoStartTime = System.currentTimeMillis();
		if (instance != null) {
			instance.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					VideoView videoView = new VideoView(instance);
					videoView.setLuaOnFinishCallback(luaCallback);
					try {
						AssetFileDescriptor afd = instance.getAssets().openFd(
								name);
						videoView.setVideo(afd);
						ViewGroup group = (ViewGroup) instance.getWindow()
								.getDecorView();
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
