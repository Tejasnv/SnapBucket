package com.tejasvarsekar.project14;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class UserAreaActivity extends AppCompatActivity {

    String usrname;
    Context context;
    GridView gv;
    String json_string;
    String[] ar = new String[30];
    String[] desc = new String[30];
    int id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_area);

        final TextView usrname = (TextView) findViewById(R.id.bktname);

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
//        id = intent.getIntExtra("id", -1);
        id = Global.GV;
        String msg = "Welcome " + username;
        usrname.setText(msg);
    }
    //        gv = (GridView) findViewById(R.id.gv);
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    BackgroundTask task= (BackgroundTask) new BackgroundTask().execute();
    class BackgroundTask extends AsyncTask<Void, Void, String> {
        String json_url;
        String JSON_STRING;

        @Override
        public void onPreExecute() {
            json_url = "http://10.0.2.2/photobucket/andBucket.php?id=";
//                json_url = "http://10.0.2.2/photobucket/andBucketView.php?id=";
        }

        @Override
        public String doInBackground(Void... voids) {
            try {

                URL url = new URL(json_url  + Global.GV);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                while ((JSON_STRING = bufferedReader.readLine()) != null) {

                    stringBuilder.append(JSON_STRING + "\n");
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

                String finalJson = stringBuilder.toString();

                StringBuffer finalBufferedData = new StringBuffer();
                JSONObject parentObject = new JSONObject(finalJson);
                JSONArray parentArray = parentObject.getJSONArray("res");

                gv = (GridView) findViewById(R.id.gv);

//                    String[] ar = new String[parentArray.length()];
                for (int i = 0; i < 30; i++) {
                    ar[i] = "";
                }

                if (parentArray.length()== 0 ){
                    finalBufferedData.append("");
                }
                else {
                    for (int i = 0; i < parentArray.length(); i++) {
                        JSONObject finalObject = parentArray.getJSONObject(i);
                        String bucketname = finalObject.getString("bucketname");
                        String creator = finalObject.getString("creator");
                        String description = finalObject.getString("description");
//                        finalBufferedData.append(bucketname + " - " + creator + " - " + description + "\n");
                        finalBufferedData.append(bucketname + " - " + creator + " - " + description + "\n");
                        ar[i] = bucketname;
                        desc[i] = description;
                    }
                }

                return finalBufferedData.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onPostExecute(String result) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(UserAreaActivity.this, android.R.layout.simple_list_item_1, ar);
            gv.setAdapter(adapter);

            gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                        Toast.makeText(UserAreaActivity.this, ar[i], Toast.LENGTH_SHORT).show();
                    Intent intent2 = new Intent(UserAreaActivity.this, UserBucketActivity.class);
                    intent2.putExtra("bucketname", ar[i]);
                    intent2.putExtra("description", desc[i]);

                    Global.bucket = ar[i];
                    UserAreaActivity.this.startActivity(intent2);
                }
            });
        }
    }
}


