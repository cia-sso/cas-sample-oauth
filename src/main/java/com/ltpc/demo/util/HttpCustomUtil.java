package com.ltpc.demo.util;

import com.ltpc.demo.Exception.PaasRuntimeException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: liutong
 * Date: 2018/6/6
 * Time: 下午4:25
 * Description:
 **/
public class HttpCustomUtil {
    private HttpCustomUtil() {
        // 禁止私有化
    }

    private static transient final Logger log = LoggerFactory.getLogger(HttpCustomUtil.class);

    public static final String CHARSET_UTF8 = "UTF-8";
    /**
     * 默认连接超时1分钟
     */
    private static final int _cnnTimeOut = 60000;

    /**
     * 默认请求超时时间3分钟
     */
    private static final int _cnnRequestTimeOut = 180000;

    /**
     * 默认套接字超时时间5分钟
     */
    private static final int _socketTimeOut = 300000;

    private static PoolingHttpClientConnectionManager connMgr;

    private static RequestConfig requestConfig;

    static {

        RequestConfig.Builder configBuilder = RequestConfig.custom();
        // 设置连接超时
        configBuilder.setConnectTimeout(_cnnTimeOut);
        // 设置读取超时
        configBuilder.setSocketTimeout(_socketTimeOut);
        configBuilder.setCookieSpec(CookieSpecs.STANDARD_STRICT);
        // 设置从连接池获取连接实例的超时
        configBuilder.setConnectionRequestTimeout(_cnnRequestTimeOut);
        requestConfig = configBuilder.build();
    }

    /**
     * 发送post请求
     *
     * @param urlAddr
     *            地址
     * @param param
     *            参数对象
     * @param readCharset
     *            返回流编码格式
     * @param cnnTimeOut
     *            连接超时时间
     * @param cnnRequestTimeOut
     *            请求超时时间
     * @param socketTimeOut
     *            套接字超时时间
     * @return
     */
    public static String doPost(String urlAddr, StringEntity param, Map<String, String> header, String readCharset,
                                int cnnTimeOut, int cnnRequestTimeOut, int socketTimeOut) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        try {
            RequestConfig config = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT)
                    .setConnectTimeout(cnnTimeOut).setConnectionRequestTimeout(cnnRequestTimeOut)
                    .setSocketTimeout(socketTimeOut).build();
            httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
            HttpPost httpPost = new HttpPost(urlAddr);
            httpPost.setEntity(param);
            if (null != header) {
                for (String key : header.keySet()) {
                    httpPost.addHeader(key, header.get(key));
                }
            }
            response = httpClient.execute(httpPost);
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity, readCharset);
            } else {
                throw new PaasRuntimeException("" + response.getStatusLine().getStatusCode(),
                        response.getStatusLine().toString());
            }
        } catch (Exception e) {
            throw new PaasRuntimeException("", e);
        } finally {
            if (null != response) {
                try {
                    EntityUtils.consume(response.getEntity());
                    response.close();
                } catch (IOException e) {
                    log.error("", e);
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    log.error("", e);
                }
            }
        }
    }

    /**
     * post请求
     *
     * @param urlAddr
     * @param param
     * @return
     */
    public static String doPost(String urlAddr, StringEntity param) {
        return doPost(urlAddr, param, null, CHARSET_UTF8, _cnnTimeOut, _cnnRequestTimeOut, _socketTimeOut);
    }

    public static String doPost(String urlAddr, Map<String, String> param, String paraCharset, String sendCharset,
                                String readCharset, String contentType, int cnnTimeOut, int cnnRequestTimeOut, int socketTimeOut) {
        try {
            List<NameValuePair> pairList = new ArrayList<>(param.size());
            for (Map.Entry<String, String> entry : param.entrySet()) {
                NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue().toString());
                pairList.add(pair);
            }

            StringEntity entity = new UrlEncodedFormEntity(pairList, paraCharset);
            entity.setContentType(contentType);
            entity.setContentEncoding(sendCharset);
            return doPost(urlAddr, entity, null, readCharset, cnnTimeOut, cnnTimeOut, cnnTimeOut);
        } catch (UnsupportedEncodingException t) {
            throw new PaasRuntimeException("", t);
        }
    }

    public static String doPost(String urlAddr, Map<String, String> param, String paraCharset, String sendCharset,
                                String readCharset, String contentType, int cnnTimeOut) {
        return doPost(urlAddr, param, paraCharset, sendCharset, readCharset, contentType, cnnTimeOut, cnnTimeOut,
                cnnTimeOut);
    }

    public static String doPost(String urlAddr, Map<String, String> param, int cnnTimeOut) {
        return doPost(urlAddr, param, CHARSET_UTF8, CHARSET_UTF8, CHARSET_UTF8,CHARSET_UTF8, cnnTimeOut);

    }

    public static String doPost(String urlAddr, Map<String, String> param) {
        //
        return doPost(urlAddr, param, CHARSET_UTF8, CHARSET_UTF8, CHARSET_UTF8,CHARSET_UTF8, _cnnTimeOut, _cnnRequestTimeOut, _socketTimeOut);
    }

    public static String doGet(String urlAddr, Map<String, String> param, Map<String, String> header, String paraCharset,
                               String sendCharset, String readCharset, int cnnTimeOut, int cnnRequestTimeOut, int socketTimeOut) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        try {
            RequestConfig config = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT)
                    .setConnectTimeout(cnnTimeOut).setConnectionRequestTimeout(cnnRequestTimeOut)
                    .setSocketTimeout(socketTimeOut).build();
            httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

            boolean hasQM = urlAddr.indexOf("?") >= 0;
            StringBuffer sb = new StringBuffer();
            if (null != param) {
                int i = 0;
                for (String key : param.keySet()) {
                    if (i == 0 && !hasQM) {
                        sb.append("?");
                    } else
                        sb.append("&");
                    sb.append(key).append("=").append(URLEncoder.encode(param.get(key), paraCharset));
                    i++;
                }
            }
            urlAddr += sb.toString();
            HttpGet httpGet = new HttpGet(urlAddr);
            if (null != header) {
                for (String key : header.keySet()) {
                    httpGet.addHeader(key, header.get(key));
                }
            }
            response = httpClient.execute(httpGet);
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity, readCharset);
            } else {
                throw new PaasRuntimeException("" + response.getStatusLine().getStatusCode(),
                        response.getStatusLine().toString());
            }
        } catch (Exception e) {
            throw new PaasRuntimeException("", e);
        } finally {
            if (null != response) {
                try {
                    EntityUtils.consume(response.getEntity());
                    response.close();
                } catch (IOException e) {
                    log.error("", e);
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    log.error("", e);
                }
            }
        }
    }

    public static String doGet(String urlAddr, Map<String, String> param, Map<String, String> header, int cnnTimeOut,
                               int cnnRequestTimeOut, int socketTimeOut) {
        return doGet(urlAddr, param, header, CHARSET_UTF8, CHARSET_UTF8, CHARSET_UTF8,
                cnnTimeOut, cnnRequestTimeOut, socketTimeOut);
    }

    public static String doGet(String urlAddr, Map<String, String> param, Map<String, String> header) {
        return doGet(urlAddr, param, header, CHARSET_UTF8, CHARSET_UTF8, CHARSET_UTF8,
                _cnnTimeOut, _cnnRequestTimeOut, _socketTimeOut);
    }

    public static String doGet(String urlAddr, Map<String, String> param) {
        return doGet(urlAddr, param, null, CHARSET_UTF8, CHARSET_UTF8, CHARSET_UTF8,
                _cnnTimeOut, _cnnRequestTimeOut, _socketTimeOut);
    }

    /**
     * 发送 SSL POST 请求（HTTPS），K-V形式
     *
     * @param urlAddr
     *            API接口URL
     * @param params
     *            参数map
     * @return
     */
    public static String doPostSSL(String urlAddr, Map<String, String> headers, Map<String, Object> params) {
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", createSSLConnSocketFactory())
                .build();
        // 设置连接池
        connMgr = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        // 设置连接池大小
        connMgr.setMaxTotal(100);
        connMgr.setDefaultMaxPerRoute(connMgr.getMaxTotal());
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(createSSLConnSocketFactory())
                .setConnectionManager(connMgr).setDefaultRequestConfig(requestConfig).build();
        HttpPost httpPost = new HttpPost(urlAddr);
        CloseableHttpResponse response = null;

        try {
            httpPost.setConfig(requestConfig);
            if(params!=null && params.size() > 0) {
                List<NameValuePair> pairList = new ArrayList<NameValuePair>(params.size());
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue().toString());
                    pairList.add(pair);
                }
                httpPost.setEntity(new UrlEncodedFormEntity(pairList, CHARSET_UTF8));
            }
            if (null != headers && !headers.isEmpty()) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    httpPost.addHeader(header.getKey(), header.getValue());
                }
            }
            response = httpClient.execute(httpPost);
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity, CHARSET_UTF8);
            } else {
                throw new PaasRuntimeException("" + response.getStatusLine().getStatusCode(),
                        response.getStatusLine().toString());
            }
        } catch (Exception e) {
            throw new PaasRuntimeException("", e);
        } finally {
            if (null != response) {
                try {
                    EntityUtils.consume(response.getEntity());
                    response.close();
                } catch (IOException e) {
                    log.error("", e);
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    log.error("", e);
                }
            }
        }
    }

    public static String doGetSSL(String urlAddr, Map<String, String> param, Map<String, String> headers) {
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
                .register("http", PlainConnectionSocketFactory.INSTANCE).register("https", createSSLConnSocketFactory())
                .build();
        // 设置连接池
        connMgr = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        // 设置连接池大小
        connMgr.setMaxTotal(100);
        connMgr.setDefaultMaxPerRoute(connMgr.getMaxTotal());
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(createSSLConnSocketFactory())
                .setConnectionManager(connMgr).setDefaultRequestConfig(requestConfig).build();
        HttpGet httpGet = null;
        CloseableHttpResponse response = null;

        try {
            boolean hasQM = urlAddr.indexOf("?") >= 0;
            StringBuffer sb = new StringBuffer();
            if (null != param) {
                int i = 0;
                for (String key : param.keySet()) {
                    if (i == 0 && !hasQM) {
                        sb.append("?");
                    } else
                        sb.append("&");
                    sb.append(key).append("=").append(URLEncoder.encode(param.get(key), CHARSET_UTF8));
                    i++;
                }
            }
            urlAddr += sb.toString();
            httpGet = new HttpGet(urlAddr);
            httpGet.setConfig(requestConfig);
            if (null != headers && !headers.isEmpty()) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    httpGet.addHeader(header.getKey(), header.getValue());
                }
            }
            response = httpClient.execute(httpGet);
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity, CHARSET_UTF8);
            } else {
                throw new PaasRuntimeException("" + response.getStatusLine().getStatusCode(),
                        response.getStatusLine().toString());
            }
        } catch (Exception e) {
            throw new PaasRuntimeException("", e);
        } finally {
            if (null != response) {
                try {
                    EntityUtils.consume(response.getEntity());
                    response.close();
                } catch (IOException e) {
                    log.error("", e);
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    log.error("", e);
                }
            }
        }
    }

    /**
     * 创建SSL安全连接
     *
     * @return
     */
    private static SSLConnectionSocketFactory createSSLConnSocketFactory() {
        SSLConnectionSocketFactory sslsf = null;
        try {
            TrustManager manager = new X509TrustManager() {

                @Override
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new TrustManager[] { manager }, null);
            sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1", "SSLv3" }, null,
                    SSLConnectionSocketFactory.getDefaultHostnameVerifier());

        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return sslsf;
    }
}
