package com.enel.test.ble.velocity;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.http.util.EncodingUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.enerty.ModuleEntity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.text.format.Time;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

@SuppressWarnings({ "deprecation", "unused" })
public class MainActivity extends Activity {
	private static final String TAG = "sBLEVelocityTestingzsz";

	private BluetoothAdapter mBTAdapter;
	private BluetoothLeScanner mScanner;
	private ScanSettings mSettings;
	private List<ScanFilter> mFilters;
	private BluetoothGatt mGatt;
	private BluetoothGattCharacteristic mWrite;

	private TextView lblConn, lblInfo, lblMaxTime, lblMinTime, lblTimeZone, lblStartTime, lblEndTime, lblLost;
	private Button btnStart;
	private Dialog builder;
	private ListView mBleDeviceListView;
	private static DeviceAdapter mDeviceAdapter;

	private int REQUEST_ENABLE_BT = 1;
	private long SCAN_PERIOD = 10000;
	private StringBuilder zoneContent;
	private long[] count;
	private int range = 10, number = 20;
	private int type = 0;
	private String option = "";
	private byte[] lastData = null;
	private int times = 0;
	private String interval = "0.0";
	// private int radix = 1000;
	private int radix = 10000;
	private boolean isConnected = false;
	private boolean hasResponse = true;
	private List<byte[]> mDataList;
	private String SDPATH = "";
	private long mmmtime = 0;

	private static final UUID SERVICE_UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
	private static final UUID CHARA_WRITE = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
	private static final UUID CHARA_NOTIFY = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb");

	private final byte[] REQ_INTERVAL = { (byte) 0x0A, (byte) 0xF8 };
	private final byte[] RSP_INTERVAL = { (byte) 0x0A, (byte) 0xF9 };
	private final byte[] REQ_AUTO = { (byte) 0x0A, (byte) 0xFA };
	private final byte[] RSP_AUTO = { (byte) 0x0A, (byte) 0xFB };
	private final byte[] REQ_COUNT = { (byte) 0x0A, (byte) 0xFC };
	private final byte[] RSP_COUNT = { (byte) 0x0A, (byte) 0xFD };
	private final byte[] SET_COUNT = { (byte) 0x27, (byte) 0x10 };

	// add by smith
	private final byte[] REQ_STATUS = { (byte) 0x08, (byte) 0x0E };
	private final byte[] RSP_STATUS = { (byte) 0x08, (byte) 0x0F };
	private final byte[] REQ_UUID = { (byte) 0x08, (byte) 0x08 };
	private final byte[] RSP_UUID = { (byte) 0x08, (byte) 0x09 };
	private final byte[] REQ_EAPP = { (byte) 0x08, (byte) 0x02 };
	private final byte[] RSP_EAPP = { (byte) 0x08, (byte) 0x03 };
	private final byte[] REQ_PROGRAM = { (byte) 0x08, (byte) 0x04 };
	private final byte[] RSP_PROGRAM = { (byte) 0x08, (byte) 0x05 };
	private final byte[] RSP_ERROR = { (byte) 0x04, (byte) 0x80 };
	private final byte[] REQ_ENTER_BSL = { (byte) 0x08, (byte) 0x06 };
	private final byte[] RSP_ENTER_BSL = { (byte) 0x08, (byte) 0x07 };
	private final byte[] REQ_ENTERAPP = { (byte) 0x08, (byte) 0x00 };
	private final byte[] RSP_ENTERAPP = { (byte) 0x08, (byte) 0x01 };

	private final byte[] REQ_BSLINFORMATIOM = { (byte) 0x08, (byte) 0x0A };
	private final byte[] RSP_BSLINFORMATIOM = { (byte) 0x08, (byte) 0x0B };

	private final byte[] REQ_FRIMWAREINFORMATION = { (byte) 0x08, (byte) 0x0C };
	private final byte[] RSP_FRIMWAREINFORMATION = { (byte) 0x08, (byte) 0x0D };
	private final byte[] REQ_NODE = { (byte) 0x0B, (byte) 0x02 };
	private final byte[] RSP_NODE = { (byte) 0x0B, (byte) 0x03 };

	private final static byte CRC = (byte) 0xEE;
	private final static byte LT = (byte) 0x7E;
	private final static byte SENDER = (byte) 0x00;

	// private List<String> current = new ArrayList<String>();
	private List<Byte> last = new ArrayList<Byte>();
	private ModuleEntity module;
	private int countflag;
	private String muid = null;
	private ArrayAdapter<String> adapter;
	private ListView listView;
	private List<String> list;
	private TextView moduleName;
	private Handler handler;
	String mresult = "";
	private int modulenode;
	private int mcount = 0;
	private int progress = 0;
	private TextView text;
	private int cyclecount = 0;

	private byte[] unityData, partData;
	private int unitSize = 0;
	private int flag = 0;
	private int programflag = 0;
	private ProgressBar bar;

	private int overtime = 3000;
	private long initProgrmasTime = 0;
	private Message message;
	private int modulenum = 0;
	private boolean isapp = true;
	// 进度条
	@SuppressLint("HandlerLeak")
	private Handler mhandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 10:
				Log.i(TAG, msg.arg1 + "...........................");
				if (msg.arg1 < 100 && msg.arg1 >= 0)
					text.setText(msg.arg1 + "%");
				else if (msg.arg1 == 100) {
					text.setText("烧写进度完成完成");
				} else if (msg.arg1 == 10001) {
					text.setText("烧写失败");
				} else if (msg.arg1 == 10002) {
					text.setText("烧写成功");
					try {
						Thread.sleep(4000);
						Log.i(TAG, "request node success");
						countlength = 0;
						flag = 1;
						runcount = 1;
						progress = 0;
						byte[] value = configCmd(modulenode, REQ_STATUS, null);
						Log.i(TAG, "Double Module=request uuid =" + HexDump.toHexString(value));
						sendCmd(value);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				break;

			default:
				break;
			}
		};
	};

	private SparseArray<String> spare = new SparseArray<String>();
	private int filecount = 0;
	private int countlength = 0;
	private int runcount = 1;
	// private final byte[] SET_COUNT = { (byte) 0x03, (byte) 0xE8 };

	private static final int WHAT_CONNECTED = 1001;
	private static final int WHAT_DISCONNECTED = 1002;
	private static final int WHAT_INTERVAL = 1003;
	private static final int WHAT_TIME = 1004;
	private static final int WHAT_RESTART = 1005;
	private static final int WHAT_RESULT = 1006;
	private static final int WHAT_TIME_START = 1007;
	private static final int WHAT_TIME_END = 1008;
	private static final int WHAT_SCREEN_SHOT = 1009;

	// ===================================================
	// ===================================================
	// ===================================================

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		option = getResources().getString(R.string.txt_send);
		SDPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Tom" + File.separator;

		checkFeatureBLE();
		initView();
		initValue();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!isConnected) {
			scanBLE();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mBTAdapter != null && mBTAdapter.isEnabled()) {
			scan(false);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mGatt == null) {
			return;
		}
		mGatt.close();
		mGatt = null;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == Activity.RESULT_CANCELED) {
				finish();
				return;
			}
		}
	}

	// ===================================================
	// ===================================================
	// ===================================================

	private void checkFeatureBLE() {
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			finish();
		}
		BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBTAdapter = btManager.getAdapter();
	}

	private void initView() {
		lblConn = (TextView) findViewById(R.id.lblConnStatus);

		lblInfo = (TextView) findViewById(R.id.lblInfo);
		lblMaxTime = (TextView) findViewById(R.id.lblMaxTime);
		lblMinTime = (TextView) findViewById(R.id.lblMinTime);
		lblTimeZone = (TextView) findViewById(R.id.lblTimeZone);
		lblStartTime = (TextView) findViewById(R.id.lblStartTime);
		lblEndTime = (TextView) findViewById(R.id.lblEndTime);
		lblLost = (TextView) findViewById(R.id.lblLost);
		btnStart = (Button) findViewById(R.id.btnStart);
		btnStart.setEnabled(false);
		btnStart.setOnClickListener(mClickListener);
		// add by smith
		handler = new Handler();
		moduleName = (TextView) findViewById(R.id.activity_list_moduleName);
		listView = (ListView) findViewById(R.id.activity_tv_show);
		list = new ArrayList<String>();
		adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_single_choice, list);
		listView.setAdapter(adapter);
		bar = (ProgressBar) findViewById(R.id.activity_program);

		text = (TextView) findViewById(R.id.activity_tv_result);
		// end
	}

	private void initValue() {
		String format = getResources().getString(R.string.lbl_state);
		String state = getResources().getString(R.string.lbl_disconn);
		if (isConnected) {
			state = getResources().getString(R.string.lbl_conn);
			lblConn.setText(String.format(format, state));
		}
		lblConn.setText(String.format(format, state));

		int num = 0;
		String content = "";
		switch (type) {
		case 0:
			lblMaxTime.setVisibility(View.VISIBLE);
			lblMinTime.setVisibility(View.VISIBLE);
			// modify by smith
			// lblTimeZone.setVisibility(View.VISIBLE);
			// end
			lblStartTime.setVisibility(View.GONE);
			lblEndTime.setVisibility(View.GONE);
			lblLost.setVisibility(View.GONE);

			String max = getResources().getString(R.string.lbl_time_max);
			lblMaxTime.setText(String.format(max, num));
			String min = getResources().getString(R.string.lbl_time_min);
			lblMinTime.setText(String.format(min, num));
			String zone = getResources().getString(R.string.lbl_zone_content);

			// modify by smith
			// lblTimeZone.setText(String.format(zone, content));
			lblTimeZone.setVisibility(View.GONE);

			// end
			zoneContent = new StringBuilder();
			count = new long[number + 1];
			String item = getResources().getString(R.string.txt_zone);
			String value = "";
			for (int i = 0; i < number; i++) {
				count[i] = 0;
				value = String.format(item, i * range, (i + 1) * range, 0);
				zoneContent.append(value);
			}
			String over = getResources().getString(R.string.txt_over);
			zoneContent.append(String.format(over, 0));
			content = getResources().getString(R.string.lbl_zone_content);
			lblTimeZone.setText(String.format(content, zoneContent));
			break;
		case 1:
			lblMaxTime.setVisibility(View.GONE);
			lblMinTime.setVisibility(View.GONE);
			lblTimeZone.setVisibility(View.GONE);
			lblStartTime.setVisibility(View.VISIBLE);
			lblEndTime.setVisibility(View.VISIBLE);
			lblLost.setVisibility(View.VISIBLE);

			String strat = getResources().getString(R.string.lbl_time_start);
			lblStartTime.setText(String.format(strat, num));
			String end = getResources().getString(R.string.lbl_time_stop);
			lblEndTime.setText(String.format(end, num));
			String lost = getResources().getString(R.string.lbl_lost);
			lblLost.setText(String.format(lost, num));
			break;
		}
	}

	private void scanBLE() {
		if (mBTAdapter == null || !mBTAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		} else {
			mScanner = mBTAdapter.getBluetoothLeScanner();
			mSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
			mFilters = new ArrayList<ScanFilter>();
			scan(true);
		}

	}

	private void scan(boolean enable) {
		if (enable) {
			initDialog();

			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanner.stopScan(mScanCallback);
				}
			}, SCAN_PERIOD);

			// add by smith
			List<ScanFilter> filters = new ArrayList<ScanFilter>();
			ParcelUuid muuid = new ParcelUuid(SERVICE_UUID);
			filters.add(new ScanFilter.Builder().setServiceUuid(muuid).build());
			mScanner.startScan(filters, mSettings, mScanCallback);
			// end
		} else {
			builder.dismiss();
			mScanner.stopScan(mScanCallback);
		}

	}

	@SuppressLint("InflateParams")
	private void initDialog() {
		builder = new Dialog(this);
		builder.show();

		LayoutInflater inflater = LayoutInflater.from(this);
		View viewDialog = inflater.inflate(R.layout.dialog_ble_list, null);
		builder.setContentView(viewDialog);

		mBleDeviceListView = (ListView) viewDialog.findViewById(R.id.list);
		mDeviceAdapter = new DeviceAdapter(this);
		mBleDeviceListView.setAdapter(mDeviceAdapter);

		mBleDeviceListView.setOnItemClickListener(itemClickListener);
	}

	private void refreshConnState(boolean result) {

		Log.i(TAG, "refreshConnState");

		isConnected = result;
		String format = getResources().getString(R.string.lbl_state);
		String state = getResources().getString(R.string.lbl_conn);
		btnStart.setEnabled(result);
		lblConn.setText(String.format(format, state));
		if (!result) {
			state = getResources().getString(R.string.lbl_disconn);
			lblConn.setText(String.format(format, state));
			takeScreenShot();
		}
	}

	private void discoverServices() {
		Log.i(TAG, "discoverServices");
		BluetoothGattService service = mGatt.getService(SERVICE_UUID);
		if (service != null) {
			Log.i(TAG, "discoverServices" + "....." + "service not null");
			BluetoothGattCharacteristic notifyCharac = service.getCharacteristic(CHARA_NOTIFY);
			mWrite = service.getCharacteristic(CHARA_WRITE);
			if (notifyCharac != null && mWrite != null) {
				mWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
				mGatt.setCharacteristicNotification(notifyCharac, true);

				byte[] value = configCmd(1, REQ_STATUS, null);
				sendCmd(value);
			}
		}
	}

	private void notification(byte[] data, String address) {
		int node = (int) data[1];
		int contentLen = (int) data[6];
		if (contentLen == 0)
			return;
		byte[] rspType = { data[4], data[5] };
		byte[] content = new byte[contentLen];
		for (int i = 0; i < contentLen; i++) {
			content[i] = data[7 + i];
		}

		if ((Arrays.equals(rspType, RSP_INTERVAL)) && flag == 0) {
			double value = ((content[0] & 0xFF) * 16 * 16 + content[1]) * 1.25;
			DecimalFormat df = new DecimalFormat("#.00");
			interval = df.format(value);

			byte[] cmd = null;
			switch (type) {
			case 0:
				cmd = configCmd(node, REQ_COUNT, null);
				break;
			case 1:
				cmd = configCmd(node, REQ_AUTO, SET_COUNT);
				break;
			}
			mDataList.add(cmd);
		} else if ((Arrays.equals(rspType, RSP_STATUS))) {

			if (flag == 0) {
				String result = HexDump.toHexString(content);
				Log.i(TAG, "Result:" + result);
				if (content[0] == 0x01) {
					Log.i(TAG, "inter bsl");
					byte[] value = configCmd(node, REQ_ENTER_BSL, null);
					Log.i(TAG, "B=enterBSL==" + HexDump.toHexString(value) + "\n");
					sendCmd(value);
				} else if (content[0] == 0x00) {
					// REQ UUID
					Log.i(TAG, "request uuid");
					byte[] value = configCmd(node, REQ_UUID, null);
					Log.i(TAG, "C=Get uuid=" + HexDump.toHexString(value));
					sendCmd(value);
				} else {

					Log.i(TAG, "RSP_STATUS  error");
				}
			} else if (flag == 1) {
				if (content[0] == 0x00) {
					// bsl
					Log.i(TAG, "bsl-------------------------------------uuid");
					byte[] value = configCmd(modulenode, REQ_UUID, null);
					Log.i(TAG, "Module=request uuid =" + HexDump.toHexString(value));
					sendCmd(value);
					flag = 1;
				} else if (content[0] == 0x01) {
					// app
					Log.i(TAG, "app-------------------------------------bsl");
					byte[] value = configCmd(modulenode, REQ_ENTER_BSL, null);
					Log.i(TAG, "Module=REQ_ENTER_BSLd =" + HexDump.toHexString(value));
					sendCmd(value);
				} else {
					Log.i(TAG, "error");
				}
			} else if (flag == 4) {
				new Thread(new Runnable() {

					@SuppressLint("DefaultLocale")
					@Override
					public void run() {
						String str = getFromAssets("Product.txt");
						try {
							JSONObject jsonObject = new JSONObject(str);
							modulenum = jsonObject.getInt(muid.toLowerCase());
							caseUpdate(modulenum + ".txt");
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}).start();
				if (content[0] == 0x00) {
					// bsl module
					byte[] value = configCmd(modulenode, REQ_EAPP, null);
					Log.i(TAG, "4444Module=REQ_EAPP=" + HexDump.toHexString(value));
					sendCmd(value);
				} else if (content[0] == 0x01) {
					// app module
					byte[] value = configCmd(modulenode, REQ_ENTER_BSL, null);
					Log.i(TAG,
							"4444Module=REQ_ENTER_BSL=" + "...." + modulenode + "......" + HexDump.toHexString(value));
					sendCmd(value);
				}
			} else if (flag == 5) {
				if (content[0] == 0x00) {
					// bsl
					byte[] value = configCmd(modulenode, REQ_UUID, null);
					Log.i(TAG, "55Module=REQ_uuid=" + HexDump.toHexString(value));
					sendCmd(value);
				} else if (content[0] == 0x01) {
					// app
					byte[] value = configCmd(modulenode, REQ_ENTER_BSL, null);
					Log.i(TAG, "55Module=REQ_ENTER_BSL=" + HexDump.toHexString(value));
					sendCmd(value);
				}
			}
			Log.i(TAG, HexDump.toHexString(content));

		} else if ((Arrays.equals(rspType, RSP_ENTER_BSL))) {
			if (flag == 0) {
				Log.i(TAG, "enter BSL Success");
				// REQ UUID
				byte[] value = configCmd(node, REQ_UUID, null);
				Log.i(TAG, "=REQ UUID=" + HexDump.toHexString(value));
				sendCmd(value);
			} else if (flag == 1) {
				Log.i(TAG, "144444444444Module===== inter bsl====" + "........................." + "success");
				byte[] value = configCmd(modulenode, REQ_UUID, null);
				Log.i(TAG, "1555555555555Module=request uuid =" + HexDump.toHexString(value));
				sendCmd(value);
			} else if (flag == 5) {
				byte[] value = configCmd(modulenode, REQ_UUID, null);
				Log.i(TAG, "5Module=request uuid =" + HexDump.toHexString(value));
				sendCmd(value);
			} else {
				Log.i(TAG, "4module RSP_ENTER_BSL success");
				byte[] value = configCmd(modulenode, REQ_EAPP, null);
				Log.i(TAG, "4444Module=REQ_EAPP=" + HexDump.toHexString(value));
				sendCmd(value);
			}

		} else if ((Arrays.equals(rspType, RSP_UUID))) {
			if (flag == 0) {
				byte[] value = configCmd(node, REQ_BSLINFORMATIOM, null);
				Log.i(TAG, "=REQ_BSLINFORMATIOM=" + HexDump.toHexString(value));
				sendCmd(value);
			} else if (flag == 1) {

				Log.i(TAG, "16666666666Module rsponse  uuid  " + "........" + "success");
				// 将uuid存入 hexdump供下文使用
				byte[] value = configCmd(modulenode, REQ_BSLINFORMATIOM, null);
				Log.i(TAG, "177777777Module=REQ_BSLINFORMATIOM=" + HexDump.toHexString(value));
				sendCmd(value);
				String uuid = HexDump.toHexString(content);
				muid = uuid;
				new Thread(new Runnable() {

					@SuppressLint("DefaultLocale")
					@Override
					public void run() {
						String str = getFromAssets("Product.txt");
						try {
							JSONObject jsonObject = new JSONObject(str);
							modulenum = jsonObject.getInt(muid.toLowerCase());
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}).start();
				new Thread(new Runnable() {
					@SuppressLint("DefaultLocale")
					@Override
					public void run() {
						String str = getFromAssets("Product.txt");
						try {
							JSONObject jsonObject = new JSONObject(str);
							modulenum = jsonObject.getInt(muid.toLowerCase());
							caseUpdate(modulenum + ".txt");

							Log.i(TAG, modulenum + "....." + "...smith");

						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}).start();

			} else if (flag == 5) {
				byte[] value = configCmd(modulenode, REQ_BSLINFORMATIOM, null);
				Log.i(TAG, "5MODULE=REQ_BSLINFORMATIOM=" + HexDump.toHexString(value));
				sendCmd(value);
			}
		} else if ((Arrays.equals(rspType, RSP_BSLINFORMATIOM))) {
			if (flag == 0) {
				// RSP enter app

				Log.i(TAG, "=RSP_BSLINFORMATIOM success =");
				if (content[3] == 0x01) {
					byte[] value = configCmd(node, REQ_ENTERAPP, null);
					Log.i(TAG, "=RSP_BSLINFORMATIOM success =" + HexDump.toHexString(value));
					sendCmd(value);
				} else if (content[3] == 0x00) {
					Log.i(TAG, "bsl information error ");
				}
			} else if (flag == 1) {

				Log.i(TAG, "188888888module RSP_BSLINFORMATIOM success");
				programflag = 1;
				if (content[3] == 0x00) {
					// 即使没有app也要先擦除
					byte[] value = configCmd(modulenode, REQ_EAPP, null);
					sendCmd(value);
					isapp = false;
				} else if (content[3] == 0x01) {
					Log.i(TAG, "1aaaaaaaa  Module=REQ_ENTERAPP=" + ".....content[3]=" + content[3]
							+ "          sssssssssss" + modulenode + "               " + (cyclecount++));
					byte[] value = configCmd(modulenode, REQ_ENTERAPP, null);
					sendCmd(value);
				}

			} else if (flag == 5) {
				if (content[3] == 0x00) {
					// meiyou app
					//
					message = new Message();
					message.what = 10;
					message.arg1 = 10001;
					mhandler.sendMessage(message);
				} else if (content[3] == 0x01) {
					byte[] value = configCmd(modulenode, REQ_ENTERAPP, null);
					sendCmd(value);
				}
			}

		} else if ((Arrays.equals(rspType, RSP_ENTERAPP))) {
			if (flag == 0) {
				byte[] value = configCmd(node, REQ_FRIMWAREINFORMATION, null);
				sendCmd(value);
			} else if (flag == 5) {
				byte[] value = configCmd(node, REQ_FRIMWAREINFORMATION, null);
				sendCmd(value);
			} else {
				Log.i(TAG, "1bbbbbbbbbb module REQ_ENTERAPP success");

				byte[] value = configCmd(modulenode, REQ_FRIMWAREINFORMATION, null);
				Log.i(TAG, "cModule=REQ_FRIMWAREINFORMATION=" + HexDump.toHexString(value));
				sendCmd(value);

			}

		} else if ((Arrays.equals(rspType, RSP_FRIMWAREINFORMATION))) {
			if (flag == 0) {
				Log.i(TAG, "REQ_FRIMWAREINFORMATION...  success");
				byte[] value = configCmd(node, REQ_NODE, null);
				sendCmd(value);
			} else if (flag == 1) {
				Log.i(TAG, "1ddddddddddddd  module RSP_FRIMWAREINFORMATION success" + "..................");
				flag = 4;
				byte[] value = configCmd(modulenode, REQ_STATUS, null);
				Log.i(TAG, "111111111Module=REQ_STATUS=" + HexDump.toHexString(value));
				sendCmd(value);
			} else if (flag == 5) {
				message = new Message();
				message.what = 10;
				message.arg1 = 10002;
				mhandler.sendMessage(message);
			}

		} else if (((Arrays.equals(rspType, RSP_NODE)))) {
			Log.i(TAG, "request node success");
			modulenode = mlog(content[0], 2) + 3;
			last.add(content[0]);
			bar.setProgress(0);
			countlength = 0;
			flag = 1;
			runcount = 1;
			progress = 0;
			if (last.size() == 2) {
				if (last.get(0).equals(last.get(1))) {
					last.clear();
				} else {
					last.remove(0);
				}
			}
			if (content[0] == 0) {
				write("module:  " + getModuleName(modulenum) + "  Time:" + getCurrentTime() + "\n" + "status:  " + "被拔出"
						+ "\n", getday() + ".txt");
				write("current progress:" + progress + "    ,flash degree:" + runcount + "\n", getday() + ".txt");
				write("==============================================" + "\n", getday() + ".txt");
				// 拔出或者没有被检测到
				Log.i(TAG, "last.size=====" + last.size() + ".....modulenode=======" + content[0]);
			} else {
				Log.i(TAG, "last.size=====" + last.size() + ".....modulenode=======" + content[0]);
				// 询问模式
				byte[] value = configCmd(modulenode, REQ_STATUS, null);
				Log.i(TAG, "Module=request uuid =" + HexDump.toHexString(value));
				sendCmd(value);
			}
		} else if (Arrays.equals(rspType, RSP_EAPP)) {

			if (!isapp) {
				// no app
				Log.i(TAG, "199999999 NO APP,begain programm " + "....." + spare.size());
				new Thread(new Runnable() {
					@SuppressLint("DefaultLocale")
					@Override
					public void run() {
						String str = getFromAssets("Product.txt");
						try {
							JSONObject jsonObject = new JSONObject(str);
							modulenum = jsonObject.getInt(muid.toLowerCase());
							caseUpdate(modulenum + ".txt");
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}).start();
				try {
					if (countlength < 100) {
						byte[] value = generateCmd(modulenode, REQ_PROGRAM, spare.get(countlength));
						countlength++;
						sendCmd(value);
						int progress = (countlength + 1) * 100 / spare.size();
						Log.i(TAG, "........." + (countlength + 1) + "========programflag==1============"
								+ "..............." + progress);
						bar.setProgress(progress);
					}
				} catch (Exception e) {
					mcount++;
					e.printStackTrace();
				}

			} else if (isapp) {
				try {
					programflag = 3;
					byte[] value = generateCmd(modulenode, REQ_PROGRAM, spare.get(countlength));
					sendCmd(value);
					int progress = (countlength + 1) * 100 / spare.size();
					bar.setProgress(progress);
				} catch (Exception e) {
					mcount++;
					write("module:  " + getModuleName(modulenum) + "  Time:" + getCurrentTime() + "\n" + "status:  "
							+ e.getMessage().toString() + "异常次数：" + mcount + "\n", getday() + ".txt");
					write("current progress:" + progress + "    ,flash degree:" + runcount + "\n", getday() + ".txt");
					write("==============================================" + "\n", getday() + ".txt");
					sendCmd(configCmd(1, REQ_STATUS, null));
					e.printStackTrace();
				}
			}
		} else if (Arrays.equals(rspType, RSP_PROGRAM)) {
			mmmtime = getmillisecond();
			Log.v(TAG, "success");
			if (programflag == 1) {
				programflag = 1;
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							try {
								if (countlength < 200) {
									if (progress == 1) {
										write("module:  " + getModuleName(modulenum) + "  Time:" + getCurrentTime()
												+ "\n" + "status:  " + "烧写开始" + "\n", getday() + ".txt");
										write("current progress:" + progress + "    ,flash degree:" + runcount + "\n",
												getday() + ".txt");
										write("==============================================" + "\n",
												getday() + ".txt");
									}
									byte[] value = generateCmd(modulenode, REQ_PROGRAM, spare.get(countlength));
									countlength++;
									sendCmd(value);
									progress = (countlength + 1) * 100 / spare.size();
									Log.i(TAG, "........." + (countlength + 1) + "...222222222222222222............"
											+ progress + ".............." + countlength);
									bar.setProgress(progress);
									message = new Message();
									message.what = 10;
									message.arg1 = progress;
									mhandler.sendMessage(message);
								}
							} catch (Exception e) {
								mcount++;
								write("module:  " + getModuleName(modulenum) + "  Time:" + getCurrentTime() + "\n"
										+ "status:  " + e.getMessage().toString() + "异常次数：" + mcount + "\n",
										getday() + ".txt");
								write("current progress:" + progress + "    ,flash degree:" + runcount + "\n",
										getday() + ".txt");
								write("==============================================" + "\n", getday() + ".txt");
								sendCmd(configCmd(1, REQ_STATUS, null));
								e.printStackTrace();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						if (progress == 100) {
							write("module:  " + getModuleName(modulenum) + "  Time:" + getCurrentTime() + "\n"
									+ "status:  " + "完成烧写" + "\n", getday() + ".txt");
							write("current progress:" + progress + "    ,flash degree:" + runcount + "\n",
									getday() + ".txt");
							write("==============================================" + "\n", getday() + ".txt");

							byte[] value = configCmd(modulenode, REQ_STATUS, null);
							sendCmd(value);
							flag = 5;
						}
					}
				}).start();
			} else if (programflag == 3) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						programflag = 3;
						if (progress == 1) {
							write("module:  " + getModuleName(modulenum) + "  Time:" + getCurrentTime() + "\n"
									+ "status:  " + "  开始烧录  " + "\n", getday() + ".txt");
							write("current progress:" + progress + "    ,flash degree:" + runcount + "\n",
									getday() + ".txt");
							write("==============================================" + "\n", getday() + ".txt");
						}
						try {
							byte[] value = generateCmd(modulenode, REQ_PROGRAM, spare.get(countlength));
							sendCmd(value);
							int progress = (countlength + 1) * 100 / spare.size();
							Log.i(TAG, "........." + (countlength + 1) + ".++++++++++++++++.............." + progress);
							bar.setProgress(progress);
							message = new Message();
							message.what = 10;
							message.arg1 = progress;
							mhandler.sendMessage(message);
						} catch (Exception e) {
							mcount++;
							write("module:  " + getModuleName(modulenum) + "  Time:" + getCurrentTime() + "\n"
									+ "status:  " + e.getMessage().toString() + "\n", getday() + ".txt");
							write("current progress:" + progress + "    ,flash degree:" + runcount + "\n",
									getday() + ".txt");
							write("==============================================" + "\n", getday() + ".txt");
							sendCmd(configCmd(1, REQ_STATUS, null));
							e.printStackTrace();
						}
						if (progress == 100) {
							write("module:  " + getModuleName(modulenum) + "  Time:" + getCurrentTime() + "\n"
									+ "status:  " + "完成烧写" + "\n", getday() + ".txt");
							write("current progress:" + progress + "    ,flash degree:" + runcount + "\n",
									getday() + ".txt");
							write("==============================================" + "\n", getday() + ".txt");

							byte[] value = configCmd(modulenode, REQ_STATUS, null);
							Log.i(TAG, "Modulenode--------req_status");
							sendCmd(value);
							flag = 5;
						}
					}
				}).start();

			}
		}
	}

	public void onClick(View view) {
		Intent intent = new Intent(this, DetailActivity.class);
		startActivity(intent);
	}

	// 内容以追加的方式放到文件
	private void write(String conetent, String file_name) {
		try {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				File sdkFile = Environment.getExternalStorageDirectory();
				// File targetFile = new File(sdkFile.getCanonicalPath() + "/" +
				// ps.close();
				// file_name); } catch (FileNotFoundException e) {
				File targetFile = new File(sdkFile.getCanonicalPath() + "/" + "Module test" + "/" + file_name);
				File mfile = new File(sdkFile.getCanonicalPath() + "/" + "Module test" + "/");
				if (!mfile.exists()) {
					mfile.mkdir();
				}
				if (!targetFile.exists()) {
					targetFile.createNewFile();
				}
				RandomAccessFile raf = new RandomAccessFile(targetFile, "rw");
				raf.seek(targetFile.length());
				raf.write(conetent.getBytes());
				raf.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getFromAssets(String fileName) {
		String Result = "";
		try {
			InputStreamReader inputReader = new InputStreamReader(getResources().getAssets().open(fileName));
			BufferedReader bufReader = new BufferedReader(inputReader);
			String line = "";
			while ((line = bufReader.readLine()) != null)
				Result += line;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Result;
	}

	public String getTxt(Context context, String fileName) {
		String res = "";
		try {

			AssetManager assetManager = context.getAssets();
			BufferedReader br = new BufferedReader(new InputStreamReader(assetManager.open(fileName)));
			String temp = null;
			while ((temp = br.readLine()) != null) {
				res += temp;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	public void caseUpdate(String fileName) {
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		try {
			inputStreamReader = new InputStreamReader(getAssets().open(fileName));
			bufferedReader = new BufferedReader(inputStreamReader);
			for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
				spare.put(filecount, line);
				filecount++;
			}
			inputStreamReader.close();
			bufferedReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		filecount = 0;
	}

	// read file
	// calculate logarithm
	public static int mlog(double value, double base) {
		return (int) (Math.log(value) / Math.log(base));
	}

	public static String byteToBit(byte b) {
		return "" + (byte) ((b >> 7) & 0x1) + (byte) ((b >> 6) & 0x1) + (byte) ((b >> 5) & 0x1)
				+ (byte) ((b >> 4) & 0x1) + (byte) ((b >> 3) & 0x1) + (byte) ((b >> 2) & 0x1) + (byte) ((b >> 1) & 0x1)
				+ (byte) ((b >> 0) & 0x1);
	}

	private void refreshStartTime() {
		Log.v(TAG, "refreshStartTime");
		String format = getResources().getString(R.string.lbl_time_start);
		String time = getTime();
		lblStartTime.setText(String.format(format, time));
	}

	private void refreshEndTime() {
		Log.v(TAG, "refreshEndTime");
		String format = getResources().getString(R.string.lbl_time_stop);
		String time = getTime();
		lblEndTime.setText(String.format(format, time));
	}

	private void refreshInfo() {
		Log.v(TAG, "refreshInfo");
		String info = getResources().getString(R.string.lbl_info);
		lblInfo.setText(String.format(info, option, times, interval));
		lblInfo.setVisibility(View.VISIBLE);
	}

	private void restart() {
		Log.v(TAG, "restart");
		refreshConnState(true);
		times = 0;

		type = (type + 1) % 2;
		Log.e(TAG, "========" + type);
		switch (type) {
		case 0:
			option = getResources().getString(R.string.txt_send);
			break;
		case 1:
			option = getResources().getString(R.string.txt_receive);
			break;
		}
		initValue();
		byte[] data = configCmd(2, REQ_INTERVAL, null);
		mDataList.add(data);
	}

	private void result() {
		Log.v(TAG, "result");
		String lost = getResources().getString(R.string.lbl_lost);
		lblLost.setText(String.format(lost, radix - times));
		takeScreenShot();
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mHandler.sendEmptyMessage(WHAT_RESTART);
			}
		}, 10 * 1000);
	}

	private void sendCmd(byte[] value) {
		if (mGatt == null) {
			Log.e(TAG, "lost connection");
			return;
		}

		if (mWrite == null) {
			Log.e(TAG, "characteristic null");
			return;
		}
		value = CRC16.setCrcResult(value);
		mWrite.setValue(value);
		boolean result = mGatt.writeCharacteristic(mWrite);

		if (result) {
			hasResponse = false;
			lastData = value;
			Log.i(TAG, "0===" + HexDump.toHexString(value));
			if (mmmtime != 0 && progress < 100) {
				if (getmillisecond() - mmmtime > overtime) {
					write("module:  " + getModuleName(modulenum) + "  Time:" + getCurrentTime() + "\n" + "status:  "
							+ "超时" + "\n", getday() + ".txt");
					write("current progress:" + progress + "    ,flash degree:" + runcount + "\n", getday() + ".txt");
					write("==============================================" + "\n", getday() + ".txt");
					sendCmd(configCmd(1, REQ_STATUS, null));
				}
			}

		}
	}

	private byte[] configCmd(int node, byte[] cmd, byte[] params) {
		int length = 0;
		if (params != null) {
			length = params.length;
		}
		int size = length + 10;
		byte[] data = new byte[size];
		data[0] = (byte) 0x7E;
		data[1] = (byte) 0x00;
		data[2] = (byte) node;
		data[3] = (byte) (length + 3);
		data[4] = cmd[0];
		data[5] = cmd[1];
		data[6] = (byte) length;
		for (int j = 0; j < length; j++) {
			data[j + 7] = params[j];
		}
		data[size - 3] = (byte) 0xEE;
		data[size - 2] = (byte) 0xEE;
		data[size - 1] = (byte) 0x7E;
		return data;
	}

	// add by smith

	public static byte[] generateCmd(int node, byte[] cmd, String value) {
		byte[] content = value.getBytes();
		int length = content.length;
		int size = length + 10;
		byte[] data = new byte[size];
		data[0] = LT;
		data[1] = SENDER;
		data[2] = (byte) node;
		data[3] = (byte) (length + 3);
		data[4] = cmd[0];
		data[5] = cmd[1];
		data[6] = (byte) length;
		for (int j = 0; j < length; j++) {
			data[j + 7] = content[j];
		}
		data[size - 3] = CRC;
		data[size - 2] = CRC;
		data[size - 1] = LT;
		return data;
	}

	// end

	private boolean inCRC(byte[] data) {
		byte[] target = { data[data.length - 2], data[data.length - 3] };
		byte[] crc = CRC16.getCrcResult(data);

		if (Arrays.equals(crc, target)) {
			return true;
		} else {
			return false;
		}
	}

	private String getTime() {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ssSSS", Locale.CHINA);
		return format.format(new Date());
	}

	private void takeScreenShot() {
		Log.v(TAG, "takeScreenShot");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.CHINA);
		Date curDate = new Date();
		String name = "[" + option + "]" + dateFormat.format(curDate) + ".png";
		String path = SDPATH + name;

		View view = this.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap b1 = view.getDrawingCache();
		Rect frame = new Rect();
		view.getWindowVisibleDisplayFrame(frame);
		Bitmap bitmap = Bitmap.createBitmap(b1, 0, 0, view.getWidth(), view.getHeight());
		view.destroyDrawingCache();

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(path);
			if (null != fos) {
				bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
				fos.flush();
				fos.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void startTesting() {
		Log.v(TAG, "startTesting");
		mDataList = new ArrayList<byte[]>();

		btnStart.setVisibility(View.GONE);
		lblInfo.setVisibility(View.VISIBLE);

		byte[] data = configCmd(2, REQ_INTERVAL, null);
		mDataList.add(data);

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (isConnected) {
					if (hasResponse && mDataList.size() > 0) {
						lastData = mDataList.get(0);
						if (lastData != null) {
							sendCmd(lastData);
						}
					}
				}
			}
		}).start();
	}

	// ===================================================
	// ===================================================
	// ===================================================

	OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			startTesting();
		}
	};

	ScanCallback mScanCallback = new ScanCallback() {
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			BluetoothDevice device = result.getDevice();
			mDeviceAdapter.addDevice(device);
			mDeviceAdapter.notifyDataSetChanged();
		}

		@Override
		public void onBatchScanResults(List<ScanResult> results) {
		}

		@Override
		public void onScanFailed(int errorCode) {
			Log.e(TAG, "Error Code: " + errorCode);
		}
	};

	OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			BluetoothDevice device = mDeviceAdapter.getDevice(position);
			if (mGatt == null) {
				mGatt = device.connectGatt(MainActivity.this, false, mGattCallback);
				scan(false);
			}
		}
	};

	BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			super.onConnectionStateChange(gatt, status, newState);
			switch (newState) {
			case BluetoothProfile.STATE_CONNECTED:
				mHandler.sendEmptyMessage(WHAT_CONNECTED);
				gatt.discoverServices();
				break;
			case BluetoothProfile.STATE_DISCONNECTED:
				mHandler.sendEmptyMessage(WHAT_DISCONNECTED);
				break;
			default:
				Log.e(TAG, "STATE_OTHER");
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				discoverServices();
			} else {
				list.add("discovered fail");
				adapter.notifyDataSetChanged();
			}
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			byte[] data = characteristic.getValue();
			Log.w(TAG, "1===" + HexDump.toHexString(data));
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			byte[] data = characteristic.getValue();
			String address = gatt.getDevice().getAddress();
			if (data[0] == 0x7E && data[3] > 13) {
				partData = new byte[data.length];
				partData = data;
				unitSize = data[3];
				return;
			} else if (unitSize + 7 == 20 + data.length) {
				unityData = new byte[unitSize + 7];
				System.arraycopy(partData, 0, unityData, 0, partData.length);
				System.arraycopy(data, 0, unityData, partData.length, data.length);
				data = unityData;
				partData = null;
				unityData = null;
			}
			if (data.length < 10)
				return;
			if (inCRC(data)) {
				notification(data, address);
				Log.e(TAG, "N!!!" + HexDump.toHexString(data));
			}
		}
	};

	private String getModuleName(int num) {
		String modulename = null;
		switch (num) {
		case 10:
			modulename = "station";
			break;
		case 11:
			modulename = "LED";
			break;
		case 12:
			modulename = "TF card";
			break;
		case 13:
			modulename = "Battery";
			break;
		case 14:
			modulename = "H&T";
			break;
		case 15:
			modulename = "Hotkeys";
			break;
		case 16:
			modulename = "Backup";
			break;
		case 17:
			modulename = "Laser";
			break;
		case 18:
			modulename = "Speak";
			break;
		case 19:
			modulename = "USB";
			break;
		case 20:
			modulename = "Alcohol";
			break;
		case 21:
			modulename = "Air";
			break;
		case 22:
			modulename = "Dummy";
			break;
		default:
			break;
		}
		return modulename;
	}

	// get current millisecond
	private long getmillisecond() {
		Date date = new Date();
		long time = date.getTime();
		return time;
	}

	// get format current time
	@SuppressLint("SimpleDateFormat")
	private static String getCurrentTime() {
		long time = System.currentTimeMillis();
		Date date = new Date(time);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String mtime = dateFormat.format(date);
		return mtime;
	}

	private int getday() {
		Time time = new Time("GMT+8");
		time.setToNow();
		int day = time.monthDay;
		return day;
	}

	/**
	 * //read file ./data/data/package/files/down
	 * 
	 * @param fileName
	 * @return
	 */
	public String readFromAsset() {
		String content = null; // 结果字符串
		try {
			InputStream is = getApplicationContext().getAssets().open("Product.txt");
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			content = new String(buffer, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			Log.v(TAG, "HandlerLeak" + msg.what);
			int what = msg.what;
			switch (what) {
			case WHAT_CONNECTED:
				refreshConnState(true);
				break;
			case WHAT_DISCONNECTED:
				refreshConnState(false);
				break;
			case WHAT_INTERVAL:
				refreshInfo();
				break;
			case WHAT_TIME:
				refreshInfo();
				break;
			case WHAT_RESTART:
				restart();
				break;
			case WHAT_RESULT:
				result();
				break;
			case WHAT_TIME_START:
				refreshStartTime();
				break;
			case WHAT_TIME_END:
				refreshEndTime();
				break;
			case WHAT_SCREEN_SHOT:
				takeScreenShot();
				break;
			}
		};
	};

}
