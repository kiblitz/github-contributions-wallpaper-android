package com.example.githubcontributionswallpaper;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private EditText github_username;
    private Button github_search;
    private TextView github_status;
    private ImageView github_graph;
    private Switch github_wallpaper_switch;

    private static final int RECT_DIM = 14;

    private static final int DAYS_IN_WEEK = 7;
    private static final int WEEKS_TO_SHOW = 8;

    private String[][] contribution_depths = new String[WEEKS_TO_SHOW][DAYS_IN_WEEK];

    enum GithubColors {
        ONE("#ebedf0"),
        TWO("#9be9a8"),
        THREE("#40c463"),
        FOUR("#30a14e"),
        FIVE("#216e39");

        String colorHex_;

        GithubColors(String colorHex) {
            this.colorHex_ = colorHex;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        github_username = (EditText) findViewById(R.id.github_username);
        github_search = (Button) findViewById(R.id.github_search);
        github_status = (TextView) findViewById(R.id.github_status);
        github_graph = (ImageView) findViewById(R.id.github_graph);
        github_wallpaper_switch = (Switch) findViewById(R.id.github_wallpaper_switch);

        github_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGraph();
            }
        });

        github_wallpaper_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // TODO set wallpaper
                } else {
                    // TODO remove wallpaper
                }
            }
        });
    }

    private void getGraph() {
        new Thread() {
            @Override
            public void run() {
                try {
                    Document document = Jsoup.connect("https://github.com/" + github_username.getText().toString()).get();
                    Elements points = document.select("rect[x~=-3(1|2|3|4|5|6|7|8)]");
                    int i = 0;
                    for (Element point : points) {
                        contribution_depths[i / 7][i % 7] = point.attr("fill");
                        i++;
                    }
                    generateGraph();
                    github_status.setText(getResources().getString(R.string.empty));
                } catch (HttpStatusException e) {
                    github_status.setText("");
                } catch (Exception e) {
                    Log.d("ERROR", e.toString());
                }
            }
        }.start();
    }

    private void generateGraph() {
        Bitmap rect = BitmapFactory.decodeResource(getResources(), R.drawable.github_empty_contribution);
        Bitmap result = Bitmap.createBitmap(WEEKS_TO_SHOW * RECT_DIM,
                DAYS_IN_WEEK * RECT_DIM, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();

        for (int r = 0; r < contribution_depths.length; r++) {
            for (int c = 0; c < contribution_depths[r].length; c++) {
                if (contribution_depths[r][c] != null) {
                    String hex = contribution_depths[r][c];
                    ColorFilter filter = new PorterDuffColorFilter(Color.parseColor(contribution_depths[r][c]), PorterDuff.Mode.MULTIPLY);
                    paint.setColorFilter(filter);
                    canvas.drawBitmap(rect, null, new Rect(r * RECT_DIM,
                            c * RECT_DIM,
                            (r + 1) * RECT_DIM,
                            (c + 1) * RECT_DIM), paint);
                }
            }
        }
        github_graph.setImageBitmap(result);
    }
}