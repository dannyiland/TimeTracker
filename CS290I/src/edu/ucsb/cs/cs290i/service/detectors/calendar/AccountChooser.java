package edu.ucsb.cs.cs290i.service.detectors.calendar;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.services.GoogleKeyInitializer;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.common.collect.Lists;

/**
 * Sample for Google Calendar API v3. It shows how to authenticate, get calendars, add a new
 * calendar, update it, and delete it.
 * 
 * <p>
 * To enable logging of HTTP requests/responses, change {@link #LOGGING_LEVEL} to {@link Level#CONFIG} or {@link Level#ALL} and run this command:
 * </p>
 * 
 * <pre>
 * adb shell setprop log.tag.HttpTransport DEBUG
 * </pre>
 * 
 * @author Yaniv Inbar
 */
public final class AccountChooser extends ListActivity {

    /** Logging level for HTTP requests/responses. */
    private static final Level LOGGING_LEVEL = Level.ALL;

    private static final String TAG = "CalendarSample";

    private static final String AUTH_TOKEN_TYPE = "cl";

    private static final int REQUEST_AUTHENTICATE = 0;

    final HttpTransport transport = AndroidHttp.newCompatibleTransport();

    final JsonFactory jsonFactory = new GsonFactory();

    static final String PREF_ACCOUNT_NAME = "accountName";

    static final String PREF_AUTH_TOKEN = "authToken";

    GoogleAccountManager accountManager;

    SharedPreferences settings;

    String accountName;

    String authToken;

    com.google.api.services.calendar.Calendar client;

    final List<CalendarListEntry> calendars = Lists.newArrayList();

    private boolean received401;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        accountName = settings.getString(PREF_ACCOUNT_NAME, null);
        authToken = settings.getString(PREF_AUTH_TOKEN, null);
        Logger.getLogger("com.google.api.client").setLevel(LOGGING_LEVEL);
        accountManager = new GoogleAccountManager(this);

        gotAccount();
    }


    void gotAccount() {
        Account account = accountManager.getAccountByName(accountName);
        if (account == null) {
            chooseAccount();
            return;
        }
        if (authToken != null) {
            onAuthToken();
            return;
        }
        accountManager.getAccountManager()
                .getAuthToken(account, AUTH_TOKEN_TYPE, true, new AccountManagerCallback<Bundle>() {

                    public void run(AccountManagerFuture<Bundle> future) {
                        try {
                            Bundle bundle = future.getResult();
                            if (bundle.containsKey(AccountManager.KEY_INTENT)) {
                                Intent intent = bundle.getParcelable(AccountManager.KEY_INTENT);
                                intent.setFlags(intent.getFlags() & ~Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivityForResult(intent, REQUEST_AUTHENTICATE);
                            } else if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
                                setAuthToken(bundle.getString(AccountManager.KEY_AUTHTOKEN));
                                onAuthToken();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage(), e);
                        }
                    }
                }, null);
    }


    private void chooseAccount() {
        accountManager.getAccountManager().getAuthTokenByFeatures(GoogleAccountManager.ACCOUNT_TYPE,
                AUTH_TOKEN_TYPE,
                null,
                AccountChooser.this,
                null,
                null,
                new AccountManagerCallback<Bundle>() {

                    public void run(AccountManagerFuture<Bundle> future) {
                        Bundle bundle;
                        try {
                            bundle = future.getResult();
                            setAccountName(bundle.getString(AccountManager.KEY_ACCOUNT_NAME));
                            setAuthToken(bundle.getString(AccountManager.KEY_AUTHTOKEN));
                            onAuthToken();
                        } catch (OperationCanceledException e) {
                            // user canceled
                        } catch (AuthenticatorException e) {
                            Log.e(TAG, e.getMessage(), e);
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage(), e);
                        }
                    }
                },
                null);
    }


    void setAccountName(String accountName) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_ACCOUNT_NAME, accountName);
        editor.commit();
        this.accountName = accountName;
    }


    void setAuthToken(String authToken) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_AUTH_TOKEN, authToken);
        editor.commit();
        this.authToken = authToken;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case REQUEST_AUTHENTICATE:
            if (resultCode == RESULT_OK) {
                gotAccount();
            } else {
                chooseAccount();
            }
            break;
        }
    }


    void onAuthToken() {

    }


    void onRequestCompleted() {
        received401 = false;
    }


    void handleGoogleException(IOException e) {
        if (e instanceof GoogleJsonResponseException) {
            GoogleJsonResponseException exception = (GoogleJsonResponseException) e;
            if (exception.getStatusCode() == 401 && !received401) {
                received401 = true;
                accountManager.invalidateAuthToken(authToken);
                authToken = null;
                SharedPreferences.Editor editor2 = settings.edit();
                editor2.remove(PREF_AUTH_TOKEN);
                editor2.commit();
                gotAccount();
                return;
            }
        }
        Log.e(TAG, e.getMessage(), e);
    }
}