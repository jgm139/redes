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

    // Variables término con su correspondiente información
    String conectadoState;
    String conectionType;
    String imei;
    String networkOperatorName;
    String simOperatorName;
    String imsi;
    String simSerial;
    String isoCodeCountryNetwork;
    String isoCodeCountrySim;
    String softwareVersionIMEI;
    String answeringNumer;
    String networkType;
    String roamingState;
    int cellID;
    int lac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.infoTextView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    private void init(){

        // Comprobamos los permisos de la aplicación para utilizar los componentes requeridos
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // En caso de que el usuario aún no haya dado permiso a la aplicación
                // le pedimos que lo haga
                ActivityCompat.requestPermissions(this,new String[]{
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },REQUEST_PHONE_INFO_PERMISSION);
                return;
            }
        }

        // Mediante la clase TelephonyManager accedemos a la información de la red celular del móvil
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        // Mediante la clase GsmCellLocation obtendremos información de la celda correspondiente
        gsmCellLocation = (GsmCellLocation) telephonyManager.getCellLocation();

        // Tipo de conexión
        // Como la función getDataNetworkType devuelve el enumerado correspondiente al tipo de conexión
        // lo casteamos a String con la función propia getConectionTypeString
        conectionType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? getConectionTypeString(telephonyManager.getDataNetworkType()) : "NO DATA";

        // IMEI - Identidad internacional del equipo móvil
        imei = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? telephonyManager.getImei() : "NO DATA";

        // Nombre del operador red - físico
        networkOperatorName = telephonyManager.getNetworkOperatorName();

        // Nombre del operador de la SIM - virtual
        simOperatorName = telephonyManager.getSimOperatorName();

        // IMSI - Identificador de la línea o servicio
        imsi = telephonyManager.getSubscriberId();

        // Número de serie SIM
        simSerial = telephonyManager.getSimSerialNumber();

        // Código ISO País Red
        isoCodeCountryNetwork = telephonyManager.getNetworkCountryIso();

        // Código ISO País SIM
        isoCodeCountrySim = telephonyManager.getSimCountryIso();

        // Versión software IMEI
        softwareVersionIMEI = telephonyManager.getDeviceSoftwareVersion();

        // Número del contestador
        answeringNumer = telephonyManager.getVoiceMailNumber();

        // Tipo de red móvil
        // Como la función getNetworkType devuelve el enumerado correspondiente al tipo de red
        // lo casteamos a String con la función propia getNetworkTypeString
        networkType = getNetworkTypeString(telephonyManager.getNetworkType());

        // ID de la celda
        cellID = gsmCellLocation.getCid();

        // Código de localización de área
        lac = gsmCellLocation.getLac();

        // Mediante la clase ConnectivityManager obtenemos información sobre el estado de conexión de la red
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            // Sí hay conexión a Internet en este momento
            conectadoState = "CONECTADO";

            // Si está conectado a la red, comprobamos si está activado el Roaming
            roamingState = networkInfo.isRoaming() ? "TRUE" : "FALSE";
        } else {
            // No hay conexión a Internet en este momento
            conectadoState = "NO CONECTADO";
            roamingState = "FALSE";
        }


        String allInformation = "Estado de los datos: " + conectadoState + "\n\n"
                + "Tipo de conexión: " + conectionType + "\n\n"
                + " IMEI: " + imei + "\n\n"
                + " Operador de la red (físico): " + networkOperatorName + "\n\n"
                + " Operador de la SIM (virtual): " + simOperatorName + "\n\n"
                + " ID Subscriptor: " + imsi + "\n\n"
                + " Número de serie SIM: " + simSerial + "\n\n"
                + " Código ISO País Red: " + isoCodeCountryNetwork + "\n\n"
                + " Código ISO País SIM: " + isoCodeCountrySim + "\n\n"
                + " Versión software IMEI: " + softwareVersionIMEI + "\n\n"
                + " Número del contestador: " + answeringNumer + "\n\n"
                + " Tipo red móvil: " + networkType + "\n\n"
                + " Roaming activated: " + roamingState + "\n\n"
                + " ID de la celda: " + cellID + "\n\n"
                + " Código de localización de área: " + lac + "\n\n";


        textView.setText(allInformation);
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

    private String getNetworkTypeString(int networkType) {
        String networkTypeinString = getConectionTypeString(networkType);

        if (networkTypeinString.equals("NO RECOGNISED")) {
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GSM:
                    networkTypeinString = "GSM";
                    break;
                case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                    networkTypeinString = "TD SCDMA";
                    break;
                case TelephonyManager.NETWORK_TYPE_IWLAN:
                    networkTypeinString = "IWLAN";
                    break;
                default:
            }
        }

        return networkTypeinString;
    }

    private String getConectionTypeString(int conectionType) {
        String conectionTypeinString;

        switch (conectionType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
                conectionTypeinString = "GPRS";
                break;
            case TelephonyManager.NETWORK_TYPE_EDGE:
                conectionTypeinString = "EDGE";
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
                conectionTypeinString = "UMTS";
                break;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                conectionTypeinString = "HSDPA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                conectionTypeinString = "HSUPA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSPA:
                conectionTypeinString = "HSPA";
                break;
            case TelephonyManager.NETWORK_TYPE_CDMA:
                conectionTypeinString = "CDMA";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                conectionTypeinString = "EVDO_0";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                conectionTypeinString = "EVDO_A";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                conectionTypeinString = "EVDO_B";
                break;
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                conectionTypeinString = "1xRTT";
                break;
            case TelephonyManager.NETWORK_TYPE_IDEN:
                conectionTypeinString = "IDEN";
                break;
            case TelephonyManager.NETWORK_TYPE_LTE:
                conectionTypeinString = "LTE";
                break;
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                conectionTypeinString = "EHRPD";
                break;
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                conectionTypeinString = "HSPAP";
                break;
            default:
                conectionTypeinString = "NO RECOGNISED";
        }

        return conectionTypeinString;
    }
}
