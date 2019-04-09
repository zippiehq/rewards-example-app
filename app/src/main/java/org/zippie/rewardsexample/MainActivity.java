package org.zippie.rewardsexample;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Context context = this;
        final TextView balanceTextView = findViewById(R.id.balance);

        //TODO: Read these from somewhere
        final String tokenAddress = "0x374FaBa19192a123Fbb0c3990e3EeDcFeeaad42A";
        final String userId = "F07E51B3E0FF2492364B35382E697D73";
        final String apiKey = "apiKey";

        final RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://rewardapi-kovan.zippie.org/get_user_balance";

        JSONObject postParams = new JSONObject();
        try {
            postParams.put("userid", userId);
            postParams.put("token_address", tokenAddress);
            postParams.put("api-key", apiKey);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postParams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {
                        try {
                            Log.d("ZippieRewardsExample", response.toString());
                            balanceTextView.setText(response.getString("balance"));
                            balanceTextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    try {
                                        String pending = response.getString("pending");
                                        String wallets = response.getString("wallets");
                                        String cheques = response.getString("cheques");

                                        // Construct url like https://customer.zippie.org/#/userid/pending/wallets/cheques
                                        String url = "https://customer.zippie.org/#/" + userId + "/" + pending + "/" + wallets + "/" + cheques;
                                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                                        builder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary));
                                        CustomTabsIntent customTabsIntent = builder.build();
                                        customTabsIntent.launchUrl(context, Uri.parse(url));
                                    } catch (JSONException e) {
                                        toast("Could not launch wallet");
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            toast("Could not receive balance");
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.getMessage() != null) {
                            Log.d("ZippieRewardsExample", error.getMessage());
                        }
                    }
                });

        queue.add(request);
    }

    void toast(String msg) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, msg, duration);
        toast.show();
    }
}