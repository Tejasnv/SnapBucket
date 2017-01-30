package com.tejasvarsekar.project14;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class UserBucketActivity extends AppCompatActivity {

    String usrname;
    Context context;
    GridView gv;
    String json_string;
    String[] ar = new String[8];
    String[] desc = new String[8];
    public static int id;

    private Button button;

    ArrayList<Product> arrayList;
    ListView lv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_bucket);

        final TextView usrname = (TextView) findViewById(R.id.bktname);


        button = (Button) findViewById(R.id.button);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},100);
                return;
            }
        }

        enable_button();


        Intent intent2 = getIntent();
        String username = intent2.getStringExtra("username");
        id = intent2.getIntExtra("id", -1);

        String msg = "Welcome to " + Global.bucket ;
        usrname.setText(msg);


        arrayList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.listView);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                new ReadJSON().execute("http://quocnguyen.16mb.com/products.json");
                new ReadJSON().execute("http://10.0.2.2/photobucket/andBucketGallery.php?id="+ Global.bucket );
            }
        });

    }
    //        gv = (GridView) findViewById(R.id.gv);
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void enable_button() {

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialFilePicker()
                        .withActivity(UserBucketActivity.this)
                        .withRequestCode(10)
                        .start();

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 100 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)){
            enable_button();
        }else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},100);
            }
        }
    }

    ProgressDialog progress;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if(requestCode == 10 && resultCode == RESULT_OK){

            progress = new ProgressDialog(UserBucketActivity.this);
            progress.setTitle("Uploading");
            progress.setMessage("Please wait...");
            progress.show();

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {

                    File f  = new File(data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH));
                    String content_type  = getMimeType(f.getPath());

                    String file_path = f.getAbsolutePath();
                    OkHttpClient client = new OkHttpClient();
                    RequestBody file_body = RequestBody.create(MediaType.parse(content_type),f);

                    RequestBody request_body = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("type",content_type)
                            .addFormDataPart("uploaded_file",file_path.substring(file_path.lastIndexOf("/")+1), file_body)
                            .build();

                    Request request = new Request.Builder()
                            .url("http://10.0.2.2/photobucket/z2.php")
                            .post(request_body)
                            .build();

                    try {
                        Response response = client.newCall(request).execute();

                        if(!response.isSuccessful()){
                            throw new IOException("Error : "+response);
                        }

                        progress.dismiss();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
            });

            t.start();




        }
    }

    private String getMimeType(String path) {

        String extension = MimeTypeMap.getFileExtensionFromUrl(path);

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }


    BackgroundTask task= (BackgroundTask) new BackgroundTask().execute();
    class BackgroundTask extends AsyncTask<Void, Void, String> {

        String json_url;
        String JSON_STRING;

        @Override
        public void onPreExecute() {
//            json_url = "http://10.0.2.2/photobucket/andBucket.php?id=";
                json_url = "http://10.0.2.2/photobucket/andBucketView.php?id=";
        }

        @Override
        public String doInBackground(Void... voids) {
            try {

                URL url = new URL(json_url  + Global.bucket);
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
                for (int i = 0; i < 8; i++) {
                    ar[i] = "";
                }

                for (int i = 0; i < parentArray.length(); i++) {
                    JSONObject finalObject = parentArray.getJSONObject(i);
                    String subbucketname = finalObject.getString("subbucketname");
                    String creator = finalObject.getString("creator");
                    String description = finalObject.getString("description");
//                        finalBufferedData.append(bucketname + " - " + creator + " - " + description + "\n");
                    finalBufferedData.append(subbucketname + " - " + creator + " - " + description + "\n");
                    ar[i] = subbucketname;
                    desc[i] = description;
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
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(UserBucketActivity.this, android.R.layout.simple_list_item_1, ar);
            gv.setAdapter(adapter);

            gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Toast.makeText(UserBucketActivity.this, ar[i], Toast.LENGTH_SHORT).show();
                    Intent intent3 = new Intent(UserBucketActivity.this, UserSubbucketActivity.class);
                    intent3.putExtra("bucketname", ar[i]);
                    intent3.putExtra("description", desc[i]);

                    Global.subbucket = ar[i];
                    UserBucketActivity.this.startActivity(intent3);
                }
            });
        }
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
class ReadJSON extends AsyncTask<String, Integer, String> {

    @Override
    protected String doInBackground(String... params) {
        return readURL(params[0]);
    }

    @Override
    protected void onPostExecute(String content) {
        try {
            JSONObject jsonObject = new JSONObject(content);
            JSONArray jsonArray =  jsonObject.getJSONArray("products");

            for(int i =0;i<jsonArray.length(); i++){
                JSONObject productObject = jsonArray.getJSONObject(i);
                arrayList.add(new Product(
                        productObject.getString("image"),
                        productObject.getString("name"),
                        productObject.getString("price")
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CustomListAdapter adapter = new CustomListAdapter(
                getApplicationContext(), R.layout.custom_list_layout, arrayList
        );
        lv.setAdapter(adapter);
    }
}


    private static String readURL(String theUrl) {
        StringBuilder content = new StringBuilder();
        try {
            // create a url object
            URL url = new URL(theUrl);
            // create a urlconnection object
            URLConnection urlConnection = url.openConnection();
            // wrap the urlconnection in a bufferedreader
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            // read from the urlconnection via the bufferedreader
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line + "\n");
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }
}

