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

			/* ����File����ȷ����Ҫ��ȡ�ļ�����Ϣ */
			File file = new File(Environment.getExternalStorageDirectory() + "/" + "Module test" + "/", fileName);

			/* FileInputSteam �������Ķ��� */FileInputStream fis = new FileInputStream(file);
			/* ׼��һ���ֽ������û�װ������ȡ������ */
			byte[] buffer = new byte[fis.available()];

			/* ��ʼ�����ļ��Ķ�ȡ */
			fis.read(buffer);
			/* �ر��� */
			fis.close();

			/* ���ֽ�����ת�����ַ����� ��ת������ĸ�ʽ */
			res = EncodingUtils.getString(buffer, "UTF-8");

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return res;
	}

	/**
	 * ��ȡSD����Ŀ¼·��
	 * 
	 * @return
	 */
	public static String getSdCardPath() {
		boolean exist = isSdCardExist();
		String sdpath = "";
		if (exist) {
			sdpath = Environment.getExternalStorageDirectory().getAbsolutePath();
		} else {
			sdpath = "������";
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
			result = "�Ҳ����ļ�!!";
		}
		return result;
	}
}
