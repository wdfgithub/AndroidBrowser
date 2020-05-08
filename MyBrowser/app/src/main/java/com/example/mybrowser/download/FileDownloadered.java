package com.example.mybrowser.download;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.util.Log;


public class FileDownloadered {

    private static final String TAG = "文件下载类";
    private static final int RESPONSEOK = 200;
    private FileService fileService;
    private boolean exited;
    private Context context;
    private int downloadedSize = 0;
    private int fileSize = 0;
    private DownloadThread[] threads;
    private File saveFile;
    private Map<Integer, Integer> data = new ConcurrentHashMap<Integer, Integer>();
    private int block;
    private String downloadUrl;


    public void exit() { this.exited = true; }

    public boolean getExited()
    {
        return this.exited;
    }

    public int getFileSize()
    {
        return fileSize;
    }

    protected synchronized void append(int size) { downloadedSize += size; }

    protected synchronized void update(int threadId,int pos) {
        this.data.put(threadId, pos);
        this.fileService.update(this.downloadUrl, threadId, pos);
    }

    public FileDownloadered(Context context,String downloadUrl,File fileSaveDir,int threadNum) {
        try {
            this.context = context;
            this.downloadUrl = downloadUrl;
            fileService = new FileService(this.context);
            URL url = new URL(this.downloadUrl);
            if(!fileSaveDir.exists()) fileSaveDir.mkdir();
            this.threads = new DownloadThread[threadNum];


            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, " +
                    "image/pjpeg, application/x-shockwave-flash, application/xaml+xml, " +
                    "application/vnd.ms-xpsdocument, application/x-ms-xbap," +
                    " application/x-ms-application, application/vnd.ms-excel," +
                    " application/vnd.ms-powerpoint, application/msword, */*");

            conn.setRequestProperty("Accept-Language", "zh-CN");
            conn.setRequestProperty("Referer", downloadUrl);
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; " +
                    "Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727;" +
                    " .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");

            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.connect();
            printResponseHeader(conn);
            if(conn.getResponseCode() == RESPONSEOK) {
                this.fileSize = conn.getContentLength();
                if(this.fileSize <= 0)throw new RuntimeException("不知道文件大小");
                String filename = getFileName(conn);
                this.saveFile = new File(fileSaveDir,filename);
                Map<Integer,Integer> logdata = fileService.getData(downloadUrl);
                if(logdata.size() > 0) {
                    for(Map.Entry<Integer, Integer> entry : logdata.entrySet()) {
                        data.put(entry.getKey(), entry.getValue());
                    }
                }
                if(this.data.size() == this.threads.length) {
                    for(int i = 0;i < this.threads.length;i++) {
                        this.downloadedSize += this.data.get(i+1);
                    }
                    print("已下载的长度" + this.downloadedSize + "个字节");
                }
                this.block = (this.fileSize % this.threads.length) == 0?
                        this.fileSize / this.threads.length:
                        this.fileSize / this.threads.length + 1;
            }else{
                print("服务器响应错误:" + conn.getResponseCode() + conn.getResponseMessage());
                throw new RuntimeException("服务器反馈出错");
            }


        }catch (Exception e)
        {
            print(e.toString());
            throw new RuntimeException("无法连接URL");
        }
    }

    private String getFileName(HttpURLConnection conn) {
        String filename = this.downloadUrl.substring(this.downloadUrl.lastIndexOf('/') + 1);
        if(filename == null || "".equals(filename.trim())){
            for(int i = 0;;i++) {
                String mine = conn.getHeaderField(i);
                if (mine == null) break;
                if("content-disposition".equals(conn.getHeaderFieldKey(i).toLowerCase())){
                    Matcher m = Pattern.compile(".*filename=(.*)").matcher(mine.toLowerCase());
                    if(m.find()) return m.group(1);
                }
            }
            filename = UUID.randomUUID()+ ".tmp";
        }
        return filename;
    }
    public int download(DownloadProgressListener listener) throws Exception{
        try {
            RandomAccessFile randOut = new RandomAccessFile(this.saveFile, "rwd");
            if(this.fileSize>0) randOut.setLength(this.fileSize);
            randOut.close();
            URL url = new URL(this.downloadUrl);
            if(this.data.size() != this.threads.length){
                this.data.clear();
                for (int i = 0; i < this.threads.length; i++) {
                    this.data.put(i+1, 0);
                }
                this.downloadedSize = 0;
            }

            for (int i = 0; i < this.threads.length; i++) {
                int downLength = this.data.get(i+1);
                if(downLength < this.block && this.downloadedSize<this.fileSize){
                    this.threads[i] = new DownloadThread(this, url, this.saveFile, this.block, this.data.get(i+1), i+1);
                    this.threads[i].setPriority(7);
                    this.threads[i].start();
                }else{
                    this.threads[i] = null;
                }
            }
            fileService.delete(this.downloadUrl);
            fileService.save(this.downloadUrl, this.data);
            boolean notFinish = true;
            while (notFinish) {
                Thread.sleep(900);
                notFinish = false;
                for (int i = 0; i < this.threads.length; i++){
                    if (this.threads[i] != null && !this.threads[i].isFinish()) {
                        notFinish = true;
                        if(this.threads[i].getDownLength() == -1){
                            this.threads[i] = new DownloadThread(this, url, this.saveFile, this.block, this.data.get(i+1), i+1);
                            this.threads[i].setPriority(7);
                            this.threads[i].start();
                        }
                    }
                }
                if(listener!=null) listener.onDownloadSize(this.downloadedSize);
            }
            if(downloadedSize == this.fileSize) fileService.delete(this.downloadUrl);
        } catch (Exception e) {
            print(e.toString());
            throw new Exception("文件下载异常");
        }
        return this.downloadedSize;
    }
    public static Map<String, String> getHttpResponseHeader(HttpURLConnection http) {
        Map<String, String> header = new LinkedHashMap<String, String>();
        for (int i = 0;; i++) {
            String mine = http.getHeaderField(i);
            if (mine == null) break;
            header.put(http.getHeaderFieldKey(i), mine);
        }
        return header;
    }
    public static void printResponseHeader(HttpURLConnection http){
        Map<String, String> header = getHttpResponseHeader(http);
        for(Map.Entry<String, String> entry : header.entrySet()){
            String key = entry.getKey()!=null ? entry.getKey()+ ":" : "";
            print(key+ entry.getValue());
        }
    }
    private static void print(String msg) {
        Log.i(TAG, msg);
    }
}
