package com.example.daftarbelanjahari;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

public class HomeActivity extends AppCompatActivity {

    private FloatingActionButton fab_btn;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private TextView totalResult;

    //Global Variable
    private String type;
    private int amount;
    private String note;
    private String post_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uId = mUser.getUid();//mengambil ID auth

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Daftar Belanja").child(uId);//menambah child dengan nama ID auth

        mDatabase.keepSynced(true);

        //ketika tombol tambah di klik
        fab_btn = findViewById(R.id.fab);
        fab_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog();
            }
        });

        totalResult = findViewById(R.id.total_amount);

        recyclerView = findViewById(R.id.recycler_home);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        //Total Sum Number
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int totalAmount = 0;
                for(DataSnapshot snap:dataSnapshot.getChildren()){
                    Data data = snap.getValue(Data.class);

                    totalAmount += data.getAmount();
                    String sttotal = String.valueOf("Rp "+totalAmount);

                    totalResult.setText(sttotal);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void customDialog(){
        AlertDialog.Builder mydialog = new AlertDialog.Builder(HomeActivity.this);

        //menampilkan menu tambah data
        LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
        View myview = inflater.inflate(R.layout.input_data, null);
        final AlertDialog dialog = mydialog.create();
        dialog.setView(myview);
        dialog.show();

        //init view
        final EditText type = myview.findViewById(R.id.edt_type);
        final EditText amount = myview.findViewById(R.id.edt_amount);
        final EditText note = myview.findViewById(R.id.edt_note);
        Button btnSave = myview.findViewById(R.id.btn_save);

        //ketika tombol save di klik
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mengambuk data dati text view
                String mType = type.getText().toString().trim();
                String mAmount = amount.getText().toString().trim();
                String mNote = note.getText().toString().trim();

                //mengubah tipe data string ke int
                int ammint = Integer.parseInt(mAmount);

                //pengecekan kondisi, kosong atau tidaknya
                if(TextUtils.isEmpty(mType)){
                    type.setError("Require Field...");
                    return;
                }

                if(TextUtils.isEmpty(mAmount)){
                    amount.setError("Require Field...");
                    return;
                }

                if (TextUtils.isEmpty(mNote)){
                    note.setError("Require Field...");
                    return;
                }

                String id = mDatabase.push().getKey();//mengambil id
                String date = DateFormat.getDateInstance().format(new Date());//mengambil tanggal
                Data data = new Data(mType, ammint, mNote, date, id);//mengeset data menurut Data.class

                mDatabase.child(id).setValue(data);//mengeset data di firebase

                Toast.makeText(getApplicationContext(), "Data Add", Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    protected void onStart(){
        super.onStart();

        //menampilkan list
        FirebaseRecyclerAdapter<Data, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>
                (
                        Data.class,
                        R.layout.item_data,
                        MyViewHolder.class,
                        mDatabase
                )
        {
            @Override
            protected void populateViewHolder(MyViewHolder viewHolder, final Data model, final int position) {
                viewHolder.setDate(model.getDate());
                viewHolder.setType(model.getType());
                viewHolder.setNote(model.getNote());
                viewHolder.setAmount(model.getAmount());

                viewHolder.myview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        post_key = getRef(position).getKey();
                        type=model.getType();
                        note=model.getNote();
                        amount=model.getAmount();

                        updateData();
                    }
                });
            }
        };

        recyclerView.setAdapter(adapter);
    }

    public void updateData(){
        //menmpilkan layout update data
        AlertDialog.Builder mydialog = new AlertDialog.Builder(HomeActivity.this);
        LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
        View mView = inflater.inflate(R.layout.update_data, null);

        final AlertDialog dialog = mydialog.create();
        dialog.setView(mView);

        //init view
        final EditText edt_type = mView.findViewById(R.id.edt_type_upd);
        final EditText edt_amount = mView.findViewById(R.id.edt_amount_upd);
        final EditText edt_note = mView.findViewById(R.id.edt_note_upd);

        //set data di layout update data
        edt_type.setText(type);
        edt_type.setSelection(type.length());

        edt_amount.setText(String.valueOf(amount));
        edt_amount.setSelection(String.valueOf(amount).length());

        edt_note.setText(note);
        edt_note.setSelection(note.length());

        Button btnUpdate = mView.findViewById(R.id.btn_save_upd);
        Button btnDelete = mView.findViewById(R.id.btn_delete_upd);

        //ketika tombol update di klik
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mengambil nilai
                type = edt_type.getText().toString().trim();

                String mAmount = String.valueOf(amount);
                mAmount = edt_amount.getText().toString().trim();

                note = edt_note.getText().toString().trim();

                int intAmount = Integer.parseInt(mAmount);
                String date = DateFormat.getDateInstance().format(new Date());

                //set data
                Data data = new Data(type, intAmount, note, date, post_key);

                //set update data
                mDatabase.child(post_key).setValue(data);

                //menutup layout
                dialog.dismiss();

            }
        });

        //ketika tombol delete di klik
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //menghapus data di firebase
                mDatabase.child(post_key).removeValue();

                //menutup layout
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            // ketika button menu log out dli klik
            case R.id.log_out:
                //keluar dari auth
                mAuth.signOut();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}
