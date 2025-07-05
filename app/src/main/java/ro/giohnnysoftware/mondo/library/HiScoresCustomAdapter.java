package ro.giohnnysoftware.mondo.library;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import ro.giohnnysoftware.mondo.R;

/**
 * Created by GIOhnny on 25/04/2016.
 */
public class HiScoresCustomAdapter extends BaseAdapter {
    private final Context context;
    private String idToMatch;
    private final List<HiScore_details> scoresList;

    public HiScoresCustomAdapter(Context context, List<HiScore_details> scoresList, String match_id) {
        this.context = context;
        this.scoresList = scoresList;
        this.idToMatch = match_id;
    }

    public void setIdToMatch(String idToMatch) {
        this.idToMatch = idToMatch;
    }

    public int getCount() {
        return scoresList.size();
    }

    public Object getItem(int position) {
        return scoresList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.score_line_layout, parent, false);
        }
        TextView tvNo = convertView.findViewById(R.id.tvNo);
        TextView tvUsername = convertView.findViewById(R.id.tvUsername);
        TextView tvScore = convertView.findViewById(R.id.tvScore);
        tvNo.setText(Integer.toString(scoresList.get(position).getNo()));
        tvScore.setText(Integer.toString(scoresList.get(position).getScore()));
        tvUsername.setText(scoresList.get(position).getUsername());

        if (scoresList.get(position).getId().equals(idToMatch))
            convertView.setBackgroundColor(Color.argb(50, 0, 255, 0));
        else convertView.setBackgroundColor(Color.TRANSPARENT);
        Typeface face2 = Typeface.createFromAsset(context.getAssets() , "fonts/digital.ttf");
        tvNo.setTypeface(face2);
        tvScore.setTypeface(face2);
        tvUsername.setTypeface(face2);
        return convertView;
    }
}
