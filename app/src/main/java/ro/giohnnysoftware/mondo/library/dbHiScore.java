package ro.giohnnysoftware.mondo.library;

import java.io.Serializable;

public class dbHiScore implements Serializable {
    private String userId;
    private String nickName;
    private int score;
    private String key;

    public dbHiScore() {

    }

    public dbHiScore(String userId, String nickName, int score, String key) {
        this.setKey(key);
        this.setUserId(userId);
        this.setScore(score);
        this.setNickName(nickName);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
