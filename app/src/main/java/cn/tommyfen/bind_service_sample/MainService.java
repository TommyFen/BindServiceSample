package cn.tommyfen.bind_service_sample;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class MainService extends Service {

    public static final String TAG = "MainService";

    public static final int MSG_REGISTER = 201;
    public static final int MSG_UNREGISTER = 202;
    public static final int MSG_ACTIVITY = 203;
    public static final int MSG_SERVICE = 204;

    private Messenger mMessenger;
    private Messenger mActivityMessenger;

    public MainService() {
        Handler mHandler = new IncomingHandler(this);
        mMessenger = new Messenger(mHandler);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    public void sendMsgToService(Bundle bundle) {
        try {
            if (mActivityMessenger != null) {
                Message message = Message.obtain(null, MainService.MSG_SERVICE);
                message.setData(bundle);
                mActivityMessenger.send(message);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private static class IncomingHandler extends Handler {

        private final WeakReference<MainService> mService;

        public IncomingHandler(MainService service) {
            mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            //outer Service weak reference
            final MainService service = mService.get();

            switch (msg.what) {
                case MSG_REGISTER:
                    Log.d(TAG, "MSG：register");
                    if (service.mActivityMessenger != null) {
                        Log.w(TAG, "MSG：instead of existed messenger");
                    }
                    service.mActivityMessenger = msg.replyTo;
                    break;
                case MSG_UNREGISTER:
                    Log.d(TAG, "MSG：unregister");
                    service.mActivityMessenger = null;
                    break;

                case MSG_ACTIVITY:
                    Log.d(TAG, "MSG：from activity");
                    Toast.makeText(service, "Service: received msg from activity", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            service.sendMsgToService(new Bundle());;
                        }
                    }, 4000L);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

}
