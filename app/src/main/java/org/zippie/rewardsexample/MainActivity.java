package org.zippie.rewardsexample;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerClient.InstallReferrerResponse;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    TextView balanceTextView;
    TextView referrerTextView;
    InstallReferrerClient referrerClient;
    //TODO: Read these from somewhere
    final String tokenAddress = "0x374FaBa19192a123Fbb0c3990e3EeDcFeeaad42A";
    final String userId = "F07E51B3E0FF2492364B35382E697D73";
    final String apiKey = "user";
    String url = "https://rewardapi-kovan.zippie.org/get_user_balance";
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        balanceTextView = findViewById(R.id.balance);
        referrerTextView = findViewById(R.id.referrerTextView);

        final RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject postParams = new JSONObject();
        try {
            postParams.put("userid", userId);
            postParams.put("token_address", tokenAddress);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postParams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {
                        try {
                            log(response.toString());
                            balanceTextView.setText(response.getString("balance"));
                            balanceTextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    try {
                                        String pending = response.getString("pending");
                                        String wallets = response.getString("wallets");
                                        String cheques = response.getString("cheques");

                                        // Construct url like https://customer-test.zippierewards.com/#/userid/tokenAddress
                                        String url = "https://customer-test.zippierewards.com/#/" + userId + "/" + tokenAddress;
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
                            log(error.getMessage());
                        }
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("api-key", apiKey);
                return params;
            }
        };

        queue.add(request);

        setupInstallReferrerClient();
    }

    private void setupInstallReferrerClient() {
        referrerClient = InstallReferrerClient.newBuilder(this).build();
        referrerClient.startConnection(new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {
                final StringBuilder sb = new StringBuilder();
                sb.append("Referrer result: ");

                switch (responseCode) {
                    case InstallReferrerResponse.OK:
                        String errorMsg = "Connection established";
                        referrerTextView.setText(errorMsg);
                        log(errorMsg);

                        ReferrerDetails response = null;
                        try {
                            response = referrerClient.getInstallReferrer();

                            if (response == null) {
                                sb.append("\ninstallReferrerDetails == NULL");
                            } else {
                                // This is where you would contact your backend api
                                // Eg. If link is https://play.google.com/store/apps/details?id=org.appname&referrer=MY_REFERRAL_CODE,
                                // response.getInstallReferrer() will be MY_REFERRAL_CODE
                                // Note: These values will only work if app is released to Production in Google Play Store
                                sb.append("\ngetInstallReferrer = " + response.getInstallReferrer());
                                sb.append("\ngetInstallBeginTimestampSeconds = " + response.getReferrerClickTimestampSeconds());
                                sb.append("\ngetReferrerClickTimestampSeconds = " + response.getInstallBeginTimestampSeconds());
                            }
                            referrerClient.endConnection();
                        } catch (RemoteException e) {
                            sb.append("\nonInstallReferrerSetupFinished. exception: " + e.getMessage());
                            referrerTextView.setText(sb.toString());
                            e.printStackTrace();
                        }
                        break;
                    case InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                        errorMsg = "API not available on the current Play Store app";
                        sb.append(errorMsg);
                        log(errorMsg);
                        break;
                    case InstallReferrerResponse.SERVICE_UNAVAILABLE:
                        errorMsg = "Connection could not be established";
                        sb.append(errorMsg);
                        log(errorMsg);
                        break;
                }

                referrerTextView.setText(sb.toString());
            }

            @Override
            public void onInstallReferrerServiceDisconnected() {
                log("Connection disconnected");
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }

    void toast(String msg) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, msg, duration);
        toast.show();
    }

    void log(String msg) {
        Log.d("ZippieRewardsExample", msg);
    }
}