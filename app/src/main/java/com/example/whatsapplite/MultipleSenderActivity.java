package com.example.whatsapplite;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.ColorSpace;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class MultipleSenderActivity extends AppCompatActivity  implements Adapter.ChnageStatusListener, View.OnClickListener {
    private RecyclerView recyclerView=null;
    private Adapter adapter=null;
    private ArrayList<ColorSpace.Model> models;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buildResources();
        recyclerView=(RecyclerView)findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager manager=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(manager);
        adapter=new Adapter(models,MultipleSenderActivity.this,this);
        recyclerView.setAdapter(adapter);
        findViewById(R.id.delete).setOnClickListener(MultipleSenderActivity.this);
    }

    private void buildResources() {
        Resources resources=getResources();
        if(models==null&& resources!=null){
            String[] text=resources.getStringArray(R.array.text);
            TypedArray image=resources.obtainTypedArray(R.array.image);
            models=new ArrayList<>();
            for(int i=0;i<text.length;i++){
                Contacts model=new Contacts();
                model.setName(text[i]);
                model.setImage(image.getResourceId(i,R.mipmap.ic_launcher));
                model.setSelect(false);
                models.add(model);
            }
        }
    }

    @Override
    public void onItemChangeListener(int position, Contacts model) {
        try{
            models.set(position,model);

        }catch (Exception e){

        }
    }

    @Override
    public void onClick(View v) {
        updateList();
    }

    private void updateList() {
        ArrayList<Contacts> temp=new ArrayList<>();
        try{
            for(int i=0;i<models.size();i++){
                if(!models.get(i).isSelect()){
                    temp.add(models.get(i));
                }
            }

        }catch (Exception e){

        }
        models=temp;
        if(models.size()==0){
            recyclerView.setVisibility(View.GONE);
        }
        adapter.setModel(models);
        adapter.notifyDataSetChanged();
    }
}
