package com.example.quota;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

public class SheetActivity extends AppCompatActivity {

    Toolbar toolbar;
    private TextView subtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheet);

        setToolbar();
        showTable();
    }

    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.title_toolbar);
        subtitle = toolbar.findViewById(R.id.subtitle_toolbar);
        ImageButton back = toolbar.findViewById(R.id.back);
        ImageButton save = toolbar.findViewById(R.id.save);
        save.setVisibility(View.GONE);

        title.setText("Attendance Sheet");
        subtitle.setVisibility(View.GONE);

        back.setOnClickListener(v -> onBackPressed());
    }


    private void showTable() {
        TableLayout tableLayout = findViewById(R.id.tableLayout);
        DbHelper dbHelper = new DbHelper(this);


        long [] idArray = getIntent().getLongArrayExtra("idArray");
        int [] rollArray = getIntent().getIntArrayExtra("rollArray");
        String [] nameArray = getIntent().getStringArrayExtra("nameArray");
        String month = getIntent().getStringExtra("month");

            int DAY_IN_MONTH = getDayInMonth(month);


            //row setup
            int rowSize = idArray.length + 1;

            TableRow[] rows = new TableRow[rowSize];
            TextView[] roll_tvs = new TextView[rowSize];
            TextView[] name_tvs = new TextView[rowSize];
            TextView[][] status_tvs = new TextView[rowSize][DAY_IN_MONTH + 1];



        for (int i = 0; i < rowSize; i++) {
            roll_tvs[i] = new TextView(this);
            name_tvs[i] = new TextView(this);
            for (int j = 1; j <= DAY_IN_MONTH; j++) {
                status_tvs[i][j] = new TextView(this);
            }
        }

        //HEADER
        roll_tvs[0].setText("Roll");
        roll_tvs[0].setTypeface(roll_tvs[0].getTypeface(), Typeface.BOLD);
        name_tvs[0].setText("Name");
        name_tvs[0].setTypeface(name_tvs[0].getTypeface(), Typeface.BOLD);
        for (int i = 1; i <= DAY_IN_MONTH; i++) {
            status_tvs[0][i].setText(String.valueOf(i));
            status_tvs[0][i].setTypeface(status_tvs[0][i].getTypeface(),Typeface.BOLD);
        }

        for (int i = 1; i < rowSize; i++) {
            roll_tvs[i].setText(String.valueOf(rollArray[i - 1]));
            name_tvs[i].setText(nameArray[i - 1]);

            for (int j = 1; j <= DAY_IN_MONTH; j++) {
                String day = String.valueOf(j);
                if (day.length() == 1) day = "0" + day;
                String date = day + "," + month;
                String status = dbHelper.getStatus(idArray[i - 1], date);
                Log.d("Attendance", "ID: " + idArray[i - 1] + ", Date: " + date + ", Status: " + status);
                if (status == null) {
                    status_tvs[i][j].setText("-");
                    // Handle empty status (set default value or handle accordingly)
                } else {
                    Log.d("Attendance", "   Status for student " + nameArray[i - 1] + " on date " + date + ": " + status);
                    status_tvs[i][j].setText(status);

                    //To change color
                    switch(status) {
                        case "P":
                            status_tvs[i][j].setBackgroundColor(Color.parseColor("#3300C500"));
                            break;
                        case "L":
                            status_tvs[i][j].setBackgroundColor(Color.parseColor("#FFA500"));
                            break;
                        case "A":
                            status_tvs[i][j].setBackgroundColor(Color.parseColor("#33FF0000"));
                            break;
                    }
                }
            }
        }

        for (int i = 0; i < rowSize; i++) {
            rows[i] = new TableRow(this);

            if(i % 2 == 0)
                rows[i].setBackgroundColor(Color.parseColor("#EEEEEE"));
            else rows[i].setBackgroundColor(Color.parseColor("#E4E4E4"));

            roll_tvs[i].setPadding(16,16,16,16);
            name_tvs[i].setPadding(16,16,16,16);

            rows[i].addView(roll_tvs[i]);
            rows[i].addView(name_tvs[i]);

            for (int j = 1; j <= DAY_IN_MONTH; j++) {
                status_tvs[i][j].setPadding(16,16,16,16);
                rows[i].addView(status_tvs[i][j]);
            }
            tableLayout.addView(rows[i]);
        }
        tableLayout.setShowDividers(TableLayout.SHOW_DIVIDER_MIDDLE);

    }

    private int getDayInMonth(String month) {
        int monthIndex = Integer.valueOf(month.substring(0,1));
        int year = Integer.valueOf(month.substring(4));

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH,monthIndex);
        calendar.set(Calendar.YEAR,year);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
}

