package com.example.likeyoutube2.from_libs;

import android.os.Handler;

import com.example.likeyoutube2.from_libs.interfaces.HandlerWrapperInterface;


public class HandlerWrapper implements HandlerWrapperInterface {
    private final Handler mHandler;

    public HandlerWrapper() {
        mHandler = new Handler();
    }

    @Override
    public void post(Runnable r) {
        mHandler.post(r);
    }
}
