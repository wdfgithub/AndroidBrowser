package com.example.mybrowser;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mybrowser.activity.BookActivity;
import com.example.mybrowser.activity.DownloadActivity;
import com.example.mybrowser.activity.RecentSiteActivity;
import com.example.mybrowser.activity.SearchActivity;
import com.example.mybrowser.activity.SettingActivity;
import com.example.mybrowser.activity.WebActivity;
import com.example.mybrowser.dbhelper.MyDBOpenHelper;


public class MainActivity extends AppCompatActivity {

    private long exitTime = 0;
    public final static String EXTRA_MESSAGE="com.example.mybrowser.MESSAGE";
    private SearchView sv;
    public Activity G_Activity=null;
    public ListView listView;
    private String[] titles;
    private String[] sites;
    private SQLiteDatabase db;
    private MyDBOpenHelper sql;
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        G_Activity=this;
        sv=findViewById(R.id.Main_SearchView);
        sv.setSubmitButtonEnabled(true);
        sv.setQueryHint("使用百度搜索");
        buttonlisten bl = new buttonlisten();
        findViewById(R.id.imageButton1).setOnClickListener(bl);
        findViewById(R.id.imageButton2).setOnClickListener(bl);
        findViewById(R.id.imageButton3).setOnClickListener(bl);
        findViewById(R.id.imageButton4).setOnClickListener(bl);
        findViewById(R.id.imageButton5).setOnClickListener(bl);
        findViewById(R.id.imageButton19).setOnClickListener(bl);
        findViewById(R.id.imageButton16).setOnClickListener(bl);
        findViewById(R.id.imageButton17).setOnClickListener(bl);
        findViewById(R.id.imageButton18).setOnClickListener(bl);
        findViewById(R.id.imageButton_fold).setOnClickListener(bl);
        sv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(G_Activity, SearchActivity.class);
                startActivity(intent);
            }
        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        verifyStoragePermissions(G_Activity);
        sql = new MyDBOpenHelper(this, "mysqldb.db", null, 1);
        db=sql.getWritableDatabase();

        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        Cursor cursor = db.query("book", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndex("title"));
                sb1.append(title+'\n');
                String site = cursor.getString(cursor.getColumnIndex("site"));
                sb2.append(site+'\n');
            } while (cursor.moveToNext());
        }
        cursor.close();

        titles=sb1.toString().split("\n");
        sites=sb2.toString().split("\n");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, titles);
        listView =findViewById(R.id.ListView_Main_fav);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openWebActivity(sites[position]);
            }
        });
    }
    public void openWebActivity(String str){
        Intent intent=new Intent(this , WebActivity.class);
        intent.putExtra(EXTRA_MESSAGE,str);
        startActivity(intent);
    }
    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }
    class buttonlisten implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if(v.getId()==R.id.imageButton1){
                openWebActivity("");
                Intent intent=new Intent(G_Activity , BookActivity.class);
                startActivity(intent);
            }else if(v.getId()==R.id.imageButton2){
                openWebActivity("");
                Intent intent=new Intent(G_Activity , RecentSiteActivity.class);
                startActivity(intent);
            }else if(v.getId()==R.id.imageButton3){
                Intent intent=new Intent(G_Activity , SettingActivity.class);
                startActivity(intent);
            }else if(v.getId()==R.id.imageButton4){
                Intent textIntent = new Intent(Intent.ACTION_SEND);
                textIntent.setType("text/plain");
                textIntent.putExtra(Intent.EXTRA_TEXT, "把这个好用的浏览器分享给你");
                startActivity(Intent.createChooser(textIntent, "分享"));
            }else if(v.getId()==R.id.imageButton5){
                Toast.makeText(G_Activity,"SZU2016150060\n826337195@qq.com\n2018y6m27d",Toast.LENGTH_SHORT).show();
            }else if(v.getId()==R.id.imageButton19){
                openWebActivity("https://www.baidu.com/");
            }else if(v.getId()==R.id.imageButton16){
                openWebActivity("https://www.apple.com/cn/");
            }else if(v.getId()==R.id.imageButton17){
                openWebActivity("https://www.google.com.hk/");
            }else if(v.getId()==R.id.imageButton18){
                openWebActivity("https://www.yahoo.com/");
            }else if(v.getId()==R.id.imageButton_fold){
                Intent intent=new Intent(G_Activity , DownloadActivity.class);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onRestart() {
        WebActivity.changehold("");
        super.onRestart();
    }
}