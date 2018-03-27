package com.mobinius.myapplicationlist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by prajna on 5/6/17.
 */

public class RVAdapter extends RecyclerView.Adapter<ItemViewHolder> {

    private Context context;
    private ArrayList<TaskClass> task;
    private ArrayList<TaskClass> itemsPendingRemoval;
    public static boolean swiped = false;
    Realm realm;
    private Handler handler = new Handler(); // hanlder for running delayed runnables
    HashMap<TaskClass, Runnable> pendingRunnables = new HashMap<>(); // map of items to pending runnables, so we can cancel a removal if need be

    public RVAdapter(Context context, ArrayList<TaskClass> task) {
        this.context = context;
        this.task = task;
        itemsPendingRemoval = new ArrayList<>();
        Log.d("listsize adapter", "listsize " + task.size());
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.v("aaaaa","oncreate");
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        realm = Realm.getDefaultInstance();
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, final int position) {
        Log.v("aaaaa","onbind");

        final TaskClass taskClass = task.get(position);
        if (itemsPendingRemoval.contains(taskClass)) {
            /** {show swipe layout} and {hide regular layout} */

            holder.regularLayout.setVisibility(View.GONE);
            holder.swipeLayout.setVisibility(View.VISIBLE);
            swiped = true;

            holder.cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    undoOpt(taskClass);
                }
            });

            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    remove(position);
                    deleteTask(task.get(position).getId(), position);
                }
            });

            holder.edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    undoOpt(taskClass);
                    Intent intent = new Intent(context, TaskDetailActivity.class);
                    intent.putExtra("name.", task.get(position).getName());
                    intent.putExtra("description.", taskClass.getDescription());
                    intent.putExtra("location.", taskClass.getLocation());
                    intent.putExtra("date.", taskClass.getDate());
                    intent.putExtra("time.", taskClass.getTime());
                    intent.putExtra("status.", task.get(position).getIsCompleted());
                    intent.putExtra("position.", position);
                    intent.putExtra("id.", task.get(position).getId());
//                    ((MainActivity) context).startActivityForResult(intent, 2);
                    context.startActivity(intent);
                }
            });
/*
           holder.edit.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   Intent intent = new Intent(context, TaskDetailActivity.class);
                   undoOpt(taskClass);

                   updateTask(position);
                   context.startActivity(intent);
               }
           });*/
        } else {

            if (task.get(position).getIsCompleted() == null) {
                holder.checkbox.setChecked(false);
                realm.beginTransaction();
                holder.name.setPaintFlags(holder.name.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                holder.description.setPaintFlags(holder.name.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                realm.commitTransaction();
            } else {
                holder.checkbox.setChecked(true);
                realm.beginTransaction();
                holder.name.setPaintFlags(holder.name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.description.setPaintFlags(holder.name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                realm.commitTransaction();
            }

            /** {show regular layout} and {hide swipe layout} */
            holder.regularLayout.setVisibility(View.VISIBLE);
            holder.swipeLayout.setVisibility(View.GONE);
            holder.name.setText(taskClass.getName());
            holder.description.setText(taskClass.getDescription());
            holder.location.setText(taskClass.getLocation());

            if (MainActivity.isCloudClicked == true) {
                // load image into imageview using glide
                Glide.with(context).load(taskClass.getImage())
                        .placeholder(R.drawable.image)
//                        .error(R.drawable.plus__)
                        .into(holder.imageView);
                holder.dueDateText.setVisibility(View.INVISIBLE);
                holder.completedDateText.setVisibility(View.INVISIBLE);

            }

            String dd = String.valueOf(taskClass.getDate());
            String result = null;
            try {
                SimpleDateFormat parseFormat =
                        new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
                Date date = parseFormat.parse(String.valueOf(dd));
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                result = format.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String cd = String.valueOf(taskClass.getIsCompleted());
            String result2 = null;
            try {
                SimpleDateFormat parseFormat =
                        new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
                Date date = parseFormat.parse(String.valueOf(cd));
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                result2 = format.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            holder.dueDate.setText(result);

            if (result2 != null && !result2.isEmpty()) {
                holder.completedDate.setText(result2);
            } else {
                holder.completedDate.setText("");
            }

            Log.v("name....onbind", position + "");

            if (MainActivity.isCloudClicked == true) {
                holder.checkbox.setVisibility(View.INVISIBLE);
            }else{
                holder.checkbox.setVisibility(View.VISIBLE);

                holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int id = buttonView.getId();
                    if (id == R.id.check_box) {
                        if (holder.checkbox.isChecked()) {
                            realm.beginTransaction();
                            task.get(position).setIsCompleted(getCompletedDate());
                            holder.name.setPaintFlags(holder.name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            holder.description.setPaintFlags(holder.name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            String cd = String.valueOf(taskClass.getIsCompleted());
                            String result2 = null;
                            try {
                                SimpleDateFormat parseFormat =
                                        new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
                                Date date = parseFormat.parse(String.valueOf(cd));
                                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                                result2 = format.format(date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            holder.completedDate.setText(result2);
                            realm.commitTransaction();
                        } else {
                            realm.beginTransaction();
                            task.get(position).setIsCompleted(null);
                            holder.name.setPaintFlags(holder.name.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                            holder.description.setPaintFlags(holder.name.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                            holder.completedDate.setText("");
                            realm.commitTransaction();
                        }
                    }
                }
            });
        }
            holder.linearlayout.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View view) {
                    InputMethodManager imm = (InputMethodManager) view.getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                    if(MainActivity.isCloudClicked==true){
                    }
                    else{
                    swiped = false;

                    Intent intent = new Intent().setClass(context, TaskDetailActivity.class);
                    // Launch the new activity and add the additional flags to the intent
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("name.", task.get(position).getName());
                    intent.putExtra("description.", taskClass.getDescription());
                    intent.putExtra("location.", taskClass.getLocation());
                    intent.putExtra("date.", taskClass.getDate());
//                    Log.v("dateeeee", "" + taskClass.getDate());
                    intent.putExtra("time.", taskClass.getTime());
                    intent.putExtra("status.", task.get(position).getIsCompleted());
                    intent.putExtra("position.", position);
                    intent.putExtra("id.", task.get(position).getId());
                    context.startActivity(intent);
                    }
                }
            });
        }
    }

    private Date getCompletedDate() {
        Date date = new Date();
        return date;
    }


    public void removeItem(int position) {
        task.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, task.size());
    }

    private void undoOpt(TaskClass taskClass) {
        Runnable pendingRemovalRunnable = pendingRunnables.get(taskClass);
        pendingRunnables.remove(taskClass);
        if (pendingRemovalRunnable != null)
            handler.removeCallbacks(pendingRemovalRunnable);
        itemsPendingRemoval.remove(taskClass);
        // this will rebind the row in "normal" state
        notifyItemChanged(task.indexOf(taskClass));
    }


    @Override
    public long getItemId(int position) {
        Log.d("pos", "pos*** " + position);
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        Log.v("aaaaa","count11");

        Log.d("listsize adapter", "listsize*** " + task.size());
        if (task.size() == 0) {
            MainActivity.showText();
        }
        return task.size();
    }

    public void pendingRemoval(int position) {

        final TaskClass taskClass = task.get(position);
        if (!itemsPendingRemoval.contains(taskClass)) {
            itemsPendingRemoval.add(taskClass);
            // this will redraw row in "undo" state
            notifyItemChanged(position);
            // let's create, store and post a runnable to remove the data
            Runnable pendingRemovalRunnable = new Runnable() {
                @Override
                public void run() {
                    remove(task.indexOf(taskClass));
                }
            };
            pendingRunnables.put(taskClass, pendingRemovalRunnable);
        }
    }

    public void remove(int position) {
        TaskClass taskClass = task.get(position);
        if (itemsPendingRemoval.contains(taskClass)) {
            itemsPendingRemoval.remove(taskClass);
        }
        if (task.contains(taskClass)) {
            task.remove(position);
            notifyItemRemoved(position);
        }
    }

    public boolean isPendingRemoval(int position) {
        TaskClass taskClass = task.get(position);
        return itemsPendingRemoval.contains(taskClass);
    }

    public void deleteTask(String id, int position) {

        TaskClass taskClass1 = task.get(position);
        RealmResults<TaskClass> results = realm.where(TaskClass.class).equalTo("id", id).findAll();
        realm.beginTransaction();

        if (itemsPendingRemoval.contains(taskClass1)) {
            itemsPendingRemoval.remove(taskClass1);
        }
        if (task.contains(taskClass1)) {
            task.remove(position);
            notifyItemRemoved(position);
        }
        results.deleteAllFromRealm();
        realm.commitTransaction();
    }
}
