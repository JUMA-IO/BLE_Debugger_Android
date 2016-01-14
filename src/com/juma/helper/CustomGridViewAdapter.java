package com.juma.helper;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

@SuppressLint("UseSparseArrays")
public class CustomGridViewAdapter extends BaseAdapter{

	private Context context = null;
	
	private OnClickListener clickListener = null;
	
	private  OnLongClickListener longClickListener = null;

	private int[] keyIds = null;

	private HashMap<Integer, Button> keys = null;
	
	public CustomGridViewAdapter(Context context, int[] keyIds, OnClickListener clickListener, OnLongClickListener longClickListener) {

		this.context = context;
		
		this.keyIds = keyIds;
		
		this.clickListener = clickListener;
		
		this.longClickListener = longClickListener;
		
		keys = new HashMap<Integer, Button>();
		
	}

	@Override
	public int getCount() {
		return keyIds.length;
	}

	@Override
	public Button getItem(int id) {
		return keys.get(id);
	}

	@Override
	public long getItemId(int id) {
		return id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		Button btnKey = null;

		if(convertView == null){
			btnKey = new Button(context);
			btnKey.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));  
			btnKey.setTextSize(16);
			btnKey.setOnClickListener(clickListener);
			btnKey.setOnLongClickListener(longClickListener);
			btnKey.setId(keyIds[position]);
			btnKey.setBackgroundResource(R.drawable.button_background);
			keys.put(keyIds[position], btnKey);
		}else {
			btnKey = (Button) convertView;
		}

		btnKey.setText(keyIds[position]);

		return btnKey;
	}


}
