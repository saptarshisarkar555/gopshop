package com.example.whatsapplite;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    public interface ChnageStatusListener {
        void onItemChangeListener(int position, Contact model);
    }

    ArrayList<Contact> models;
    Context mContext;
    ChnageStatusListener chnageStatusListener;

    public void setModel(ArrayList<Contact> models) {
        this.models = models;
    }

    public Adapter(ArrayList<Contact> models, Context mContext, ChnageStatusListener chnageStatusListener) {
        this.models = models;
        this.mContext = mContext;
        this.chnageStatusListener = chnageStatusListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_layout, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {

        Contact model = models.get(i);
        if (model != null) {
            viewHolder.text.setText(model.getName());
            viewHolder.position = i;
//            viewHolder.image.setImageResource(model.getImage());
            if (model.isSelect()) {
                viewHolder.view.setBackgroundColor(mContext.getResources().getColor(R.color.colorAccent));
            } else {
                viewHolder.view.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimary));
            }
        }
        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Contact model1 = models.get(i);
                if (model1.isSelect()) {
                    model1.setSelect(false);
                } else {
                    model1.setSelect(true);
                }
                models.set(viewHolder.position, model1);
                if (chnageStatusListener != null) {
                    chnageStatusListener.onItemChangeListener(viewHolder.position, model1);
                }
                notifyItemChanged(viewHolder.position);

            }
        });


    }

    @Override
    public int getItemCount() {
        if (models != null) {
            return models.size();
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView text;
        public View view;
        public int position;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            image = itemView.findViewById(R.id.image);
            text = itemView.findViewById(R.id.text);
        }
    }
}


