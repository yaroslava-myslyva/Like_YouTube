package com.example.likeyoutube2.from_libs;

import android.webkit.JavascriptInterface;

import com.example.likeyoutube2.from_libs.interfaces.CallJavaResultInterface;

/**
 * Passed in addJavascriptInterface of WebView to allow web views's JS execute
 * Java code
 */
public class JavaScriptInterface {
    private final CallJavaResultInterface mCallJavaResultInterface;

    public JavaScriptInterface(CallJavaResultInterface callJavaResult) {
        mCallJavaResultInterface = callJavaResult;
    }

    @JavascriptInterface
    public void returnResultToJava(String value) {
        mCallJavaResultInterface.jsCallFinished(value);
    }
}
