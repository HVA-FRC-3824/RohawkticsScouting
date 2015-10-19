package com.example.akmessing1.scoutingtest.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.akmessing1.scoutingtest.R;

public class CustomCheckbox extends CustomScoutView {

    private final CheckBox checkbox;

    public CustomCheckbox(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.custom_checkbox, this);

        checkbox = (CheckBox)this.findViewById(R.id.checkbox);
        TextView label = (TextView) this.findViewById(R.id.label);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomScoutView);
        label.setText(typedArray.getString(R.styleable.CustomScoutView_label));
        typedArray.recycle();

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkbox.setChecked(!checkbox.isChecked());
            }
        });
    }

}
