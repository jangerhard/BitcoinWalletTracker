package com.example.jangerhard.BitcoinWalletTracker;

import android.Manifest;
import android.accounts.Account;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Rect;
import android.support.v4.widget.ListViewAutoScrollHelper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RequestQueue mRequestQueue;
    String url = "https://blockchain.info/";
    Activity mActivity;
    private List<BitcoinAccount> accountList;
    private List<String> addresses;
    AccountAdapter adapter;
    int numRefreshed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        accountList = new ArrayList<>();
        addresses = new ArrayList<>();
        adapter = new AccountAdapter(this, accountList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        prepareAccounts();

        Button bGetAccount = (Button) findViewById(R.id.bGetAccount);
        bGetAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshData();
            }
        });

    }

    private void prepareAccounts() {
        /**
         * Add test accounts
         */

        addresses.add("1FfmbHfnpaZjKFvyi1okTjJJusN455paPH");
        addresses.add("1AJbsFZ64EpEfS5UAjAfcUG8pH8Jn3rn1F");
        addresses.add("1A8JiWcwvpY7tAopUkSnGuEYHmzGYfZPiq");

    }

    private void refreshData() {

        if (!accountList.isEmpty())
            accountList.clear();

        Dexter.withActivity(mActivity)
                .withPermission(Manifest.permission.INTERNET)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        getWalletInfo(addresses);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(
                                getBaseContext(),
                                "You need to activate Internet permission!",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }

    private void getWalletInfo(List<String> addresses) {

        for (String address : addresses) {

            // Create blank account
            BitcoinAccount b = new BitcoinAccount();
            b.setFinal_balance(BigInteger.valueOf(0L));
            accountList.add(b);

            // Request a string response from the provided URL.
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.GET,
                            url + "rawaddr/" + address,
                            null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            handleRefreshedAccount(new Gson().fromJson(response.toString(), BitcoinAccount.class));
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("MainActivity", error.toString());
                            Toast.makeText(getBaseContext(),
                                    "That didn't work!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

            // Add the request to the RequestQueue.
            getVolleyRequestQueue().add(jsObjRequest);
        }

        adapter.notifyDataSetChanged();

    }

    private void handleRefreshedAccount(BitcoinAccount acc) {

        acc.setNickName("TestAccount " + numRefreshed);
        accountList.remove(numRefreshed);
        accountList.add(numRefreshed, acc);
        numRefreshed++;

        if(numRefreshed >= addresses.size()){
            adapter.notifyDataSetChanged();
            numRefreshed = 0;
        }


    }

    public RequestQueue getVolleyRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(this);
        }

        return mRequestQueue;
    }

}
