package com.test.googledrive.Config;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;

/**
 * Created by ANDT on 5/17/2016.
 */
public class TimeDialog {

    Context context;

    public TimeDialog(Context context) {
        this.context = context;
    }

    public void timeDialog() {
        Calendar now = Calendar.getInstance();
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                (TimePickerDialog.OnTimeSetListener) context,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
        );
        tpd.vibrate(false);
        tpd.dismissOnPause(false);
        tpd.enableSeconds(false);
        tpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Log.d("TimePicker", "Dialog was cancelled");
            }
        });
        tpd.show(((Activity) context).getFragmentManager(), "Timepickerdialog");
    }

    public void dateDialog() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                (DatePickerDialog.OnDateSetListener) context,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        dpd.vibrate(false);
        dpd.dismissOnPause(false);
        dpd.showYearPickerFirst(false);
        Calendar[] dates = new Calendar[13];
        for (int i = -6; i <= 6; i++) {
            Calendar date = Calendar.getInstance();
            date.add(Calendar.WEEK_OF_YEAR, i);
            dates[i + 6] = date;
        }
        dpd.setHighlightedDays(dates);
        dpd.show(((Activity) context).getFragmentManager(), "Datepickerdialog");
    }
}
