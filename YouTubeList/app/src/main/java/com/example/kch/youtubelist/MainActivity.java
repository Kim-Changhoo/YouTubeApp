package com.example.kch.youtubelist;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private EditText et;
    AsyncTask<?, ?, ?> searchTask;
    ArrayList<SearchData> sdata = new ArrayList<SearchData>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et = (EditText) findViewById(R.id.eturl);

        Button search = (Button) findViewById(R.id.search);
        search.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            searchTask = new searchTask().execute();
            Toast.makeText(getApplicationContext(), check, Toast.LENGTH_LONG).show();
        }
    };

    // 검색 task 클래스
    private class searchTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result) {
            // 검색 결과를 ListView로 전달.
            ListView searchlist = (ListView) findViewById(R.id.searchlist);

            //StoreListAdapter
            //super.onPostExecute(result);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                paringJsonData(getUtube());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    String vodid = "";
    // 유튜브에서 보내는 검색 데이터 파싱
    private void paringJsonData(JSONObject jsonObject) throws JSONException {
        sdata.clear();

        JSONArray contacts = jsonObject.getJSONArray("items");

        for(int i=0; i<contacts.length(); i++) {
            JSONObject c = contacts.getJSONObject(i);
            String kind = c.getJSONObject("id").getString("kind");  // 종류를 체크하여 playlist 저장

            if(kind.equals("youtube#video")){
                vodid = c.getJSONObject("id").getString("videoId"); // 유튜브 동영상 id값. 재생시 필요!!
            } else {
                vodid = c.getJSONObject("id").getString("playlistId");
            }

            String title = c.getJSONObject("snippet").getString("title"); // 유튜브 제목 파싱
            String changString = "";
            try {
                changString = new String(title.getBytes("8859_1"), "utf-8"); // 한글 깨짐 방지. utf-8 인코딩
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String date = c.getJSONObject("snippet").getString("publishedAt").substring(0,10); // 등록날짜
            String imgUrl = c.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("default").getString("url"); // 썸네일 이미지 URL
            sdata.add(new SearchData(vodid, changString, imgUrl, date));
            Toast.makeText(getApplicationContext(), changString, Toast.LENGTH_LONG).show();
        }
    }

    String strCookie ;
    /*public JSONObject getUtube() {
        String result ="" ;
        String youtubeUrl = "https://www.googleapis.com/youtube/v3/search?"
                +"part=snippet&q=" + et.getText().toString()
                +"&maxResults=50" +"&key=" +DeveloperKey.DEVELOPER_KEY ;
        // EditText에 입력
        // part(snippet), q(검색값), developerkey
        try {
            //URL Url = new URL(youtubeUrl);
            URL Url = new URL("http://www.naver.com");

            HttpURLConnection conn = (HttpURLConnection) Url.openConnection();
            conn.setDoInput(true);  // 읽기 모드
            conn.setDoOutput(true); // 쓰기 모드
            conn.setRequestMethod("GET");
            Log.i("URLConnection", conn.toString());

            strCookie = conn.getHeaderField("Set-Cookie");  // 쿠키 데이터 보관

            //InputStream is = conn.getInputStream(); // input 스트림 개방
            //BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")); // 문자열 셋트 세팅
            InputStream is = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder builder = new StringBuilder();    // 문자열 담기 위한 객체

            String line;
            while((line = reader.readLine()) != null)
                builder.append(line);

        } catch (ProtocolException | MalformedURLException exception) {
            exception.printStackTrace();
        } catch (IOException io) {
            io.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject = new JSONObject(result) ;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }*/

    String check = "";
    public JSONObject getUtube() {

        HttpGet httpGet = new HttpGet(
                "https://www.googleapis.com/youtube/v3/search?"
                        + "part=snippet&q=" + et.getText().toString()
                        + "&key="+DeveloperKey.DEVELOPER_KEY +"&maxResults=50");  //EditText에 입력된 값으로 겁색을 합니다.
        // part(snippet),  q(검색값) , key(서버키)
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }
        check = stringBuilder.toString();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return jsonObject;
    }
}
