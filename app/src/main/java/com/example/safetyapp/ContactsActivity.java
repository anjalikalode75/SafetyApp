package com.example.safetyapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ContactsActivity extends AppCompatActivity {

    private EditText edtName, edtNumber;
    private Button btnAdd;
    private RecyclerView recyclerView;
    private ContactsAdapter adapter;
    private ArrayList<Contact> contactList;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        edtName = findViewById(R.id.edtContactName);
        edtNumber = findViewById(R.id.edtContactNumber);
        btnAdd = findViewById(R.id.btnAddContact);
        recyclerView = findViewById(R.id.recyclerContacts);

        prefs = getSharedPreferences("trustedContacts", MODE_PRIVATE);

        contactList = new ArrayList<>();
        loadContacts();

        adapter = new ContactsAdapter(this, contactList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> addContact());

        // Swipe to delete
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                contactList.remove(pos);
                saveContacts();
                adapter.notifyItemRemoved(pos);
                Toast.makeText(ContactsActivity.this, "Contact deleted", Toast.LENGTH_SHORT).show();
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void addContact() {
        String name = edtName.getText().toString().trim();
        String number = edtNumber.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(number)) {
            Toast.makeText(this, "Enter name and number", Toast.LENGTH_SHORT).show();
            return;
        }

        Contact contact = new Contact(name, number);
        contactList.add(contact);
        adapter.notifyItemInserted(contactList.size() - 1);
        saveContacts();

        edtName.setText("");
        edtNumber.setText("");
    }

    private void saveContacts() {
        JSONArray jsonArray = new JSONArray();
        for (Contact c : contactList) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("name", c.getName());
                obj.put("number", c.getNumber());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(obj);
        }
        prefs.edit().putString("contacts", jsonArray.toString()).apply();
    }

    private void loadContacts() {
        String data = prefs.getString("contacts", null);
        contactList.clear();
        if (data != null) {
            try {
                JSONArray jsonArray = new JSONArray(data);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    contactList.add(new Contact(obj.getString("name"), obj.getString("number")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}