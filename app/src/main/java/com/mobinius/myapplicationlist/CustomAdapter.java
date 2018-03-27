/*
package com.mobinius.myapplicationlist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

*/
/**
 * Created by prajna on 30/5/17.
 *//*


public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {
    private Context context;
    private ArrayList<TaskClass> task;

    private List<String> dataList;
    private List<String> itemsPendingRemoval;

    private static final int PENDING_REMOVAL_TIMEOUT = 3000; // 3sec
    private Handler handler = new Handler(); // hanlder for running delayed runnables
    HashMap<String, Runnable> pendingRunnables = new HashMap<>(); // map of items to pending runnables, so we can cancel a removal if need be


    public CustomAdapter(Context context, ArrayList<TaskClass> task) {
        this.context = context;
        this.task = task;
        itemsPendingRemoval = new ArrayList<>();

        Log.d("listsize adapter", "listsize " + task.size());
    }


    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        Log.v("name****....", viewType + "");
        return new CustomViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int position) {

        final TaskClass taskClass = task.get(position);
        if (itemsPendingRemoval.contains(taskClass)) {
            holder.regularLayout.setVisibility(View.GONE);
            holder.swipeLayout.setVisibility(View.VISIBLE);
            holder.swipeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context,"hiiiii",Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            */
/** {show regular layout} and {hide swipe layout} *//*

            holder.regularLayout.setVisibility(View.VISIBLE);
            holder.swipeLayout.setVisibility(View.GONE);

            holder.name.setText(taskClass.getName());
            holder.description.setText(taskClass.getDescription());
            Log.v("name....onbind", position + "");
            holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int id = buttonView.getId();
                    if (id == R.id.check_box) {
                        if (holder.checkbox.isChecked()) {
                            holder.name.setPaintFlags(holder.name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            holder.description.setPaintFlags(holder.name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                            AddTaskActivity.statusLinearLayout.setVisibility(View.VISIBLE);
                        } else {
                            holder.name.setPaintFlags(holder.name.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                            holder.description.setPaintFlags(holder.name.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                            AddTaskActivity.statusLinearLayout.setVisibility(View.VISIBLE);

                        }
                    }
                }
            });
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    alertDialogBuilder.setMessage("Are you sure,You wanted to delete " + taskClass.getName() + " details..");

                    alertDialogBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                            int newPosition = holder.getAdapterPosition();
                            task.remove(newPosition);
                            notifyItemRemoved(newPosition);
                            notifyItemRangeChanged(newPosition, task.size());
                            Toast.makeText(context, taskClass.getName() + " deleted successfully", Toast.LENGTH_LONG).show();

                        }
                    });

                    alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();

                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            });

            holder.cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            holder.linearlayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, TaskDetailActivity.class);
                    intent.putExtra("name.", task.get(position).getName());
                    intent.putExtra("description.", taskClass.getDescription());
                    intent.putExtra("date.", taskClass.getDate());
                    intent.putExtra("time.", taskClass.getTime());

                    Log.v("aa", taskClass.getName());
                    Log.v("aa", taskClass.getDescription());
                    Log.v("aa", taskClass.getDate());
                    Log.v("aa", taskClass.getTime());

                    context.startActivity(intent);
                }
            });

        }

    }   @Override
    public long getItemId(int position) {
        Log.d("pos", "pos*** " + position);
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        Log.d("listsize adapter", "listsize*** " + task.size());

        if(task.size()==0){
            MainActivity.showText();
        }
        return task.size();
    }
    public void removeItem(int position) {
        task.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, task.size());
    }


    void changeItem(int position) {
        TaskClass item = task.get(position);
        item.setName("Deleting Task");
        item.setDescription("");
        notifyItemChanged(position);
    }
    private void undoOpt(String customer) {
        Runnable pendingRemovalRunnable = pendingRunnables.get(customer);
        pendingRunnables.remove(customer);
        if (pendingRemovalRunnable != null)
            handler.removeCallbacks(pendingRemovalRunnable);
        itemsPendingRemoval.remove(customer);
        // this will rebind the row in "normal" state
        notifyItemChanged(dataList.indexOf(customer));
    }



    public void pendingRemoval(int position) {

        final TaskClass taskClass = task.get(position);
        if (!itemsPendingRemoval.contains(taskClass)) {
            itemsPendingRemoval.add("bvbv");
            // this will redraw row in "undo" state
            notifyItemChanged(position);
            // let's create, store and post a runnable to remove the data
            Runnable pendingRemovalRunnable = new Runnable() {
                @Override
                public void run() {
                    remove(task.indexOf(taskClass));
                }
            };
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
            pendingRunnables.put("fvfv", pendingRemovalRunnable);
        }
    }

    public void remove(int position) {
        final TaskClass taskClass = task.get(position);
        if (itemsPendingRemoval.contains(taskClass)) {
            itemsPendingRemoval.remove(taskClass);
        }
        if (task.contains(taskClass)) {
            task.remove(position);
            notifyItemRemoved(position);
        }
    }

    public boolean isPendingRemoval(int position) {
        final TaskClass taskClass = task.get(position);
        return itemsPendingRemoval.contains(taskClass);
    }
    public class CustomViewHolder extends RecyclerView.ViewHolder {
        public TextView name, description,text;
        public CheckBox checkbox;
        public Button delete,cancel;
        public LinearLayout linearlayout;




        public RelativeLayout regularLayout;
        public LinearLayout swipeLayout;

        public CustomViewHolder(View view) {
            super(view);
            text = (TextView) view.findViewById(R.id.text);
            name = (TextView) view.findViewById(R.id.name_textview);
            description = (TextView) view.findViewById(R.id.description_textview);
            checkbox = (CheckBox) view.findViewById(R.id.check_box);
            delete = (Button) view.findViewById(R.id.delete_button);
            cancel = (Button) view.findViewById(R.id.cancel_button);
            linearlayout=(LinearLayout)view.findViewById(R.id.item_linear_layout);


            regularLayout = (RelativeLayout) view.findViewById(R.id.regularLayout);
            swipeLayout = (LinearLayout) view.findViewById(R.id.swipeLayout);

        }
    }



}

*/
