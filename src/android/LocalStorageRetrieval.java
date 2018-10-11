package org.apache.cordova.plugin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Message;
import android.support.annotation.Keep;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * This class echoes a string called from JavaScript.
 */
public class LocalStorageRetrieval extends CordovaPlugin {

    @Override
    public void initialize(final CordovaInterface cordova, final CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("getLocalStorageData")) {
            Activity cordovaActivity = cordova.getActivity();
            String baseUrl = args.optString(0);
            getLocalStorageData(baseUrl, cordovaActivity, callbackContext);
            return true;
        }
        return false;
    }

    /**
     * We execute this on the UiThread because of WebView. It gives a lot of warning
     * that it should only be executed on main thread to prevent concurrent
     * problems.
     *
     * @param baseUrl         - Il baseUrl di partenza della WebView
     * @param activity        - L'activity sulla quale creare la WebView
     * @param callbackContext - Il {@link CallbackContext} cordova per la risposta del plugin
     */
    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled"})
    private void getLocalStorageData(final String baseUrl, final Activity activity, final CallbackContext callbackContext) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final WebView migrationWebView = new WebView(activity);

                try {
                    final String js = "" +
                            "console.log('Importing localStorage now...');" +
                            "JSInterface.passBackValues(JSON.stringify(localStorage));" +
                            "console.log('done.');";

                    final JSInterface jsInterface = new JSInterface(callbackContext);
                    migrationWebView.addJavascriptInterface(jsInterface, "JSInterface");
                    migrationWebView.getSettings().setJavaScriptEnabled(true);

                    // Find the correct database path for cordova.
                    final String databasePath = migrationWebView.getContext().getApplicationContext()
                            .getDir("database", Context.MODE_PRIVATE).getPath();
                    migrationWebView.getSettings().setDatabaseEnabled(true);
                    migrationWebView.getSettings().setDomStorageEnabled(true);
                    migrationWebView.getSettings().setDatabasePath(databasePath);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        migrationWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
                        migrationWebView.getSettings().setAllowFileAccessFromFileURLs(true);
                    }

                    migrationWebView.setWebChromeClient(new MyWebChromeClient());

                    final String summary = "<html><body><script>" + js + "</script></body></html>";
                    migrationWebView.loadDataWithBaseURL(baseUrl, summary, "text/html", "UTF-8", "about:blank");
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    private static class MyWebChromeClient extends WebChromeClient {
        private final String TAG = MyWebChromeClient.class.getSimpleName();

        MyWebChromeClient() {
            super();
            Log.d(TAG, "MyWebChromeClient()");
        }

        @Override
        public boolean onConsoleMessage(final ConsoleMessage consoleMessage) {
            Log.d(TAG, String.format("%s @ %d: %s",
                    consoleMessage.message(),
                    consoleMessage.lineNumber(),
                    consoleMessage.sourceId()
            ));
            return true;
        }
    }

    /**
     * This is the class that interfaces with javascript. From javascript we call
     * passBackValues function.
     */
    private static class JSInterface {
        final CallbackContext callbackContext;

        /**
         * Instantiate the interface and set the context
         */
        JSInterface(final CallbackContext callbackContext) {
            this.callbackContext = callbackContext;
        }

        @Keep
        @JavascriptInterface
        public void passBackValues(String items) {
            callbackContext.success(items);
        }
    }
}
