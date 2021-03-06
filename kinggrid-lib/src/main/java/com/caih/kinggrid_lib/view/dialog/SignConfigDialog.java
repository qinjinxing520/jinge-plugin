package com.caih.kinggrid_lib.view.dialog;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.caih.kinggrid_lib.util.SPUtils;
import com.caih.kinggrid_lib.view.ConstantValue;
import com.ebensz.eink.R;
import com.kinggrid.iapppdf.ui.viewer.PDFHandWriteView;

/**
 * 手写笔宽与颜色设置类
 * com.kinggrid.iapppdf.demo.SignConfig
 * @author wmm
 * create at 2015年8月24日 下午4:57:40
 */
public class SignConfigDialog {

	private SharedPreferences sharedPreferences;
	private Editor editor;
	private Context context;
	private PDFHandWriteView pdfWriteView;
	
	/**
	 * Pen Settings
	 */
	private PopupWindow mSettingWindow;
	/**
	 * 笔的颜色
	 */
	private int changeColor;
	/**
	 * 笔宽
	 */
	private float changeWidth;
	/**
	 * 笔型
	 */
	private int changeType = 0;
	/**
	 * 保存至XML的笔宽Key
	 */
	private String penMaxSizeName = "penMaxSize";
	/**
	 * 保存至XML的笔颜色Key
	 */
	private String penColorName = "penColor";
	/**
	 * 保存至XML的笔型
	 */
	private String penTypeName = "penType";
	/**
	 * 笔宽
	 */
	private int DEFAULT_PENSIZE;
	private final int BRUSH_DEFAULT_PENSIZE = 70;
	public static final int BALL_DEFAULT_PENSIZE = 2;
	private final int PENCIL_DEFAULT_PENSIZE = 5;
	private final int WATER_DEFAULT_PENSIZE = 30;
	private String pen = "";
	
	/**
     * 笔宽设置的跨度
     */
    private int progress = ConstantValue.DOCUMENT_PENCIL_MAX_WIDTH;
	private float density;
	
	private Resources resources;
	private String packageName;
	
	
	private String brushPenSize = "brushPenSize";
	private String brushPenColor = "brushPenColor";
	
	private String ballPenSize = "ballPenSize";
	private String ballPenColor = "ballPenColor";
	
	private String pencilPenSize = "pencilPenSize";
	private String pencilPenColor = "pencilPenColor";

	private String waterPenSize = "waterPenSize";
	private String waterPenColor = "waterPenColor";
	/**
	 * 构造方法
	 * @param mContext
	 * @param pdfHandWriteView
	 * @param penMaxSizeName
	 * @param penColorName
	 * @param penTypeName
	 */
	public SignConfigDialog(Context mContext, PDFHandWriteView pdfHandWriteView, String penMaxSizeName, String penColorName, String penTypeName) {
		context = mContext;
		pdfWriteView = pdfHandWriteView;
		if(!TextUtils.isEmpty(penMaxSizeName)){
			this.penMaxSizeName = penMaxSizeName;
		}
		if(!TextUtils.isEmpty(penColorName)){
			this.penColorName = penColorName;
		}
		if(!TextUtils.isEmpty(penTypeName)){
			this.penTypeName = penTypeName;
		}
		
		init();
	}
	/**
	 * 消失对话框
	 */
	public void dismiss(){
		if(mSettingWindow != null){
			float width = changeWidth * density;
			if(pdfWriteView!=null) {
				pdfWriteView.setPenInfo(width, changeColor, changeType);
			}
			Log.i("SignConfigDialog", "setPencilWidth "+changeWidth);
			SPUtils.setPencilWidth(context, (int)changeWidth);
			setPenTypeToXML(changeType);
			setPenColorToXML(changeColor);
			setPenMaxSizeToXML(width);
			mSettingWindow.dismiss();
		}
	}
	/**
	 * 构造方法
	 * @param mContext
	 * @param writeView
	 * @param penMaxSizeName
	 * @param penColorName
	 * @param penTypeName
	 * @param progress �ʿ�Ŀ��
	 */
	public SignConfigDialog(Context mContext, PDFHandWriteView writeView, String penMaxSizeName, String penColorName, String penTypeName, int progress) {
		context = mContext;
		pdfWriteView = writeView;
		if(!TextUtils.isEmpty(penMaxSizeName)){
			this.penMaxSizeName = penMaxSizeName;
		}
		if(!TextUtils.isEmpty(penColorName)){
			this.penColorName = penColorName;
		}
		if(!TextUtils.isEmpty(penTypeName)){
			this.penTypeName = penTypeName;
		}
		this.progress = progress;
		
		init();
	}
	/**
	 * 构造方法
	 * @param mContext
	 */
	public SignConfigDialog(Context mContext){
		context = mContext;
		init();
	}
	/**
	 * 以PopopWindow的方式显示签名设置窗口
	 * @param width 窗口显示的宽度
	 * @param height 窗口显示的高度
	 */
	public void showSettingWindow(int width, int height) {

		View view = initSettingView(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);

		mSettingWindow = new PopupWindow(view, width, height);
		mSettingWindow.setFocusable(true);
		// 设置不允许在外点击消失
		mSettingWindow.setOutsideTouchable(false);
		mSettingWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
	}
	/**
	 * 初始化
	 */
	private void init(){
		sharedPreferences = context.getSharedPreferences("pen_info",
				Context.MODE_PRIVATE);
		editor = sharedPreferences.edit();
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getResources().getDisplayMetrics();  
		density = dm.density;
	}
	/**
	 * 初始化界面
	 * @param mWidth
	 * @param mHeight
	 * @return
	 */
	private View initSettingView(int mWidth, int mHeight) {
		resources = context.getResources();
		packageName = context.getPackageName();
		
		View settingView = LayoutInflater.from(context).inflate(R.layout.pensetting, null);
		final TextView width_textView = (TextView) settingView.findViewById(R.id.width);
		final SeekBar seekBar = (SeekBar) settingView.findViewById(R.id.seekset);
		final TextView lineshow = (TextView) settingView.findViewById(R.id.textshow);
		Button closeWindowButton = (Button) settingView.findViewById(R.id.btn_close_setting);
		seekBar.setMax(ConstantValue.DOCUMENT_PENCIL_MAX_WIDTH-BALL_DEFAULT_PENSIZE);
		
		int color = getPenColorFromXML(this.penColorName); 
		
//		int penSize = (int) (getPenMaxSizeFromXML(this.penMaxSizeName) / density);
		int penSize = SPUtils.getPencilWidth(context);
		Log.d("tbz","penSize = " + penSize);
		changeType = getPenTypeFromXML(this.penTypeName);
		switch (changeType) {
		case PDFHandWriteView.TYPE_BRUSHPEN:
			DEFAULT_PENSIZE = BRUSH_DEFAULT_PENSIZE;
			pen = "毛笔";
			break;
		case PDFHandWriteView.TYPE_PENCIL:
			DEFAULT_PENSIZE = PENCIL_DEFAULT_PENSIZE;
			pen = "铅笔";
			break;
		case PDFHandWriteView.TYPE_WATERPEN:
			DEFAULT_PENSIZE = WATER_DEFAULT_PENSIZE;
			pen = "水彩笔";
			break;
		case PDFHandWriteView.TYPE_BALLPEN:
		default:
			DEFAULT_PENSIZE = BALL_DEFAULT_PENSIZE;
			pen = "钢笔";
			break;
		}
		penSize = (penSize < DEFAULT_PENSIZE) ? DEFAULT_PENSIZE :penSize;
		penSize = (penSize > DEFAULT_PENSIZE + progress) ? DEFAULT_PENSIZE + progress :penSize;
		Log.d("tbz","penSize1 = " + penSize);
		width_textView.setText(pen+"宽度:" +penSize);
		seekBar.setProgress(penSize - DEFAULT_PENSIZE); 
		lineshow.setBackgroundColor(color);
		closeWindowButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		final LayoutParams linearParams = (LayoutParams) lineshow
				.getLayoutParams(); // 取控件textView当前的布局参数
		linearParams.height = (int) (penSize/1.5);
		lineshow.setLayoutParams(linearParams);
		changeColor = color;
		changeWidth = penSize;

		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
				linearParams.height = (int) ((progress + DEFAULT_PENSIZE)/1.5);
				lineshow.setLayoutParams(linearParams); // 使设置好的布局参数应用到控件
				changeWidth = progress+DEFAULT_PENSIZE;
				width_textView.setText(pen+"宽度：" +(progress+DEFAULT_PENSIZE));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

		});
		int selectId = getPenColorId(changeColor);
		GridView penColor_gridView = (GridView) settingView.findViewById(R.id.pen_color_selector);
		final ColorAdapter colorAdapter = new ColorAdapter(context,selectId);
		penColor_gridView.setAdapter(colorAdapter);
		penColor_gridView.requestFocus();
		penColor_gridView.setSelection(selectId);
		penColor_gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
				colorAdapter.setColorId(position);
				colorAdapter.notifyDataSetChanged();
				changeColor = m_penColors[position];
				lineshow.setBackgroundColor(changeColor);
			}
		});
		
		GridView penType_gridView = (GridView) settingView.findViewById(R.id.pen_type_selector);
		penType_gridView.setAdapter(new ImageAdapter(context));
		penType_gridView.requestFocus();
		penType_gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
				int penSize = 0;
				int color = 0;
				changeType = position;
				switch (position) {
					
				case PDFHandWriteView.TYPE_BRUSHPEN:
					DEFAULT_PENSIZE = BRUSH_DEFAULT_PENSIZE;
					pen = "毛笔";
					color = getPenColorFromXML(brushPenColor);
					penSize = (int) getPenMaxSizeFromXML(brushPenSize);
					break;
				case PDFHandWriteView.TYPE_PENCIL:
					DEFAULT_PENSIZE = PENCIL_DEFAULT_PENSIZE;
					pen = "铅笔";
					color = getPenColorFromXML(pencilPenColor);
					penSize = (int) getPenMaxSizeFromXML(pencilPenSize);
					break;
				case PDFHandWriteView.TYPE_WATERPEN:
					DEFAULT_PENSIZE = WATER_DEFAULT_PENSIZE;
					pen = "水彩笔";
					color = getPenColorFromXML(waterPenColor);
					penSize = (int) getPenMaxSizeFromXML(waterPenSize);
					break;
				case PDFHandWriteView.TYPE_BALLPEN:
				default:
					DEFAULT_PENSIZE = BALL_DEFAULT_PENSIZE;
					pen = "钢笔";
					color = getPenColorFromXML(ballPenColor);
					penSize = (int) getPenMaxSizeFromXML(ballPenSize);
					break;
				}
				
				penSize = (penSize < DEFAULT_PENSIZE) ? DEFAULT_PENSIZE :penSize;
				penSize = (penSize > DEFAULT_PENSIZE + progress) ? DEFAULT_PENSIZE + progress :penSize;
				width_textView.setText(pen+"宽度：" +penSize);
				linearParams.height = (int) (penSize/1.5);
				
				lineshow.setLayoutParams(linearParams);
				seekBar.setProgress(penSize - DEFAULT_PENSIZE);
				changeWidth = penSize;
				changeColor = color;
				lineshow.setBackgroundColor(changeColor);
			}
		});
		return settingView;
	}
	
	/**
	 * 保存颜色至XML
	 * @param color
	 */
	private void setPenColorToXML(int color) {
		editor.putInt(penColorName, color);
		editor.commit();
	}
	/**
	 * 保存笔宽至XML
	 * @param penMaxSize
	 */
	private void setPenMaxSizeToXML(float penMaxSize) {
		editor.putFloat(penMaxSizeName, penMaxSize);
		editor.commit();
	}
	/**
	 * 保存笔型至XML 
	 * @param penType
	 */
	private void setPenTypeToXML(int penType){
		editor.putInt(penTypeName, penType);
		editor.commit();
	}
	/**
	 * 获取手写笔的颜色
	 * @param penColorName
	 * @return
	 */
	public int getPenColorFromXML(String penColorName) {
		return sharedPreferences.getInt(penColorName, Color.BLACK);
	}
	/**
	 * 获取笔宽最大值
	 * @param penMaxSizeName
	 * @return
	 */
	public float getPenMaxSizeFromXML(String penMaxSizeName) {
		return sharedPreferences.getFloat(penMaxSizeName, 2f);
	}
	/**
	 * 获取笔型
	 * @param penTypeName
	 * @return
	 */
	public int getPenTypeFromXML(String penTypeName){
		return sharedPreferences.getInt(penTypeName, PDFHandWriteView.TYPE_BALLPEN);
	}
	
    
    public void setPenSettingProgress(int progress){
    	this.progress = progress;
    }
    
    private int getPenColorId(int color){
		if(m_penColors != null && m_penColors.length != 0){
			for(int i=0; i < m_penColors.length; i++){
				if(color == m_penColors[i]){
					return i;
				}
			}
		}
		return -1;
	}
	
    /**
	 * 适配器
	 * com.kinggrid.iapppdf.demo.ImageAdapter
	 * @author wmm
	 * create at 2015年8月24日 下午5:03:43
	 */
	public class ImageAdapter extends BaseAdapter {
		
		private Context mContext;
		private int ballpenId = R.drawable.ballpen;
		private int brushpenId = R.drawable.brushpen;
		private int pencilId = R.drawable.pencil;
		private int waterpenId = R.drawable.waterpen;

//		private Integer[] mThumbIds = {
//				ballpenId, brushpenId,pencilId,waterpenId};
		private Integer[] mThumbIds = {ballpenId};
        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mThumbIds.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
        	
        	final ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                imageView.setAdjustViewBounds(false);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setImageResource(mThumbIds[position]);

            return imageView;
        }

	}
	
	private static final int[] m_penColors = {
			Color.argb(255, 44, 152, 140), Color.argb(255, 48, 115, 170),
			Color.argb(255, 139, 26, 99), Color.argb(255, 112, 101, 89),
			Color.argb(255, 40, 36, 37), Color.argb(255, 226, 226, 226),
			Color.argb(255, 219, 88, 50), Color.argb(255, 129, 184, 69),
			Color.argb(255, 255, 0, 0), Color.argb(255, 0, 255, 0) };
	
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
			if(position == selectColorId){
				int backgroundColorId = resources.getIdentifier("my_background", "drawable", packageName);
				convertView.setBackgroundResource(backgroundColorId);
			}else{
				convertView.setBackgroundResource(R.color.transparent);
			}
			return convertView;

		}
	}
	
	class ViewHolder{
		ImageView color_imageView;
	}
}
