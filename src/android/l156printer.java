package cordova.plugin.l156printer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.caysn.autoreplyprint.sample.Task;
import com.caysn.autoreplyprint.sample.TestTask;
import java.io.UnsupportedEncodingException;
import javax.naming.Context;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class l156printer extends CordovaPlugin {

  public String ReceiptContent;
  public String BarcodeData;
  public String QRCodeData;
  public String ExtraOrderData;
  public byte[] printContent1;
  public byte[] extraPrintContent1 = null;
  public String printQRCodeContent;
  private Context context = null;
  private Context tContext = null;
  public Context mContext;
  private TextView txt;
  private String str;
  private String s1;

  private Task task = new Task();

  @Override
  public boolean execute(
    String action,
    JSONArray args,
    CallbackContext callbackContext
  ) throws JSONException {
    if (action.equals("printString")) {
      ReceiptContent = data.getString(0);
      QRCodeData = data.getString(1);
      ExtraOrderData = data.getString(2);
      printerPrint();
      return true;
    }
    return false;
  }

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    tContext = webView.getContext();
    context = this.cordova.getActivity().getApplicationContext();
    //   mHandler = new Handler();
    task.bUSB = true;
    task.strUSBPort = "usb"; //cbxListUSB.getText();
    System.out.println("Plugin Init  = 1");
    initPrinter();
    Toast
      .makeText(tContext, "L156 Printer initialized...", Toast.LENGTH_SHORT)
      .show();
    //   UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    //   usbAutoConn(manager);

  }

  public void onToast(String msg) {
    Toast.makeText(tContext, (String) msg, Toast.LENGTH_SHORT).show();
  }

  public void printerPrint() {
    try {
      // mPrinter.init();
      printContent1 = strToByteArray(ReceiptContent, "UTF-8");

      if (ExtraOrderData != "" || ExtraOrderData != null) {
        extraPrintContent1 = strToByteArray(ExtraOrderData, "UTF-8");
      }
    } catch (UnsupportedEncodingException e2) {
      // TODO Auto-generated catch block
      e2.printStackTrace();
    }

    try {
      task.PosPrint(
        true,
        true,
        printContent1,
        extraPrintContent1,
        QRCodeData,
        null
      );
      // device.printText(ReceiptContent);
    } catch (Exception e) {
      Toast.makeText(tContext, e.getMessage(), Toast.LENGTH_SHORT).show();
      System.out.println("Plugin Init  = 4" + e.getMessage());
      e.printStackTrace();
    }
  }

  public static byte[] strToByteArray(String str, String encodeStr)
    throws UnsupportedEncodingException {
    if (str == null) {
      return null;
    }
    byte[] byteArray = null;
    if (encodeStr.equals("IBM852")) {
      byteArray = str.getBytes("IBM852");
    } else if (encodeStr.equals("GB2312")) {
      byteArray = str.getBytes("GB2312");
    } else if (encodeStr.equals("ISO-8859-1")) {
      byteArray = str.getBytes("ISO-8859-1");
    } else if (encodeStr.equals("UTF-8")) {
      byteArray = str.getBytes("UTF-8");
    } else {
      byteArray = str.getBytes();
    }
    return byteArray;
  }
}
