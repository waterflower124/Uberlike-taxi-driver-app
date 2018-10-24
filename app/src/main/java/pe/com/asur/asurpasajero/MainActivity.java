package pe.com.asur.asurpasajero;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import io.paperdb.Paper;
import pe.com.asur.asurpasajero.Common.Common;
import pe.com.asur.asurpasajero.Model.Token;
import pe.com.asur.asurpasajero.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    Button btnSignIn,btnRegister;
    RelativeLayout rootLayout;


    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;

    TextView txt_forgot_pwd;

    int car_number = 0;



    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Before setContentView
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_main);

        //Init PapedDB
        Paper.init(this);



        //Init Firebase
        auth = FirebaseAuth.getInstance();
        db= FirebaseDatabase.getInstance();
        users = db.getReference(Common.user_driver_tbl);

        //Init View
        btnRegister = (Button)findViewById(R.id.btnRegister);
        btnSignIn = (Button)findViewById(R.id.btnSignIn);
        rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);
        txt_forgot_pwd = (TextView)findViewById(R.id.txt_forgot_password);
        txt_forgot_pwd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showDialogForgotPwd();
                return false;
            }
        });

        //Event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterDialog();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoginDialog();

            }
        });


        //AutoLogin system
        String user=Paper.book().read(Common.user_field);
        String pwd=Paper.book().read(Common.pwd_field);

        if(user!=null && pwd!=null){
            if(!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pwd)){
                autoLogin(user, pwd);
            }
        }



        /////car_number generate

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        final DatabaseReference driver_table = db.getReference(Common.user_driver_tbl);
        driver_table.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
//                            if(car_number < snapshot.child("car_number")) {
//
//                            }
                    if(snapshot.hasChild("car_number")) {
                        String number_str = snapshot.child("car_number").getValue().toString();
                        if (car_number <= Integer.parseInt(number_str)) {
                            car_number = Integer.parseInt(number_str) + 1;
                        }


                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void checkOrderExist() {

        final String key = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        final DatabaseReference current_order = db.getReference(Common.register_current_order);

        current_order.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    if (key.equals(orderSnapshot.child("driverID").getValue())) {
                        Common.ordering = true;
                        Common.passengerID = orderSnapshot.getKey();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });

    }

    private void autoLogin(String user, String pwd) {
        //Check validation
        if (TextUtils.isEmpty(user)) {
            Snackbar.make(rootLayout, "Plase enter email address", Snackbar.LENGTH_SHORT).show();
            return;
        }


        if (TextUtils.isEmpty(pwd)) {
            Snackbar.make(rootLayout, "Plase enter Password", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (pwd.toString().length() < 6) {
            Snackbar.make(rootLayout, "Password too short", Snackbar.LENGTH_SHORT).show();
            return;
        }

        final AlertDialog waitingDialog = new SpotsDialog(MainActivity.this);
        waitingDialog.show();

        //Login
        auth.signInWithEmailAndPassword(user,pwd)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        waitingDialog.dismiss();

                        FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl)
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        final DataSnapshot userDataSnapshot = dataSnapshot;

                                        waitingDialog.dismiss();

//                                        Log.d("00000000000: ", "autologin   :" + FirebaseAuth.getInstance().getCurrentUser().getUid());
//
//                                        FirebaseDatabase db_driver = FirebaseDatabase.getInstance();
//                                        final DatabaseReference tokens = db_driver.getReference(Common.driver_tbl);
//
//                                        tokens.addListenerForSingleValueEvent(new ValueEventListener() {
//                                            @Override
//                                            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                                                if(dataSnapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
//                                                    Snackbar.make(rootLayout,"Failed to Login. Please login other user.", Snackbar.LENGTH_SHORT)
//                                                            .show();
//                                                } else {
//                                                    Common.currentUser = userDataSnapshot.getValue(User.class);
//
//                                                    checkOrderExist();
//                                                    startActivity(new Intent(MainActivity.this,ChoferHome.class));
////                                        waitingDialog.dismiss();
//                                                    finish();
//                                                }
//
////
//                                            }
//
//                                            @Override
//                                            public void onCancelled(DatabaseError databaseError) {
//
//                                            }
//                                        });





                                        Common.currentUser = dataSnapshot.getValue(User.class);

                                        checkOrderExist();
                                        startActivity(new Intent(MainActivity.this,ChoferHome.class));
//                                        waitingDialog.dismiss();
                                        finish();

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });



                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                waitingDialog.dismiss();
                Snackbar.make(rootLayout,"Failed "+e.getMessage(),Snackbar.LENGTH_SHORT)
                        .show();

                //Active button
                btnSignIn.setEnabled(true);
            }
        });

    }

    private void showDialogForgotPwd() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("OLVIDO CONTRASEÑA");
        alertDialog.setMessage("POR FAVOR INGRESE CORREO");

        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View forgot_pwd_layout = inflater.inflate(R.layout.layout_forgot_pwd,null);

        final MaterialEditText edtEmail = (MaterialEditText)forgot_pwd_layout.findViewById(R.id.edtEmail);
        alertDialog.setView(forgot_pwd_layout);

        //set buttom
        alertDialog.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                final AlertDialog waitingDialog = new SpotsDialog(MainActivity.this);
                waitingDialog.show();

                auth.sendPasswordResetEmail(edtEmail.getText().toString().trim())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dialogInterface.dismiss();
                                waitingDialog.dismiss();

                                Snackbar.make(rootLayout,"LINK ENVIADO A TU CORREO",Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialogInterface.dismiss();
                        waitingDialog.dismiss();

                        Snackbar.make(rootLayout,""+e.getMessage(),Snackbar.LENGTH_SHORT)
                                .show();
                    }
                });
            }
        });

        alertDialog.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        //fix dialog not show
        alertDialog.show();
    }

    private void showLoginDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("INICIAR ");
        dialog.setMessage("Email y Clave");

        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.layout_login,null);

        final MaterialEditText edtEmail = login_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword = login_layout.findViewById(R.id.edtPassword);


        dialog.setView(login_layout);

        //Set button
        dialog.setPositiveButton("INICIAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                dialogInterface.dismiss();

                //Set disable button Sign in if is processing
                btnSignIn.setEnabled(false);

                //Check validation
                if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                    Snackbar.make(rootLayout, "Plase enter email address", Snackbar.LENGTH_SHORT).show();
                    btnSignIn.setEnabled(true);
                    return;
                }


                if (TextUtils.isEmpty(edtPassword.getText().toString())) {
                    Snackbar.make(rootLayout, "Plase enter Password", Snackbar.LENGTH_SHORT).show();
                    btnSignIn.setEnabled(true);
                    return;
                }

                if (edtPassword.getText().toString().length() < 6) {
                    Snackbar.make(rootLayout, "Password too short", Snackbar.LENGTH_SHORT).show();
                    btnSignIn.setEnabled(true);
                    return;
                }

                final AlertDialog waitingDialog = new SpotsDialog(MainActivity.this);
                waitingDialog.show();

                //Login
                auth.signInWithEmailAndPassword(edtEmail.getText().toString(),edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                waitingDialog.dismiss();


                                FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl)
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                final DataSnapshot userdataSnapshot = dataSnapshot;

                                                Log.d("1111111111", "click login button   :" + FirebaseAuth.getInstance().getCurrentUser().getUid());


//                                                FirebaseDatabase db_driver = FirebaseDatabase.getInstance();
//                                                DatabaseReference tokens = db_driver.getReference(Common.driver_tbl);
//                                                Log.d("111111111", "33333333  :" +  ":  uuuuuuuuu");

//                                                FirebaseDatabase.getInstance().getReference(Common.driver_tbl)
//                                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
//                                                    @Override
//                                                    public void onDataChange(DataSnapshot dataSnapshot) {
//
//                                                        if(dataSnapshot.exists()) {
//                                                            //Active button
//                                                            btnSignIn.setEnabled(true);
//                                                            Snackbar.make(rootLayout,"Failed to Login. Please login other user.", Snackbar.LENGTH_SHORT)
//                                                                    .show();
//                                                            Log.d("1111111111", "eeeeeeeeeeee   :");
//
//                                                        } else {
//                                                            //Save value autlogin
//                                                            Paper.book().write(Common.user_field,edtEmail.getText().toString());
//                                                            Paper.book().write(Common.pwd_field,edtPassword.getText().toString());
//
//                                                            Common.currentUser = userdataSnapshot.getValue(User.class);
//                                                            if(Common.currentUser == null)
//                                                                Log.d("111111111", "22222" + FirebaseAuth.getInstance().getCurrentUser().getUid());
//                                                            else
//                                                                Log.d("111111111", "33333333  :" + Common.currentUser.getName() +  ":  ERROR          FOUND");
//                                                            checkOrderExist();
//                                                            startActivity(new Intent(MainActivity.this, ChoferHome.class));
//
//                                                            finish();
//                                                        }
//                                                    }
//
//                                                    @Override
//                                                    public void onCancelled(DatabaseError databaseError) {
//                                                        Toast.makeText(getApplicationContext(), "canceled", Toast.LENGTH_LONG).show();
//                                                    }
//                                                });




                                                //Save value autlogin
                                                Paper.book().write(Common.user_field,edtEmail.getText().toString());
                                                Paper.book().write(Common.pwd_field,edtPassword.getText().toString());

                                                Common.currentUser = dataSnapshot.getValue(User.class);
                                                if(Common.currentUser == null)
                                                    Log.d("111111111", "22222" + FirebaseAuth.getInstance().getCurrentUser().getUid());
                                                else
                                                    Log.d("111111111", "33333333  :" + Common.currentUser.getName() +  ":  ERROR FOUND");
                                                checkOrderExist();
                                                startActivity(new Intent(MainActivity.this, ChoferHome.class));
                                                finish();

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });



                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingDialog.dismiss();
                        Snackbar.make(rootLayout,"Failed "+e.getMessage(),Snackbar.LENGTH_SHORT)
                                .show();

                        //Active button
                        btnSignIn.setEnabled(true);
                    }
                });

            }

        });
        dialog.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });




        dialog.show();
    }


    private void showRegisterDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("REGISTERARSE ");
        dialog.setMessage("Plase use email to register");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.layout_register,null);

        final MaterialEditText edtEmail = register_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword = register_layout.findViewById(R.id.edtPassword);
        final MaterialEditText edtName = register_layout.findViewById(R.id.edtName);
        final MaterialEditText edtPhone = register_layout.findViewById(R.id.edtPhone);
        final MaterialEditText edtPlaca = register_layout.findViewById(R.id.edtPlaca);


        dialog.setView(register_layout);

        //Set button
        dialog.setPositiveButton("REGISTERARSE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                dialogInterface.dismiss();

                //Check validation
                if(TextUtils.isEmpty(edtEmail.getText().toString()))
                {
                    Snackbar.make(rootLayout,"Plase enter email address",Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                if(TextUtils.isEmpty(edtPhone.getText().toString()))
                {
                    Snackbar.make(rootLayout,"Plase enter phone number",Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                if(TextUtils.isEmpty(edtPassword.getText().toString()))
                {
                    Snackbar.make(rootLayout,"Plase enter Password",Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                if(edtPassword.getText().toString().length() < 6)
                {
                    Snackbar.make(rootLayout,"Password too short",Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }





                //Register new user
                auth.createUserWithEmailAndPassword(edtEmail.getText().toString(),edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                //Save user to db
                                User user = new User();
                                user.setEmail(edtEmail.getText().toString());
                                user.setName(edtName.getText().toString());
                                user.setPhone(edtPhone.getText().toString());
                                user.setPassword(edtPassword.getText().toString());
                                user.setPlaca(edtPlaca.getText().toString());
                                user.setCar_number(car_number);
                            //    user.setAvatarUrl("");creoq ue dberia  deir como en pasajero
                            //    user.setRates("");


                                //Use email to key

                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Snackbar.make(rootLayout,"Register success fully !!!",Snackbar.LENGTH_SHORT)
                                                        .show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Snackbar.make(rootLayout,"Failed"+e.getMessage(),Snackbar.LENGTH_SHORT)
                                                        .show();
                                            }
                                        });


                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(rootLayout,"Failed"+e.getMessage(),Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                        });

            }
        });

        dialog.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

            }
        });

        dialog.show();
    }
}
