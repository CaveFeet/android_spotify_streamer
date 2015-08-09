/*
COPYRIGHT 1995-2015 ESRI

TRADE SECRETS: ESRI PROPRIETARY AND CONFIDENTIAL 
Unpublished material - all rights reserved under the Copyright Laws of the United States.

For additional information, contact: Environmental Systems Research Institute, Inc. 
Attn: Contracts Dept 380 New York Street Redlands, California, USA 92373

email: contracts@esri.com
*/
package com.n8.spotifystreamer;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

public class SpotifyStreamerApplication extends Application {

  private static class LazyHolder{
    private static SpotifyApi sSpotifyApi = new SpotifyApi();
  }

  public static SpotifyService getSpotifyService() {
    return LazyHolder.sSpotifyApi.getService();
  }

  @Override
  public void onCreate() {
    super.onCreate();

    final String clientId = "d08688cc7e874c2d989a59a06b479bd3";
    final String clientSecret = "3d833aa7faae4054831a1da9308959ac";

    final String foo = clientId + ":" + clientSecret;

    AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void... params) {
        try {

          URL url = new URL("https://accounts.spotify.com/api/token");

          HttpPost post = new HttpPost(url.toString());
          post.addHeader("Authorization", "Basic " +
              new String(Base64.encode(foo.getBytes("UTF-8"), Base64.DEFAULT)));

          ArrayList<NameValuePair> postParameters;
          postParameters = new ArrayList<>();
          postParameters.add(new BasicNameValuePair("grant_type", "client_credentials"));

          post.setEntity(new UrlEncodedFormEntity(postParameters));

          //post.setHeader("Accept", "application/json");


//          JSONObject jsonobj = new JSONObject();
//          jsonobj.put("grant_type", "client_credentials");
//          StringEntity se = new StringEntity(jsonobj.toString());
//          post.setEntity(se);

          HttpClient client = new DefaultHttpClient();
          HttpResponse response = client.execute(post);

          String responseText = null;
          try {
            responseText = EntityUtils.toString(response.getEntity());
            //JSONObject json = new JSONObject(responseText);
            Log.d("foo", responseText);
          } catch (Exception e) {
            e.printStackTrace();
            Log.i("Parse Exception", e + "");
          }

        } catch (MalformedURLException e) {
          Log.d("fo", "failed to post because of malformed url exception" + e.getMessage());
        } catch (IOException e) {
          Log.d("foo", "failed to post becasue of io exception " + e.getMessage());
        }

        return null;
      }
    };
    task.execute();

  }
}
