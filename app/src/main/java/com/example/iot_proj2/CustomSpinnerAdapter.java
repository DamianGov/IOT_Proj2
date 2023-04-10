package com.example.iot_proj2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {

    private Context context;
    private List<String> items;
    private boolean[] selectedItems;

    public CustomSpinnerAdapter(Context context, List<String> items) {
        super(context, android.R.layout.simple_spinner_item, items);
        this.context = context;
        this.items = items;
        selectedItems = new boolean[items.size()];
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Use the default layout for the spinner item
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(items.get(position));

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Use a custom layout for the dropdown items
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.spinner_item_multi_select, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.txtSpnText);
        CheckBox checkBox = convertView.findViewById(R.id.chkSpnRes);

        textView.setText(items.get(position));
        checkBox.setChecked(selectedItems[position]);

        // Set a click listener for the checkbox to toggle its state
        checkBox.setOnClickListener(view -> {
            selectedItems[position] = !selectedItems[position];
            notifyDataSetChanged();
        });

        return convertView;
    }

    // Helper method to get selected items
    public List<String> getSelectedItems() {
        List<String> selected = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            if (selectedItems[i]) {
                selected.add(items.get(i));
            }
        }
        return selected;
    }
}

