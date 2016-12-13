package com.th.eoss.setoperator;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.th.eoss.util.SET;

public class StockFilterDialog extends Dialog {


	public StockFilterDialog(final Context context, final SET.Filter filter, final FilterListener filterListener) {
		super(context, R.style.Dialog_Filter);

		setContentView(R.layout.filter);
		setTitle(filter.name);

		Spinner spinner = (Spinner) findViewById(R.id.spinnerOperators);	
		EditText editValue = (EditText) findViewById(R.id.editValue);

		Button applyButton = (Button) findViewById(R.id.buttonApply);
		applyButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View view) {
				Spinner spinner = (Spinner) findViewById(R.id.spinnerOperators);	
				EditText editValue = (EditText) findViewById(R.id.editValue);

				int orange = ContextCompat.getColor(context, android.R.color.holo_orange_light);
                int blue = ContextCompat.getColor(context, android.R.color.holo_blue_bright);

				if (filter.type==SET.Filter.TYPE) {

					filterListener.onValue(filter.name, "=", spinner.getSelectedItem().toString());
				}

				else if (filter.type==SET.Filter.VALUE) {

					filterListener.onValue(filter.name, spinner.getSelectedItem().toString(), editValue.getText().toString());
				}

				dismiss();
			}

		});
		Button removeButton = (Button) findViewById(R.id.buttonRemove);
		removeButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View view) {

				filterListener.onClearValue(filter.name);

				dismiss();

			}

		});

		if ( filter.type==SET.Filter.TYPE ) {

			String [] selects = context.getResources().getStringArray(R.array.stock_type);

			//Hide editValue
			editValue.setVisibility(View.INVISIBLE);

			ArrayAdapter<String> adapter = 
					new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, selects);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter);

			if (filter.value!=null) {
				for (int i=0;i<selects.length;i++)
					if (selects[i].equals(filter.value)) {
						spinner.setSelection(i);
						break;
					}			
			} 

		} else if ( filter.type==SET.Filter.VALUE ) {

			String [] selects;

			if (filter.name.equals("ROA %") || filter.name.equals("ROE %") || filter.name.equals("DVD %")) {
				selects = new String[]{ ">", "<"};
			} else {
				selects = new String[]{ "<", ">"};				
			}

			editValue.setText(filter.value);

			ArrayAdapter<String> adapter = 
					new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, selects);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter);

			if (filter.opt!=null) {
				for (int i=0;i <selects.length; i++) {

					if (filter.opt.equals(selects[i])) {
						spinner.setSelection(i);
						break;				
					}
				}

			}

		}
	}

}
