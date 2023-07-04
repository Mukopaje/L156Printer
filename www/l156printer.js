var exec = require("cordova/exec");

module.exports = {
  printerInit: function (resolve, reject) {
    exec(resolve, reject, "l156printer", "printerInit", []);
  },
  printString: function (text, qrCodeData, extraOrder, resolve, reject) {
    exec(resolve, reject, "l156printer", "printString", [
      text,
      qrCodeData,
      extraOrder,
    ]);
  },
  printBitmap: function (base64Data, width, height, resolve, reject) {
    exec(resolve, reject, "l156printer", "printBitmap", [
      base64Data,
      width,
      height,
    ]);
  },
  printBarCode: function (barCodeData, resolve, reject) {
    exec(resolve, reject, "l156printer", "printBarCode", [barCodeData]);
  },
  printQRCode: function (qrCodeData, resolve, reject) {
    exec(resolve, reject, "l156printer", "printQRCode", [qrCodeData]);
  },
};
