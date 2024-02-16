package com.example.universitymarket;

import android.app.Activity;
import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.microsoft.identity.client.AcquireTokenSilentParameters;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.IPublicClientApplication;
import com.microsoft.identity.client.ISingleAccountPublicClientApplication;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.SignInParameters;
import com.microsoft.identity.client.SilentAuthenticationCallback;
import com.microsoft.identity.client.exception.MsalException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public abstract class Network {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<String> db_user;
    public boolean signIn(Activity cur_act){
        String[] scopes = {
            "https://graph.microsoft.com/User.Read",
            "https://graph.microsoft.com/profile",
            "https://graph.microsoft.com/openid",
            "https://graph.microsoft.com/offline_access",
            "https://graph.microsoft.com/email",
            "https://graph.microsoft.com/Domain.Read.All",
            "https://graph.microsoft.com/CustomTags.ReadWrite.All",
            "https://graph.microsoft.com/AppRoleAssignment.ReadWrite.All",
            "https://graph.microsoft.com/Application.ReadWrite.All"
        };
        ISingleAccountPublicClientApplication msalApp = getMSALObj(cur_act);
        final IAccount[] account = new IAccount[1];
        final String[] access_token = new String[1];

        //check for token
        try {
            final boolean[] needs_resync = new boolean[1];
            msalApp.acquireTokenSilent(
                    new AcquireTokenSilentParameters.Builder()
                            .withScopes(Arrays.asList(scopes))
                            .withCallback(new SilentAuthenticationCallback() {
                                @Override public void onSuccess(IAuthenticationResult res) {
                                    access_token[0] = res.getAccessToken();
                                    account[0] = res.getAccount();
                                }
                                @Override public void onError(MsalException exception) {
                                    //user is logged out
                                    needs_resync[0] = true;
                                }
                            })
                            .build()
            );

            msalApp.signIn(
                    SignInParameters.builder()
                            .withActivity(cur_act)
                            .withScopes(Arrays.asList(scopes))
                            .withCallback(new AuthenticationCallback() {
                                @Override public void onCancel() { Log.d("Login", "User cancelled"); }
                                @Override public void onSuccess(IAuthenticationResult res) {
                                    Log.d("Login", "Signed on");

                                    boolean dne = false; //check with firebase against email
                                    if(!dne) {
                                        if (needs_resync[0]) {
                                            clearCache(cur_act);
                                            syncCache(cur_act, "download");
                                            access_token[0] = res.getAccessToken();
                                            account[0] = res.getAccount();
                                        }
                                        updateCache(cur_act, "access_token", access_token[0]);
                                        syncCache(cur_act, "upload");
                                    } else {
                                        /*TODO:  String[][] data = createAccount(); //graph API
                                        for (String[] dat : data) {
                                            updateCache(cur_act, dat[0], dat[1]);
                                        }*/
                                        syncCache(cur_act, "upload");
                                    }
                                }
                                @Override public void onError(MsalException e) {
                                    Log.e(e.getErrorCode(), e.getMessage());
                                }
                            })
                            .build()
            );
        } catch (Exception e) {
            Log.e("Azure connection error", e.getMessage());
        }
//add return false for bad case
        return true;
    }

    private ISingleAccountPublicClientApplication getMSALObj(Activity cur_act) {
        final ISingleAccountPublicClientApplication[] msalApp = new ISingleAccountPublicClientApplication[1];
        try {
            PublicClientApplication.createSingleAccountPublicClientApplication(
                    cur_act,
                    R.raw.auth_config,
                    new IPublicClientApplication.ISingleAccountApplicationCreatedListener() {
                        @Override public void onError(MsalException e) { Log.e(e.getExceptionName(), e.getMessage()); }
                        @Override public void onCreated(ISingleAccountPublicClientApplication app) {
                            msalApp[0] = app;
                        }
                    }
            );
        } catch (Exception e) {
            Log.e(e.toString(), e.getMessage());
            return null;
        }
        return msalApp[0];
    }

    private void updateCache(Activity cur_act, String tag, String value) {
        try {
            FileDescriptor fd = cur_act.getAssets().openNonAssetFd(String.valueOf(R.raw.user_cache)).getFileDescriptor();
            InputStream cache = new FileInputStream(fd);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(cache);


        } catch (IOException | ParserConfigurationException | SAXException e) { Log.e(e.toString(), e.getMessage()); }
    }

    private void syncCache(Activity cur_act, String direction) {

    }

    private void clearCache(Activity cur_act) {

    }
}
