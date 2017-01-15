package com.example.kch.youtubelist;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

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
import java.net.URLEncoder;
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
        search.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                searchTask = new searchTask().execute();
            }
        });
    }

    /*View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            searchTask = new searchTask().execute();
            Toast.makeText(getApplicationContext(), check, Toast.LENGTH_LONG).show();
        }
    };*/

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
            // 파싱한 Json Data를 리스트어댑터를 거쳐 리스트뷰의 아이템(뷰)들로 만듦.
            StoreListAdapter mAdapter = new StoreListAdapter(MainActivity.this,R.layout.listview_start, sdata);
            searchlist.setAdapter(mAdapter);
            //super.onPostExecute(result);
        }

        // 백그라운드 쓰레드에서 유튜브 검색, 검색 결과 파싱
        @Override
        protected Void doInBackground(Void... params) {
            try {
                parsingJsonData(getYoutube("search"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    String vodid = "";
    String chid = "";
    // 유튜브에서 보내는 검색 데이터 파싱
    private void parsingJsonData(JSONObject jsonObject) throws JSONException {
        sdata.clear();

        JSONArray contacts = jsonObject.getJSONArray("items");

        for(int i=0; i<contacts.length(); i++) {
            JSONObject c = contacts.getJSONObject(i);
            String kind = c.getJSONObject("id").getString("kind");  // 종류를 체크하여 playlist 저장

            String title = c.getJSONObject("snippet").getString("title"); // 유튜브 제목 파싱
            String changString = "";
            try {
                changString = new String(title.getBytes("8859_1"), "utf-8"); // 한글 깨짐 방지. utf-8 인코딩
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String date = c.getJSONObject("snippet").getString("publishedAt").substring(0,10); // 등록날짜
            String imgUrl = c.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("default").getString("url"); // 썸네일 이미지 URL

            if(kind.equals("youtube#video")){
                vodid = c.getJSONObject("id").getString("videoId"); // 유튜브 동영상 id값. 재생시 필요!!
                sdata.add(new SearchData(vodid, changString, imgUrl, date));
            } else if(kind.equals("youtube#channels")){
                chid = c.getJSONObject("id").getString("channelId");
                sdata.add(new SearchData(chid, changString, imgUrl, date));
            }
            //Toast.makeText(getApplicationContext(), changString, Toast.LENGTH_LONG).show();
        }
    }

    String strCookie ;

    String check = "";
    public JSONObject getYoutube(String YoutubeMethod) {

        requestGet YoutubeClient ;
        String rqUrl = "https://www.googleapis.com/youtube/v3/search?"
                + "part=snippet&q=" + et.getText().toString()
                + "&key="+DeveloperKey.DEVELOPER_KEY +"&maxResults=5";

        YoutubeClient = new requestGet(rqUrl);
        JSONObject jsonObject = YoutubeClient.getJson();

        return jsonObject;
    }

    public JSONObject getYoutube(String YoutubeMethod, String list) {
        requestGet YoutubeClient ;
        String rqUrl = "https://www.googleapis.com/youtube/v3/channels?"
                + "part=snippet&id=" + list
                + "&key="+DeveloperKey.DEVELOPER_KEY +"&maxResults=5" ;

        YoutubeClient = new requestGet(rqUrl);
        JSONObject jsonObject = YoutubeClient.getJson();

        return jsonObject;
    }

    public class requestGet {
        private String rqUrl ;

        public requestGet(String url) {
            this.rqUrl = url;
        }

        public JSONObject getJson() {
            HttpGet httpGet = new HttpGet(this.rqUrl);  //EditText에 입력된 값으로 겁색을 합니다.
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

    public class StoreListAdapter extends ArrayAdapter<SearchData> {
        private ArrayList<SearchData> items;
        SearchData fInfo;

        public StoreListAdapter(Context context, int textViewResourceId, ArrayList<SearchData> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            //출력
            View v = convertView;
            fInfo = items.get(position);
            //Context context = parent.getContext();

            // 레이아웃 인플레이터, Layout Inflater Service 에 대한 SystemService 얻어와야 함.
            LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.listview_start,null);    // 리스트뷰에 대한 xml 인플레이트,

            ImageView img = (ImageView) v.findViewById(R.id.img);    // 리스트뷰 xml의 이미지뷰 객체화

            // ***************   URI에 한글과 공백이 함께 있기 때문에 가공  ******************** //
            String url = fInfo.getUrl();

            String sUrl = "";
            String eUrl = "";
            sUrl = url.substring(0, url.lastIndexOf("/") +1);  // url의 처음부터 '/'까지 잘라서 저장
            eUrl = url.substring(url.lastIndexOf("/")+1, url.length()); // url의 '/'부터 끝까지 저장
            try {
                eUrl = URLEncoder.encode(eUrl, "EUC-KR").replace("+", "%20");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String new_url = sUrl + eUrl ;
            // ***************************************************************************** //

            // 이미지 로더
            Glide.with(getApplicationContext()).load(new_url).into(img);

            v.setTag(position);
            v.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int pos = (Integer) v.getTag();

                    Intent intent = new Intent(MainActivity.this, YouTubePlayerActivity.class);
                    intent.putExtra("id", items.get(pos).getVideoId()); // video id 전달
                    startActivity(intent);  // 리스트 터치 시 재생 액티비티로 이동.
                }
            });

            ((TextView) v.findViewById(R.id.title)).setText(fInfo.getTitle());
            ((TextView) v.findViewById(R.id.date)).setText(fInfo.getPublishedAt());

            return v ;
        }
    }
}




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