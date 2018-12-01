package com.virtualrobe.virtualrobe.virtualrobe_app.Registration;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.virtualrobe.virtualrobe.virtualrobe_app.Profile_Activity;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.Color;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.MainActivityViewModel;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.Style;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.user_model;

import java.util.HashMap;
import java.util.Map;

/*Activity to save user preferences and details to the database*/

public class save_to_database extends AppCompatActivity {
    private FirebaseFirestore mFirestore;
    private DocumentReference mUserRef;
    private Query mQuery;
    DocumentSnapshot snapshot;
    private MainActivityViewModel mViewModel;
    private static final int LIMIT = 1;

    String Nickname;
    String Gender;
    String Birthday;
    String Location;
    String Favourite_Colors;
    String Fashion_Style;
    String email;
    String full_name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.save_to_database);

        mFirestore = FirebaseFirestore.getInstance();
        // Enable Firestore logging
        //FirebaseFirestore.setLoggingEnabled(true);

        // View model
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        // Query to check if current user details exist in the database
        initFirestore();

        // Collect user details from Sign_up_Activity
        Intent data = getIntent();
        try {
            if (data != null) {
                Nickname = data.getStringExtra("Username");
                Gender = data.getStringExtra("Gender");
                Birthday = data.getStringExtra("Birthday");
                Location = data.getStringExtra("Location");
                Favourite_Colors = data.getStringExtra("Colors");
                Fashion_Style = data.getStringExtra("Styles");

                //save user details to database
                firestore_batch_saving();

            } else {
                Toast.makeText(save_to_database.this, "Error receiving user details", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception exception) {
            Toast.makeText(getApplicationContext(), "Error Occured: " + exception.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    /*query database to check for registered email*/
    private void initFirestore() {
        email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        full_name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        if (email == null) {
            Toast.makeText(getApplicationContext(), "No email found", Toast.LENGTH_SHORT).show();
        }
    }

    /*Save to database*/
    public void firestore_batch_saving() {

        Toast.makeText(getApplicationContext(), Nickname + "" + Gender + "" + Birthday + "" +
                Location + "" + Favourite_Colors + "" + Fashion_Style, Toast.LENGTH_LONG).show();

        user_model model = new user_model();

        /*save User details*/
        if (Nickname != null) {
            model.setUsername(Nickname);
        }
        if (email != null) {
            model.setEmail(email);
        }
        if (full_name != null) {
            model.setFullname(full_name);
        }
        if (Gender != null) {
            model.setGender(Gender);
        }
        if (Location != null) {
            model.setAddress(Location);
        }
        if (Birthday != null) {
            model.setDOB(Birthday);
        }
        model.setPrivate_account(0);

        mFirestore.collection("Users")
                .add(model)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Snackbar.make(findViewById(android.R.id.content), "User Details Added",
                                Snackbar.LENGTH_SHORT).show();

                        /*Save details of User favourite Colors*/
                        if (Favourite_Colors != null && !Favourite_Colors.equals("")) {
                            String[] Colors = Favourite_Colors.split("\\W+");
                            for (final String color : Colors) {
                                // Create Reference for favourite Colors
                                Map<String, Object> colors = new HashMap<>();
                                colors.put("name", color);
                                documentReference.collection("Favourite_Colors")
                                        .document()
                                        .set(colors)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Snackbar.make(findViewById(android.R.id.content), "User Colors Added",
                                                        Snackbar.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Snackbar.make(findViewById(android.R.id.content), e.getMessage(),
                                                        Snackbar.LENGTH_LONG).show();
                                                Log.e("color error", e.getMessage());
                                            }
                                        });
                            }
                        } else {
                            Snackbar.make(findViewById(android.R.id.content), "User Colors Not found",
                                    Snackbar.LENGTH_SHORT).show();
                        }

                        /*Save Details of Fashion style*/
                        if (Fashion_Style != null && !Fashion_Style.equals("")) {
                            String[] Styles = Fashion_Style.split("\\W+");
                            for (String style : Styles) {
                                // Create Reference for Fashion Styles
                                Map<String, Object> styles = new HashMap<>();
                                styles.put("name", style);
                               documentReference.collection("Fashion_Styles")
                                        .document()
                                        .set(styles)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Snackbar.make(findViewById(android.R.id.content), "User fashion styles Added",
                                                        Snackbar.LENGTH_SHORT).show();
                                                Intent intent = new Intent(save_to_database.this, Profile_Activity.class);
                                                intent.putExtra("Status", "Success");
                                                startActivity(intent);
                                                finish();
                                            }
                                        })

                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Snackbar.make(findViewById(android.R.id.content), e.getMessage(),
                                                        Snackbar.LENGTH_LONG).show();
                                            }
                                        });
                            }
                        }
                    }
                });
    }
}



