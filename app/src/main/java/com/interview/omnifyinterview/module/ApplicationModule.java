package com.interview.omnifyinterview.module;

import android.app.Application;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.realm.Realm;
import io.realm.RealmConfiguration;

@Module
public class ApplicationModule {

    private Application application;

    public ApplicationModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    public Context providesApplicationContext() {
        return application;
    }

    @Provides
    @Singleton
    public RequestQueue provideRequestQueue() {
        return Volley.newRequestQueue(application);
    }

    @Provides
    @Singleton
    public Realm provideRealm() {
        RealmConfiguration mRealmConfiguration = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        return Realm.getInstance(mRealmConfiguration);
    }
}
