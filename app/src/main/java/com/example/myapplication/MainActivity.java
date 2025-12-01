package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final int SIZE = 4;
    private Button[][] buttons = new Button[SIZE][SIZE];
    private Cordinate empty = new Cordinate(3, 3);
    private List<Integer> values = new ArrayList<>(15);
    private TextView txtCounter;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);

        pref = getSharedPreferences("MyApp", Context.MODE_PRIVATE);

        txtCounter = findViewById(R.id.txt1);
        RelativeLayout container = findViewById(R.id.rlv1);

        for (int i = 0; i < container.getChildCount(); i++) {
            View v = container.getChildAt(i);
            if (!(v instanceof Button)) continue;

            Button b = (Button) v;
            Cordinate c = new Cordinate(i / SIZE, i % SIZE);
            b.setTag(c);
            b.setOnClickListener(this::onTileClick);
            buttons[c.x][c.y] = b;
        }

        generateValues();
        loadNewBoard();

        Button reset = findViewById(R.id.btnrt);
        reset.setOnClickListener(v -> {
            txtCounter.setText("0");
            generateValues();
            loadNewBoard();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        int cnt = pref.getInt("COUNT", 0);
        txtCounter.setText(String.valueOf(cnt));

        int ex = pref.getInt("EMPTY_X", 3);
        int ey = pref.getInt("EMPTY_Y", 3);
        empty = new Cordinate(ex, ey);

        String saved = pref.getString("BOARD", null);

        if (saved != null) {
            String[] arr = saved.split(",");
            int index = 0;

            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    buttons[i][j].setText(arr[index].equals("0") ? "" : arr[index]);
                    index++;
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences.Editor ed = pref.edit();
        ed.putInt("COUNT", Integer.parseInt(txtCounter.getText().toString()));
        ed.putInt("EMPTY_X", empty.x);
        ed.putInt("EMPTY_Y", empty.y);

        List<String> list = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                String t = buttons[i][j].getText().toString();
                list.add(t.isEmpty() ? "0" : t);
            }
        }
        ed.putString("BOARD", String.join(",", list));
        ed.apply();
    }

    private void onTileClick(View v) {
        Button b = (Button) v;
        Cordinate c = (Cordinate) b.getTag();

        int dx = Math.abs(c.x - empty.x);
        int dy = Math.abs(c.y - empty.y);

        if (dx + dy == 1) {
            Button emptyBtn = buttons[empty.x][empty.y];
            emptyBtn.setText(b.getText());
            b.setText("");

            empty = c;

            int cnt = Integer.parseInt(txtCounter.getText().toString());
            txtCounter.setText(String.valueOf(cnt + 1));

            if (checkWin()) {
                Toast.makeText(this, "YOU WIN!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void generateValues() {
        values.clear();
        for (int i = 1; i <= 15; i++) values.add(i);
    }

    private void loadNewBoard() {
        do {
            Collections.shuffle(values);
        } while (!isSolvable(values));

        for (int i = 0; i < 15; i++) {
            buttons[i / 4][i % 4].setText(String.valueOf(values.get(i)));
        }

        buttons[3][3].setText("");
        empty = new Cordinate(3, 3);
    }

    private boolean isSolvable(List<Integer> list) {
        int inv = 0;
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(i) > list.get(j)) inv++;
            }
        }
        return ((inv + 1) % 2) == 0;
    }

    private boolean checkWin() {
        int expect = 1;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (i == 3 && j == 3) {
                    return buttons[i][j].getText().toString().isEmpty();
                }
                String t = buttons[i][j].getText().toString();
                if (!t.equals(String.valueOf(expect))) return false;
                expect++;
            }
        }
        return true;
    }

    public static class Cordinate {
        int x, y;
        public Cordinate(int x, int y) { this.x = x; this.y = y; }
        public String toString() { return x + "," + y; }
    }
}
