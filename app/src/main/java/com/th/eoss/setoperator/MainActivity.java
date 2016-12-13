package com.th.eoss.setoperator;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {

	static WatchFragment watchFragment;
	
	static ValuesFragment valuesFragment;
	
	static XDFragment xdFragment;

    static HistoricalFragment historicalFragment;

	static ViewPager viewPager;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		if (watchFragment ==null) {
			watchFragment = new WatchFragment();
		}
		
		if (valuesFragment ==null) {
			valuesFragment = new ValuesFragment();
		}
		
		if (xdFragment==null) {
			xdFragment = new XDFragment();
		}

        if (historicalFragment ==null) {
            historicalFragment = new HistoricalFragment();
        }

		setContentView(R.layout.activity_main);

        final EditText searchText = (EditText) findViewById(R.id.symbol);

        searchText.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchText.setFilters(new InputFilter[] {new InputFilter.AllCaps()});

        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String symbol = searchText.getText().toString().toUpperCase();
                    watchFragment.addSymbol(symbol);
                    searchText.setText("");
                }
                return false;
            }

        });

		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(new AppSectionsPagerAdapter(getSupportFragmentManager()));

		TabLayout tabLayout = (TabLayout) findViewById(R.id.tab);
		tabLayout.setupWithViewPager(viewPager);
		tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

			@Override
			public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getPosition()!=0) {
                    searchText.setVisibility(View.GONE);
                } else {
                    searchText.setVisibility(View.VISIBLE);
                    searchText.clearFocus();
                }

                viewPager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {

			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {

			}
		});

        Theme.register(this);
        SingleHandler.handler = new Handler();
	}

	public class AppSectionsPagerAdapter extends FragmentPagerAdapter {

		String [] titles = {"Watch", "Values", "XD", "Historical"};

		public AppSectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			    case 0: return watchFragment;
			    case 1: return valuesFragment;
			    case 2: return xdFragment;
				case 3: return historicalFragment;
			}
			return null;
		}

		@Override
		public int getCount() {
			return titles.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return titles[position];
		}
	}

}
