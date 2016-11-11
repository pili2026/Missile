package tab.list;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;


import com.example.thunder.missile.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jeremy on 2016/9/18.
 */

public class FileTab_km extends AppCompatActivity {

    Uri uri;
    FileContentProvider test = new FileContentProvider();
    int a = 0, b = 0, c = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getIntent().setData(Uri.parse("content://tab.list.d2d/file_choice"));
        uri = getIntent().getData();

        setContentView(R.layout.tab_file);
        // Adding Toolbar to Main screen

        // Setting ViewPager for each Tabs
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        // Set Tabs inside Toolbar
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        // 初始化temp_file與file_choice
        test.del_table(uri);
    }

    // Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager) {
        FileTab_km.Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new FileTab_kmImage(), "IMAGE");
        viewPager.setAdapter(adapter);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
