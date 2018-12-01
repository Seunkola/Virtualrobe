package com.virtualrobe.virtualrobe.virtualrobe_app.Utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.rarepebble.colorpicker.ColorPickerView;


public class color_picker_dialog extends DialogFragment {
    public interface OnCompleteListener {
        void onComplete(int color);
    }

    private OnCompleteListener mListener;

    Dialog dialog;
    ColorPickerView picker;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Change Outfit Background");
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.color_picker_dialog, null);
        picker = (ColorPickerView)view.findViewById(R.id.colorPicker);
        picker.setColor(0xffff0000);
        builder.setView(view);
        builder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // on success
                try {
                    final int color = picker.getColor();
                    SharedPreferences background = getActivity().getSharedPreferences("background_color", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = background.edit();
                    editor.putInt("background", color);
                    editor.apply();
                    if(mListener!=null) {
                        mListener.onComplete(color);
                    }
                    else {
                        Toast.makeText(getActivity(), "not working", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }
                catch (Exception e){

                }
                //getActivity().startActivity(new Intent(getActivity(),Styling_activity.class));
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder.create();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity;
        /*if (context instanceof Activity){
            activity=(Activity) context;
            try {
                this.mListener = (OnCompleteListener)activity;
            }
            catch (final ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement OnCompleteListener");
            }
        }*/

        if (context instanceof OnCompleteListener){
            this.mListener = (OnCompleteListener)context;
        }

    }

}
