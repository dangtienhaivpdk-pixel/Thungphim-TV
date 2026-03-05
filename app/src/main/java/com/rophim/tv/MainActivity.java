package com.rophim.tv;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class MainActivity extends Activity {

    private WebView webView;
    private ProgressBar progressBar;
    private static final String HOME_URL = "https://pointnorth.io";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Toàn màn hình, ẩn thanh trạng thái
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progressBar);

        setupWebView();
        webView.loadUrl(HOME_URL);
    }

    private void setupWebView() {
        WebSettings settings = webView.getSettings();

        // Bật JavaScript
        settings.setJavaScriptEnabled(true);

        // Hỗ trợ HTML5 video
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        // Tối ưu cache
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAppCacheEnabled(true);

        // Zoom phù hợp màn hình TV
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        // User agent giả lập desktop để web hiển thị đúng
        settings.setUserAgentString(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/120.0.0.0 Safari/537.36"
        );

        // Hỗ trợ video fullscreen
        webView.setWebChromeClient(new WebChromeClient() {
            private View customView;
            private CustomViewCallback customViewCallback;

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                // Vào chế độ fullscreen khi bấm phóng to video
                customView = view;
                customViewCallback = callback;
                setContentView(view);
            }

            @Override
            public void onHideCustomView() {
                // Thoát fullscreen video, quay về WebView
                setContentView(R.layout.activity_main);
                webView = findViewById(R.id.webview);
                progressBar = findViewById(R.id.progressBar);
                setupWebView();
                if (customViewCallback != null) {
                    customViewCallback.onCustomViewHidden();
                }
                customView = null;
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // Chỉ cho phép load URL trong cùng domain
                String url = request.getUrl().toString();
                if (url.startsWith("https://pointnorth.io") || url.startsWith("http://pointnorth.io")) {
                    return false; // Cho WebView tự xử lý
                }
                // Các link ngoài vẫn load trong WebView
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Nút BACK trên remote: quay lại trang trước hoặc thoát
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            }
        }
        // Nút HOME: về trang chủ RoPhim
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            webView.loadUrl(HOME_URL);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
