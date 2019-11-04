package es.ua.eps.readphone;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    TelephonyManager telephonyManager;
    private static final int REQUEST_PHONE_INFO_PERMISSION = 200;
    GsmCellLocation gsmCellLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,new String[]{
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },REQUEST_PHONE_INFO_PERMISSION);
                return;
            }
        }

        textView = findViewById(R.id.infoTextView);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        gsmCellLocation = (GsmCellLocation) telephonyManager.getCellLocation();

        // Information variables
        String conectadoState;
        int conectionType;
        String imei;
        String networkOperatorName;
        String simOperatorName;
        String imsi;
        String simSerial;
        String isoCodeCountryNetwork;
        String isoCodeCountrySim;
        String softwareVersionIMEI;
        String answeringNumer;
        int networkType;
        String roamingState;
        int cellID;
        int lac;




        conectionType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? telephonyManager.getDataNetworkType() : -1;

        imei = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? telephonyManager.getImei() : "NO DATA";

        networkOperatorName = telephonyManager.getNetworkOperatorName();
        simOperatorName = telephonyManager.getSimOperatorName();
        imsi = telephonyManager.getSubscriberId();
        simSerial = telephonyManager.getSimSerialNumber();
        isoCodeCountryNetwork = telephonyManager.getNetworkCountryIso();
        isoCodeCountrySim = telephonyManager.getSimCountryIso();
        softwareVersionIMEI = telephonyManager.getDeviceSoftwareVersion();
        answeringNumer = telephonyManager.getVoiceMailNumber();
        networkType = telephonyManager.getNetworkType();
        cellID = gsmCellLocation.getCid();
        lac = gsmCellLocation.getLac();

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            // Si hay conexión a Internet en este momento

            conectadoState = "CONECTADO";

            roamingState = networkInfo.isRoaming() ? "TRUE" : "FALSE";

        } else {
            // No hay conexión a Internet en este momento
            conectadoState = "NO CONECTADO";
            roamingState = "FALSE";
        }


        String allInformation = "Estado de los datos: " + conectadoState + "\n"
                + "Tipo de conexión: " + conectionType + "\n"
                + " IMEI: " + imei + "\n"
                + " Operador de la red (físico): " + networkOperatorName + "\n"
                + " Operador de la SIM (virtual): " + simOperatorName + "\n"
                + " ID Subscriptor: " + imsi + "\n"
                + " Número de serie SIM: " + simSerial + "\n"
                + " Código ISO País Red: " + isoCodeCountryNetwork + "\n"
                + " Código ISO País SIM: " + isoCodeCountrySim + "\n"
                + " Versión software IMEI: " + softwareVersionIMEI + "\n"
                + " Número del contestador: " + answeringNumer + "\n"
                + " Tipo red móvil: " + networkType + "\n"
                + " Roaming activated: " + roamingState + "\n"
                + " ID de la celda: " + cellID + "\n"
                + " Código de localización de área: " + lac + "\n";


        textView.setText(allInformation);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == REQUEST_PHONE_INFO_PERMISSION)
        {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "You can't use camera without permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
