/*
 * Performance Control - An Android CPU Control application Copyright (C) 2012
 * James Roberts
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.brewcrewfoo.performance.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.brewcrewfoo.performance.R;
import com.brewcrewfoo.performance.fragments.*;
import com.brewcrewfoo.performance.util.ActivityThemeChangeInterface;
import com.brewcrewfoo.performance.util.Constants;
import com.brewcrewfoo.performance.util.Helpers;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements Constants,ActivityThemeChangeInterface {

    SharedPreferences mPreferences;
    PagerTabStrip mPagerTabStrip;
    ViewPager mViewPager;
    private boolean mIsLightTheme;
    public static Boolean thide=false;
    public static String mCurGovernor=null;
    public static String mCurIO=null;
    public static String mMaxFreqSetting=null;
    public static String mMinFreqSetting=null;
    public static String mCPUon=null;
    public static int curcpu=0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme();


        boolean canSu = Helpers.checkSu();
        boolean canBb = !Helpers.binExist("busybox").equals(NOT_FOUND);

        if (!canSu || !canBb) {
            final String failedTitle = getString(R.string.su_failed_title);
            final String message = getString(R.string.su_failed_su_or_busybox);
            suResultDialog(failedTitle, message);
        }
        else{
            setContentView(R.layout.activity_main);

            mViewPager = (ViewPager) findViewById(R.id.viewpager);
            TitleAdapter titleAdapter = new TitleAdapter(getFragmentManager());
            mViewPager.setAdapter(titleAdapter);
            mViewPager.setCurrentItem(0);

            mPagerTabStrip = (PagerTabStrip) findViewById(R.id.pagerTabStrip);
            mPagerTabStrip.setBackgroundColor(getResources().getColor(R.color.pc_light_gray));
            mPagerTabStrip.setTabIndicatorColor(getResources().getColor(R.color.pc_blue));
            mPagerTabStrip.setDrawFullUnderline(true);
            Intent i=getIntent();
            curcpu=i.getIntExtra("cpu",0);
        }

    }

    class TitleAdapter extends FragmentPagerAdapter {
        String titles[] = getTitles();
        private Fragment frags[] = new Fragment[titles.length];

        public TitleAdapter(FragmentManager fm) {
            super(fm);

            int i=0;
            int j=0;
            while (i<getResources().getStringArray(R.array.tabs).length) {
                boolean isvisible=mPreferences.getBoolean(getResources().getStringArray(R.array.tabs)[i],true);
                if(Helpers.is_Tab_available(i) && isvisible){
                    switch(i){
                        case 0:
                            frags[j] = new CPUSettings();
                            break;
                        case 1:
                            frags[j] = new CPUAdvanced();
                            break;
                        case 2:
                            frags[j] = new BatteryInfo();
                            break;
                        case 3:
                            frags[j] = new OOMSettings();
                            break;
                        case 4:
                            frags[j] = new VoltageControlSettings();
                            break;
                        case 5:
                            frags[j] = new Advanced();
                            break;
                        case 6:
                            frags[j] = new TimeInState();
                            break;
                        case 7:
                            frags[j] = new DiskInfo();
                            break;
                        case 8:
                            frags[j] = new Tools();
                            break;
                        case 9:
                            frags[j] = new CPUInfo();
                            break;
                    }
                    j++;
                }
                i++;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return frags[position];
        }

        @Override
        public int getCount() {
            return frags.length;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isThemeChanged() || thide) {
            thide=false;
            Helpers.restartPC(this);
        }
    }

    private void suResultDialog(String title, String message) {
        LayoutInflater factory = LayoutInflater.from(this);
        final View suResultDialog = factory.inflate(R.layout.su_dialog, null);
        TextView tv = (TextView) suResultDialog.findViewById(R.id.message);
        tv.setText(message);
        new ProgressDialog.Builder(this).setTitle(title).setView(suResultDialog)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).create().show();
    }

    /**
     * Get a list of titles for the tabstrip to display depending on if the
     * @return String[] containing titles
     */
    private String[] getTitles() {
        List<String> titleslist = new ArrayList<String>();
        int i=0;
        while (i<getResources().getStringArray(R.array.tabs).length) {
            boolean isvisible=mPreferences.getBoolean(getResources().getStringArray(R.array.tabs)[i],true);
            if(Helpers.is_Tab_available(i) && isvisible)
                titleslist.add(getResources().getStringArray(R.array.tabs)[i]);
            i++;
        }
        return titleslist.toArray(new String[titleslist.size()]);
    }

    @Override
    public boolean isThemeChanged() {
        final boolean is_light_theme = mPreferences.getBoolean(PREF_USE_LIGHT_THEME, false);
        return is_light_theme != mIsLightTheme;
    }

    @Override
    public void setTheme() {
        final boolean is_light_theme = mPreferences.getBoolean(PREF_USE_LIGHT_THEME, false);
        mIsLightTheme = mPreferences.getBoolean(PREF_USE_LIGHT_THEME, false);
        setTheme(is_light_theme ? R.style.Theme_Light : R.style.Theme_Dark);
    }
}

