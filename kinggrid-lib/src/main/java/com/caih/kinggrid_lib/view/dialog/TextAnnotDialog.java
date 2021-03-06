package com.caih.kinggrid_lib.view.dialog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.caih.kinggrid_lib.KinggridActivity;
import com.ebensz.eink.R;
import com.kinggrid.iapppdf.Annotation;
import com.kinggrid.iapppdf.util.KinggridConstant;


/**
 * 文字批注的弹出界面
 * 
 * @author Kevin <br/>
 *         create at 2013-10-23 上午11:29:09
 */
public class TextAnnotDialog implements KinggridConstant, OnClickListener {

    public PopupWindow popupWindow;
    public Context context;
    protected Annotation annotOld;
    protected View view;

    protected Button annot_save = null;
    protected ImageButton annot_delete = null;
    protected ImageButton annotConfig = null;
    protected Button annot_close = null;

    protected TextView annotAuthor;
    protected TextView annotModifyTime;
    protected EditText annotContent;

    protected OnSaveAnnotListener saveAnnotListener = null;
    protected OnCloseAnnotListener closeAnnotListener = null;
    protected OnDeleteAnnotListener deleteAnnotListener = null;
    
    private static final int[] m_penColors = {
		Color.argb(255, 44, 152, 140), Color.argb(255, 48, 115, 170),
		Color.argb(255, 139, 26, 99), Color.argb(255, 112, 101, 89),
		Color.argb(255, 40, 36, 37), Color.argb(255, 226, 226, 226),
		Color.argb(255, 219, 88, 50), Color.argb(255, 129, 184, 69),
		Color.argb(255, 255, 0, 0), Color.argb(255, 0, 255, 0) };
    
    private PopupWindow mSettingWindow;
    private int freetextColor = Color.BLACK;
    private int freetextSize = 12;
    private boolean isFreeTextAnnot = false;

    /**
     * 构造函数
     *
     * @param annotation
     */
    public TextAnnotDialog(Context context, final Annotation annotation, boolean isFreeText) {
        this.context = context;
        this.annotOld = annotation;
        this.isFreeTextAnnot = isFreeText;
        initWindow();
        show();
    }

    /**
     * 初始化布局
     *
     */
	public void initWindow() {
		view = LayoutInflater.from(context).inflate(R.layout.eben_annot_layout,
				null);
		annotAuthor = (TextView) view.findViewById(R.id.annot_author);
		annotAuthor.setText(annotOld.getAuthorName());
		annotModifyTime = (TextView) view.findViewById(R.id.annot_modify_time);
		annotModifyTime.setText(annotOld.getCurDateTime());
		annotContent = (EditText) view.findViewById(R.id.annot_text);
		Log.v("tbz","annotOld content = " + annotOld.getAnnoContent());
		annotContent.setText(annotOld.getAnnoContent());
		

		// 关闭
		annot_close = (Button) view.findViewById(R.id.annot_close);
		annot_close.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				closeAnnotWindow();
			}
		});

		// 保存
		annot_save = (Button) view.findViewById(R.id.annot_save);
		annot_save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				saveAnnot(annotContent.getText().toString());
			}
		});

		// 删除
		annot_delete = (ImageButton) view.findViewById(R.id.annot_delete);
		annot_delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
//				if (annotOld.getAuthorName().equals(DocumentActivity.userName)) {
//					deleteAnnot();
//				} else {
					Toast.makeText(context, R.string.username_different_del,
							Toast.LENGTH_LONG).show();
//				}
			}
		});
		
		annotConfig = (ImageButton) view.findViewById(R.id.annot_config);
		annotConfig.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showSettingWindow();
			}
		});

		if (!annotAuthor.getText().toString().equals(KinggridActivity.userName)) {
			annot_delete.setVisibility(View.GONE);
		}

		if (!annotAuthor.getText().toString().equals(KinggridActivity.userName)) {
			annotContent.setEnabled(false);
			
		}
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getResources().getDisplayMetrics();
		popupWindow = new PopupWindow(view, (int) (dm.widthPixels / 1.29), (int) (dm.heightPixels / 2.62));
		
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(false);
		popupWindow.setBackgroundDrawable(null);
		popupWindow.getContentView().setFocusable(true); 
        popupWindow.getContentView().setFocusableInTouchMode(true);
       final int mWidth = popupWindow.getWidth();
       final int mHeight = popupWindow.getHeight(); 
      //在Android 6.0以上 ，只能通过拦截事件来解决
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                final int x = (int) event.getX();
                final int y = (int) event.getY();

                if ((event.getAction() == MotionEvent.ACTION_DOWN)
                        && ((x < 0) || (x >= mWidth) || (y < 0) || (y >= mHeight))) { 
                     // donothing
                    // 消费事件
                	System.out.println("====pop down");
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                	System.out.println("====pop outside");
                    return true;
                }
                return false;
            }
        });
	}
	
	private void showSettingWindow(){
		LinearLayout layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.annot_freetext_config, null);
		final TextView width_textView = (TextView) layout.findViewById(R.id.freetext_width);
		final SeekBar seekBar = (SeekBar) layout.findViewById(R.id.freetext_seekset);
		Button closeWindowButton = (Button) layout.findViewById(R.id.freetext_close_setting);
		seekBar.setMax(36); 
		seekBar.setProgress(0);
		width_textView.setText("字体大小:" + 12);
		seekBar.setProgress(12);
		closeWindowButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
				width_textView.setText("字体大小:" +(progress + 6));
				freetextSize = progress + 6;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

		});
		
		GridView penColor_gridView = (GridView) layout.findViewById(R.id.freetext_color_selector);
		final ColorAdapter colorAdapter = new ColorAdapter(context, 0);
		penColor_gridView.setAdapter(colorAdapter);
		penColor_gridView.requestFocus();
		penColor_gridView.setSelection(0);
		penColor_gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
				colorAdapter.setColorId(position);
				freetextColor = m_penColors[position];
				colorAdapter.notifyDataSetChanged();
			}
		});
		
		mSettingWindow = new PopupWindow(layout, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mSettingWindow.setFocusable(true);
		// 设置不允许在外点击消失
		mSettingWindow.setOutsideTouchable(false);
		mSettingWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
	}
	
	private void dismiss(){
		if(mSettingWindow != null){
			mSettingWindow.dismiss();
		}
	}
	
	public class ColorAdapter extends BaseAdapter {
		private Context context;
		private int selectColorId;

		public ColorAdapter(Context mContext, int id) {
			this.context = mContext;
			selectColorId = id;
		}

		public void setColorId(int id){
			selectColorId = id;
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
			return m_penColors.length;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;
			if(convertView == null){
				convertView = LayoutInflater.from(context).inflate(R.layout.pencolor, null);
				
				holder = new ViewHolder();
				holder.color_imageView = (ImageView) convertView.findViewById(R.id.pen_color);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.color_imageView.setBackgroundColor(m_penColors[position]);
//			if(position == selectColorId){
//				convertView.setBackgroundResource(backgroundColorId);
//			}else{
				convertView.setBackgroundResource(R.color.transparent);
//			}
			return convertView;

		}
	}
	
	class ViewHolder{
		ImageView color_imageView;
	}

    /**
     * 显示文字批注PopupWindow
     *
     */
    public void show() {
         popupWindow.showAtLocation(view, Gravity.CENTER_HORIZONTAL, 0, 0);
//         popupWindow.update();
         /*annotContent.requestFocus();
         if(annotContent.isEnabled()){
         	annotContent.post(new Runnable(){

 				@Override
 				public void run() {
 					// TODO Auto-generated method stub
 					InputMethodManager im = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
 		            im.showSoftInput(annotContent,0);
 				}
             	
             });
         }*/
    }

    /**
     * 隐藏删除按钮
     */
    public void setDeleteBtnGone() {
        if (annot_delete != null && annot_delete.getVisibility() == View.VISIBLE) {
            annot_delete.setVisibility(View.GONE);
        }
    }
    
    /**
     * 设置“配置”按钮是否可见
     * @param visible
     */
    public void setConfigBtnVisible(boolean visible){
    	if (annotConfig != null) {
			if (visible) {
				annotConfig.setVisibility(View.VISIBLE);
			} else {
				annotConfig.setVisibility(View.GONE);
			}
		}
    }
    
    
    @Override
    public void onClick(final View v) {


    }

    /**
     * 删除批注
     */
    private void deleteAnnot() {
    	if(deleteAnnotListener != null){
    		deleteAnnotListener.onAnnotDelete();
    	}
        closeAnnotWindow();
    }

    /**
     * 保存批注
     * 
     * @param annotTextNew
     *            批注内容
     */
    private void saveAnnot(final String annotTextNew) {
        if (!annotTextNew.equals(annotOld.getAnnoContent()) && !annotTextNew.equals("")) {
            annotOld.setAnnoContent(annotTextNew);
            annotOld.setAuthorName(KinggridActivity.userName);
            final long dateTaken = System.currentTimeMillis();
            annotOld.setCurDateTime(DateFormat.format("yyyy-MM-dd kk:mm:ss", dateTaken).toString());
            //TODO COLOR AND SIZE
            if (isFreeTextAnnot) {
				annotOld.setAnnotFontColor(freetextColor);
				annotOld.setAnnotFontSize(freetextSize);
			}
            if(saveAnnotListener != null){
            	saveAnnotListener.onAnnotSave(annotOld);
            }
        } else if(annotTextNew.equals("")){
        	Toast.makeText(context, "没有注释内容", Toast.LENGTH_LONG).show();
        }else if(annotTextNew.equals(annotOld.getAnnoContent())){
        	Toast.makeText(context, "注释内容未修改", Toast.LENGTH_LONG).show();
        }
        closeAnnotWindow();
    }

    /**
     * 关闭文字批注窗口
     */
    private void closeAnnotWindow() {
        if (popupWindow != null) {
        	if(closeAnnotListener != null){
        		closeAnnotListener.onAnnotClose();
        	}
            Intent intent = new Intent();
			intent.setAction("com.kinggrid.annotation.close");
			context.sendBroadcast(intent);
            popupWindow.dismiss();
            popupWindow = null;
            view.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    /**
     * 设置删除注释监听
     * 
     * @param deleteAnnotListener
     *            监听接口
     */
    public void setDeleteAnnotListener(final OnDeleteAnnotListener deleteAnnotListener) {
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
     * 设置关闭注释监听
     * 
     * @param closeAnnotListener
     *            监听接口
     */
    public void setCloseAnnotListener(final OnCloseAnnotListener closeAnnotListener) {
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

        public void onAnnotDelete();

    }

    /**
     * 关闭批注窗口监听
     * 
     * @author mmwan
     * 
     */
    public interface OnCloseAnnotListener {

        public void onAnnotClose();

    }

}
