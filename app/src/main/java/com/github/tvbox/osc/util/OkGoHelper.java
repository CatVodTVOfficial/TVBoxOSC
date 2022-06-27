package com.github.tvbox.osc.util;

import android.net.Uri;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;

import com.github.tvbox.osc.base.App;
import com.github.tvbox.osc.util.SSL.SSLSocketFactoryCompat;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.https.HttpsUtils;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.orhanobut.hawk.Hawk;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.CacheControl;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import xyz.doikki.videoplayer.exo.ExoMediaSourceHelper;

public class OkGoHelper {
    public static final long DEFAULT_MILLISECONDS = 10000;      //默认的超时时间

    private static OkHttpClient speedTestClient = null;

    public static OkHttpClient getSpeedTestClient(long timeOut) {
        if (speedTestClient == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("OkGo");

            if (Hawk.get(HawkConfig.DEBUG_OPEN, false)) {
                loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);
                loggingInterceptor.setColorLevel(Level.INFO);
            } else {
                loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.NONE);
                loggingInterceptor.setColorLevel(Level.OFF);
            }

            builder.retryOnConnectionFailure(false);

            builder.addInterceptor(loggingInterceptor);

            builder.readTimeout(timeOut, TimeUnit.MILLISECONDS);
            builder.writeTimeout(timeOut, TimeUnit.MILLISECONDS);
            builder.connectTimeout(timeOut, TimeUnit.MILLISECONDS);

            try {
                setOkHttpSsl(builder);
            } catch (Throwable th) {
                th.printStackTrace();
            }

            speedTestClient = builder.build();
        }
        return speedTestClient;
    }

    private static OkHttpClient fastParseClient = null;

    public static OkHttpClient getFastParseClient(long timeOut) {
        if (fastParseClient == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("OkGo");

            if (Hawk.get(HawkConfig.DEBUG_OPEN, false)) {
                loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);
                loggingInterceptor.setColorLevel(Level.INFO);
            } else {
                loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.NONE);
                loggingInterceptor.setColorLevel(Level.OFF);
            }

            //builder.retryOnConnectionFailure(false);

            builder.addInterceptor(loggingInterceptor);

            builder.readTimeout(timeOut, TimeUnit.MILLISECONDS);
            builder.writeTimeout(timeOut, TimeUnit.MILLISECONDS);
            builder.connectTimeout(timeOut, TimeUnit.MILLISECONDS);

            try {
                setOkHttpSsl(builder);
            } catch (Throwable th) {
                th.printStackTrace();
            }

            fastParseClient = builder.build();
        }
        return fastParseClient;
    }

    static void initExoOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("OkExoPlayer");

        if (Hawk.get(HawkConfig.DEBUG_OPEN, false)) {
            loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);
            loggingInterceptor.setColorLevel(Level.INFO);
        } else {
            loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.NONE);
            loggingInterceptor.setColorLevel(Level.OFF);
        }
        builder.addInterceptor(loggingInterceptor);

        builder.retryOnConnectionFailure(true);
        builder.followRedirects(true);
        builder.followSslRedirects(true);


        try {
            setOkHttpSsl(builder);
        } catch (Throwable th) {
            th.printStackTrace();
        }

        ExoMediaSourceHelper.getInstance(App.getInstance()).setOkClient(builder.build());
    }

    public static void init() {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("OkGo");

        if (Hawk.get(HawkConfig.DEBUG_OPEN, false)) {
            loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);
            loggingInterceptor.setColorLevel(Level.INFO);
        } else {
            loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.NONE);
            loggingInterceptor.setColorLevel(Level.OFF);
        }

        //builder.retryOnConnectionFailure(false);

        builder.addInterceptor(loggingInterceptor);

        builder.readTimeout(DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        builder.writeTimeout(DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        builder.connectTimeout(DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);

        try {
            setOkHttpSsl(builder);
        } catch (Throwable th) {
            th.printStackTrace();
        }

        OkHttpClient okHttpClient = builder.build();
        OkGo.getInstance().setOkHttpClient(okHttpClient);

        initExoOkHttpClient();
    }

    private static synchronized void setOkHttpSsl(OkHttpClient.Builder builder) {
        try {
            // 自定义一个信任所有证书的TrustManager，添加SSLSocketFactory的时候要用到
            final X509TrustManager trustAllCert =
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    };
            final SSLSocketFactory sslSocketFactory = new SSLSocketFactoryCompat(trustAllCert);
            builder.sslSocketFactory(sslSocketFactory, trustAllCert);
            builder.hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class OkHttpWebViewClient extends WebViewClient {

        private static OkHttpClient okHttpClientWebView = null;

        private static void initOkHttp() {
            OkHttpClient.Builder builderWebView = new OkHttpClient.Builder();
            HttpLoggingInterceptor loggingInterceptorWebView = new HttpLoggingInterceptor("OkHttpWebView");

            if (Hawk.get(HawkConfig.DEBUG_OPEN, false)) {
                loggingInterceptorWebView.setPrintLevel(HttpLoggingInterceptor.Level.BODY);
                loggingInterceptorWebView.setColorLevel(Level.INFO);
            } else {
                loggingInterceptorWebView.setPrintLevel(HttpLoggingInterceptor.Level.NONE);
                loggingInterceptorWebView.setColorLevel(Level.OFF);
            }

            builderWebView.addInterceptor(loggingInterceptorWebView);

            builderWebView.readTimeout(DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
            builderWebView.writeTimeout(DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
            builderWebView.connectTimeout(DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);

            builderWebView.cookieJar(new CookieJar() {
                @Override
                public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                    String urlString = url.toString();
                    for (Cookie cookie : cookies) {
                        CookieManager.getInstance().setCookie(urlString, cookie.toString());
                    }
                }

                @Override
                public List<Cookie> loadForRequest(HttpUrl url) {
                    String urlString = url.toString();
                    String cookiesString = CookieManager.getInstance().getCookie(urlString);

                    if (cookiesString != null && !cookiesString.isEmpty()) {
                        String[] cookieHeaders = cookiesString.split(";");
                        List<Cookie> cookies = new ArrayList<>(cookieHeaders.length);

                        for (String header : cookieHeaders) {
                            cookies.add(Cookie.parse(url, header));
                        }

                        return cookies;
                    }
                    return Collections.emptyList();
                }
            });

            try {
                setOkHttpSsl(builderWebView);
            } catch (Throwable th) {
                th.printStackTrace();
            }
            okHttpClientWebView = builderWebView.build();
        }

        static {
            // 初始化 webview 的 okhttpclient 4.0 以下
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                initOkHttp();
            }
        }

        private static final List<String> SUPPORTED_SCHEMES = Arrays.asList("http", "https");
        private static final String CONTENT_TYPE_SVG = "image/svg+xml";

        @SuppressWarnings("deprecation")
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            if (okHttpClientWebView != null) {
                if (!SUPPORTED_SCHEMES.contains(Uri.parse(url).getScheme())) {
                    return null;
                }
                try {
                    Response rsp = request(url);
                    onLoadResource(view, url);
                    return new WebResourceResponse(
                            contentType(rsp),
                            contentCharset(rsp),
                            getInputStream(rsp));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return super.shouldInterceptRequest(view, url);
            } else {
                return super.shouldInterceptRequest(view, url);
            }
        }

        @NonNull
        private Response request(String url) throws IOException {
            final Request okReq = new Request.Builder().url(url)
                    .cacheControl(new CacheControl.Builder().noCache().build())
                    .build();
            final long startMillis = System.currentTimeMillis();
            final Response okResp = okHttpClientWebView.newCall(okReq).execute();
            final long dtMillis = System.currentTimeMillis() - startMillis;
            if (Hawk.get(HawkConfig.DEBUG_OPEN, false)) {
                LOG.i("Got response: " + okResp + " after " + dtMillis + "ms");
            }
            return okResp;
        }

        private String contentType(@NonNull Response rsp) {
            try {
                return rsp.body().contentType().type() + "/" + rsp.body().contentType().subtype();
            } catch (Throwable th) {
                return "";
            }
        }

        private String contentCharset(Response rsp) {
            try {
                return rsp.body().contentType().charset().toString();
            } catch (Throwable th) {
                return "";
            }
        }

        @NonNull
        private InputStream getInputStream(@NonNull Response rsp) throws IOException {
            InputStream inputStream = rsp.body().byteStream();

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT
                    && CONTENT_TYPE_SVG.equals(contentType(rsp))) {
                return transformSvgFile(inputStream);
            }

            return inputStream;
        }

        @NonNull
        static InputStream transformSvgFile(@NonNull InputStream inputStream) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("<svg")) {
                    line = line.replace("ex\"", "em\"").replace("ex;", "em;");
                }
                sb.append(line);
            }
            inputStream.close();
            return new ByteArrayInputStream(sb.toString().getBytes());
        }
    }
}
