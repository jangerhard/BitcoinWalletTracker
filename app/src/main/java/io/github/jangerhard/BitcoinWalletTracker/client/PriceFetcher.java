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
import io.github.jangerhard.BitcoinWalletTracker.utilities.BitcoinUtils;
import io.github.jangerhard.BitcoinWalletTracker.MainActivity;
import io.github.jangerhard.BitcoinWalletTracker.R;
import org.json.JSONException;
import org.json.JSONObject;

public class PriceFetcher {

    private String LOG_TAG = "PriceFetcher";
    private String url_exchange = "https://api.coinbase.com/v2/prices/";
    private RequestQueue requestQueue;
    private MainActivity activity;
    private BitcoinUtils utils;

    public PriceFetcher(RequestQueue requestQueue, MainActivity activity, BitcoinUtils utils) {
        this.requestQueue = requestQueue;
        this.activity = activity;
        this.utils = utils;
    }

    public void getCurrentPrice() {

        // Request a response from the provided URL.
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET,
                        url_exchange + "BTC-" + utils.getCurrencyPair() + "/spot",
                        null,
                        this::handleRefreshedCurrency,
                        this::handleErrors);

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
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        activity.updateUI();
    }

    private void handleRefreshedCurrency(JSONObject response) {

        double price = 0.0;

        try {
            price = response
                    .getJSONObject("data")
                    .getDouble("amount");
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error parsing price information");
        }

        Log.i(LOG_TAG, "Got price in pricefetcher: " + price);
        //Toast.makeText(mActivity, "Got price: " + price, Toast.LENGTH_SHORT).show();
        utils.updateCurrency(price);
        activity.getAllWalletsInfo(utils.getTrackedWallets());
    }
}
