package com.virtualrobe.virtualrobe.virtualrobe_app.Registration;

/*A form to collect user data and preferences*/

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.User;
import com.heinrichreimersoftware.singleinputform.SingleInputFormActivity;
import com.heinrichreimersoftware.singleinputform.steps.DateStep;
import com.heinrichreimersoftware.singleinputform.steps.Step;
import com.heinrichreimersoftware.singleinputform.steps.TextStep;
import com.medialablk.easygifview.EasyGifView;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class Sign_up_Activity extends SingleInputFormActivity{
    private static final String DATA_KEY_NICKNAME = "nickname";
    private static final String DATA_KEY_GENDER = "gender";
    private static final String DATA_KEY_BIRTHDAY = "birthday";
    private static final String DATA_KEY_LOCATION = "location";
    private static final String DATA_KEY_FAVOURITE_COLORS = "favourite_colors";
    private static final String DATA_KEY_FASHION_STYLE = "fashion_style";


    @Override
    protected List<Step> onCreateSteps() {
        List<Step> steps = new ArrayList<>();

        setInputGravity(Gravity.CENTER);

        /*username/nickname step 1*/
        steps.add(new TextStep.Builder(this,DATA_KEY_NICKNAME)
                .titleResId(R.string.nickname)
                .errorResId(R.string.nickname_error)
                .detailsResId(R.string.nickname_details)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .validator(new TextStep.Validator()
                {
                    @Override
                    public boolean validate(String input) {
                        String username = input.trim();
                        return username.length() >= 2;
                    }
                })
                .build());

        /*gender step 2*/
        steps.add(new TextStep.Builder(this,DATA_KEY_GENDER)
                .titleResId(R.string.gender)
                .errorResId(R.string.gender_error)
                .detailsResId(R.string.gender_details)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .validator(new TextStep.Validator()
                {
                    @Override
                    public boolean validate(String input) {
                        String gender = input.trim();
                        return gender.equalsIgnoreCase("Male")|| gender.equalsIgnoreCase("Female")
                                || gender.equalsIgnoreCase("Other");
                    }
                })
                .build()
        );

        /*DOB step 3*/
        steps.add(new DateStep.Builder(this, DATA_KEY_BIRTHDAY)
                .titleResId(R.string.birthday)
                .errorResId(R.string.birthday_error)
                .detailsResId(R.string.birthday_details)
                .validator(new DateStep.Validator() {
                    @Override
                    public boolean validate(int year, int month, int day) {
                        Calendar today = new GregorianCalendar();
                        Calendar birthday = new GregorianCalendar(year, month, day);
                        today.add(Calendar.YEAR, -13);
                        return today.after(birthday);

                    }
                })
                .build());

        /*Location step 4*/
        steps.add(new TextStep.Builder(this, DATA_KEY_LOCATION)
                .titleResId(R.string.location)
                .errorResId(R.string.location_error)
                .detailsResId(R.string.location_details)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .build());


        /*favourite colors step 5*/
        steps.add(new TextStep.Builder(this,DATA_KEY_FAVOURITE_COLORS)
                .titleResId(R.string.favourite_colors)
                .errorResId(R.string.favourite_colors_error)
                .detailsResId(R.string.favourite_colors_details)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .validator(new TextStep.Validator()
                {
                    @Override
                    public boolean validate(String input) {
                        return input.length() >= 6;
                    }
                })
                .build()
        );

        /*Fashion style step 6*/
        steps.add(new TextStep.Builder(this,DATA_KEY_FASHION_STYLE)
                .titleResId(R.string.fashion_style)
                .errorResId(R.string.fashion_style_error)
                .detailsResId(R.string.fashion_style_details)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .validator(new TextStep.Validator()
                {
                    @Override
                    public boolean validate(String input) {
                        return input.length() >= 3;
                    }
                })
                .build()
        );


        return steps;
    }

    @Override
    protected View onCreateFinishedView(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.signup_dialog, parent, false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        welcome_dialog();

    }

    @Override
    protected void onFormFinished(Bundle bundle) {
        //send data to save_to_database activity
        String Username = TextStep.text(bundle,DATA_KEY_NICKNAME);
        String Gender = TextStep.text(bundle,DATA_KEY_GENDER);
        String DOB = DateStep.day(bundle, DATA_KEY_BIRTHDAY) + "/" + DateStep.month(bundle, DATA_KEY_BIRTHDAY)
                + "/" + DateStep.year(bundle, DATA_KEY_BIRTHDAY);
        String Location = TextStep.text(bundle,DATA_KEY_LOCATION);
        String Favourite_Colors = TextStep.text(bundle,DATA_KEY_FAVOURITE_COLORS);
        String Fashion_Styles = TextStep.text(bundle,DATA_KEY_FASHION_STYLE);

        Intent Send_to_Database = new Intent(Sign_up_Activity.this, save_to_database.class);
        Send_to_Database.putExtra("Username", Username.toLowerCase());
        Send_to_Database.putExtra("Gender", Gender.toLowerCase());
        Send_to_Database.putExtra("Birthday", DOB.toLowerCase());
        Send_to_Database.putExtra("Location", Location.toLowerCase());
        Send_to_Database.putExtra("Colors", Favourite_Colors.toLowerCase());
        Send_to_Database.putExtra("Styles", Fashion_Styles.toLowerCase());
        startActivity(Send_to_Database);
        finish();
    }

    private void welcome_dialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(Sign_up_Activity.this);
        LayoutInflater inflater = LayoutInflater.from(Sign_up_Activity.this);
        View view = inflater.inflate(R.layout.signup_dialog,null);
        builder.setView(view);

        /*Assigning custom dialog button*/
        Button Ok = view.findViewById(R.id.ok_button);

        final AlertDialog dialog = builder.create();
        /*call on actions*/
        Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        /*Assign gif animation*/
        EasyGifView easyGifView = view.findViewById(R.id.easyGifView);
        easyGifView.setGifFromResource(R.drawable.boy);

        dialog.show();
    }
}
