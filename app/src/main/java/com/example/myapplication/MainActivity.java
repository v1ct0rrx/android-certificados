package com.example.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.StrictMode;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });



        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try{

                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);

                    RestTemplate restTemplate = MainActivity.this.ejemplo();
                    String url = "https://api-lite.brlabsdev.com:9451/auraWebServices/service/api/v1/configuration/version";
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Content-Type", "application/json");
                    HttpEntity<String> request = new HttpEntity<String>(headers);
                    restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
                    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

                    System.out.println(response);

                }catch(Exception ex){
                    System.out.println(ex.fillInStackTrace());
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public RestTemplate ejemplo() throws Exception {

        InputStream is = getResources().openRawResource(R.raw.victor);
        InputStream is2 = getResources().openRawResource(R.raw.lite);

        SSLContext sslcontext = null;
        TrustManager[] trustManagers = null;
        KeyManager[] keyManagers = null;
        CredentialsProvider credsProvider = null;
        CloseableHttpClient httpClient = null;

        HttpHeaders headers = null;

        sslcontext = SSLContext.getInstance("TLS");
       // trustManagers = getTrustManagers("JKS", is2, "123456");
        keyManagers = getKeyManagers("pkcs12", is, "Victor");
        sslcontext.init(keyManagers, trustManagers, new SecureRandom());



        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[] { "TLSv1", "TLSv1.1", "TLSv1.2" },
                null, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        //AUTH BASIC
        credsProvider = new BasicCredentialsProvider();
        httpClient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .setSSLSocketFactory(sslsf)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(requestFactory);

        return restTemplate;

    }

    protected static KeyManager[] getKeyManagers(String keyStoreType, InputStream keyStoreFile, String keyStorePassword) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(keyStoreFile, keyStorePassword.toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyStorePassword.toCharArray());
        return kmf.getKeyManagers();
    }

    protected static TrustManager[] getTrustManagers(String trustStoreType, InputStream trustStoreFile, String trustStorePassword) throws Exception {
        KeyStore trustStore = KeyStore.getInstance(trustStoreType);
        trustStore.load(trustStoreFile, trustStorePassword.toCharArray());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        return tmf.getTrustManagers();
    }
}
