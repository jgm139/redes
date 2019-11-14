package es.ua.eps.readwifi;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PHONE_INFO_PERMISSION = 10;
    private WifiInfo wifiInfo;
    private WifiManager wifiManager;
    private List<ScanResult> scanResults;
    private DhcpInfo dhcpInfo;
    private TextView textView;
    private Context context;

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

        // Comprobamos que el usuario esté conectado a una red WiFi
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!mWifi.isConnected()) {
            // En caso de que no esté conectado se lo hacemos saber mediante un mensaje
            Toast.makeText(this, "Debes estar conectado a una red WiFi", Toast.LENGTH_LONG).show();
            return;
        }

        // A través de la clase WifiManager obtenemos las clases que nos darán la información requerida
        wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();
        scanResults = wifiManager.getScanResults();
        dhcpInfo = wifiManager.getDhcpInfo();

        setWifiInfo();
        setDhcpInfo();

        // A través de esta clase obtenemos la IP externa
        IPfinder iPfinder = new IPfinder();

        try {
            //Con el método get después de execute obtenenos el resultado de doInBackground y así no tocamos elementos UI en el asynctask
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
                    + " Hidden?: " + is_hidden + "\n\n";

            textView.setText(allInformation);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void setDhcpInfo() {
        // Hay un bug de Android con la variable netmask de DhcpInfo, siempre sale 0.0.0.0
        // getIpFormat(dhcpInfo.netmask)
        this.netmask = getNetmask(dhcpInfo.ipAddress);

        this.dns1 = getIpFormat(dhcpInfo.dns1);
        this.dns2 = getIpFormat(dhcpInfo.dns2);
        this.gateway = getIpFormat(dhcpInfo.gateway);
        this.dhcp_server = getIpFormat(dhcpInfo.serverAddress);
        this.dhcp_lease = getHourFormat(dhcpInfo.leaseDuration);
    }

    private void setWifiInfo() {
        this.ssid = wifiInfo.getSSID();
        this.bssid = wifiInfo.getBSSID();
        this.wifi_speed = String.valueOf(wifiInfo.getLinkSpeed());
        this.wifi_strength = String.valueOf(wifiInfo.getRssi());

        this.frecuency = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? String.valueOf(wifiInfo.getFrequency()) : "NO DATA";

        for (ScanResult sr : scanResults) {
            this.encryption = sr.capabilities;
            this.channel = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? String.valueOf(getChannelString(sr.channelWidth)) : "NO DATA";
        }

        // wifiInfo.getIpAddress () devuelve un entero.
        // Esto es posible, porque una dirección IPv4 contiene 4 bytes (por ejemplo, 192.168.255.2 ==> 4 veces 1 byte),
        // al igual que un número entero consume 4 bytes en Java / Android. Al cambiar el número entero dado,
        // puede determinar cada uno de los valores de bytes de la ip.
        this.ip = getIpFormat(wifiInfo.getIpAddress());

        this.is_hidden = wifiInfo.getHiddenSSID() ? "Network is hidden" : "Network is visible";
    }

    private String getNetmask(int dhcpIpAdress) {
        int netPrefix = 0;

        try {
            InetAddress inetAddress = InetAddress.getByName(getIpFormat(dhcpIpAdress));
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
            for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                netPrefix = address.getNetworkPrefixLength();
            }
        } catch (IOException e) {
            Log.e("DebugApp", e.getMessage());
        }

        return getIpFormat(prefixToNetmask(netPrefix));
    }

    private int prefixToNetmask(int netPrefix) {
        if(netPrefix < 0 || netPrefix > 32) {
            throw new IllegalArgumentException("Invalid prefix length");
        }

        int value = 0xffffffff << (32 - netPrefix);
        return Integer.reverseBytes(value);
    }

    private String getHourFormat(int leaseDuration) {
        String result;

        int seconds = leaseDuration % 60;
        int minutes = leaseDuration / 60;
        int hours = minutes / 60;
        minutes = minutes % 60;

        result = hours < 24 ? hours + ":" + minutes + ":" + seconds : "24:00:00";

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

        result = String.format("%d.%d.%d.%d", (code & 0xff), (code >> 8 & 0xff), (code >> 16 & 0xff), (code >> 24 & 0xff));

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
