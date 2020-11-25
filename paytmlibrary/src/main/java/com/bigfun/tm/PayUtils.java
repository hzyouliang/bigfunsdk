package com.bigfun.tm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.bigfun.tm.model.PaymentOrderBean;

import java.util.HashMap;
import java.util.Map;

public class PayUtils {
    private static final String TAG = "PayUtils2";

    private PayUtils() {
    }

    private static class InstanceHolder {
        private static PayUtils instance = new PayUtils();
    }

    public static PayUtils getInstance() {
        return InstanceHolder.instance;
    }

    public void pay(PaymentOrderBean.DataBean bean,
                    Activity activity,
                    int requestCode) {
        if (Integer.parseInt(bean.getPaymentChannel()) == 1) {
            if (Integer.parseInt(bean.getOpenType()) == 5) {
                paytm(bean, activity, requestCode);
            } else {
                activity.runOnUiThread(() -> {
                    Intent intent = new Intent(activity, PayActivity.class);
                    intent.putExtra(Constant.EXTRA_KEY_PAY_URL, bean.getJumpUrl());
                    activity.startActivity(intent);
                });
            }
        } else if (Integer.parseInt(bean.getPaymentChannel()) == 0) {
            if (Integer.parseInt(bean.getOpenType()) == 1) {
                activity.runOnUiThread(() -> {
                    Intent intent = new Intent(activity, PayActivity.class);
                    intent.putExtra(Constant.EXTRA_KEY_PAY_URL, bean.getJumpUrl());
                    activity.startActivity(intent);
                });
            } else {
                activity.runOnUiThread(() -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(bean.getJumpUrl()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(intent);
                });
            }
        } else {
            activity.runOnUiThread(() -> {
                Intent intent = new Intent(activity, PayActivity.class);
                String url = bean.getJumpUrl();
                intent.putExtra(Constant.EXTRA_KEY_PAY_URL, url);
                activity.startActivity(intent);
            });
        }
    }

    private void paytm(PaymentOrderBean.DataBean bean, Activity activity, int requestCode) {
        String[] arr = bean.getJumpUrl().split("\\?")[1].split("&");
        Map<String, String> map = new HashMap<>();
        for (String s : arr) {
            String[] split = s.split("=");
            map.put(split[0], split[1]);
        }
        if (Utils.isInstall(BigFunSDK.mContext, "net.one97.paytm")) {
            //安装Paytm
            String version = Utils.getVersion(BigFunSDK.mContext);
            if (Utils.versionCompare(version, "8.6.0") < 0) {
                Intent paytmIntent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putDouble("nativeSdkForMerchantAmount", bean.getOutPayAmount());
                bundle.putString("orderid", map.get("orderId"));
                bundle.putString("txnToken", map.get("txnToken"));
                bundle.putString("mid", map.get("mid"));
                paytmIntent.setComponent(new ComponentName("net.one97.paytm", "net.one97.paytm.AJRJarvisSplash"));
                paytmIntent.putExtra("paymentmode", 2); // You must have to pass hard coded 2 here, Else your transaction would not proceed.
                paytmIntent.putExtra("bill", bundle);
                activity.startActivityForResult(paytmIntent, requestCode);
            } else {
                //未安装Paytm
                Intent paytmIntent = new Intent();
                paytmIntent.setComponent(new ComponentName("net.one97.paytm", "net.one97.paytm.AJRRechargePaymentActivity"));
                paytmIntent.putExtra("paymentmode", 2);
                paytmIntent.putExtra("enable_paytm_invoke", true);
                paytmIntent.putExtra("paytm_invoke", true);
                paytmIntent.putExtra("price", String.valueOf(bean.getOutPayAmount())); //this is string amount
                paytmIntent.putExtra("nativeSdkEnabled", true);
                paytmIntent.putExtra("orderid", map.get("orderId"));
                paytmIntent.putExtra("txnToken", map.get("txnToken"));
                paytmIntent.putExtra("mid", map.get("mid"));
                activity.startActivityForResult(paytmIntent, requestCode);
            }
        } else {
            //未安装Paytm
            Intent intent = new Intent(activity, PayActivity.class);
            intent.putExtra(Constant.EXTRA_KEY_PAY_URL, bean.getJumpUrl());
            activity.startActivity(intent);
        }
    }
}
