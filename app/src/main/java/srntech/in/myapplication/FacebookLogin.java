package srntech.in.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by tanuj on 26/01/2016.
 */
public class FacebookLogin extends Fragment {

    LoginButton loginButton;
    CallbackManager callbackManager;

    AccessTokenTracker accessTokenTracker;
    AccessToken accessToken;


    // ProfileTracker profileTracker;
    ProgressDialog progressDialog;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.facebook_layout_login, container, false);
        callbackManager = CallbackManager.Factory.create();
        MapWidgetIds(v);
        return v;
    }

    private void MapWidgetIds(View v) {
        progressDialog= new ProgressDialog(getActivity());
        loginButton = (LoginButton) v.findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_friends"));
        // If using in a fragment
        loginButton.setFragment(this);
        // Other app specific specialization

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i("loginResult:", "success");
                Log.i("getSource().name():", loginResult.getAccessToken().getSource().name());
                Log.i("getToken():", loginResult.getAccessToken().getToken());
                Log.i("getUserId():", loginResult.getAccessToken().getUserId());

                accessTokenTracker.startTracking();
            }

            @Override
            public void onCancel() {
                Log.i("loginResult:", "onCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.i("loginResult:exception", ":" + exception.toString());
            }


        });

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                // Set the access token using
                // currentAccessToken when it's loaded or set.

                if (oldAccessToken != null) {
                    Log.i("oldAccessToken:", ":" + oldAccessToken.getToken());
                    Log.i("oldAccessToken:", ":" + oldAccessToken.getUserId());
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }

                if (currentAccessToken != null) {
                    Log.i("currentAccessToken:", ":" + currentAccessToken.getToken());
                    Log.i("currentAccessToken:", ":" + currentAccessToken.getUserId());

                    if (progressDialog != null && !progressDialog.isShowing()) {
                        progressDialog.show();
                    }

                    GraphRequest request = GraphRequest.newMeRequest(
                            accessToken,
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(
                                        JSONObject object,
                                        GraphResponse response) {
                                    // Application code
                                    if( response.getRawResponse()!=null) {
                                        Log.i("ProfileTrackerresponse:", ":" + response.getRawResponse());
                                        if (progressDialog != null && progressDialog.isShowing()) {
                                            progressDialog.dismiss();
                                        }
                                    }else {
                                        LoginManager.getInstance().logOut();
                                    }
                                }
                            });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,link,email");
                    request.setParameters(parameters);
                    request.executeAsync();
                }
            }
        };
        // If the access token is available already assign it.
        accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null && !TextUtils.isEmpty(accessToken.getToken())) {
            Log.i("accessToken:", ":" + accessToken.getToken());
            Log.i("accessToken:", ":" + accessToken.getUserId());

            if (progressDialog != null && !progressDialog.isShowing()) {
                progressDialog.show();
            }

            GraphRequest request = GraphRequest.newMeRequest(
                    accessToken,
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {
                            // Application code
                            Log.i("accessToken:response:", ":" + response.getRawResponse());
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,link,email");
            request.setParameters(parameters);
            request.executeAsync();
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
