package es.ua.eps.readwifi;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PHONE_INFO_PERMISSION = 10;
    private WifiInfo wifiInfo;
    private WifiManager wifiManager;
    private Context context;
    private List<ScanResult> scanResults;
    private TextView textView;

    //Information variables
    String ssid;
    String bssid;
    String frecuency;
    String wifi_strength;
    String wifi_speed;
    String encryption;
    String channel;
    String ip;
    String netmask;
    String gateway;
    String dhcp_server;
    String dns1;
    String dns2;
    String dhcp_lease;
    String external_ip;
    String is_hidden;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context = getApplicationContext();
        this.textView = findViewById(R.id.info);
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    private void init() {
        // Comprobamos los permisos de la aplicación para utilizar los componentes requeridos
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
                // En caso de que el usuario aún no haya dado permiso a la aplicación
                // le pedimos que lo haga
                ActivityCompat.requestPermissions(this,new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                },REQUEST_PHONE_INFO_PERMISSION);
                return;
            }
        }

        wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();
        scanResults = wifiManager.getScanResults();

        this.ssid = wifiInfo.getSSID();
        this.bssid = wifiInfo.getBSSID();
        this.wifi_speed = String.valueOf(wifiInfo.getLinkSpeed());
        this.wifi_strength = String.valueOf(wifiInfo.getRssi());

        this.frecuency = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? String.valueOf(wifiInfo.getFrequency()) : "NO DATA";

        for (ScanResult sr : scanResults) {
            this.encryption = sr.capabilities;
            this.channel = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? String.valueOf(getChannelString(sr.channelWidth)) : "NO DATA";
        }

        // wifiInfo.getIpAddress() returns an integer. This is possible, because an IPv4-address contains 4 bytes (f.e. 192.168.255.2 ==> 4 times 1 byte),
        // just like an integer consumes 4 byte in Java/Android. By shifting the given integer number, you can determine each of the ip's byte values.
        this.ip = getIpFormat(wifiInfo.getIpAddress());

        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();

        this.netmask = getIpFormat(dhcpInfo.netmask);
        this.dns1 = getIpFormat(dhcpInfo.dns1);
        this.dns2 = getIpFormat(dhcpInfo.dns2);
        this.gateway = getIpFormat(dhcpInfo.gateway);
        this.dhcp_server = getIpFormat(dhcpInfo.serverAddress);
        this.dhcp_lease = getHourFormat(dhcpInfo.leaseDuration);

        this.is_hidden = wifiInfo.getHiddenSSID() ? "Network is hidden" : "Network is visible";

        IPfinder iPfinder = new IPfinder();
        try {
            //con el método get después de execute obtemenos el resultado de doInBackground y así no tocamos elementos UI en el asyntask
            external_ip = iPfinder.execute().get();
            String allInformation = "SSID: " + ssid + "\n\n"
                    + "BSSID: " + bssid + "\n\n"
                    + " WiFi speed: " + wifi_speed + " Mbps \n\n"
                    + " WiFi strength: " + wifi_strength + " dBm\n\n"
                    + " Encryption: " + encryption + "\n\n"
                    + " Frecuency: " + frecuency + "\n\n"
                    + " Channel: " + channel + "\n\n"
                    + " IP: " + ip + "\n\n"
                    + " Netmask: " + netmask + "\n\n"
                    + " Gateway: " + gateway + "\n\n"
                    + " DHPC Server: " + dhcp_server + "\n\n"
                    + " DNS1: " + dns1 + "\n\n"
                    + " DNS2: " + dns2 + "\n\n"
                    + " DHCP lease: " + dhcp_lease + " (hh:mm:ss)\n\n"
                    + " External IP: " + external_ip + "\n\n"
                    + " Hidden ?: " + is_hidden + "\n\n";


            textView.setText(allInformation);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



    }

    private String getHourFormat(int leaseDuration) {
        String result;

        int seconds = leaseDuration % 60;
        int minutes = leaseDuration / 60;
        int hours = minutes / 60;
        minutes = minutes % 60;

        result = hours<24 ? hours + ":" + minutes + ":" + seconds : "24:00:00";

        return result;
    }

    private String getChannelString(int channelWidth) {
        String channelString = "NO DATA";

        switch (channelWidth) {
            case ScanResult.CHANNEL_WIDTH_20MHZ:
                channelString = "20MHZ";
                break;
            case ScanResult.CHANNEL_WIDTH_40MHZ:
                channelString = "40MHZ";
                break;
            case ScanResult.CHANNEL_WIDTH_80MHZ:
                channelString = "80MHZ";
                break;
            case ScanResult.CHANNEL_WIDTH_160MHZ:
                channelString = "160MHZ";
                break;
            case ScanResult.CHANNEL_WIDTH_80MHZ_PLUS_MHZ:
                channelString = "80MHZ_PLUS_MHZ";
                break;
        }

        return channelString;
    }

    private static String getIpFormat(int code) {
        String result;

        result = String.format("%d.%d.%d.%d", (code & 0xff), (code >> 8 & 0xff), (code >> 16 & 0xff),
                (code >> 24 & 0xff));

        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == REQUEST_PHONE_INFO_PERMISSION) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "You can't use the app without permission", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }




}
