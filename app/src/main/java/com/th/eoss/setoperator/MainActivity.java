package com.th.eoss.setoperator;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

public class MainActivity extends FragmentActivity /*implements ActionBar.TabListener*/ {

	AppSectionsPagerAdapter mAppSectionsPagerAdapter;
	
	static RealtimeFragment realtimeFragment;
	
	static BasicFragment basicFragment;
	
	static XDFragment xdFragment;
	
	static ActionBar actionBar;
	
	ViewPager mViewPager;

	TabLayout tabLayout;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		if (realtimeFragment==null) {
			realtimeFragment = new RealtimeFragment();
		}
		
		if (basicFragment==null) {
			basicFragment = new BasicFragment();
		}
		
		if (xdFragment==null) {
			xdFragment  = new XDFragment();
		}

		//TODO Remove this code on production!
		
		//setContentView(R.layout.activity_main);
		setContentView(R.layout.main);

		// Create the adapter that will return a fragment for each of the three primary sections
		// of the app.
		mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

		// Set up the action bar.
		//actionBar = getActionBar();

		// Specify that the Home/Up button should not be enabled, since there is no hierarchical
		// parent.
		//actionBar.setHomeButtonEnabled(false);
		
		// Specify that we will be displaying tabs in the action bar.
		//actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Set up the ViewPager, attaching the adapter and setting up a listener for when the
		// user swipes between sections.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mAppSectionsPagerAdapter);

		tabLayout = (TabLayout) findViewById(R.id.tab);
		tabLayout.setupWithViewPager(mViewPager);
		tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				mViewPager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {

			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {

			}
		});

		/*
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				
				// When swiping between different app sections, select the corresponding tab.
				// We can also use ActionBar.Tab#select() to do this if we have a reference to the
				// Tab.
								
				actionBar.setSelectedNavigationItem(position);
				
			}
		});*/


		// For each of the sections in the app, add a tab to the action bar.
		/*for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by the adapter.
			// Also specify this Activity object, which implements the TabListener interface, as the
			// listener for when this tab is selected.
			actionBar.addTab(
					actionBar.newTab()
					.setText(mAppSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}*/
				
	}
/*
	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());		
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}
*/
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
	 * sections of the app.
	 */
	public class AppSectionsPagerAdapter extends FragmentPagerAdapter {

		public AppSectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case 0: return realtimeFragment;
			case 1: return basicFragment; 
			case 2: return xdFragment;				
			}
			return null;
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {

			switch (position) {
			case 0: return "Watch";
			case 1: return "Values";
			case 2: return "XD";
			}
			return "";
		}
	}

}
