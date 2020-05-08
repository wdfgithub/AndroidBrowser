package com.example.mybrowser.activity;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mybrowser.R;
import com.example.mybrowser.dbhelper.MyDBOpenHelper;

public class SettingActivity extends AppCompatActivity  implements AdapterView.OnItemClickListener{
    private String[] data = { "清除cache", "清除cookie", "清除历史纪录"};
    private SQLiteDatabase db;
    private MyDBOpenHelper sql;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        sql = new MyDBOpenHelper(this, "mysqldb.db", null, 1);
        db=sql.getWritableDatabase();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settinglayout);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, data);
        ListView listView =findViewById(R.id.listview4);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(position==0){
            deleteDatabase("WebViewCache.db");
            Toast.makeText(this,"Cache清除成功",Toast.LENGTH_SHORT).show();
        }else if(position==1){
            WebActivity.clearcookie();
            Toast.makeText(this,"Cookie清除成功",Toast.LENGTH_SHORT).show();
        }else if(position==2){
            db.delete("recent",null,null);
            db.delete("search",null,null);
            Toast.makeText(this,"历史纪录清除成功",Toast.LENGTH_SHORT).show();
        }
    }
}
