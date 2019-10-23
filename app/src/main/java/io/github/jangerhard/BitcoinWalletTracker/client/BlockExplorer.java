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
import io.github.jangerhard.BitcoinWalletTracker.model.BlockonomicsBalanceResponse;
import io.github.jangerhard.BitcoinWalletTracker.model.BlockonomicsTransactionsResponse;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.gson.VavrGson;
import org.json.JSONException;
import org.json.JSONObject;

public class BlockExplorer {

    private String LOG_TAG = "BlockExplorer";

    private String url_blockonomics = "https://www.blockonomics.co/api";

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

    public void getUpdatedBalance(String address) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url_blockonomics + "/balance",
                createParams(HashMap.of("addr", address)),
                this::handleUpdatedBalance,
                this::handleErrors
        );

        requestQueue.add(jsonObjectRequest);
    }

    public void getTransactions(String address) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url_blockonomics + "/searchhistory",
                createParams(HashMap.of("addr", address)),
                response -> handleUpdatedTransactions(address, response),
                this::handleErrors
        );

        requestQueue.add(jsonObjectRequest);
    }

    private JSONObject createParams(Map<String, String> params) {
        JSONObject object = new JSONObject();

        params.forEach((s1, s2) -> {
            try {
                object.put(s1, s2);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Could not add to params: " + s1 + ":" + s2);
            }
        });
        return object;
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

    private void handleUpdatedBalance(JSONObject response) {
        BlockonomicsBalanceResponse accountInfo = gson.fromJson(response.toString(), BlockonomicsBalanceResponse.class);
        activity.handleUpdatedBalance(accountInfo.getResponse().get(0).getAddr(), accountInfo.getResponse().get(0).getConfirmed());
    }

    private void handleUpdatedTransactions(String address, JSONObject response) {
        BlockonomicsTransactionsResponse transactions = gson.fromJson(response.toString(), BlockonomicsTransactionsResponse.class);
        activity.handleUpdatedTransactions(address, transactions.getHistory());
    }
}
