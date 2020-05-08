package com.example.mybrowser.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.mybrowser.MainActivity;
import com.example.mybrowser.R;
import com.example.mybrowser.dbhelper.MyDBOpenHelper;
import com.example.mybrowser.view.MyWebView;

public class WebActivity extends AppCompatActivity {
    private MyWebView wView;
    private Toolbar toptoolbar;
    private Toolbar bottomtoolbar;
    private Toolbar menutoolbar;
    private FloatingActionButton bt;
    private boolean flag=false;
    private SearchView sv;
    private WebSettings webSettings;
    private Activity G_Activity=null;
    public final static String EXTRA_MESSAGE="com.example.mybrowser.MESSAGE";
    private final static String APP_CACHE_DIRNAME = "/webcache";
    private static String holdurl="";
    private SQLiteDatabase db;
    private MyDBOpenHelper sql;
    private ProgressBar progressBar;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mywebview);

        G_Activity=this;

        toptoolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toptoolbar);

        menutoolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(menutoolbar);

        bottomtoolbar =  findViewById(R.id.toolbar4);
        setSupportActionBar(bottomtoolbar);


        wView = findViewById(R.id.wView);

        buttonlisten bl = new buttonlisten();
        bt=findViewById(R.id.floatingActionButton);
        findViewById(R.id.imageButton6).setOnClickListener(bl);
        findViewById(R.id.imageButton7).setOnClickListener(bl);
        findViewById(R.id.imageButton8).setOnClickListener(bl);
        findViewById(R.id.imageButton9).setOnClickListener(bl);
        findViewById(R.id.imageButton10).setOnClickListener(bl);
        findViewById(R.id.imageButton11).setOnClickListener(bl);
        findViewById(R.id.imageButton12).setOnClickListener(bl);
        findViewById(R.id.imageButton13).setOnClickListener(bl);
        findViewById(R.id.imageButton14).setOnClickListener(bl);
        findViewById(R.id.imageButton15).setOnClickListener(bl);
        findViewById(R.id.imageButton).setOnClickListener(bl);
        bt.setOnClickListener(bl);

        webSettings=wView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        String cacheDirPath = getFilesDir().getAbsolutePath() + APP_CACHE_DIRNAME;
        webSettings.setAppCachePath(cacheDirPath);
        webSettings.setAppCacheEnabled(true);

        sv=findViewById(R.id.searchview_2);
        sv.setIconifiedByDefault(false);
        sv.setSubmitButtonEnabled(true);

        sql = new MyDBOpenHelper(G_Activity, "mysqldb.db", null, 1);
        db=sql.getWritableDatabase();
        progressBar=findViewById(R.id.progressBar3);

        Intent intent=getIntent();
        String message=intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        if(message.equals("")){
            if(holdurl.equals("")){
                finish();
            }
            wView.loadUrl(holdurl);
        }else if(message.startsWith("http")){
            wView.loadUrl(message);
        }else{
            wView.loadUrl("https://m.baidu.com/s?word="+message);
        }

        wView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                changehold(url);
                toptoolbar.setVisibility(View.GONE);
                bottomtoolbar.setVisibility(View.GONE);
                bt.setVisibility(View.GONE);
                menutoolbar.setVisibility(View.GONE);
                flag=false;
                syncCookie(url);
                return false;
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon){
                Cursor cursor = db.query("recent", null, null, null, null, null, null);
                if (cursor.moveToFirst()) {
                    do {
                        String site = cursor.getString(cursor.getColumnIndex("site"));
                        if(site.equals(url)){
                            db.delete("recent","site=?",new String[]{url});
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();

                ContentValues values = new ContentValues();
                values.put("site", url);
                values.put("title",wView.getTitle());
                db.insert("recent", null, values);
                super.onPageStarted(view,url,favicon);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
            @Override
            public void onReceivedError(WebView view, int errorCode, String description,
                                        String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                wView.loadUrl("file:///android_asset/error.html");
                toptoolbar.setVisibility(View.VISIBLE);
                bottomtoolbar.setVisibility(View.VISIBLE);
            }
        });


        wView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                sv.setQueryHint(wView.getTitle());
            }
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // TODO 自动生成的方法存根

                if(newProgress==100){
                    progressBar.setVisibility(View.GONE);
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                }

            }
        });


        wView.setDownloadListener(new DownloadListener(){
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                        String mimetype, long contentLength) {

                Intent intent=new Intent(G_Activity , DownloadActivity.class);
                intent.putExtra(EXTRA_MESSAGE,url);
                startActivity(intent);
                alart("开始下载");
            }
        });


        wView.setOnScrollChangedCallback(new MyWebView.OnScrollChangedCallback() {
            @Override
            public void onScroll(int dx, int dy) {
                if (dy < 0) {
                    toptoolbar.setVisibility(View.VISIBLE);
                    bottomtoolbar.setVisibility(View.VISIBLE);
                    bt.setVisibility(View.VISIBLE);
                } else {
                    toptoolbar.setVisibility(View.GONE);
                    bottomtoolbar.setVisibility(View.GONE);
                    bt.setVisibility(View.GONE);
                    menutoolbar.setVisibility(View.GONE);
                    flag=false;
                }

            }
        });
        wView.setOnTouchListener(new View.OnTouchListener() {
            float xDown, yDown, xUp, yUp;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    xDown = event.getX();
                    yDown = event.getY();
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    xUp = event.getX();
                    yUp = event.getY();
                    if(xUp - xDown < -500){
                        if(wView.canGoForward()){
                            wView.goForward();
                        }
                    }else if(xUp - xDown > 500){
                        onBackPressed();
                    }
                }
                return false;
            }
        });

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                wView.loadUrl("https://m.baidu.com/s?word="+query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }
    private void alart(String s) {
        Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onBackPressed() {
        if (wView.canGoBack()) {
            wView.goBack();
        } else {
            super.onBackPressed();
        }
    }
    class buttonlisten implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if(v.getId()==R.id.floatingActionButton){
                wView.setScrollY(0);
                bt.setVisibility(View.GONE);
            }else if(v.getId()==R.id.imageButton6){
                onBackPressed();
            }else if(v.getId()==R.id.imageButton7){
                if(wView.canGoForward()){
                    wView.goForward();
                }
            }else if(v.getId()==R.id.imageButton8){
                if(flag){
                    flag=false;
                    menutoolbar.setVisibility(View.GONE);
                }else{
                    flag=true;
                    bt.setVisibility(View.GONE);
                    menutoolbar.setVisibility(View.VISIBLE);
                }
            }else if(v.getId()==R.id.imageButton9){
                Cursor cursor = db.query("book", null, null, null, null, null, null);
                if (cursor.moveToFirst()) {
                    do {
                        String site = cursor.getString(cursor.getColumnIndex("site"));
                        if(site.equals(wView.getUrl())){
                            db.delete("book","site=?",new String[]{wView.getUrl()});
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
                ContentValues values = new ContentValues();
                values.put("site",wView.getUrl());
                values.put("title",wView.getTitle());
                db.insert("book", null, values);
                alart("添加收藏成功");
            }else if(v.getId()==R.id.imageButton10){
                finish();
            }else if(v.getId()==R.id.imageButton11){
                Intent intent=new Intent(G_Activity , BookActivity.class);
                startActivity(intent);
                menutoolbar.setVisibility(View.GONE);
            }else if(v.getId()==R.id.imageButton12){
                Intent intent=new Intent(G_Activity , RecentSiteActivity.class);
                startActivity(intent);
                menutoolbar.setVisibility(View.GONE);
                flag=false;
            }else if(v.getId()==R.id.imageButton13){
                Intent intent=new Intent(G_Activity , SettingActivity.class);
                startActivity(intent);
                menutoolbar.setVisibility(View.GONE);
                flag=false;
            }else if(v.getId()==R.id.imageButton14){
                Intent textIntent = new Intent(Intent.ACTION_SEND);
                textIntent.setType("text/plain");
                textIntent.putExtra(Intent.EXTRA_TEXT, "我正在看"+wView.getUrl()+"这个网页，分享给你");
                startActivity(Intent.createChooser(textIntent, "分享"));
                menutoolbar.setVisibility(View.GONE);
            }else if(v.getId()==R.id.imageButton15){
                wView.reload();
                menutoolbar.setVisibility(View.GONE);
                flag=false;
            }else if(v.getId()==R.id.imageButton){
                Intent intent=new Intent(G_Activity , DownloadActivity.class);
                startActivity(intent);
                flag=false;
            }
        }
    }
    public static void changehold(String s){
        holdurl=s;
    }
    @Override
    protected void onRestart() {
        syncCookie(holdurl);
        if(holdurl.equals(""))
            finish();
        Cursor cursor = db.query("recent", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String site = cursor.getString(cursor.getColumnIndex("site"));
                if(site.equals(holdurl)){
                    db.delete("recent","site=?",new String[]{holdurl});
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put("site", holdurl);
        values.put("title",wView.getTitle());
        db.insert("recent", null, values);
        wView.loadUrl(holdurl);
        super.onRestart();
    }
    public void syncCookie(String url) {

        CookieSyncManager.createInstance(WebActivity.this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        String CookieStr = cookieManager.getCookie(url);
        cookieManager.setCookie(url, CookieStr);
        CookieSyncManager.getInstance().sync();
    }
    public static void clearcookie(){
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeSessionCookie();
        cookieManager.removeAllCookie();
    }
}
