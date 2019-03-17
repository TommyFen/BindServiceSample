package cn.tommyfen.bind_service_sample;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import cn.tommyfen.bind_service_sample.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding mBinding;
    private MainViewModel mViewModel;
    private Messenger mServiceMessenger;
    private Messenger mMessenger;
    private boolean mBound;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //get Service's messenger
            mServiceMessenger = new Messenger(service);
            try {
                //send Activity's messenger to Service
                Message message = Message.obtain(null, MainService.MSG_REGISTER);
                message.replyTo = mMessenger;
                mServiceMessenger.send(message);
            } catch (RemoteException e) {
                mServiceMessenger = null;
                e.printStackTrace();
            }
            mBound = true;
            mViewModel.setServiceBound(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.w(TAG, "onServiceDisconnected");
            mServiceMessenger = null;
            mBound = false;
            mViewModel.setServiceBound(false);

        }
    };

    public MainActivity() {
        super();
        Handler mHandler = new IncomingHandler(this);
        mMessenger = new Messenger(mHandler);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mViewModel = new MainViewModel(this);
        mBinding.setViewModel(mViewModel);

        setSupportActionBar(mBinding.toolBar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        unbindMainService();
        super.onDestroy();
    }

    public void bindMainService() {
        Intent intent = new Intent(this, MainService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    public void unbindMainService() {
        if (mBound && mServiceMessenger != null) {
            try {
                mServiceMessenger.send(Message.obtain(null, MainService.MSG_UNREGISTER));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mServiceMessenger = null;
            unbindService(mConnection);
            mBound = false;
            mViewModel.setServiceBound(false);
        }
    }

    /**
     * handle msg that from Service
     *
     * @param bundle msg data
     */
    public void handleMsg(Bundle bundle) {
        Toast.makeText(this, "Activity: received msg from service", Toast.LENGTH_SHORT).show();
    }

    public void sendMsgToService(Bundle bundle) {
        try {
            if (mServiceMessenger != null) {
                Message message = Message.obtain(null, MainService.MSG_ACTIVITY);
                message.setData(bundle);
                mServiceMessenger.send(message);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private static class IncomingHandler extends Handler {

        private final WeakReference<MainActivity> mActivity;

        public IncomingHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                //receive msg from service
                case MainService.MSG_SERVICE:
                    Log.d(TAG, "MSG：from service");
                    mActivity.get().handleMsg(msg.getData());
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }
}
