package cn.tommyfen.bind_service_sample;

import android.databinding.ObservableBoolean;
import android.os.Bundle;

/**
 * @author : tommy
 * @version : 1.0.0
 * @since : 2019/3/17
 */
public class MainViewModel {

    private MainActivity mMainActivity;

    private ObservableBoolean serviceBound = new ObservableBoolean(false);

    public MainViewModel(MainActivity activity) {
        mMainActivity = activity;
    }

    public void sendMsg() {
        Bundle bundle = new Bundle();
        mMainActivity.sendMsgToService(bundle);
    }

    public void bindService() {
        //bind service
        if (mMainActivity != null) {
            if (serviceBound.get()) {
                mMainActivity.unbindMainService();
            } else {
                mMainActivity.bindMainService();
            }
        }
    }

    public ObservableBoolean getServiceBound() {
        return serviceBound;
    }

    public void setServiceBound(boolean enable) {
        if (serviceBound != null) {
            serviceBound.set(enable);
        }
    }


}
