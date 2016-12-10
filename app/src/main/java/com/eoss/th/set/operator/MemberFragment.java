package com.eoss.th.set.operator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import bean.Member;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MemberFragment extends Fragment {

	TextView accountText;
	
	TextView expiryText;

    EditText trueCode;
	
	Handler handler = new Handler();
	
	MainActivity mainActivity;
	
	Button reqPaymentButton;
	
	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);
		
		mainActivity = (MainActivity) activity;		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.member, container, false);

		accountText = (TextView) rootView.findViewById(R.id.accountText);
		
		expiryText = (TextView) rootView.findViewById(R.id.expiryText);

        trueCode = (EditText) rootView.findViewById(R.id.trueCode);

		reqPaymentButton = (Button) rootView.findViewById(R.id.requestOtherPaymentButton);
		
		reqPaymentButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

                final String code = trueCode.getText().toString();

                if (code.length()!=14) {

                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "True code must be 14 digits", Toast.LENGTH_LONG).show();
                        }

                    });
                    return;
                }

                char c;
                for (int i=0; i<14; i++) {
                    c = code.charAt(i);
                    if (Character.isDigit(c)==false) {
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "True code must be 14 digits", Toast.LENGTH_LONG).show();
                            }

                        });
                        return;

                    }
                }

				new Thread() {
					
					@Override
					public void run() {
						
						try {
							
							URL url = new URL("http://setoperator.appspot.com/requestPayment?email=" + URLEncoder.encode(Member.instance().getEmail(), "UTF-8") +  "&code=" + URLEncoder.encode(code, "UTF-8"));
							BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
							
							String line = br.readLine();
							
							if (line!=null && line.equals("done")) {
								
								handler.post(new Runnable() {

									@Override
									public void run() {
										Toast.makeText(getActivity(), "Thanks, We will update your account as soon as possible!", Toast.LENGTH_LONG).show();
									}
									
								});
								
							}
							
						} catch (Exception e) {
							e.printStackTrace();
						}
						
					}
					
				}.start();
				
			}
			
		});
		
		accountText.setText(Member.instance().getEmail());
				
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		
		checkAccountStatus();

	}

	@Override
	public void onPause() {
		super.onPause();
	}

	private void popup(String message) {
		
		AlertDialog.Builder altDialog= new AlertDialog.Builder(getActivity());
		altDialog.setMessage(message);
		altDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
		   
		   @Override
		   public void onClick(DialogInterface dialog, int which) {
			   dialog.dismiss();
		   }
		  });
		altDialog.show();						
		
	}
	
	private void checkAccountStatus() {
		new Thread() {
			public void run() {
				
				try {
					URL url = new URL("http://setoperator.appspot.com/member?email=" + Member.instance().getEmail());
					BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
					
					final String line = br.readLine();
					
					if (line==null) return;
					
					if (line.equals("invalid")) {
						
						handler.post(new Runnable() {

							@Override
							public void run() {
								
								//popup("Please set your device with a google account");
								Toast.makeText(getActivity(), "Please set your device with a google account!", Toast.LENGTH_LONG).show();			
								
							}
							
						});
						
					} else {
						
						if (line.equals("expired")) {
							
							handler.post(new Runnable() {

								@Override
								public void run() {
									expiryText.setTextColor(Color.RED);
									expiryText.setText("Expired");
								}
								
							});
							
						} else if (line.equals("notfound") == false) {
							handler.post(new Runnable() {

								@Override
								public void run() {
									expiryText.setTextColor(Color.GREEN);
									expiryText.setText(line);
								}
								
							});
						}
						
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}.start();
				
	}

}
