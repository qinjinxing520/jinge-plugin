package com.caih.kinggrid_lib.view.dialog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.caih.kinggrid_lib.KinggridActivity;
import com.ebensz.eink.R;
import com.kinggrid.iapppdf.Annotation;
import com.kinggrid.iapppdf.PlaySound;
import com.kinggrid.iapppdf.RecordSound;
import com.kinggrid.iapppdf.ui.viewer.IAppPDFView;
import com.kinggrid.iapppdf.util.KinggridConstant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

//import com.umeng.analytics.MobclickAgent;

public class SoundAnnotDialog implements KinggridConstant, OnClickListener {
	/** Called when the activity is first created. */

	private View view;
	private boolean isShort = false;
	private RecordSound mSoundMeter;
	private long startVoiceT, endVoiceT;
	private String currFilePath = "";
	private String fileName = "";

	public Context context;
	protected Annotation annotOld;

	// E人E本定制需求
	private View recordView, playView, alertView, timeView;
	private ImageView volume;
	private ImageButton mBtnRecord, mBtnStop, mBtnPlay, mBtnPause, mBtnDelete,
			mBtnClose,mBtnShutDown;
	private TextView timeH, timeM, timeS, playTime, totalTimeView;
	private ProgressBar progressBar;
	protected int i = 0, j = 0, k = 0;
	private ShowTime showTime;
	protected int totalTime = 0;
	protected int progress = 0;

	protected OnSaveAnnotListener saveAnnotListener = null;
	protected OnCloseAnnotListener closeAnnotListener = null;
	protected OnDeleteAnnotListener deleteAnnotListener = null;
	private PopupWindow popupWindow, alertWindow;
	protected PlaySound playSound;
	protected MediaPlayer mediaPlayer;

	private boolean isPlay = false;
	protected boolean isRecord = false;
	protected boolean isPause = false;
	private boolean isSaved = false;
	
	private static final int POLL_INTERVAL = 100;
	private String timePeriod = "";
	private IAppPDFView mPDFView;

	/**
	 * 构造函数
	 *
	 * @param width
	 * @param height
	 * @param annotation
	 */
	public SoundAnnotDialog(final Context context, final int width,
                            final int height, final Annotation annotation, IAppPDFView pdfView) {
		this.context = context;
		this.annotOld = annotation;
		this.mPDFView = pdfView;
		initView();
		show(20,20);
	}

	// 初始化E人E本定制界面
	private void initView() {
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnCompletionListener(new completelistener());
		mSoundMeter = new RecordSound(mHandler);
		playSound = new PlaySound(mediaPlayer);
		showTime = new ShowTime();

		view = LayoutInflater.from(context).inflate(R.layout.eben_voice_record,
				null);
		recordView = view.findViewById(R.id.record_layout);
		playView = view.findViewById(R.id.play_layout);
		alertView = view.findViewById(R.id.alert_layout);
		timeView = view.findViewById(R.id.time_layout);
		mBtnRecord = (ImageButton) view.findViewById(R.id.btn_record);
		mBtnStop = (ImageButton) view.findViewById(R.id.btn_stop);
		mBtnPlay = (ImageButton) view.findViewById(R.id.btn_play);
		mBtnPause = (ImageButton) view.findViewById(R.id.btn_pause);
		timeH = (TextView) view.findViewById(R.id.rcd_time_h);
		timeM = (TextView) view.findViewById(R.id.rcd_time_m);
		timeS = (TextView) view.findViewById(R.id.rcd_time_s);
		playTime = (TextView) view.findViewById(R.id.play_time);
		totalTimeView = (TextView) view.findViewById(R.id.play_time_total);
		volume = (ImageView) view.findViewById(R.id.sound_volume);
		mBtnDelete = (ImageButton) view.findViewById(R.id.btn_delete);
		mBtnClose = (ImageButton) view.findViewById(R.id.btn_close_annot);
		mBtnShutDown = (ImageButton) view.findViewById(R.id.btn_shutdown);
		progressBar = (ProgressBar) view.findViewById(R.id.sound_progress);

		mBtnRecord.setOnClickListener(this);
		mBtnStop.setOnClickListener(this);
		mBtnPlay.setOnClickListener(this);
		mBtnPause.setOnClickListener(this);
		mBtnDelete.setOnClickListener(this);
		mBtnClose.setOnClickListener(this);
		mBtnShutDown.setOnClickListener(this);
		
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getResources().getDisplayMetrics();
		popupWindow = new PopupWindow(view, (int) (dm.widthPixels / 1.28),
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		System.out.println("====sound dialog w&h:( " + popupWindow.getWidth() + ", " + popupWindow.getHeight() + " )");
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(false);
		popupWindow.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss() {
				if(isRecord){
					mSoundMeter.stopRecord();
				}
				closePopupWindow();
			}
		});
	}

	private void showAlert(View parent, boolean playFlag) {
		View alertView = LayoutInflater.from(context).inflate(
				R.layout.eben_alert_layout, null);
		TextView alertMsg = (TextView) alertView.findViewById(R.id.alert_text);
		if (playFlag) {
			alertMsg.setText("正在读取，请稍候...");
		} else {
			alertMsg.setText("正在嵌入，请稍候...");
		}
		alertWindow = new PopupWindow(alertView, (int)(594 * KinggridActivity.densityCoefficient), (int)(214 * KinggridActivity.densityCoefficient));
//				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		alertWindow.setFocusable(true);
		alertWindow.setOutsideTouchable(false);

		alertWindow.showAtLocation(parent, Gravity.CENTER_HORIZONTAL, 0, (int) (-75 * KinggridActivity.densityCoefficient));
		alertWindow.update();
	}

	private Runnable mSleepTask = new Runnable() {
		@Override
		public void run() {
			stop();
		}
	};
	private Runnable mPollTask = new Runnable() {
		@Override
		public void run() {
			double amp = mSoundMeter.getAmplitudeEMA();
			updateDisplayEBEN(amp);
			mHandler.postDelayed(mPollTask, POLL_INTERVAL);
		}
	};

	private void start(String name) {
		isRecord = true;
		i = 0;
		j = 0;
		k = 0;
		timeH.setText("00");
		timeM.setText("00");
		timeS.setText("00");
		showTime.Timestart();
		mSoundMeter.startRecord(currFilePath);
		mHandler.postDelayed(mPollTask, POLL_INTERVAL);
	}

	private void stop() {
		mHandler.removeCallbacks(mSleepTask);
		mHandler.removeCallbacks(mPollTask);
		mSoundMeter.stopRecord();
		showTime.Timestop();
		showTime.clearTime();
		volume.setImageResource(R.drawable.eben_bg_voice_0);
	}

	private void playSound() {
		if (isPlay) {
			return;
		}

		if (isPause) {
			isPlay = true;
			isPause = false;
			playSound.continuePlay();
			showTime.Timestart();
			return;
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				boolean play_flag = playSound.play(currFilePath);
				if (play_flag) {
					totalTime = playSound.getTotalTime();
					Log.e("wmm", "totalTime==" + totalTime);
					Message msg1 = new Message();
					msg1.what = 3;
					mHandler.sendMessage(msg1);
				}
			}
		}).start();

		isPlay = true;
		isPause = false;
		showTime.Timestart();
	}

	private void pauseSound() {
		isPlay = false;
		isPause = true;
		showTime.Timestop();
		playSound.pause();
	}

	private void updateDisplayEBEN(double signalEMA) {
		switch ((int) signalEMA) {
		case 0:
		case 1:
			volume.setImageResource(R.drawable.eben_bg_voice_1);
			break;
		case 2:
		case 3:
			volume.setImageResource(R.drawable.eben_bg_voice_2);
			break;
		case 4:
		case 5:
			volume.setImageResource(R.drawable.eben_bg_voice_3);
			break;
		case 6:
		case 7:
			volume.setImageResource(R.drawable.eben_bg_voice_4);
			break;
		case 8:
		case 9:
			volume.setImageResource(R.drawable.eben_bg_voice_5);
			break;
		case 10:
		case 11:
			volume.setImageResource(R.drawable.eben_bg_voice_6);
			break;
		default:
			volume.setImageResource(R.drawable.eben_bg_voice_7);
			break;
		}
	}

	/**
	 * 显示语音批注窗口
	 */
	public void show(final int offsetX, final int offsetY) {

			if (!annotOld.getUnType().equals("")) {
				progress = 0;
				currFilePath = FILEDIR_PATH + "/tempsounds/"
						+ annotOld.getUnType();
				new Thread(new Runnable() {
					@Override
					public void run() {
						File file = new File(currFilePath);
						if (!file.exists()) {
							long sample = annotOld.getSoundBitspersample();
							long channel = annotOld.getSoundChannels();
							long rate = annotOld.getSoundRate();
							byte[] rawData = annotOld.getSoundData();
//							Log.d("bb", "record resource to sample=" + sample);
//							Log.d("bb", "record resource to channel=" + channel);
//							Log.d("bb", "record resource to rate=" + rate);
//							Log.d("bb", "record resource to rawData="
//									+ rawData.length);
							if (rawData != null) {
								copyWaveFile(rawData, currFilePath, sample,
										channel, rate);	
							}
						}
						Message msg = new Message();
						msg.what = 5;
						mHandler.sendMessage(msg);
					}
				}).start();
			} else {
				popupWindow.showAtLocation(view, Gravity.CENTER_HORIZONTAL, offsetX,
						offsetY);
				//调用下面这句在android7.0上popupwindow不居中显示
//				popupWindow.update();
			}
	}

	/**
	 * 关闭语音批注窗口
	 */
	private void closePopupWindow() {
		if (popupWindow != null) {
			if(closeAnnotListener != null){
				closeAnnotListener.onAnnotClose(fileName); // 取消语音窗口监听
			}
			Intent intent = new Intent();
			intent.setAction("com.kinggrid.annotation.close");
			context.sendBroadcast(intent);
			isPlay = false;
			isRecord = false;
			isPause = false;
			popupWindow.dismiss();
			popupWindow = null;
			view.setBackgroundColor(Color.TRANSPARENT);
		}
	}

	/**
	 * 保存批注
	 *
	 *            批注内容
	 */
	private void saveAnnot() {
		Log.v("bb","saveAnnot()");
		if (annotOld.getUnType().equals("")) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					Message msg = new Message();
					msg.what = 4;
					mHandler.sendMessage(msg);
					final long dateTaken = System.currentTimeMillis();
					annotOld.setCurDateTime(DateFormat.format(
							"yyyy-MM-dd kk:mm:ss", dateTaken).toString());
					byte[] data = mSoundMeter.getRawData();
					Log.d("bb", "rawData length=" + data.length);
					annotOld.setSoundData(data); // 将录制的raw裸数据存入
					annotOld.setUnType(fileName);
					annotOld.setSoundBitspersample(RecordSound.bitSample);
					annotOld.setSoundChannels(RecordSound.channels);
					annotOld.setSoundRate(RecordSound.sampleRateInHz);

					if(saveAnnotListener != null){
						saveAnnotListener.onAnnotSave(annotOld);
					}
					Message msg1 = new Message();
					msg1.what = 5;
					mHandler.sendMessage(msg1);
				}
			}).start();
			//MobclickAgent.onEvent(context, "OC2_PDF_RECORD_TIME", timePeriod);
		}
	}

	/**
	 * 删除批注
	 */
	private void deleteAnnot() {
		Log.e("wmm", "(deleteAnnotListener != null):"+(deleteAnnotListener != null));
		if(deleteAnnotListener != null){
			deleteAnnotListener.onAnnotDelete(currFilePath);
		}
		closePopupWindow();
	}

	class completelistener implements OnCompletionListener {

		@Override
		public void onCompletion(final MediaPlayer mp) {
			isPlay = false;
			isPause = false;
			closePopupWindow();
		}

	}

	/**
	 * 隐藏删除按钮
	 */
	public void setDeleteBtnGone() {

	}

	/**
	 * 设置删除注释监听
	 * 
	 * @param deleteAnnotListener
	 *            监听接口
	 */
	public void setDeleteAnnotListener(
			final OnDeleteAnnotListener deleteAnnotListener) {
		this.deleteAnnotListener = deleteAnnotListener;
	}

	/**
	 * 设置保存注释监听
	 * 
	 * @param saveAnnotListener
	 *            监听接口
	 */
	public void setSaveAnnotListener(final OnSaveAnnotListener saveAnnotListener) {
		this.saveAnnotListener = saveAnnotListener;
	}

	/**
	 * 设置保存注释监听
	 * 
	 * @param closeAnnotListener
	 *            监听接口
	 */
	public void setCloseAnnotListener(
			final OnCloseAnnotListener closeAnnotListener) {
		this.closeAnnotListener = closeAnnotListener;
	}

	/**
	 * 保存事件通知
	 * 
	 * @author mmwan
	 * 
	 */
	public interface OnSaveAnnotListener {

		public void onAnnotSave(Annotation annotTextNew);

	}

	/**
	 * 删除批注接口
	 * 
	 * @author mmwan
	 * 
	 */
	public interface OnDeleteAnnotListener {

		public void onAnnotDelete(String filePath);

	}

	/**
	 * 关闭批注窗口监听
	 * 
	 * @author mmwan
	 * 
	 */
	public interface OnCloseAnnotListener {

		public void onAnnotClose(String filePath);

	}

	// 制作wav文件
	private void copyWaveFile(byte[] rawData, String outFilename, long sample,
                              long channels, long sampleRateInHz) {
		FileOutputStream out = null;

		long totalAudioLen = 0;
		long totalDataLen = totalAudioLen + 36;
		long longSampleRate = sampleRateInHz;
		// int channels = 2; //双声道
		long byteRate = sample * sampleRateInHz * channels / 8; // 字节数据速率16位
		byte[] data = new byte[512];

		try {
			// in = new FileInputStream(inFilename);
			out = new FileOutputStream(outFilename);
			totalAudioLen = rawData.length;
			Log.d("bb", "ccccccccccccc=" + totalAudioLen);
			totalDataLen = totalAudioLen + 36;
			writeWaveFileHeader(out, totalAudioLen, totalDataLen,
					longSampleRate, sample, channels, byteRate);
			out.write(rawData);
			out.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 制作wav格式音频文件头文件
	private void writeWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate, long sample, long channels,
                                     long byteRate) throws IOException {

		byte[] header = new byte[44];
		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (channels * sample / 8); // block align
		header[33] = 0;
		header[34] = (byte) sample; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

		out.write(header, 0, 44);
	}

	final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
			case 1:
				i++;
				if (i > 9) {
					timeS.setText(String.valueOf(i));
				} else {
					timeS.setText("0" + String.valueOf(i));
				}
				if (i > 59) {
					j++;
					i = 0;
					if (j > 9) {
						timeM.setText(String.valueOf(j));
					} else {
						timeM.setText("0" + String.valueOf(j));
					}
					timeS.setText("0" + String.valueOf(i));
				}
				
				if(j < 1){
					timePeriod = "0~1min";
				}
				if(j >= 1 && j < 3){
					timePeriod = "1~3min";
				}
				if(j >= 3 && j < 5){
					timePeriod = "3~5min";
				}
				if(j >= 5){
					timePeriod = "5~10min";
				}				
				
				if(j==10 && i ==1){//限制录音十分钟
					mBtnRecord.setVisibility(View.VISIBLE);
					mBtnStop.setVisibility(View.GONE);
					stop();
					isSaved = true;
					closePopupWindow();
					//saveAnnot();				
				}
				
				if (j > 59) {
					k++;
					i = 0;
					j = 0;
					if (k > 9) {
						timeH.setText(String.valueOf(k));
					} else {
						timeH.setText("0" + String.valueOf(k));
					}
					timeM.setText("0" + String.valueOf(j));
					timeS.setText("0" + String.valueOf(i));
				}
				break;
			case 2:
				if (isPlay) {
					int pos = playSound.getPosition();
					progressBar.setProgress(pos / 10);
					playTime.setText(toTime(pos / 1000));
				}
				break;
			case 3:
				totalTimeView.setText(toTime(totalTime));
				progressBar.setMax(totalTime * 100);
				if (alertWindow != null) {
					alertWindow.dismiss();
					alertWindow = null;
				}
				break;
			case 4:
				showAlert(view, false);
				break;
			case 5:
				if (alertWindow != null) {
					alertWindow.dismiss();
					alertWindow = null;
				}
				if (annotOld.getPageNo() != null) {
					int i = Integer.parseInt(annotOld.getPageNo());
					mPDFView.refreshPage(i);
				}
				if (popupWindow != null) {
					playView.setVisibility(View.VISIBLE);
					recordView.setVisibility(View.GONE);
					mBtnPlay.setVisibility(View.GONE);
					mBtnPause.setVisibility(View.VISIBLE);
					popupWindow.showAtLocation(view, Gravity.CENTER, 20,
							20);
					popupWindow.update();
					playSound();
				}
				break;
			case 6:
				showAlert(view, true);
				break;
			case 7:
				closePopupWindow();
				Toast.makeText(context, R.string.useRecordDevice, Toast.LENGTH_LONG).show();
				break;
			case 8:
				if(isSaved){
					Log.v("bb","handle message to saveAnnot");
					saveAnnot();
				}
				break;
			}
			super.handleMessage(msg);
		}
	};

	final class MyThread implements Runnable { // thread

		@Override
		public void run() {
			while (isRecord) {
				try {
					Thread.sleep(1000); // sleep 1000ms
					Message message = new Message();
					message.what = 1;
					mHandler.sendMessage(message);
				} catch (final Exception e) {
				}
			}
			while (isPlay) {
				try {
					Thread.sleep(100); // sleep 100ms
					Message message = new Message();
					message.what = 2;
					mHandler.sendMessage(message);
				} catch (final Exception e) {
				}
			}
		}
	}

	class ShowTime {
		private Thread thread;
		private Runnable runnable = null;

		public void Timestart() {
			if (runnable == null) {
				runnable = new MyThread();
			}
			runnable = new MyThread();
			if (thread == null) {
				thread = new Thread(runnable);
			}
			thread.start();
		}

		public void Timestop() {
			thread.interrupt();
			thread = null;
		}

		public void clearTime() {
			i = 0;
			j = 0;
			k = 0;
			timeH.setText("00");
			timeM.setText("00");
			timeS.setText("00");
		}
	}

	private String toTime(int time) {
		int minute = time / 60;
		int hour = minute / 60;
		int second = time % 60;
		minute %= 60;
		return String.format("%02d:%02d:%02d", hour, minute, second);
	}

	@Override
	public void onClick(View v) {
		if (v.getId()== R.id.btn_record) {
			startVoiceT = System.currentTimeMillis();
			fileName = KinggridActivity.userName + startVoiceT + ".raw";
			currFilePath = FILEDIR_PATH + "/tempsounds/" + fileName;
			isSaved = false;
			mBtnRecord.setVisibility(View.GONE);
			mBtnStop.setVisibility(View.VISIBLE);
			alertView.setVisibility(View.GONE);
			timeView.setVisibility(View.VISIBLE);
			start(currFilePath);
		}else if (v.getId()== R.id.btn_stop){
			mBtnRecord.setVisibility(View.VISIBLE);
			mBtnStop.setVisibility(View.GONE);
			stop();
			isSaved = true;
			closePopupWindow();
		}else if (v.getId()== R.id.btn_play){
			File file = new File(currFilePath);
			if (file.exists()) {
				mBtnPlay.setVisibility(View.GONE);
				mBtnPause.setVisibility(View.VISIBLE);
				playSound();
			}
		}else if (v.getId()== R.id.btn_pause){
			mBtnPlay.setVisibility(View.VISIBLE);
			mBtnPause.setVisibility(View.GONE);
			pauseSound();
		}else if (v.getId()== R.id.btn_delete){
			if (annotOld.getAuthorName().equals(KinggridActivity.userName)) {
				if (isPlay || isPause) {
					playSound.stop();
				}
				deleteAnnot();
			} else {
				Toast.makeText(context, R.string.username_different_del,
						Toast.LENGTH_LONG).show();
			}
		}else if (v.getId()== R.id.btn_close_annot){
			isSaved = false;
			if (isPlay || isPause) {
				playSound.stop();
			}
			closePopupWindow();
		}else if (v.getId()== R.id.btn_shutdown) {
			if (isRecord) {
				mSoundMeter.stopRecord();
			}
			closePopupWindow();
		}
	}
}