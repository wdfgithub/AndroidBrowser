package com.example.mybrowser.activity;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.mybrowser.R;
import com.example.mybrowser.dbhelper.MyDBOpenHelper;

public class RecentSiteActivity extends AppCompatActivity{
    private String[] titles;
    private String[] sites;
    private SQLiteDatabase db;
    private MyDBOpenHelper sql;
    private Button bt;
    private AlertDialog Dialog = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recentsite);

        sql = new MyDBOpenHelper(this, "mysqldb.db", null, 1);
        db=sql.getWritableDatabase();
        bt=findViewById(R.id.button);

        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        Cursor cursor = db.query("recent", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndex("title"));
                sb1.insert(0,title+'\n');
                String site = cursor.getString(cursor.getColumnIndex("site"));
                sb2.insert(0,site+'\n');
            } while (cursor.moveToNext());
        }
        cursor.close();

        titles=sb1.toString().split("\n");
        sites=sb2.toString().split("\n");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, titles);
        ListView listView =findViewById(R.id.listview2);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WebActivity.changehold(sites[position]);
                finish();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                alert("提示","你想删除掉"+titles[position]+"这段历史纪录吗？",sites[position],titles[position]);
                return true;
            }
        });
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.delete("recent",null,null);
                recreate();
            }
        });
    }
    public void alert(String title, final String message,final String url,final String webtitle){

        AlertDialog.Builder dialogbuilder= new AlertDialog.Builder(this);
        dialogbuilder.setTitle(title);
        dialogbuilder.setMessage(message);
        dialogbuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                db.delete("recent","site=?",new String[]{url});
                db.delete("recent","title=?",new String[]{webtitle});
                Dialog.dismiss();
                recreate();

            }
        });
        dialogbuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Dialog.dismiss();

            }
        });
        Dialog=dialogbuilder.create();
        Dialog.show();

    }
}