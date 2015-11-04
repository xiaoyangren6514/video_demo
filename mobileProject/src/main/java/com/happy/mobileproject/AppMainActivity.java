package com.happy.mobileproject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.happy.mobileproject.activity.VideoPlayerActivity;
import com.happy.mobileproject.domain.VideoItem;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class AppMainActivity extends Activity implements View.OnClickListener {

    private WebView mWebView;
    private ProgressBar mProgressBar;
    private String url = "http://www.qiuzhimen.com/app.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appmain);
        mWebView = (WebView) this.findViewById(R.id.wbContent);
        mProgressBar = (ProgressBar) this.findViewById(R.id.pbLoad);

        mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new NativeMethodInterface(), "android");
        mWebView.getSettings().setDefaultTextEncodingName("utf-8");
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                mProgressBar.setVisibility(View.GONE);
                super.onPageFinished(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mProgressBar.setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        mWebView.setWebChromeClient(mChromeClient);

        mWebView.loadUrl(url);
    }

    private class NativeMethodInterface {

        @JavascriptInterface
        public void showToast(String message) {
            Toast.makeText(AppMainActivity.this, message, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void playVideo(String url, String title) {
            loadDataFromNetWork(url);
            Intent intent = new Intent(getApplicationContext(), VideoPlayerActivity.class);
            intent.putExtra("URI", url);
            intent.putExtra("TITLE", title);
            startActivity(intent);
        }

    }

    /**
     * 从网络加载数据
     *
     * @param url
     */
    private void loadDataFromNetWork(String url) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (response != null) {
                    int result = response.optInt("result");
                    String msg = response.optString("msg");
                    if (result == 1) {
                        String data = response.optString("data");
                        VideoItem videoItem = JSON.parseObject(data, VideoItem.class);
                        if (!TextUtils.isEmpty(videoItem.getVPlayAddr())) {
                            Intent intent = VideoPlayerActivity.createIntent(AppMainActivity.this,videoItem);
                            startActivity(intent);
                        } else {
                            Toast.makeText(AppMainActivity.this, "视频加载失败,请重试", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AppMainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AppMainActivity.this, "视频加载失败,请重试", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(AppMainActivity.this, "视频加载失败,请重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private WebChromeClient mChromeClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (newProgress > 0 && newProgress < 100) {
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(newProgress);
            } else {
                mProgressBar.setVisibility(View.GONE);
            }
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
        }
    };

    int count = 0;

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mWebView.canGoBack()
                && event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            mWebView.goBack();
            return true;
        }
        if (count < 1) {
            Toast.makeText(this, "再次点击退出!", Toast.LENGTH_SHORT).show();
            count++;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    count = 0;
                }
            }, 2000l);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
}
