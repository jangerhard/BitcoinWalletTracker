package com.example.jangerhard.BitcoinWalletTracker;

import android.Manifest;
import android.accounts.Account;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private RequestQueue mRequestQueue;
    String url = "https://blockchain.info/";
    TextView tvAccountName;
    TextView tvAccountBalance;
    Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;

        Button bGetAccount = (Button) findViewById(R.id.bGetAccount);
        bGetAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withActivity(mActivity)
                        .withPermission(Manifest.permission.INTERNET)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                getWalletInfo("1FfmbHfnpaZjKFvyi1okTjJJusN455paPH");
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {

                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();
            }
        });

        tvAccountName = (TextView) findViewById(R.id.tvAccountName);
        tvAccountBalance = (TextView) findViewById(R.id.tvAccountBalance);
    }

    private void getWalletInfo(String hashAddress) {
        // Request a string response from the provided URL.
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url + "rawaddr/" + hashAddress, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        BitcoinAccount acc = new Gson().fromJson(response.toString(), BitcoinAccount.class);
                        acc.setNickName("TestAccount");
                        Log.i("MainActivity", "Response: \n" + acc.toString());

                        tvAccountName.setText(acc.getNickName());
                        tvAccountBalance.setText("Balance: " + acc.getFormatedBalance());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        tvAccountName.setText("That didn't work..");
                        Log.e("MainActivity", error.toString());

                    }
                });

        // Add the request to the RequestQueue.
        getVolleyRequestQueue().add(jsObjRequest);

    }

    public RequestQueue getVolleyRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(this);
        }

        return mRequestQueue;
    }
}
