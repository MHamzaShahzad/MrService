package com.example.mrservice.fragments;


import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.example.mrservice.Constants;
import com.example.mrservice.R;
import com.example.mrservice.models.TaskModel;
import com.google.android.material.snackbar.Snackbar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class FragmentGetTaskDate extends Fragment implements View.OnClickListener {

    public static final String TAG = FragmentGetTaskDate.class.getName();
    private Context context;
    private View view;

    private Bundle bundleData;

    private Button btnNextFromDueDate;
    private TextView selectDueDate;

    private DatePickerDialog dialog;
    public static DateFormat formatter = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);

    public static FragmentGetTaskDate getInstance(Bundle bundle) {
        return new FragmentGetTaskDate(bundle);
    }

    public FragmentGetTaskDate(Bundle bundle) {
        // Required empty public constructor
        this.bundleData = bundle;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = container.getContext();
        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_get_task_date, container, false);

            findViewsById();
            initDatePickerDialog();
        }
        return view;
    }

    private void findViewsById() {
        selectDueDate = view.findViewById(R.id.selectDueDate);
        btnNextFromDueDate = view.findViewById(R.id.btnNextFromDueDate);

        initClickListeners();
    }

    private void initClickListeners() {
        selectDueDate.setOnClickListener(this);
        btnNextFromDueDate.setOnClickListener(this);
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            datePicker.animate();
            Log.e(TAG, "onDateSet: " + getStringDay(day) + " " + getMonthString(month) + " " + year);
            selectDueDate.setText(getStringDay(day) + " " + getMonthString(month) + " " + year);
        }
    };

    private void initDatePickerDialog() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        dialog = new DatePickerDialog(
                context,
                mDateSetListener,
                year, month, day
        );
        selectDueDate.setText(formatter.format(cal.getTime()));
    }

    private void showDatePickerDialog() {
        dialog.show();
    }

    private String getStringDay(int day) {
        if (day < 9)
            return "0" + day;
        else
            return String.valueOf(day);
    }

    private String getMonthString(int month) {
        String monthName = null;
        switch (month) {
            case 0:
                monthName = "Jan";
                break;
            case 1:
                monthName = "Feb";
                break;
            case 2:
                monthName = "Mar";
                break;
            case 3:
                monthName = "Apr";
                break;
            case 4:
                monthName = "May";
                break;
            case 5:
                monthName = "Jun";
                break;
            case 6:
                monthName = "Jul";
                break;
            case 7:
                monthName = "Aug";
                break;
            case 8:
                monthName = "Sep";
                break;
            case 9:
                monthName = "Oct";
                break;
            case 10:
                monthName = "Nov";
                break;
            case 11:
                monthName = "Dec";
                break;
        }
        return monthName;
    }


    @Override
    public void onClick(View view) {
        if (view == selectDueDate) {
            showDatePickerDialog();
        }
        if (view == btnNextFromDueDate) {

            if (TextUtils.isEmpty(selectDueDate.getText())) {
                Snackbar.make(view, "Please select due date for your poster / task!", Snackbar.LENGTH_LONG).show();
            } else {
                bundleData.putString(TaskModel.STRING_TASK_DUE_DATE_REF, selectDueDate.getText().toString().trim());
                ((FragmentActivity) context)
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(android.R.id.content, FragmentGetTaskBudget.getInstance(bundleData), Constants.TITLE_TASK_BUDGET)
                        .addToBackStack(Constants.TITLE_TASK_BUDGET)
                        .commit();
            }

        }
    }
}
