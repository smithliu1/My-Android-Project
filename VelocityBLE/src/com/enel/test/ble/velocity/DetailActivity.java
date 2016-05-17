package com.enel.test.ble.velocity;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

import org.apache.http.util.EncodingUtils;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Time;
import android.widget.TextView;

public class DetailActivity extends Activity {
	private TextView text;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		text = (TextView) findViewById(R.id.activity);
		String res = readDataFromSD(getday() + ".txt");
		text.setText("");
		text.setText(res);
	}

	//
	//
	private int getday() {
		Time time = new Time("GMT+8");
		time.setToNow();
		int day = time.monthDay;
		return day;
	}

	public String readDataFromSD(String fileName) {
		String res = null;
		try {

			/* 创建File对象，确定需要读取文件的信息 */
			File file = new File(Environment.getExternalStorageDirectory() + "/" + "Module test" + "/", fileName);

			/* FileInputSteam 输入流的对象， */FileInputStream fis = new FileInputStream(file);
			/* 准备一个字节数组用户装即将读取的数据 */
			byte[] buffer = new byte[fis.available()];

			/* 开始进行文件的读取 */
			fis.read(buffer);
			/* 关闭流 */
			fis.close();

			/* 将字节数组转换成字符创， 并转换编码的格式 */
			res = EncodingUtils.getString(buffer, "UTF-8");

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return res;
	}

	/**
	 * 获取SD卡根目录路径
	 * 
	 * @return
	 */
	public static String getSdCardPath() {
		boolean exist = isSdCardExist();
		String sdpath = "";
		if (exist) {
			sdpath = Environment.getExternalStorageDirectory().getAbsolutePath();
		} else {
			sdpath = "不适用";
		}
		return sdpath;

	}

	public static boolean isSdCardExist() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	private String searchFile(String keyword) {
		String result = "";
		File[] files = new File("/").listFiles();
		for (File file : files) {
			if (file.getName().indexOf(keyword) >= 0) {
				result += file.getPath() + "\n";
			}
		}
		if (result.equals("")) {
			result = "找不到文件!!";
		}
		return result;
	}
}
