package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private WebView webView;

    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadCallbackAboveL;
    private final int RESULT_CODE_PICK_FROM_ALBUM_BELLOW_LOLLILOP = 1;
    private final int RESULT_CODE_PICK_FROM_ALBUM_ABOVE_LOLLILOP = 2;
    String compressPath = "";


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webView);

        setUpWebViewDefaults(webView);


        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                result.confirm();
                return true;
            }

            @Override//视频播放
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                Log.d("P", "Request permissions: ");
                for (String res : request.getResources()) {
                    Log.d("P", res);
                }
                MainActivity.this.runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void run() {
                        request.grant(request.getResources());
                    }
                });
            }

            @Override
            public void onPermissionRequestCanceled(PermissionRequest request) {
                Log.d("P", "onPermissionRequestCanceled");
            }


            public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                        String acceptType) {
                if (mUploadMessage != null)
                    return;
                mUploadMessage = uploadMsg;
                selectImage(RESULT_CODE_PICK_FROM_ALBUM_BELLOW_LOLLILOP);
            }

            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                openFileChooser(uploadMsg, "");
            }

            // For Android > 4.1.1
            public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                        String acceptType, String capture) {
                openFileChooser(uploadMsg, acceptType);
            }

            // For Android 5.0+
            public boolean onShowFileChooser(WebView webView,
                                             ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                mUploadCallbackAboveL = filePathCallback;
                selectImage(RESULT_CODE_PICK_FROM_ALBUM_ABOVE_LOLLILOP);
                return true;
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }


        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        }
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAppCacheMaxSize(1024 * 1024 * 10);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        webView.getSettings().setAppCachePath(appCachePath);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://app.skylkj.com");
//        webView.loadUrl("https://192.168.0.102:8082/#/login");
//        webView.loadUrl("https://192.168.0.105:8080");
//        webView.loadUrl("https://app.skylkj.com");
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onStop() {
        super.onStop();
        webView.evaluateJavascript("if(window.localStream){window.localStream.stop();}", null);
    }


    /**
     * Convenience method to set some generic defaults for a
     * given WebView
     *
     * @param webView
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void setUpWebViewDefaults(WebView webView) {
        WebSettings settings = webView.getSettings();

        // Enable Javascript
        settings.setJavaScriptEnabled(true);

        // Use WideViewport and Zoom out if there is no viewport defined
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        // Enable pinch to zoom without the zoom buttons
        settings.setBuiltInZoomControls(true);

        // Allow use of Local Storage
        settings.setDomStorageEnabled(true);

        // Hide the zoom controls for HONEYCOMB+
        settings.setDisplayZoomControls(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        settings.setPluginState(WebSettings.PluginState.ON);

        // Enable remote debugging via chrome://inspect
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);

        webView.setWebViewClient(new WebViewClient() {
                                     @Override
                                     public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                                         handler.proceed();
                                     }

                                     @Override
                                     public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                         view.loadUrl(url);
                                         return true;
                                     }
                                 }
        );
        // AppRTC requires third party cookies to work
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasCameraPermission = checkSelfPermission(Manifest.permission.CAMERA);
            int hasRecordPermission = checkSelfPermission(Manifest.permission.RECORD_AUDIO);
            List<String> permissions = new ArrayList<>();
            if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (hasRecordPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (!permissions.isEmpty()) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), 111);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mUploadMessage == null && mUploadCallbackAboveL == null) {
            return;
        }
        Uri uri = null;
        switch (requestCode) {
            case RESULT_CODE_PICK_FROM_ALBUM_BELLOW_LOLLILOP:
                uri = afterChosePic(data);
                if (mUploadMessage != null) {
                    mUploadMessage.onReceiveValue(uri);
                    mUploadMessage = null;
                }
                break;
            case RESULT_CODE_PICK_FROM_ALBUM_ABOVE_LOLLILOP:
                try {
                    uri = afterChosePic(data);
                    if (uri == null) {
                        mUploadCallbackAboveL.onReceiveValue(new Uri[] { });
                        mUploadCallbackAboveL = null;
                        break;
                    }
                    if (mUploadCallbackAboveL != null && uri != null) {
                        mUploadCallbackAboveL.onReceiveValue(new Uri[] { uri });
                        mUploadCallbackAboveL = null;
                    }
                } catch (Exception e) {
                    mUploadCallbackAboveL = null;
                    e.printStackTrace();
                }
                break;
        }
    }

    /**
     * 选择照片后结束
     * @param data
     */
    private Uri afterChosePic(Intent data) {
        if (data == null) {
            return null;
        }
        String path = getRealFilePath(data.getData());
        String[] names = path.split("\\.");
        String endName = null;
        if (names != null) {
            endName = names[names.length - 1];
        }
        if (endName != null) {
            compressPath = compressPath.split("\\.")[0] + "." + endName;
        }
        File newFile;
        try {
            newFile = FileUtils.compressFile(path, compressPath);
        } catch (Exception e) {
            newFile = null;
        }
        return Uri.fromFile(newFile);
    }

    /**
     * 根据Uri获取图片文件的绝对路径
     */
    public String getRealFilePath(final Uri uri) {
        if (null == uri) {
            return null;
        }
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = getContentResolver().query(uri,
                    new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(webView.canGoBack()){
            webView.goBack();
        }else{
            finish();
        }
    }

    /**打开图库,同时处理图片（项目业务需要统一命名）*/
    private void selectImage(int resultCode) {
        compressPath = Environment.getExternalStorageDirectory().getPath() + "/QWB/temp";
        File file = new File(compressPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        compressPath = compressPath + File.separator + "compress.png";
        File image = new File(compressPath);
        if (image.exists()) {
            image.delete();
        }
        Intent intent = new Intent(
                Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, resultCode);

    }


}
