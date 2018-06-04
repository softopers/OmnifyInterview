package com.interview.omnifyinterview.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class TopStoryRealmObject extends RealmObject {

    @PrimaryKey
    private long id;

    private String by;
    private String descendants;
    private String kids;
    private String kidsSize;
    private String score;
    private String time;
    private String title;
    private String type;
    private String url;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getBy() {
        return by;
    }

    public void setBy(String by) {
        this.by = by;
    }

    public String getDescendants() {
        return descendants;
    }

    public void setDescendants(String descendants) {
        this.descendants = descendants;
    }

    public String getKids() {
        return kids;
    }

    public void setKids(String kids) {
        this.kids = kids;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getKidsSize() {
        return kidsSize;
    }

    public void setKidsSize(String kidsSize) {
        this.kidsSize = kidsSize;
    }

    @Override
    public String toString() {
        return "TopStoryRealmObject{" +
                "id=" + id +
                ", by='" + by + '\'' +
                ", descendants='" + descendants + '\'' +
                ", kids='" + kids + '\'' +
                ", kidsSize='" + kidsSize + '\'' +
                ", score='" + score + '\'' +
                ", time='" + time + '\'' +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}