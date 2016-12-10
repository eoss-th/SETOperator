package com.th.eoss.setoperator;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;

public class StockFilterDialog extends Dialog {

	public static final int TYPE = 0;
	public static final int VALUE = 1;

	public StockFilterDialog(final Context context, final ToggleButton toggleButton, final FilterListener filterListener, String opt, String value) {
		super(context, R.style.Dialog_Filter);

		final String filterName = toggleButton.getTextOff().toString();
		final int type = filterName.equals("Type")?TYPE:VALUE;

		setContentView(R.layout.filter);
		setTitle(filterName);

		Spinner spinner = (Spinner) findViewById(R.id.spinnerOperators);	
		EditText editValue = (EditText) findViewById(R.id.editValue);

		Button applyButton = (Button) findViewById(R.id.buttonApply);
		applyButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View view) {
				Spinner spinner = (Spinner) findViewById(R.id.spinnerOperators);	
				EditText editValue = (EditText) findViewById(R.id.editValue);

				int orange = context.getResources().getColor(android.R.color.holo_orange_light);

				if (type==TYPE) {

					filterListener.onValue(filterName, "=", spinner.getSelectedItem().toString());
				}

				else if (type==VALUE) {

					filterListener.onValue(filterName, spinner.getSelectedItem().toString(), editValue.getText().toString());

					toggleButton.setTextOn(toggleButton.getTextOff()+"\n"
							+spinner.getSelectedItem().toString()
							+editValue.getText().toString());

				}

				toggleButton.setChecked(true);
				toggleButton.setTextColor(orange);
				dismiss();
			}

		});
		Button removeButton = (Button) findViewById(R.id.buttonRemove);
		removeButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View view) {

				filterListener.onClearValue(filterName);

				int white = Color.parseColor("#FFFFFF");
				toggleButton.setTextColor(white);
				toggleButton.setChecked(false);
				toggleButton.setTextOn(toggleButton.getTextOff());

				dismiss();

			}

		});

		if ( type==TYPE ) {

			String [] selects = context.getResources().getStringArray(R.array.stock_type);

			//Hide editValue
			editValue.setVisibility(View.INVISIBLE);

			ArrayAdapter<String> adapter = 
					new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, selects);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter);

			if (value!=null) {
				for (int i=0;i<selects.length;i++)
					if (selects[i].equals(value)) {
						spinner.setSelection(i);
						break;
					}			
			} 

		} else if ( type==VALUE ) {

			String [] selects;

			if (filterName.equals("ROA") || filterName.equals("ROE") || filterName.equals("DVD")) {
				selects = new String[]{ ">", "<"};
			} else {
				selects = new String[]{ "<", ">"};				
			}

			editValue.setText(value);

			ArrayAdapter<String> adapter = 
					new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, selects);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter);

			if (opt!=null) {
				for (int i=0;i <selects.length; i++) {

					if (opt.equals(selects[i])) {
						spinner.setSelection(i);
						break;				
					}
				}

			}

		}
	}

}
