package com.juma.helper;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.juma.helper.CustomDialog.Callback;
import com.juma.helper.CustomDialog.MessageCallback;
import com.juma.sdk.JumaDevice;
import com.juma.sdk.JumaDeviceCallback;
import com.juma.sdk.ScanHelper;
import com.juma.sdk.ScanHelper.ScanCallback;

public class MainActivity extends Activity implements OnClickListener, OnLongClickListener{

	public static final String ACTION_START_SCAN = "com.juma.helper.ACTION_START_SCAN";
	public static final String ACTION_STOP_SCAN = "com.juma.helper.ACTION_STOP_SCAN";
	public static final String ACTION_DEVICE_DISCOVERED = "com.juma.helper.ACTION_DEVICE_DISCOVERED";
	public static final String ACTION_CONNECT = "com.juma.helper.ACTION_CONNECT";
	public static final String ACTION_CONNECTED = "com.juma.helper.ACTION_CONNECTED";
	public static final String ACTION_DISCONNECT = "com.juma.helper.ACTION_DISCONNECT";
	public static final String ACTION_DISCONNECTED = "com.juma.helper.ACTION_DISCONNECTED";
	public static final String ACTION_SEND_MESSAGE = "com.juma.helper.ACTION_SEND_MESSAGE";
	public static final String ACTION_RECEIVER_MESSAGE = "com.juma.helper.ACTION_RECEIVER_MESSAGE";
	public static final String ACTION_ERROR = "com.juma.helper.ACTION_ERROR";

	public static final String NAME_STR = "name";
	public static final String UUID_STR = "uuid";
	public static final String RSSI_STR = "rssi";
	public static final String MESSAGE_STR = "message";
	public static final String ERROR_STR = "error";
	public static final String STATUS_STR = "status";
	public static final String ERROR_CODE_STR = "errorCode";
	public static final String TYPE_STR = "type";

	private ArrayAdapter<String> listAdapter = null;
	private TextView tvName = null,tvUuid = null;
	private View vStateLine = null;
	private ListView lvMessage = null;
	private GridView gvKeyboard = null;
	private CustomGridViewAdapter gvAdapter = null;
	private HashMap<Integer, byte[]> messages = null;
	private int[] keyIds = {R.string.key_1,R.string.key_2, R.string.key_3, R.string.key_4, R.string.key_5, R.string.key_6, 
			R.string.key_7, R.string.key_8, R.string.key_9, R.string.scan, R.string.connect, R.string.send};

	private ScanHelper scanHelper = null;
	private com.juma.sdk.JumaDevice jumaDevice = null;
	private HashMap<UUID, JumaDevice> jumaDevices = null;

	@SuppressLint("UseSparseArrays")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		System.out.println(" onCreate ");
		
		messages = new HashMap<Integer, byte[]>();

		initView();

		scanHelper = new ScanHelper(getApplicationContext(), scanCallback);

		jumaDevices = new HashMap<UUID, JumaDevice>();
	}

	private void initView(){

		tvName = (TextView) findViewById(R.id.tvName);
		tvUuid = (TextView) findViewById(R.id.tvUuid);

		vStateLine = findViewById(R.id.vStateLine);

		lvMessage = (ListView) findViewById(R.id.lvMessage);
		lvMessage.setDivider(null);

		gvKeyboard = (GridView) findViewById(R.id.gvKeyboard);

		listAdapter = new ArrayAdapter<String>(this, R.layout.message_list_item);
		lvMessage.setAdapter(listAdapter);

		gvAdapter = new CustomGridViewAdapter(getApplicationContext(), keyIds, this, this);
		gvKeyboard.setAdapter(gvAdapter);

	}
	
	protected void onDestroy() {
		
		super.onDestroy();
		
		if(jumaDevice != null && jumaDevice.isConnected())
			jumaDevice.disconnect();
		
		
	};

	private ScanCallback scanCallback = new ScanCallback() {

		@Override
		public void onScanStateChange(int newState) {
			// TODO Auto-generated method stub
			Intent intent = null;
			if(newState == ScanHelper.STATE_STOP_SCAN){
				intent = new Intent(MainActivity.ACTION_STOP_SCAN);
			}else if(newState == ScanHelper.STATE_START_SCAN){
				intent = new Intent(MainActivity.ACTION_START_SCAN);
			}
			sendBroadcast(MainActivity.this, intent);
		}

		@Override
		public void onDiscover(JumaDevice device, int rssi) {
			// TODO Auto-generated method stub
			if(!jumaDevices.containsKey(device.getUuid())){
				jumaDevices.put(device.getUuid(), device);
			}

			Intent intent = new Intent(MainActivity.ACTION_DEVICE_DISCOVERED);
			intent.putExtra(MainActivity.NAME_STR, device.getName());
			intent.putExtra(MainActivity.UUID_STR, device.getUuid().toString());
			intent.putExtra(MainActivity.RSSI_STR, rssi);
			sendBroadcast(MainActivity.this, intent);

		}
	};

	private JumaDeviceCallback deviceCallback = new JumaDeviceCallback() {
		public void onConnectionStateChange(int status, final int newState) {
			if(status == JumaDevice.SUCCESS){
				if(newState == JumaDevice.STATE_CONNECTED){
					runOnUiThread(new Runnable() {

						@Override
						public void run() {

							String currentDate = getCurrentData(getApplicationContext());

							StringBuffer sb = new StringBuffer();
							sb.append("[");
							sb.append(currentDate);

							vStateLine.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.green));

							gvAdapter.getItem(R.string.scan).setEnabled(false);
							keyIds[10] = R.string.disconnect;
							gvAdapter.notifyDataSetChanged();

							sb.append("] : Device connected : ");

							sb.append("\nname : ");
							sb.append(jumaDevice.getName());
							sb.append("\nuuid : ");
							sb.append(jumaDevice.getUuid());

							listAdapter.add(sb.toString());
							lvMessage.smoothScrollByOffset(listAdapter.getCount() - 1);

						}
					});
				}else if(newState == JumaDevice.STATE_DISCONNECTED){
					runOnUiThread(new Runnable() {

						@Override
						public void run() {

							String currentDate = getCurrentData(getApplicationContext());

							StringBuffer sb = new StringBuffer();
							sb.append("[");
							sb.append(currentDate);

							vStateLine.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.red));

							gvAdapter.getItem(R.string.scan).setEnabled(true);
							keyIds[10] = R.string.connect;
							gvAdapter.notifyDataSetChanged();

							sb.append("] : Device disconnected : ");

							sb.append("\nname : ");
							sb.append(jumaDevice.getName());
							sb.append("\nuuid : ");
							sb.append(jumaDevice.getUuid());

							listAdapter.add(sb.toString());
							lvMessage.smoothScrollByOffset(listAdapter.getCount() - 1);


						}
					});
				}
			}else if(status == JumaDevice.ERROR){
				runOnUiThread(new Runnable() {

					@Override
					public void run() {

						String currentDate = getCurrentData(getApplicationContext());

						StringBuffer sb = new StringBuffer();
						sb.append("[");
						sb.append(currentDate);

						if(newState == JumaDevice.STATE_CONNECTED){
							sb.append("] : Device connect is error : ");
						}else if(newState == JumaDevice.STATE_DISCONNECTED){
							sb.append("] : Device disconnected : ");
						}
						vStateLine.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.red));
						gvAdapter.getItem(R.string.scan).setEnabled(true);
						keyIds[10] = R.string.connect;

						gvAdapter.notifyDataSetChanged();

						sb.append("\nname : ");
						sb.append(jumaDevice.getName());
						sb.append("\nuuid : ");
						sb.append(jumaDevice.getUuid());

						listAdapter.add(sb.toString());
						lvMessage.smoothScrollByOffset(listAdapter.getCount() - 1);

					}
				});
			}
		};
		 
		@Override
		public void onReceive(final byte type, final byte[] message) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {

					String currentDate = getCurrentData(getApplicationContext());
					StringBuffer sb = new StringBuffer();
					sb.append("[");
					sb.append(currentDate);
					sb.append("] : Receiver message : ");	
					sb.append("\nType : "+byteToHex(new byte[]{type}));
					sb.append("\nMessage : "+byteToHex(message));

					listAdapter.add(sb.toString());
					lvMessage.smoothScrollByOffset(listAdapter.getCount() - 1);

				}
			});
		}

	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.string.key_1:
			if(jumaDevice != null && jumaDevice.isConnected())
				sendMessage((byte) 0x00, messages.get(v.getId()));
			break;
		case R.string.key_2:
			if(jumaDevice != null && jumaDevice.isConnected())
				sendMessage((byte) 0x01, messages.get(v.getId()));
			break;
		case R.string.key_3:
			if(jumaDevice != null && jumaDevice.isConnected())
				sendMessage((byte) 0x02, messages.get(v.getId()));
			break;
		case R.string.key_4:
			if(jumaDevice != null && jumaDevice.isConnected())
				sendMessage((byte) 0x03, messages.get(v.getId()));
			break;
		case R.string.key_5:
			if(jumaDevice != null && jumaDevice.isConnected())
				sendMessage((byte) 0x04, messages.get(v.getId()));
			break;
		case R.string.key_6:
			if(jumaDevice != null && jumaDevice.isConnected())
				sendMessage((byte) 0x05, messages.get(v.getId()));
			break;
		case R.string.key_7:
			if(jumaDevice != null && jumaDevice.isConnected())
				sendMessage((byte) 0x06, messages.get(v.getId()));
			break;
		case R.string.key_8:
			if(jumaDevice != null && jumaDevice.isConnected())
				sendMessage((byte) 0x07, messages.get(v.getId()));
			break;
		case R.string.key_9:
			if(jumaDevice != null && jumaDevice.isConnected())
				sendMessage((byte) 0x08, messages.get(v.getId()));
			break;
		case R.string.scan:

			final CustomDialog scanDialog = new CustomDialog(MainActivity.this, CustomDialog.DIALOG_TYPE_SCAN);
			scanDialog.setScanCallback(new Callback() {

				@Override
				public void onName(String name) {

					jumaDevices.clear();
					scanHelper.startScan(name);
				}

				@Override
				public void onDevice(final UUID uuid, final String name) {

					scanHelper.stopScan();

					jumaDevice = jumaDevices.get(uuid);

					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							tvName.setText(name);
							tvUuid.setText(uuid.toString());	
						}
					});

				}
			});

			scanDialog.setNegativeButton(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					scanDialog.dismiss();

					scanHelper.stopScan();

				}
			});

			scanDialog.show();

			break;
		case R.string.connect:

			if(gvAdapter.getItem(R.string.connect).getText().equals(MainActivity.this.getResources().getString(R.string.disconnect))){
				jumaDevice.disconnect();
				runOnUiThread(new Runnable() {

					@Override
					public void run() {

						String currentDate = getCurrentData(getApplication());

						StringBuffer sb = new StringBuffer();
						sb.append("[");
						sb.append(currentDate);
						sb.append("] : Disconnect device : ");
						sb.append("\nname : ");
						sb.append(jumaDevice.getName());
						sb.append("\nuuid : ");
						sb.append(jumaDevice.getUuid());

						listAdapter.add(sb.toString());
						lvMessage.smoothScrollByOffset(listAdapter.getCount() - 1);

					}
				});
			}else{
				if(jumaDevice != null){
					jumaDevice.connect(deviceCallback);

					runOnUiThread(new Runnable() {

						@Override
						public void run() {

							String currentDate = getCurrentData(getApplicationContext());
							StringBuffer sb = new StringBuffer();
							sb.append("[");
							sb.append(currentDate);
							sb.append("] : Connect device : ");
							sb.append("\nname : ");
							sb.append(jumaDevice.getName());
							sb.append("\nuuid : ");
							sb.append(jumaDevice.getUuid());

							listAdapter.add(sb.toString());
							lvMessage.smoothScrollByOffset(listAdapter.getCount() - 1);

						}
					});
				}
			}

			break;
		case R.string.send:
			if(jumaDevice != null && jumaDevice.isConnected()){
				CustomDialog sendDialog = new CustomDialog(MainActivity.this, CustomDialog.DIALOG_TYPE_SEND_MESSAGE);
				sendDialog.setMessageCallback(new MessageCallback() {

					@Override
					public void onMessage(byte[] message, int id) {

						if(message.length > 0)
							sendMessage((byte) 0x09, message);

					}
				});

				sendDialog.show();
			}

			break;
		}
	}

	@Override
	public boolean onLongClick(View v) {
		switch (v.getId()) {
		case R.string.key_1:
			editMessage(v.getId());
			break;
		case R.string.key_2:
			editMessage(v.getId());
			break;
		case R.string.key_3:
			editMessage(v.getId());
			break;
		case R.string.key_4:
			editMessage(v.getId());
			break;
		case R.string.key_5:
			editMessage(v.getId());
			break;
		case R.string.key_6:
			editMessage(v.getId());
			break;
		case R.string.key_7:
			editMessage(v.getId());
			break;
		case R.string.key_8:
			editMessage(v.getId());
			break;
		case R.string.key_9:
			editMessage(v.getId());
			break;
		}
		return true;
	}

	private void editMessage(int id){
		CustomDialog editDialog = new CustomDialog(MainActivity.this, CustomDialog.DIALOG_TYPE_EDIT_MESSAGE);

		editDialog.setId(id);

		editDialog.setMessageCallback(new MessageCallback() {

			@Override
			public void onMessage(final byte[] message, final int id) {

				messages.put(id, message);

			}
		});

		editDialog.show();

	}

	private void sendMessage(final byte type, final byte[] message){
		if(message != null){
			jumaDevice.send(type, message);
			runOnUiThread(new Runnable() {

				@Override
				public void run() {

					String currentDate = getCurrentData(getApplicationContext());

					StringBuffer sb = new StringBuffer();
					sb.append("[");
					sb.append(currentDate);
					sb.append("] : Send message : ");	
					sb.append("\nType : "+byteToHex(new byte[]{type}));
					sb.append("\nMessage : "+byteToHex(message));

					listAdapter.add(sb.toString());
					lvMessage.smoothScrollByOffset(listAdapter.getCount() - 1);

				}
			});
		}
	}

	public static void hideSoftInput(Activity activity){
		((InputMethodManager)activity.getApplication().getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(activity.getCurrentFocus()
				.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}

	private void sendBroadcast(Context context, Intent intent){
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	@SuppressLint("SimpleDateFormat")
	private static String getCurrentData(Context context){
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");    
		return sdf.format(new java.util.Date());
	}

	@SuppressLint("DefaultLocale")
	public static String byteToHex(byte[] b) {  
		StringBuffer hexString = new StringBuffer();  
		for (int i = 0; i < b.length; i++) {  
			String hex = Integer.toHexString(b[i] & 0xFF);  
			if (hex.length() == 1) {  
				hex = '0' + hex;  
			}  
			hexString.append(hex.toUpperCase());  
		}  
		return hexString.toString();  
	}

	@SuppressLint("UseValueOf")
	public static final byte[] hexToByte(String hex)throws IllegalArgumentException {
		if (hex.length() % 2 != 0) {
			throw new IllegalArgumentException();
		}
		char[] arr = hex.toCharArray();
		byte[] b = new byte[hex.length() / 2];
		for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++) {
			String swap = "" + arr[i++] + arr[i];
			int byteint = Integer.parseInt(swap, 16) & 0xFF;
			b[j] = new Integer(byteint).byteValue();
		}
		return b;
	}

	public static String bytetoAsciiString(byte[] bytearray) {
		String result = "";
		char temp;

		int length = bytearray.length;
		for (int i = 0; i < length; i++) {
			temp = (char) bytearray[i];
			result += temp;
		}
		return result;
	}

}
