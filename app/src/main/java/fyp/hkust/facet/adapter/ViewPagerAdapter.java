package fyp.hkust.facet.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ClementNg on 26/1/2017.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = "ViewPagerAdapter";
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        Log.d(TAG + " getItem(int position)", position + "");
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        Log.d(TAG + " getCount()", mFragmentList.size()+"");
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
        Log.d(TAG + " addFragment()", title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Log.d(TAG + " getPageTitle(int position)", position + "");
        return mFragmentTitleList.get(position);
    }

}
