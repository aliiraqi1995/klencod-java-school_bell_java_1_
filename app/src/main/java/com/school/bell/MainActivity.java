package com.school.bell;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private Map<Integer, DaySchedule> schedules = new HashMap<>();
    private int selectedDay = java.util.Calendar.SATURDAY;
    private Switch switchEnable;
    private TextView firstTimeText, titleText;
    private LinearLayout dayTabs;
    private ListView listView;
    private PeriodAdapter adapter;

    private DaySchedule current() {
        return schedules.get(selectedDay);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switchEnable = findViewById(R.id.switchEnable);
        firstTimeText = findViewById(R.id.firstTimeText);
        titleText = findViewById(R.id.mainTitle);
        dayTabs = findViewById(R.id.dayTabs);
        listView = findViewById(R.id.periodsList);

        schedules = ScheduleStorage.load(this);

        // تبويبات الأيام
        for (int d : DaySchedule.WEEK_ORDER) {
            TextView tab = new TextView(this);
            tab.setText(DaySchedule.nameOf(d));
            tab.setPadding(28, 14, 28, 14);
            tab.setTextSize(16);
            tab.setTag(d);
            tab.setOnClickListener(v -> {
                selectedDay = (int) v.getTag();
                refreshUI();
            });
            dayTabs.addView(tab);
        }

        switchEnable.setOnCheckedChangeListener((btn, checked) -> {
            current().enabled = checked;
            saveAndSchedule();
        });

        firstTimeText.setOnClickListener(v -> pickFirstLesson());
        findViewById(R.id.btnTest).setOnClickListener(v -> BellScheduler.testBellNow(this));
        findViewById(R.id.btnReset).setOnClickListener(v -> resetDay());
        findViewById(R.id.btnAdd).setOnClickListener(v -> editPeriod(null));

        adapter = new PeriodAdapter(this, current().periods,
                p -> editPeriod(p), p -> {
            current().periods.remove(p);
            saveAndSchedule();
        });
        listView.setAdapter(adapter);
        refreshUI();
    }

    private void refreshUI() {
        DaySchedule s = current();
        titleText.setText("جرس المدرسة — " + s.nameOf(s.weekday));
        switchEnable.setChecked(s.enabled);
        firstTimeText.setText(Period.fmt(s.firstLessonStart));
        for (int i = 0; i < dayTabs.getChildCount(); i++) {
            TextView tab = (TextView) dayTabs.getChildAt(i);
            boolean sel = (int) tab.getTag() == selectedDay;
            tab.setBackgroundColor(sel ? 0xFF1976D2 : 0x00000000);
            tab.setTextColor(sel ? 0xFFFFFFFF : 0xFF212121);
        }
        adapter.update(s.periods);
    }

    private void pickFirstLesson() {
        int h = current().firstLessonStart / 60;
        int m = current().firstLessonStart % 60;
        new TimePickerDialog(this, (view, hour, minute) -> {
            int old = current().firstLessonStart;
            int diff = (hour * 60 + minute) - old;
            current().firstLessonStart = hour * 60 + minute;
            for (Period p : current().periods) p.startMinutes += diff;
            saveAndSchedule();
        }, h, m, false).show();
    }

    private void resetDay() {
        int wd = selectedDay;
        schedules.put(wd, DaySchedule.defaultsFor(wd));
        adapter.update(current().periods);
        saveAndSchedule();
        Toast.makeText(this, "تمت إعادة الضبط", Toast.LENGTH_SHORT).show();
    }

    private void editPeriod(Period existing) {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_period, null);
        EditText name = v.findViewById(R.id.editName);
        TextView timeText = v.findViewById(R.id.timeText);
        Spinner duration = v.findViewById(R.id.spinnerDuration);
        CheckBox isBreak = v.findViewById(R.id.checkBreak);

        Integer[] durations = {5, 10, 15, 30, 35, 40, 45, 50};
        ArrayAdapter<Integer> spAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, durations);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        duration.setAdapter(spAdapter);

        final int[] startMin = new int[1];
        if (existing != null) {
            name.setText(existing.title);
            startMin[0] = existing.startMinutes;
            duration.setSelection(java.util.Arrays.asList(durations).indexOf(existing.duration));
            isBreak.setChecked(existing.isBreak);
        } else {
            startMin[0] = current().firstLessonStart;
        }
        timeText.setText(Period.fmt(startMin[0]));
        timeText.setOnClickListener(t -> new TimePickerDialog(this,
                (view, hour, minute) -> {
                    startMin[0] = hour * 60 + minute;
                    timeText.setText(Period.fmt(startMin[0]));
                }, startMin[0] / 60, startMin[0] % 60, false).show());

        new AlertDialog.Builder(this)
                .setTitle(existing == null ? "إضافة حصة/استراحة" : "تعديل")
                .setView(v)
                .setNegativeButton("إلغاء", null)
                .setPositiveButton("حفظ", (d, w) -> {
                    String title = name.getText().toString().trim();
                    if (title.isEmpty()) title = isBreak.isChecked() ? "استراحة" : "حصة";
                    int dur = (int) duration.getSelectedItem();
                    if (existing == null) {
                        current().periods.add(new Period(
                                UUID.randomUUID().toString(), title, startMin[0], dur,
                                isBreak.isChecked()));
                    } else {
                        existing.title = title;
                        existing.startMinutes = startMin[0];
                        existing.duration = dur;
                        existing.isBreak = isBreak.isChecked();
                    }
                    DaySchedule.sortPeriods(current());
                    adapter.update(current().periods);
                    saveAndSchedule();
                })
                .show();
    }

    private void saveAndSchedule() {
        ScheduleStorage.save(this, schedules);
        BellScheduler.scheduleAll(this, schedules);
    }
}