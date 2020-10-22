package com.example.whatsapplite;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class SearchActivity extends AppCompatActivity {
    private Button searchBtn;
    private EditText inputText;
    private RecyclerView searchList;
    private String searchInput,searchInputTOUpper,searchInputTOLower;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        inputText=findViewById(R.id.search_name);
        searchBtn=findViewById(R.id.search_btn);
        searchList=findViewById(R.id.search_list);
        searchList.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                searchInput =inputText.getText().toString().toLowerCase();
                onStart();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");

        FirebaseRecyclerOptions<Contacts> options = null;
        searchInput =inputText.getText().toString().toLowerCase();
        if(searchInput.equals(""))
        {
            options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(reference,Contacts.class).build();
        }
        else
        {
            options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(reference.orderByChild("name").startAt(searchInput).endAt(searchInput+"\uf8ff"),Contacts.class).build();
        }
        FirebaseRecyclerAdapter<Contacts, ContactsFragment.ContactsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ContactsFragment.ContactsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ContactsFragment.ContactsViewHolder holder,final int position, @NonNull final Contacts model) {
                        holder.userName.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);


                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String visit_user_id = getRef(position).getKey();

                                Intent intent = new Intent(SearchActivity.this,ProfileActivity.class);
                                intent.putExtra("visit_user_id",visit_user_id);
                                intent.putExtra("profile_image",model.getImage());
                                intent.putExtra("profile_name",model.getName());
                                startActivity(intent);
                            }
                        });



                    }

                    @NonNull
                    @Override
                    public ContactsFragment.ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent,false);
                        ContactsFragment.ContactsViewHolder viewHolder =new ContactsFragment.ContactsViewHolder(view);
                        return viewHolder;
                    }
                };
        searchList.setAdapter(adapter);
        adapter.startListening();



    }
}