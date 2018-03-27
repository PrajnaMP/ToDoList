package com.mobinius.myapplicationlist;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by prajna on 5/6/17.
 */

class ItemViewHolder extends RecyclerView.ViewHolder {
    public TextView name, description, text,dueDate,completedDate,dueDateText,completedDateText,location;
    public CheckBox checkbox;
    public LinearLayout linearlayout;
    public LinearLayout regularLayout;
    public LinearLayout swipeLayout;
    public TextView cancel, delete, edit;
    public ImageView imageView;

    public ItemViewHolder(View view) {
        super(view);
        Log.v("aaaaa","viewholder");

        text = (TextView) view.findViewById(R.id.text);
        name = (TextView) view.findViewById(R.id.name_textview);
        description = (TextView) view.findViewById(R.id.description_textview);
        location = (TextView)view.findViewById(R.id.location_textview);
        checkbox = (CheckBox) view.findViewById(R.id.check_box);
        linearlayout = (LinearLayout) view.findViewById(R.id.item_linear_layout);
        dueDate=(TextView)view.findViewById(R.id.due_date_textview);
        completedDate=(TextView)view.findViewById(R.id.completed_date_textview);
        imageView=(ImageView)view.findViewById(R.id.image_view);
        dueDateText=(TextView)view.findViewById(R.id.due_date_text);
        completedDateText=(TextView)view.findViewById(R.id.completed_date_text);
        regularLayout = (LinearLayout) view.findViewById(R.id.regularLayout);

        swipeLayout = (LinearLayout) view.findViewById(R.id.swipeLayout);
        cancel = (TextView) view.findViewById(R.id.cancel);
        delete = (TextView) view.findViewById(R.id.delete);
        edit = (TextView) view.findViewById(R.id.edit);

    }
}


