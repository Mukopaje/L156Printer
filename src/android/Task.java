package com.caysn.autoreplyprint.sample;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.TextView;
import com.caysn.autoreplyprint.AutoReplyPrint;
import com.sun.jna.Pointer;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Task implements Runnable {

  private static final String TAG = "Task";

  // 端口部分参数
  public boolean bBT2;
  public boolean bBT4;
  public boolean bNET;
  public boolean bUSB;
  public boolean bCOM;
  public boolean bWiFiP2P;
  public String strBT2Address;
  public String strBT4Address;
  public String strNETAddress;
  public String strUSBPort;
  public String strCOMPort;
  public int nCOMBaudrate;
  public int nCOMFlowControl;
  public String strWiFiP2PAddress;

  // 界面部分参数
  public MainActivity activity;
  public TextView tvInfo;

  public int mTestCount;
  public int mTestInterval;

  public boolean bPrintTime;
  public boolean bPrintText;
  public boolean bPrintQRCode;
  public boolean bPrintImage;
  public Bitmap mBitmap;
  public boolean bWaitPrintFinished;

  public boolean mTaskEnabled;

  @Override
  public void run() {
    mTaskEnabled = true;
    ShowMessage("测试开始");
    List<SimpleTestResult> results = new ArrayList<SimpleTestResult>();
    for (int nTestIndex = 0; nTestIndex < mTestCount; ++nTestIndex) {
      if (!mTaskEnabled) break;
      Log.i(TAG, "正在执行第" + (nTestIndex + 1) + "次测试 ");
      ShowMessage("正在执行第" + (nTestIndex + 1) + "次测试 ");
      SimpleTestResult result = null;
      try {
        result = SimpleTest(nTestIndex + 1);
      } catch (Throwable tr) {
        tr.printStackTrace();
      }
      results.add(result);
      Log.i(
        TAG,
        "第" +
        (nTestIndex + 1) +
        "次测试 " +
        (
          (result != null) && (result.result_code == SimpleTestResult.Result_OK)
            ? "通过"
            : "失败"
        )
      );
      ShowMessage(
        "第" +
        (nTestIndex + 1) +
        "次测试 " +
        (
          (result != null) && (result.result_code == SimpleTestResult.Result_OK)
            ? "通过"
            : "失败"
        )
      );
      if (mTestInterval > 0) {
        try {
          Thread.sleep(mTestInterval);
        } catch (Throwable tr) {
          tr.printStackTrace();
        }
      }
    }
    ShowMessage("测试结束" + "\r\n" + CaculateResults(results));
    // EnableUI();
  }

  private Pointer OpenPort() {
    Pointer h = Pointer.NULL;
    String[] listUsbPort = AutoReplyPrint.CP_Port_EnumUsb_Helper.EnumUsb();
    if (listUsbPort != null) {
      for (String usbPort : listUsbPort) {
        if (usbPort.contains("0x4B43") || usbPort.contains("0x0FE6")) {
          h = AutoReplyPrint.INSTANCE.CP_Port_OpenUsb(usbPort, 1);
          break;
        }
      }
    }
    Log.i(TAG, h == Pointer.NULL ? "OpenPort Failed" : "OpenPort Success");
    if (h == Pointer.NULL) {
      showMessageOnUiThread("OpenPort Failed");
    }
    return h;
  }

  private String CaculateResults(List<SimpleTestResult> results) {
    long test_ok_count = 0;
    long test_ok_open_ms = 0;
    String open_failed_list = "";
    String begin_query_failed_list = "";
    String end_query_failed_list = "";
    for (int i = 0; i < results.size(); ++i) {
      SimpleTestResult result = results.get(i);
      if (result != null) {
        switch (result.result_code) {
          case SimpleTestResult.Result_OK:
            test_ok_count++;
            test_ok_open_ms += result.open_ms;
            break;
          case SimpleTestResult.Result_OpenFailed:
            open_failed_list += "" + (i + 1) + ",";
            break;
          case SimpleTestResult.Result_BeginQueryFailed:
            begin_query_failed_list += "" + (i + 1) + ",";
            break;
          case SimpleTestResult.Result_EndQueryFailed:
            end_query_failed_list += "" + (i + 1) + ",";
            break;
        }
      } else {
        open_failed_list += "" + (i + 1) + ",";
      }
    }
    String msg = "";
    msg += "测试通过:" + test_ok_count + "\r\n";
    if (test_ok_count > 0) msg +=
      "打开端口平均耗时:" + test_ok_open_ms / test_ok_count + "ms\r\n";
    if (test_ok_count < results.size()) {
      msg += "打开失败:" + open_failed_list + "\r\n";
      msg += "打印前查询失败:" + begin_query_failed_list + "\r\n";
      msg += "打印后查询失败:" + end_query_failed_list + "\r\n";
    }
    return msg;
  }

  class SimpleTestResult {

    public static final int Result_OK = 0;
    public static final int Result_OpenFailed = -1;
    public static final int Result_BeginQueryFailed = -2;
    public static final int Result_EndQueryFailed = -3;

    public int result_code = 0;
    public long open_ms = 0;
  }

  private SimpleTestResult SimpleTest(
    String order,
    String extraOrder,
    String qrcode
  ) {
    SimpleTestResult result = new SimpleTestResult();

    long beginTime = System.currentTimeMillis();
    Pointer h = OpenPort();
    long endTime = System.currentTimeMillis();
    result.open_ms = endTime - beginTime;

    if (AutoReplyPrint.INSTANCE.CP_Port_IsOpened(h)) {
      if (AutoReplyPrint.INSTANCE.CP_Pos_QueryRTStatus(h, 3000) != 0) {
        // if (bPrintTime) {
        //     AutoReplyPrint.INSTANCE.CP_Pos_SetAlignment(h, 0);
        //     AutoReplyPrint.INSTANCE.CP_Pos_PrintText(h, nTestIndex + " " + "Open   UsedTime:" + result.open_ms + "\r\n");
        //     AutoReplyPrint.INSTANCE.CP_Pos_SetAlignment(h, 1);
        // }
        if (order != null) {
          AutoReplyPrint.INSTANCE.CP_Pos_PrintText(h, order);
        }
        if (qrcode) {
          AutoReplyPrint.INSTANCE.CP_Pos_SetBarcodeUnitWidth(h, 6);
          AutoReplyPrint.INSTANCE.CP_Pos_PrintQRCode(
            h,
            10,
            AutoReplyPrint.CP_QRCodeECC_L,
            "http://www.caysn.com/"
          );
        }
        if (bPrintImage) {
          if (mBitmap != null) {
            AutoReplyPrint.CP_Pos_PrintRasterImageFromData_Helper.PrintRasterImageFromBitmap(
              h,
              mBitmap.getWidth(),
              mBitmap.getHeight(),
              mBitmap,
              AutoReplyPrint.CP_ImageBinarizationMethod_ErrorDiffusion,
              AutoReplyPrint.CP_ImageCompressionMethod_None
            );
          }
        }
        if (extraOrder != null) {
          AutoReplyPrint.INSTANCE.CP_Pos_PrintText(h, extraOrder);
        }
        // if (bWaitPrintFinished) {
        //     if (AutoReplyPrint.INSTANCE.CP_Pos_QueryPrintResult(h, 10000)) {
        //         result.result_code = SimpleTestResult.Result_OK;
        //     } else {
        //         result.result_code = SimpleTestResult.Result_EndQueryFailed;
        //     }
        // } else {
        //     if (AutoReplyPrint.INSTANCE.CP_Pos_QueryRTStatus(h, 10000) != 0) {
        //         result.result_code = SimpleTestResult.Result_OK;
        //     } else {
        //         result.result_code = SimpleTestResult.Result_EndQueryFailed;
        //     }
        // }
      } else {
        result.result_code = SimpleTestResult.Result_BeginQueryFailed;
      }
      AutoReplyPrint.INSTANCE.CP_Port_Close(h);
    } else {
      result.result_code = SimpleTestResult.Result_OpenFailed;
    }

    return result;
  }

  private void showMessageOnUiThread(final String msg) {
    activity.runOnUiThread(
      new Runnable() {
        @Override
        public void run() {
          Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
        }
      }
    );
  }

  private Bitmap getImageFromAssetsFile(String fileName) {
    Bitmap image = null;
    AssetManager am = getResources().getAssets();
    try {
      InputStream is = am.open(fileName);
      image = BitmapFactory.decodeStream(is);
      is.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return image;
  }

  private boolean QueryPrintResult(Pointer h) {
    boolean result = AutoReplyPrint.INSTANCE.CP_Pos_QueryPrintResult(h, 30000);
    Log.i(TAG, result ? "Print Success" : "Print Failed");
    showMessageOnUiThread(result ? "Print Success" : "Print Failed");
    if (!result) {
      LongByReference printer_error_status = new LongByReference();
      LongByReference printer__info_status = new LongByReference();
      LongByReference timestamp_ms_printer_status = new LongByReference();
      if (
        AutoReplyPrint.INSTANCE.CP_Printer_GetPrinterStatusInfo(
          h,
          printer_error_status,
          printer__info_status,
          timestamp_ms_printer_status
        )
      ) {
        AutoReplyPrint.CP_PrinterStatus status = new AutoReplyPrint.CP_PrinterStatus(
          printer_error_status.getValue(),
          printer__info_status.getValue()
        );
        String error_status_string = String.format(
          "Printer Error Status: 0x%04X",
          printer_error_status.getValue() & 0xffff
        );
        if (status.ERROR_OCCURED()) {
          if (status.ERROR_CUTTER()) error_status_string += "[ERROR_CUTTER]";
          if (status.ERROR_FLASH()) error_status_string += "[ERROR_FLASH]";
          if (status.ERROR_NOPAPER()) error_status_string += "[ERROR_NOPAPER]";
          if (status.ERROR_VOLTAGE()) error_status_string += "[ERROR_VOLTAGE]";
          if (status.ERROR_MARKER()) error_status_string += "[ERROR_MARKER]";
          if (status.ERROR_ENGINE()) error_status_string += "[ERROR_ENGINE]";
          if (status.ERROR_OVERHEAT()) error_status_string +=
            "[ERROR_OVERHEAT]";
          if (status.ERROR_COVERUP()) error_status_string += "[ERROR_COVERUP]";
          if (status.ERROR_MOTOR()) error_status_string += "[ERROR_MOTOR]";
        }
        Log.i(TAG, error_status_string);
        showMessageOnUiThread(error_status_string);
      } else {
        Log.i(TAG, "CP_Printer_GetPrinterStatusInfo Failed");
        showMessageOnUiThread("CP_Printer_GetPrinterStatusInfo Failed");
      }
    }
    return result;
  }

  private void PosPrint(
    boolean cutPaper,
    boolean kickDrawer,
    String order,
    String extraOrder,
    String qrcode,
    String mBitmap
  ) {
    Pointer h = OpenPort();
    if (h != Pointer.NULL) {
      Bitmap bitmap = getImageFromAssetsFile("RasterImage/blackwhite.png");
      AutoReplyPrint.CP_Pos_PrintRasterImageFromData_Helper.PrintRasterImageFromBitmap(
        h,
        bitmap.getWidth(),
        bitmap.getHeight(),
        bitmap,
        AutoReplyPrint.CP_ImageBinarizationMethod_Thresholding,
        AutoReplyPrint.CP_ImageCompressionMethod_None
      );

      if (mBitmap != null) {
        AutoReplyPrint.CP_Pos_PrintRasterImageFromData_Helper.PrintRasterImageFromBitmap(
          h,
          mBitmap.getWidth(),
          mBitmap.getHeight(),
          mBitmap,
          AutoReplyPrint.CP_ImageBinarizationMethod_ErrorDiffusion,
          AutoReplyPrint.CP_ImageCompressionMethod_None
        );
      }

      if (order != null) {
        AutoReplyPrint.INSTANCE.CP_Pos_PrintText(h, order);

        if (qrcode) {
          AutoReplyPrint.INSTANCE.CP_Pos_SetBarcodeUnitWidth(h, 6);
          AutoReplyPrint.INSTANCE.CP_Pos_PrintQRCode(
            h,
            10,
            AutoReplyPrint.CP_QRCodeECC_L,
            "http://www.caysn.com/"
          );
        }

        if (cutPaper) {
          AutoReplyPrint.INSTANCE.CP_Pos_FeedAndHalfCutPaper(h);
        } else {
          AutoReplyPrint.INSTANCE.CP_Pos_FeedLine(h, 10);
        }
      }

      if (extraOrder != null) {
        AutoReplyPrint.INSTANCE.CP_Pos_PrintText(h, extraOrder);
        if (cutPaper) {
          AutoReplyPrint.INSTANCE.CP_Pos_FeedAndHalfCutPaper(h);
        } else {
          AutoReplyPrint.INSTANCE.CP_Pos_FeedLine(h, 10);
        }
      }
      if (kickDrawer) {
        AutoReplyPrint.INSTANCE.CP_Pos_KickOutDrawer(h, 0, 100, 100);
        AutoReplyPrint.INSTANCE.CP_Pos_KickOutDrawer(h, 1, 100, 100);
      }

      QueryPrintResult(h);

      AutoReplyPrint.INSTANCE.CP_Port_Close(h);
    }
  }

  private void ShowMessage(final String msg) {
    activity.runOnUiThread(
      new Runnable() {
        @Override
        public void run() {
          tvInfo.setText(msg);
        }
      }
    );
  }

  private void EnableUI() {
    activity.runOnUiThread(
      new Runnable() {
        @Override
        public void run() {
          activity.EnableUI();
        }
      }
    );
  }
}
