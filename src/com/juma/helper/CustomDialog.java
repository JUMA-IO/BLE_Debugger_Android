package com.juma.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class CustomDialog extends Dialog implements android.view.View.OnClickListener,OnItemClickListener{

	public static final int DIALOG_TYPE_SCAN = 0;
	public static final int DIALOG_TYPE_EDIT_MESSAGE = 1;
	public static final int DIALOG_TYPE_SEND_MESSAGE = 2;

	private EditText etInput = null;

	private ListView lvDevice = null;

	private Button positiveButton = null, negativeButton = null;

	private Context context = null;

	private int type = -1;

	private MessageCallback messageCallback = null;

	private Callback scanCallback = null;

	private CustomListViewAdapter lvDeviceAdapter = null;

	private List<HashMap<String, Object>> deviceInfo = null;

	private android.view.View.OnClickListener positiveButtonClickListener = null;

	private android.view.View.OnClickListener negativeButtonClickListener = null;

	private int id = -1;


	public CustomDialog(Context context, int type) {
		super(context);

		this.context = context;

		this.type = type;

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		Window win = getWindow();  
		win.requestFeature(Window.FEATURE_NO_TITLE);
		if(type == DIALOG_TYPE_SCAN)
			this.setContentView(R.layout.dialog_scan);
		else if(type == DIALOG_TYPE_EDIT_MESSAGE || type == DIALOG_TYPE_SEND_MESSAGE)
			this.setContentView(R.layout.dialog_edit);

		initView();

	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		super.show();
	}

	@Override
	public void dismiss() {
		// TODO Auto-generated method stub
		super.dismiss();
	}

	private void initView(){

		if(type == DIALOG_TYPE_SCAN){
			etInput = (EditText) findViewById(R.id.etInputName);
			lvDevice = (ListView) findViewById(R.id.lvDevice);
			positiveButton = (Button) findViewById(R.id.btnStartScan);
			negativeButton = (Button) findViewById(R.id.btnCancelScan);

			deviceInfo = new ArrayList<HashMap<String,Object>>();

			lvDeviceAdapter = new CustomListViewAdapter(context, deviceInfo);

			lvDevice.setAdapter(lvDeviceAdapter);

			lvDevice.setOnItemClickListener(this);

		}else if(type == DIALOG_TYPE_EDIT_MESSAGE || type == DIALOG_TYPE_SEND_MESSAGE){
			etInput = (EditText) findViewById(R.id.etInputMessage);
			positiveButton = (Button) findViewById(R.id.btnOk);
			negativeButton = (Button) findViewById(R.id.btnCancel);
		}

		if(positiveButtonClickListener != null)
			positiveButton.setOnClickListener(positiveButtonClickListener);
		else
			positiveButton.setOnClickListener(this);

		if(negativeButtonClickListener != null)
			negativeButton.setOnClickListener(negativeButtonClickListener);
		else
			negativeButton.setOnClickListener(this);

	}

	public void setMessageCallback(MessageCallback messageCallback){

		this.messageCallback = messageCallback;

	}

	public void setScanCallback(Callback scanCallback){

		this.scanCallback = scanCallback;

	}

	public void setId(int id){

		this.id = id;

	}

	@Override
	protected void onStart() {
		super.onStart();
		LocalBroadcastManager.getInstance(context).registerReceiver(receiver, getIntentFilter());
	}

	private IntentFilter getIntentFilter(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(MainActivity.ACTION_DEVICE_DISCOVERED);
		return filter;
	}

	@Override
	protected void onStop() {
		super.onStop();
		LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
	}


	public void setPositiveButton(android.view.View.OnClickListener listener){

		positiveButtonClickListener = listener;

	}

	public void setNegativeButton(android.view.View.OnClickListener listener){

		negativeButtonClickListener = listener;

	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btnStartScan:

			String name = etInput.getText().toString();

			if(scanCallback != null)
				scanCallback.onName(name);

			etInput.setText("");

			break;
		case R.id.btnCancelScan:

			dismiss();

			break;
		case R.id.btnOk:

			if(messageCallback != null){
				String hexStr = etInput.getText().toString();
				if(hexStr.length() % 2 == 1){
					hexStr = hexStr + "0";
				}

				if(hexStr.length() > 0)
					messageCallback.onMessage(hexToByte(hexStr), id);

			}

			dismiss();

			break;
		case R.id.btnCancel:

			dismiss();

			break;
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

		HashMap<String, Object> map = (HashMap<String, Object>) lvDeviceAdapter.getItem(arg2);

		String name = (String) map.get(MainActivity.NAME_STR);

		UUID uuid = UUID.fromString((String)map.get(MainActivity.UUID_STR));

		if(scanCallback != null)
			scanCallback.onDevice(uuid, name);

		dismiss();

	}

	public interface MessageCallback{
		void onMessage(byte[] message, int id);
	}

	public interface Callback{
		void onName(String name);
		void onDevice(UUID uuid, String name);
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String uuid = intent.getStringExtra(MainActivity.UUID_STR);
			String name = intent.getStringExtra(MainActivity.NAME_STR);
			int rssi = intent.getIntExtra(MainActivity.RSSI_STR, 0);
			addDeviceInfo(name, uuid, rssi);

		}
	};

	private void addDeviceInfo(String name, String uuid, int rssi){

		if(deviceInfo != null && lvDeviceAdapter != null){
			HashMap<String , Object> map = new HashMap<String, Object>();
			map.put(MainActivity.NAME_STR, name);
			map.put(MainActivity.UUID_STR, uuid);
			map.put(MainActivity.RSSI_STR, rssi);

			deviceInfo.add(map);

			lvDeviceAdapter.notifyDataSetChanged();

		}

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

}
