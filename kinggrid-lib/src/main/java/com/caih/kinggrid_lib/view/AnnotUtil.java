
package com.caih.kinggrid_lib.view;

import android.content.Context;
import android.widget.Toast;

import com.ebensz.eink.R;
import com.caih.kinggrid_lib.view.dialog.SoundAnnotDialog;
import com.caih.kinggrid_lib.view.dialog.TextAnnotDialog;
import com.kinggrid.iapppdf.Annotation;
import com.kinggrid.iapppdf.ui.viewer.IAppPDFView;

/**
 * 批注工具类：插入和显示批注
 * com.kinggrid.iapppdf.demo.AnnotUtil
 * @author wmm
 * create at 2015年9月15日 上午9:00:36
 */
public class AnnotUtil {

	private static final String TAG = "AnnotUtil";
	private String userName;
	private IAppPDFView mPDFView;
	private Context mContext;
	
	public AnnotUtil(IAppPDFView view, String userName, Context context) {
		this.userName = userName;
		mPDFView = view;
		mContext = context;
	}

	/**
	 * 插入文字批注
	 * @param x 插入批注在文档X值
	 * @param y 插入批注在文档Y值
	 */
	public void addTextAnnot(final float x,final float y) {
		final Annotation annotation = new Annotation();
		annotation.setAuthorName(userName);
		TextAnnotDialog showAnnotContent = new TextAnnotDialog(mContext, annotation, false);
		showAnnotContent.setDeleteBtnGone();
		showAnnotContent.setConfigBtnVisible(false);
		showAnnotContent
				.setSaveAnnotListener(new TextAnnotDialog.OnSaveAnnotListener() {

					@Override
					public void onAnnotSave(final Annotation annotTextNew) {
//						if (IAppPDFActivity.progressBarStatus != 0) { //正在渲染
//							return; 
//						}
						mPDFView.doSaveTextAnnot(annotTextNew,x,y);
					}
				});

	}
	
	/**
	 * 插入打字机批注
	 * @param x 插入批注在文档X值
	 * @param y 插入批注在文档Y值
	 */
	public void addFreeTextAnnot(final float x,final float y) {
		final Annotation annotation = new Annotation();
		annotation.setAuthorName(userName);
		TextAnnotDialog showAnnotContent = new TextAnnotDialog(mContext, annotation, true);
		showAnnotContent.setDeleteBtnGone();
		showAnnotContent.setConfigBtnVisible(true);
		showAnnotContent
				.setSaveAnnotListener(new TextAnnotDialog.OnSaveAnnotListener() {

					@Override
					public void onAnnotSave(final Annotation annotTextNew) {
						//TODO 
						mPDFView.doSaveFreeTextAnnot(annotTextNew, x, y);
					}
				});
	}
	
	/**
	 * 插入语音批注
	 * @param x 插入批注在文档X值
	 * @param y 插入批注在文档Y值
	 */
	public void addSoundAnnot(final float x,final float y){
		final String autherString = mPDFView.getUserName();
		final Annotation annotation = new Annotation();
		annotation.setAuthorName(autherString);
		annotation.setUnType("");
			final SoundAnnotDialog showAnnotSound = new SoundAnnotDialog(
					mContext, 400, 300, annotation, mPDFView);
			showAnnotSound
					.setSaveAnnotListener(new SoundAnnotDialog.OnSaveAnnotListener() {

						@Override
						public void onAnnotSave(final Annotation annotTextNew) {
							mPDFView.doSaveSoundAnnot(annotTextNew, x, y);
						}
					});

			showAnnotSound
					.setCloseAnnotListener(new SoundAnnotDialog.OnCloseAnnotListener() {

						@Override
						public void onAnnotClose(final String filePath) {
//							mPDFView.doCloseSoundAnnot(filePath);
						}
					});

	}
	
	/**
	 * 显示文字批注
	 * @param annotation
	 */
	public void showTextAnnot(final Annotation annotation){

		if(!annotation.getAuthorName().equals(mPDFView.getUserName())){
			Toast.makeText(mContext, R.string.username_different_edit,
					Toast.LENGTH_SHORT).show();
		}

		TextAnnotDialog showAnnotContent = new TextAnnotDialog(mContext, annotation, false);
		showAnnotContent.setConfigBtnVisible(false);
		showAnnotContent
				.setSaveAnnotListener(new TextAnnotDialog.OnSaveAnnotListener() {

					@Override
					public void onAnnotSave(
							final Annotation annotTextNew) {
//						if (IAppPDFActivity.progressBarStatus != 0) {
//							return;
//						}
						mPDFView.doUpdateTextAnnotation(annotTextNew);
					}
				});
		showAnnotContent
				.setDeleteAnnotListener(new TextAnnotDialog.OnDeleteAnnotListener() {

					@Override
					public void onAnnotDelete() {
						mPDFView.doDeleteTextAnnotation(annotation);
					}
				});
	}
	/**
	 * 显示语音批注
	 * @param annotation
	 */
	public void showSoundAnnot(final Annotation annotation){
		
			final SoundAnnotDialog showAnnotSound = new SoundAnnotDialog(
					mContext, 400, 300, annotation, mPDFView);
			showAnnotSound
					.setDeleteAnnotListener(new SoundAnnotDialog.OnDeleteAnnotListener() {

						@Override
						public void onAnnotDelete(final String filePath) {
							mPDFView.doDeleteSoundAnnot(annotation, filePath);
//							bookShower.unLockScreen();
						}
					});

	}
}

