package com.example.quota;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;

public class StudentActivity extends AppCompatActivity {

    String status;
    Toolbar toolbar;
    private String sectionName;
    private String gradeLevel;
    public int position;
    public RecyclerView RecyclerView;
    private StudentAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;
    private final ArrayList<StudentItem> studentItems = new ArrayList<>();
    private DbHelper dbHelper;
    private long cid;
    private MyCalendar calendar;
    private TextView subtitle;
    private int scanningPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        calendar = new MyCalendar();
        dbHelper = new DbHelper(this);
        Intent intent = getIntent();
        sectionName = intent.getStringExtra("sectionName");
        gradeLevel = intent.getStringExtra("gradeLevel");
        position = intent.getIntExtra("position", -1);
        cid = intent.getLongExtra("cid",-1);


        setToolbar();
        loadData();
        RecyclerView = findViewById(R.id.student_recycler);
        RecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        RecyclerView.setLayoutManager(layoutManager);
        adapter = new StudentAdapter(this, studentItems);
        RecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this::changeStatus);
        loadStatusData();
    }

    private void loadData() {
        Cursor cursor = dbHelper.getStudentTable(cid);
        Log.i("1234567890", "loadData: "+cid);
        studentItems.clear();
        while (cursor.moveToNext()) {
            @SuppressLint("Range")
            long sid = cursor.getLong(cursor.getColumnIndex(DbHelper.S_ID));
            @SuppressLint("Range")
            int roll = cursor.getInt(cursor.getColumnIndex(DbHelper.STUDENT_ROLL_KEY));
            @SuppressLint("Range")
            String name = cursor.getString(cursor.getColumnIndex(DbHelper.STUDENT_NAME_KEY));
            studentItems.add(new StudentItem(sid, roll, name));

        }
        cursor.close();
    }

    private void changeStatus(int position) {
        status = studentItems.get(position).getStatus();
        if (status.equals("P")){
            status = "L";
        }
        else if (status.equals("L")){
            status = "A";
        }
        else {
            showScanner(position);
            return;
        }
        studentItems.get(position).setStatus(status);
        adapter.notifyItemChanged(position);
    }

    private void showScanner(int position) {
        IntentIntegrator intentIntegrator = new IntentIntegrator(StudentActivity.this);
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.setPrompt("Scan QR Code");
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        intentIntegrator.initiateScan();

        this.scanningPosition = position;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (intentResult != null){
                String contents = intentResult.getContents();
                if(contents != null){
                    status = (intentResult.getContents());
                    if (status.equals("PRESENT")) {
                        status = "P";
                    }
                    else{
                        return;
                    }
                    studentItems.get(scanningPosition).setStatus(status);
                    adapter.notifyItemChanged(scanningPosition);
                }
            }
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.title_toolbar);
        subtitle = toolbar.findViewById(R.id.subtitle_toolbar);
        ImageButton back = toolbar.findViewById(R.id.back);
        ImageButton save = toolbar.findViewById(R.id.save);
        save.setOnClickListener(v->saveStatus());

        title.setText(sectionName + " | " + gradeLevel);
        subtitle.setText(calendar.getDate());

        back.setOnClickListener(v -> onBackPressed());
        toolbar.inflateMenu(R.menu.student_menu);
        toolbar.setOnMenuItemClickListener(menuItem -> onMenuItemClick(menuItem));
    }


    private void saveStatus() {
        for (StudentItem studentItem : studentItems) {
            String status = studentItem.getStatus();
            if (status != null && !status.equals("P") && !status.equals("L")) {
                status = "A";
            } else if (status == null) {
                // Handle the case where status is null (set default value or handle accordingly)
                status = "A"; // Setting a default value, adjust as needed
            }

            long value = dbHelper.addStatus(studentItem.getSid(), cid, calendar.getDate(), status, studentItem.getName());

            if (value == -1) {
                dbHelper.updateStatus(studentItem.getSid(), cid, calendar.getDate(), status, studentItem.getName());
            }
        }
    }


    private void loadStatusData() {
        for (StudentItem studentItem : studentItems) {
            String status = dbHelper.getStatus(studentItem.getSid(), calendar.getDate());
            if (status != null) studentItem.setStatus(status);
            else studentItem.setStatus("");
        }
        adapter.notifyDataSetChanged();
    }




    private boolean onMenuItemClick(MenuItem menuItem) {
        if (menuItem.getItemId()==R.id.add_student) {
            showAddStudentDialog();
        } else if (menuItem.getItemId()==R.id.show_Calendar) {
            showCalendar();
        } else if (menuItem.getItemId()==R.id.show_attendance_sheet) {
            openSheetList();
        }
        return true;
    }

    private void openSheetList() {
        long[] idArray = new long[studentItems.size()];
        String[]nameArray = new String[studentItems.size()];
        int[] rollArray = new int[studentItems.size()];

        for(int i = 0; i <idArray.length; i++)
            idArray[i] = studentItems.get(i).getSid();
        for(int i = 0; i <rollArray.length; i++)
            rollArray[i] = studentItems.get(i).getRoll();
        for(int i = 0; i <nameArray.length;i++)
            nameArray[i] = studentItems.get(i).getName();

        Intent intent = new Intent(this, SheetListActivity.class);
        intent.putExtra("cid",cid);
        intent.putExtra("idArray",idArray);
        intent.putExtra("rollArray",rollArray);
        intent.putExtra("nameArray", nameArray);
        startActivity(intent);
    }

    private void showCalendar() {
        calendar.show(getSupportFragmentManager(), "");
        calendar.setOnCalendarOkClickListener(this::onCalendarOkClicked);
    }

    private void onCalendarOkClicked(int year, int month, int day) {
        calendar.setDate(year, month, day);
        subtitle.setText(gradeLevel + " | " + calendar.getDate());
        loadStatusData();
    }

    private void showAddStudentDialog() {
        MyDialog dialog = new MyDialog();
        dialog.show(getSupportFragmentManager(), MyDialog.STUDENT_ADD_DIALOG);
        dialog.setListener((roll, name) -> addStudent(roll, name));
    }

    private void addStudent(String roll_string, String name) {
        int roll = Integer.parseInt(roll_string);
        long sid = dbHelper.addStudent(cid, roll, name);
        if (sid != -1) {
            StudentItem studentItem = new StudentItem(sid, roll, name);
            studentItems.add(studentItem);
            adapter.notifyDataSetChanged();
        } else {
        }
}
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case 0:
                showUpdateStudentDialog(item.getGroupId());
            break;
            case 1:
                deleteStudent(item.getGroupId());
        }
        return super.onContextItemSelected(item);
    }

    private void showUpdateStudentDialog(int position) {
        MyDialog dialog = new MyDialog(studentItems.get(position).getRoll(),studentItems.get(position).getName());
        dialog.show(getSupportFragmentManager(),MyDialog.STUDENT_UPDATE_DIALOG);
        dialog.setListener((roll_string,name)->updateStudent(position,name));
    }

    private void updateStudent(int position, String name) {
        dbHelper.updateStudent(studentItems.get(position).getSid(),name);
        studentItems.get(position).setName(name);
        adapter.notifyItemChanged(position);
    }

    private void deleteStudent(int position) {
        dbHelper.deleteStudent(studentItems.get(position).getSid());
        studentItems.remove(position);
        adapter.notifyItemRemoved(position);
    }
}