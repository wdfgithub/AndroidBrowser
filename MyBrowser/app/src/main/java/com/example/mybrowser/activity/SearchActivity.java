package com.example.mybrowser.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.mybrowser.R;
import com.example.mybrowser.dbhelper.MyDBOpenHelper;

public class SearchActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE="com.example.mybrowser.MESSAGE";
    private SearchView sv;
    public Activity G_Activity=null;
    private String[] data;
    private SQLiteDatabase db;
    private MyDBOpenHelper sql;
    private ListView listView;
    private Button bt1;
    private Button bt2;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        G_Activity = this;
        sv = findViewById(R.id.Search_View);
        sv.setSubmitButtonEnabled(true);
        sv.setIconifiedByDefault(false);
        sv.setQueryHint("使用百度搜索");
        listView=findViewById(R.id.searchlist);
        sql = new MyDBOpenHelper(this, "mysqldb.db", null, 1);
        db=sql.getWritableDatabase();
        updateSuggession("");
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                Cursor cursor = db.query("search", null, null, null, null, null, null);
                if (cursor.moveToFirst()) {
                    do {
                        String text = cursor.getString(cursor.getColumnIndex("item"));
                        if(text.equals(query)){
                            db.delete("search","item=?",new String[]{text});
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
                ContentValues values = new ContentValues();
                values.put("item", query);
                db.insert("search", null, values);
                openWebActivity(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                updateSuggession(newText);
                return false;
            }
        });
        bt1=findViewById(R.id.button2);
        bt2=findViewById(R.id.button3);
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.delete("search",null,null);
                updateSuggession("");
            }
        });
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void updateSuggession(String newText){
        String[] items ;
        items = getItems(G_Activity, newText);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                G_Activity, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openWebActivity(parent.getItemAtPosition(position).toString());
            }
        });
    }
    public void openWebActivity(String str){
        Intent intent=new Intent(this , WebActivity.class);
        intent.putExtra(EXTRA_MESSAGE,str);
        startActivity(intent);
    }
    public String[] getItems(Context context, String key) {
        StringBuilder sb = new StringBuilder();
        Cursor cursor = db.query("search", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {

                String items = cursor.getString(cursor.getColumnIndex("item"));
                if(items.contains(key))
                    sb.append(items+'\n');
            } while (cursor.moveToNext());
        }
        cursor.close();

        data=sb.toString().split("\n");

        return data;
    }
}
