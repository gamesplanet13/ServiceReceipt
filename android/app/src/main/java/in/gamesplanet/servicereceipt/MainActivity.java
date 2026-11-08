package in.gamesplanet.servicereceipt;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrintManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String PAGE = "https://gamesplanet13.github.io/ServiceReceipt/";
    private WebView web;
    private boolean trusted(Uri uri) {
        return uri != null && "https".equals(uri.getScheme())
                && "gamesplanet13.github.io".equalsIgnoreCase(uri.getHost())
                && uri.getPath() != null && uri.getPath().startsWith("/ServiceReceipt/");
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        web = new WebView(this);
        setContentView(web);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.addJavascriptInterface(new PrintBridge(), "GPPrint");
        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri url = request.getUrl();
                if (trusted(url)) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, url)); }
                catch (Exception e) { Toast.makeText(MainActivity.this, "No app available to open this link", Toast.LENGTH_LONG).show(); }
                return true;
            }
            @Override public void onPageFinished(WebView view, String url) {
                if (trusted(Uri.parse(url))) view.evaluateJavascript(
                    "window.print=function(){GPPrint.printReceipt()};window.open=function(u){if(u)location.href=u;return null}", null);
            }
        });
        if (state == null) web.loadUrl(PAGE); else web.restoreState(state);
    }
    public class PrintBridge {
        @JavascriptInterface public void printReceipt() {
            runOnUiThread(() -> {
                if (!trusted(Uri.parse(web.getUrl() == null ? "" : web.getUrl()))) return;
                PrintManager pm = (PrintManager) getSystemService(PRINT_SERVICE);
                pm.print("Games Planet Receipt", web.createPrintDocumentAdapter("Games Planet Receipt"), null);
            });
        }
    }
    @Override protected void onSaveInstanceState(Bundle state) { web.saveState(state); super.onSaveInstanceState(state); }
    @Override public void onBackPressed() { if (web.canGoBack()) web.goBack(); else super.onBackPressed(); }
}
