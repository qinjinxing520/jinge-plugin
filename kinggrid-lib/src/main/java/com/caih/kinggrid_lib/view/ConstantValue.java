
package com.caih.kinggrid_lib.view;

import android.os.Environment;

/**
 * 常量
 * com.kinggrid.iapppdf.demo.ConstantValue
 * @author wmm
 * create at 2015年8月13日 下午5:13:18
 */
public interface ConstantValue {
	final static int KEY_DOCUMENT_SAVE = 0;//退出保存
	final static int KEY_EBHANDWRITE = 1;//EB手笔分离
	final static int KEY_FINGER_AND_PEN = 2;//手笔分离
	final static int KEY_SINGER = 3;//签名
//	final static int KEY_SINGER_DEL = 4;//删除签名
	final static int KEY_FULL_SINGER = 4;//全文批注
	final static int KEY_DEL_FULL_SINGER = 5;// 删除全文批注
	final static int KEY_TEXT_NOTE = 6;//文字注释
	final static int KEY_DEL_TEXT_NOTE = 7;// 删除文字注释
	final static int KEY_FREETEXT_NOTE = 8;//打字机注释
	final static int KEY_SOUND_NOTE = 9;// 语音批注
	final static int KEY_DEL_SOUND_NOTE = 10;// 删除语音批注
	final static int KEY_NOTE_LIST = 11; //批注列表
	final static int KEY_TEXT_LIST = 12;// 文字注释列表
	final static int KEY_SOUND_LIST = 13; //语音批注列表
	final static int KEY_BOOKMARK_LIST = 14;// 大纲
	final static int KEY_ANNOT_IMPORT = 15;//导入批注到文档
	final static int KEY_ANNOT_EXPORT = 16;//从文档导出批注
	final static int KEY_CAMERA = 17;// 证件拍照
	final static int KEY_DIGITAL_SIGNATURE = 18;//数字签名
	final static int KEY_VERIFY = 19;//验证
	final static int KEY_SAVEAS = 20; // 另存
	final static int KEY_SAVE_PAGES = 21; // 保存页面图片
	final static int KEY_FIELD_CONTENT = 22; // 获取全部域内容
	final static int KEY_AREA = 23; //区域签批
	final static int KEY_LOCAL_DIGITAL_SIGNATURE = 24;//数字签名
	final static int KEY_LOCAL_KG_SIGNATURE = 25;//电子签章
	final static int KEY_HANDWRITE_DIGITAL_SIGNATURE = 26;//手写数字签名
	final static int KEY_HANDWRITE_KG_SIGNATURE = 27;//手写电子签章
	final static int KEY_QIFENG_SIGNATURE = 28;//骑缝章
	final static int KEY_KGSEAL_BY_TEXT = 29;//文本定位签名
	final static int KEY_KGSERVER_SIGN = 30;//签章服务器签名
	final static int KEY_TFCARD_SIGN = 31;//TF卡数字签名
	final static int KEY_TEE_SIGN = 32;//Tee签名
	final static int KEY_OFD_SERVER_SIGN = 33;//OFD密管系统签名
	//final static int KEY_ADD_ATTACHMENT = 22; //添加附件
	//final static int KEY_DUPLEX = 21;//同步签批
	//final static int KEY_ABOUT = 22; // 关于界面
	final static int KEY_SEARCH = 34;//搜索
	final static int KEY_LOCAL_SIGNATURE = 35;//定位盖章
	
	
	

/*	final static int TYPE_ANNOT_HANDWRITE = 1;*/
//	final static int TYPE_ANNOT_STAMP = 1;//全文批注
//	final static int TYPE_ANNOT_TEXT = 2;//文字注释
//	final static int TYPE_ANNOT_SIGNATURE = 3;//签名
//	final static int TYPE_ANNOT_SOUND = 4;//语音注释
	
	final static String SDCARD_PATH = Environment
			.getExternalStorageDirectory().getPath().toString();
	
	//intent传递名称,实际使用中根据需要自定义名称
	final String NAME = "demo_name";
	final String LIC = "demo_lic";
	final String CANFIELDEIDT = "demo_fieldEdit";
	final String T7MODENAME = "demo_T7Mode";
	final String EBENSDKNAME = "demo_ebenSDK";
	final String SAVEVECTORNAME = "demo_savevectortopdf";
	final String VECTORNAME = "demo_vectorsign";
	final String VIEWMODENAME = "demo_viewMode";
	final String LOADCACHENAME = "demo_loadCache";
	final String ANNOTPROTECTNAME = "demo_annotprotect";
	final String FILLTEMPLATE = "demo_filltemplate";
	final String ANNOT_TYPE = "demo_annottype";
	
	final String FILE_DATA = "demo_filedata";
	final String FILE_NAME= "demo_filename";

	final static String SEC_FILE = "709457bb-2dcf-4a7f-94b6-2aaf920869a0.sec";
	
	//阅读模式
	final int VIEWMODE_VSCROLL = 101;
	final int VIEWMODE_SINGLEH = 102;
	final int VIEWMODE_SINGLEV = 103;
	
	//Handler 
	final int MSG_WHAT_DISMISSDIALOG = 201;
	final int MSG_WHAT_LOADANNOTCOMPLETE = 202;
	final int MSG_WHAT_REFRESHDOCUMENT = 203;
	final int HANDLE_TF_CONNET_ERROR = 204;
	final int HANDLE_TF_CONNET_SUCCESS = 205;
	final int HANDLE_TF_LOGIN_ALERT = 206;
	final int HANDLE_TF_LOGIN_ERROR = 207;
	final int HANDLE_TF_LOGIN_SUCCESS = 208;
	final int HANDLE_TF_GETCERT_SUCCESS = 209;
	final int HANDLE_TF_GETCERT_ERROR = 210;
	final int HANDLE_TF_GETCERT_ALERT = 211;
	final int HANDLE_TF_CONNET_ALERT = 212;
	final static int MSG_GET_TEEID_SUCCESS = 213;//注册Tee成功
	final static int MSG_GET_TEEID_IS_EMPTY = 214;//注册TEE返回信息为空
	final static int MSG_GET_TEEID_FAILED = 215;//注册TEE失败
	final static int MSG_GET_TEECERT_FAILED = 216;//获取证书失败
	final static int MSG_GET_TEEDIGEST_FAILED = 217;//杂凑失败
	final static int MSG_GET_TEESIGN_FAILED = 218;//签名失败

	final static int MSG_EXPORT_ANNOT_FILE_FINISH = 300;
	
	public static final String KEY_SERVER_URL = "server_url";
	public static final String KEY_USER_ID = "user_id";
	
	public static final int MSG_NET_ERROR = 1001;
	public static final int MSG_VERIFYPASSWORD_FAIL = 1002;
	public static final int MSG_GET_SIGNATURE_FAIL = 1003;
	public static final int MSG_GET_SIGNATURE_SUCCESS = 1004;
	public static final int MSG_SHOW_ANNOT_INFO = 1005;
	public static final int MSG_URL_IS_NULL = 1006;
	public static final int MSG_PASSWORD_IS_NULL = 1007;
	public static final int MSG_USERNAME_IS_NULL = 1008;
	
	//拍照需要的参数
	final int REQUESTCODE_PHOTOS_TAKE = 100;
	final int REQUESTCODE_PHOTOS_CROP = 200;
	
	//签名方式：域定位、位置定位、文字定位、数字签名等
	final int SIGN_MODE_FIELDNAME = 301;
	final int SIGN_MODE_TEXT = 302;
	final int SIGN_MODE_POSITION = 303;
	final int SIGN_MODE_SERVER = 304;
	final int SIGN_MODE_KEY = 305;
	final int SIGN_MODE_BDE = 306;
	final int SIGN_MODE_TFCARD = 307;
	
	
	final int SIGNATURE_SERVER_VERIFY_MODE_COMMON = 5001;
	final int SIGNATURE_SERVER_VERIFY_MODE_DEVICESN = 5002;
	final int SIGNATURE_SERVER_VERIFY_MODE_USERCODE = 5003;

	public final static int SAVE_TYPE_ON_CLICK_BACK = 0;	// 点返回按钮
	public final static int SAVE_TYPE_ON_CLICK_SUBMIT = 1;	// 点提交按钮
	public final static int SAVE_TYPE_ON_CLICK_SAVE = 2;	// 点保存按钮
	public final static int SAVE_TYPE_ON_CLICK_TAB = 3;	// 点切换按钮

//	public final static String SIGN_PIC_PATH_SUFFIX = "/kinggrid_sign.jpg";
//	public final static String SIGN_PIC_PATH_TEMP_SUFFIX = "/kinggrid_sign_temp.jpg";
//	public final static String SIGN_FILE_NAME = "kinggrid_sign.jpg";

	public final static String SIGN_PIC_PATH_SUFFIX = "/kinggrid_sign.png";
	public final static String SIGN_PIC_PATH_TEMP_SUFFIX = "/kinggrid_sign_temp.png";
	public final static String SIGN_FILE_NAME = "kinggrid_sign.png";

	public static final String ANNOT_PIC_PATH_SUFFIX = "/signDate.jpg";

	public static String[] DEFAULT_COMMON_ANNOTS = {"同意", "不同意", "已阅"};

	public static final String FINISH_ACTION = "com.caih.cloud.office.busi.smartlink.zhrd.finishPage";

	public static final int WRITE_MODE_PEN = 0;
	public static final int WRITE_MODE_HAND = 1;


	public static final int DOCUMENT_PENCIL_DEFAULT_WIDTH = 2;
	public static final int DOCUMENT_PENCIL_MAX_WIDTH = 20;
}

