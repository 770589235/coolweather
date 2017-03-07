package com.ivpoints.exception;

import android.content.Context;
import android.os.Build;


import com.ivpoints.util.LogUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class GlobalException implements UncaughtExceptionHandler {

	//系统默认的UncaughtException处理类
	private UncaughtExceptionHandler mDefaultHandler;
	//CrashHandler实例
	private static GlobalException INSTANCE = new GlobalException();

	/** 保证只有一个CrashHandler实例 */
	private GlobalException() {
	}

	private Context context;
	/** 获取CrashHandler实例 ,单例模式 */
	public static GlobalException getInstance() {
		return INSTANCE;
	}

	/**
	 * 初始化
	 *
	 * @param context
	 */
	public void init(Context context) {
		this.context=context;
		//获取系统默认的UncaughtException处理器
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		//设置该CrashHandler为程序的默认处理器
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			//如果用户没有处理则让系统默认的异常处理器来处理
			LogUtil.d("没有处理异常");
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			LogUtil.d("处理了异常");
			try {
				Thread.sleep(1000);
				//退出程序
				android.os.Process.killProcess(android.os.Process.myPid());
				System.exit(1);
			} catch (Exception e) {
				LogUtil.e( "error : "+ e);
			}

		}

	}

	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}
		//在此可执行其它操作，如获取设备信息、执行异常登出请求、保存错误日志到本地或发送至服务端
		StringWriter sw =  new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		try {
			final StringBuilder sb = new StringBuilder();
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.getDefault());
			sb.append("CoolWeather error time:"+sdf.format(System.currentTimeMillis())+"\n");
			//收集设备的信息 时间
			Field[]  fields = Build.class.getFields();
			for(Field field: fields){

				String value = field.get(null).toString();
				String name = field.getName();
				sb.append(name+":"+value+"\n");
			}
			String errormsg = sw.toString();
			sb.append(errormsg);
			LogUtil.e("error:---"+sb.toString());


			
			/*File file = new File(Environment.getExternalStorageDirectory(),"error.txt");
			FileOutputStream fos = new FileOutputStream(file,true);
			fos.write(sb.toString().getBytes());
			fos.close();*/
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(pw!=null)
				pw.close();
		}
		return true;
	}




}
