package com.interview.omnifyinterview;

import android.app.Application;

import com.interview.omnifyinterview.component.ApplicationComponent;
import com.interview.omnifyinterview.component.DaggerApplicationComponent;
import com.interview.omnifyinterview.module.ApplicationModule;

import io.realm.Realm;

public class MyApplication extends Application {

    private ApplicationComponent component;

    @Override
    public void onCreate() {
        super.onCreate();

        component = DaggerApplicationComponent
                .builder()
                .applicationModule(new ApplicationModule(this))
                .build();

        Realm.init(this);
    }

    public ApplicationComponent getComponent() {
        return component;
    }
}
