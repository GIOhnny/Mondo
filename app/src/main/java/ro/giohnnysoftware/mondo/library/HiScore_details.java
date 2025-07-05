package ro.giohnnysoftware.mondo.library;

/**
 * Created by GIOhnny on 25/04/2016.
 */
public class HiScore_details {
    private int m_no;
    private String m_username;
    private final String m_id;
    private int m_score;

    public HiScore_details(int no, String username, int score, String id) {
        this.m_no = no;
        this.m_username = username;
        this.m_score = score;
        this.m_id = id;
    }


    public int getNo() {
        return m_no;
    }

    public void setNo(int no) {
        this.m_no = no;
    }

    public String getUsername() {
        return m_username;
    }

    public void setUsername(String username) {
        this.m_username = username;
    }
    public int getScore() {
        return m_score;
    }
    public void setScore(int score) {
        this.m_score = score;
    }

    public String getId() {
        return m_id;
    }
}