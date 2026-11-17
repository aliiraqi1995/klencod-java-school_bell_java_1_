package com.school.bell;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class PeriodAdapter extends BaseAdapter {

    public interface OnAction { void run(Period p); }

    private final Context ctx;
    private List<Period> data;
    private final OnAction onEdit, onDelete;

    public PeriodAdapter(Context ctx, List<Period> data, OnAction onEdit, OnAction onDelete) {
        this.ctx = ctx;
        this.data = data;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
    }

    public void update(List<Period> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    @Override public int getCount() { return data.size(); }
    @Override public Object getItem(int i) { return data.get(i); }
    @Override public long getItemId(int i) { return i; }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(ctx).inflate(R.layout.item_period, parent, false);
        }
        Period p = data.get(i);
        ((TextView) view.findViewById(R.id.periodTitle)).setText(p.title);
        ((TextView) view.findViewById(R.id.periodTime)).setText(p.timeRange());
        ((TextView) view.findViewById(R.id.periodDuration))
                .setText("المدة: " + p.duration + " دقيقة");

        view.findViewById(R.id.btnEdit).setOnClickListener(v -> onEdit.run(p));
        view.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            onDelete.run(p);
            Toast.makeText(ctx, "تم الحذف", Toast.LENGTH_SHORT).show();
        });
        return view;
    }
}
