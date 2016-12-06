package missile;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import com.example.thunder.missile.R;

import database.storage.messageProcess;
import login.mcs.LocUpdate;
import login.mcs.Login;
import login.mcs.Retrieve;
import tab.list.AttachParameter;
import tab.list.FileContentProvider.UserSchema;

/**
 * Created by thunder on 2016/9/14.
 */
public class MainFragment extends AppCompatActivity{

    DrawerLayout drawerLayout;
    ActionBar actionBar;
    public String selectItem = null;
    Toolbar toolbar;
    TextView name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle bundle = getIntent().getExtras();
        selectItem = bundle.getString("location");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        actionBar.setDisplayHomeAsUpEnabled(true);


        drawerLayout = (DrawerLayout) findViewById(R.id.navigation_drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (navigationView != null) {
            setupNavigationDrawerContent(navigationView);
        }

        setupNavigationDrawerContent(navigationView);

        //set fragment location
        setFragment(Integer.valueOf(selectItem));


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupNavigationDrawerContent(NavigationView navigationView) {

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.Inbox:
                                menuItem.setChecked(true);
                                setFragment(1);
                                drawerLayout.closeDrawer(GravityCompat.START);
                                return true;
                            case R.id.Notify:
                                ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                NetworkInfo info = CM.getActiveNetworkInfo();
                                menuItem.setChecked(true);
                                setFragment(0);
                                drawerLayout.closeDrawer(GravityCompat.START);
                                if (info == null || !info.isAvailable()) {
                                    Toast.makeText(MainFragment.this, "目前沒有網路唷!所以無法進入通知", Toast.LENGTH_LONG).show();
                                }else{
                                    new getcontent().execute();
                                }
                                return true;
                            case R.id.Send:
                                menuItem.setChecked(true);
                                setFragment(2);
                                drawerLayout.closeDrawer(GravityCompat.START);
                                return true;
                            case R.id.Edit:
                                menuItem.setChecked(true);
                                setFragment(3);
                                drawerLayout.closeDrawer(GravityCompat.START);
                                return true;
                            case R.id.Logout:
                                Intent intent = new Intent();
                                intent.setClass(MainFragment.this, LoginMissile.class);
                                startActivity(intent);
                        }
                        return true;
                    }
                });
    }

    public void setFragment(int position) {
        FragmentManager fragmentManager;
        FragmentTransaction fragmentTransaction;
        switch (position) {
            case 0:
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                NotifySystem notifySystem = new NotifySystem();
                fragmentTransaction.replace(R.id.fragment, notifySystem);
                fragmentTransaction.commit();
                break;
            case 1:
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                MissileFiringRoom missileFiringRoom = new MissileFiringRoom();
                fragmentTransaction.replace(R.id.fragment, missileFiringRoom);
                fragmentTransaction.commit();
                break;
            case 2:
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                FiringReadyRoom firingReadyRoom = new FiringReadyRoom();
                fragmentTransaction.replace(R.id.fragment, firingReadyRoom);
                fragmentTransaction.commit();
                break;
            case 3:
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                MissileSet missileSet = new MissileSet();
                fragmentTransaction.replace(R.id.fragment, missileSet);
                fragmentTransaction.commit();
                break;

        }
    }
    private class getcontent extends AsyncTask<Void, Void, Void> {

        ProgressDialog pdialog = null;

        @Override
        protected void onPreExecute() {
            // 開啟資料傳送dialog
            pdialog = ProgressDialog.show(MainFragment.this, "請稍候", "資料讀取中", true);
            pdialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String[] aliveIp;
            LocUpdate locup =new LocUpdate();
            aliveIp = locup.locationupdate(Login.latest_cookie, AttachParameter.getIPAddress(getApplicationContext()), AttachParameter.port);

            if(aliveIp[0]!=null){

                messageProcess MsgSave = new messageProcess();
                MsgSave.checkwlan(getContentResolver(), aliveIp[4]);
                updateContent(Uri.parse("content://tab.list.d2d/user_data"), "retrievable","content is null");
                updateContent(Uri.parse("content://tab.list.d2d/user_reply"), "reply","filename is null");
                MsgSave=null;
                pdialog.dismiss();
                // 要加上失誤的判斷


            }else{
                pdialog.dismiss();
            }
            return null;
        }
    }

    public void updateContent(Uri location, String mod,String condition) {
        int id_this;
        String where, content, token, tittle;
        String[] Form = {UserSchema._ID, UserSchema._MESSAGETOKEN };
        String[] reretrieve = new String[5];

        Cursor up_content = getContentResolver().query(location, Form, condition, null, null);
        if (up_content.getCount() > 0) {
            up_content.moveToFirst();
            Retrieve retreive =new Retrieve();
            for (int i = 0; i < up_content.getCount(); i++) {
                token = up_content.getString(1);
                reretrieve = retreive.retrieve_req(token, mod);
                if (reretrieve[0].equals("true")) {
                    if(retreive.retrieveFileCount.length > 3){
                        content = reretrieve[1].substring(reretrieve[1].indexOf("content=") + 8, reretrieve[1].indexOf("&file"));
                    }else{
                        content = reretrieve[1].substring(reretrieve[1].indexOf("content=") + 8, reretrieve[1].length()-1);
                    }
                    id_this = Integer.valueOf(up_content.getString(0));
                    ContentValues values = new ContentValues();
                    if (mod.equals("retrievable")) {
                        tittle = reretrieve[1].substring(reretrieve[1].indexOf("subject=") + 8, reretrieve[1].indexOf("&content="));
                        values.put(UserSchema._CONTENT, content);
                        values.put(UserSchema._TITTLE, tittle);
                        values.put(UserSchema._USESTATUS, "");
                        values.put(UserSchema._FILEPATH, "");
                    }else{
                        values.put(UserSchema._FILENAME, content);
                    }

                    where = UserSchema._ID + " = " + id_this;
                    getContentResolver().update(location, values, where, null);

                }
                up_content.moveToNext();
            }

        }
        up_content.close();
    }//end of updateContent
}
