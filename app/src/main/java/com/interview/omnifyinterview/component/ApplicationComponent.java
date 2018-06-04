package com.interview.omnifyinterview.component;

import com.interview.omnifyinterview.activity.StoryActivity;
import com.interview.omnifyinterview.activity.StoryDetailsActivity;
import com.interview.omnifyinterview.module.ApplicationModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    void inject(StoryActivity storyActivity);

    void inject(StoryDetailsActivity storyDetailsActivity);
}
