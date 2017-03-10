package com.th.eoss.setoperator;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends FragmentActivity {

	WatchFragment watchFragment = new WatchFragment();

    ValuesFragment valuesFragment = new ValuesFragment();

    XDFragment xdFragment = new XDFragment();

    HistoricalFragment historicalFragment = new HistoricalFragment();

    ViewPager viewPager;

    InterstitialAd mInterstitialAd;

    AdView mAdView;

    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		setContentView(R.layout.activity_main);

		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(new AppSectionsPagerAdapter(getSupportFragmentManager()));

		final TabLayout tabLayout = (TabLayout) findViewById(R.id.tab);
		tabLayout.setupWithViewPager(viewPager);

        int [] icons = {android.R.drawable.ic_menu_view, android.R.drawable.ic_menu_sort_by_size, android.R.drawable.ic_menu_today, android.R.drawable.ic_menu_zoom};

		TextView tab;
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
			tab = new TextView(getApplicationContext());
            tab.setCompoundDrawablesWithIntrinsicBounds(0, icons[i], 0, 0);
            tabLayout.getTabAt(i).setCustomView(tab);
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

			@Override
			public void onTabSelected(TabLayout.Tab tab) {

                if (mInterstitialAd.isLoaded() && tab.getPosition()==3) {
                    mInterstitialAd.show();
                } else {
                    viewPager.setCurrentItem(tab.getPosition());
                }
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

		MobileAds.initialize(getApplicationContext(), getString(R.string.app_ad_id));

        mInterstitialAd = new InterstitialAd(this);

        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                viewPager.setCurrentItem(tabLayout.getSelectedTabPosition());
            }
        });

        mAdView = (AdView) findViewById(R.id.adView);

        requestNewBanner();
        requestNewInterstitial();

    }

    private void requestNewBanner() {

        /*
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .addTestDevice("1FC15A5092E4F30DF707EB2806644EB6")  // An example device ID
                .build();
        */

        AdRequest adRequest = new AdRequest.Builder().build();

        mAdView.loadAd(adRequest);
    }

    private void requestNewInterstitial() {

        /*
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .addTestDevice("1FC15A5092E4F30DF707EB2806644EB6")  // An example device ID
                .build();
        */

        AdRequest adRequest = new AdRequest.Builder().build();

        mInterstitialAd.loadAd(adRequest);
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

    void watch(String symbol) {
        viewPager.setCurrentItem(0);
        watchFragment.addSymbol(symbol);
    }

    void historical(String symbol) {

        viewPager.setCurrentItem(3);
        historicalFragment.load(symbol);
    }

    void toast(String text) {
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(this, text, duration);
        toast.show();
    }

}
