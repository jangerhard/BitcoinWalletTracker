package io.github.jangerhard.BitcoinWalletTracker.client;

import android.util.Log;
import android.widget.Toast;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.jangerhard.BitcoinWalletTracker.MainActivity;
import io.github.jangerhard.BitcoinWalletTracker.R;
import io.github.jangerhard.BitcoinWalletTracker.model.BlockinfoResponse;
import io.vavr.gson.VavrGson;
import org.json.JSONObject;

public class BlockExplorer {

    private String LOG_TAG = "BlockExplorer";

    private String url_blockchain = "https://blockchain.info/";
    private String url_blockonomics = "https://www.blockonomics.co/api/balance";

    private RequestQueue requestQueue;
    private MainActivity activity;
    private Gson gson;

    public BlockExplorer(RequestQueue requestQueue, MainActivity activity) {
        this.requestQueue = requestQueue;
        this.activity = activity;

        GsonBuilder builder = new GsonBuilder();
        VavrGson.registerAll(builder);
        gson = builder.create();
    }

    public void getSingleWalletInfo(String address) {

        // Request a response from the provided URL.
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET,
                        url_blockchain + "rawaddr/" + address + "?limit=5",
                        null,
                        this::handleRefreshedInfo,
                        this::handleErrors);

        // Add the request to the RequestQueue.
        requestQueue.add(jsObjRequest);

    }

    private void handleErrors(VolleyError error) {
        String message = activity.getString(R.string.error_generic);

        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
            message = activity.getString(R.string.error_no_internet);
        } else if (error instanceof ServerError) {
            message = activity.getString(R.string.error_server);
        } else if (error instanceof NetworkError) {
            message = activity.getString(R.string.Error_network);
        } else if (error instanceof ParseError) {
            message = activity.getString(R.string.error_parsing);
        }

        Log.e(LOG_TAG, message);
        Toast.makeText(activity,
                message,
                Toast.LENGTH_SHORT).show();
        activity.updateUI();
    }

    private void handleRefreshedInfo(JSONObject response) {
        BlockinfoResponse accountInfo = gson.fromJson(response.toString(), BlockinfoResponse.class);
        activity.handleRefreshedAccount(accountInfo);
    }
}
