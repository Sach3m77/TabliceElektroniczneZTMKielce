package com.example.tabliceelektroniczneztmkielce;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity wyświetlające stronę internetową wewnątrz aplikacji za pomocą WebView.
 * @author Paweł Sacha, Mateusz Pacak, Bartosz Ryś
 * @version 1.0
 */

public class WebViewActivity extends AppCompatActivity {

    private WebView mWebView;
    private RelativeLayout mBackLayout;
    private String mMarkerLink;

    /**
     * Metoda tworząca activity.
     * @param savedInstanceState Nieużuwane.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        mWebView = findViewById(R.id.webView);
        mBackLayout = findViewById(R.id.backLayout);
        mMarkerLink = getIntent().getStringExtra("link");

        mWebView.setWebViewClient(new WebViewClient());
        mWebView.loadUrl(mMarkerLink);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        mBackLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    /**
     * Metoda wywołana gdy activity wykryje naciśnięcie przez użytkownika klawisza wstecz.
     */
    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()) {
            mWebView.goBack();
        }else
            super.onBackPressed();
    }
}
