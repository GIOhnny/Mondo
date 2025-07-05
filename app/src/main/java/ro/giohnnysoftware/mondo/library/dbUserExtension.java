package ro.giohnnysoftware.mondo.library;

import android.icu.text.SimpleDateFormat;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;

public class dbUserExtension implements Serializable {
    private String id;
    private String nickName;
    private String provider;
    private String lastLoginDate;
    private Boolean music, SFX;

    public dbUserExtension(String id, String nickName, String provider) {
        this.setId(id);
        this.setProvider(provider);
        this.setMusic(true);
        this.setSFX(true);

        if (provider.equalsIgnoreCase("firebase")) setNickName("Guest" + nickName);
        else setNickName("User" + nickName);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String millisInString = dateFormat.format(new Date());
        this.setLastLoginDate(millisInString);
    }

    public Boolean getMusic() {
        return music;
    }

    public void setMusic(Boolean music) {
        this.music = music;
    }

    public Boolean getSFX() {
        return SFX;
    }

    public dbUserExtension() {
    }

    public void setSFX(Boolean SFX) {
        this.SFX = SFX;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(String lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    @NonNull
    @Override
    public String toString() {
        return "dbUserExtension{" +
                "id='" + id + '\'' +
                ", nickName='" + nickName + '\'' +
                ", provider='" + provider + '\'' +
                ", lastLoginDate=" + lastLoginDate +
                '}';
    }
}
