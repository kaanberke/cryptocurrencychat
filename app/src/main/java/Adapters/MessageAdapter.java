package Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.cryptocurrencychat.AllMethods;
import com.project.cryptocurrencychat.Message;
import com.project.cryptocurrencychat.R;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageAdapterViewHolder> {

    Context context;
    List<Message> messages;
    DatabaseReference reference;

    public MessageAdapter(Context context, List<Message> messages, DatabaseReference reference){
        this.context = context;
        this.reference = reference;
        this.messages = messages;


    }

    @NonNull
    @Override
    public MessageAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new MessageAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapterViewHolder holder, int position) {
        Message message = messages.get(position);

        if (message.getName() != null && message.getName().equals(AllMethods.name)){
            holder.tvTitle.setText("You: " + message.getMessage());
            holder.tvTitle.setGravity(Gravity.START);
            holder.llMessage.setBackgroundColor(Color.parseColor("#EF9E73"));

            holder.llMessage.post(new Runnable() {
                @Override
                public void run() {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.llMessage.getLayoutParams();
                    params.leftMargin = 50;
                    holder.llMessage.setLayoutParams(params);
                }
            });
        }
        else {
            holder.tvTitle.setText(message.getName() + ": " + message.getMessage());
            holder.imgBtnDelete.setVisibility(View.GONE);

            holder.llMessage.post(new Runnable() {
                @Override
                public void run() {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.llMessage.getLayoutParams();
                    params.rightMargin = 50;
                    holder.llMessage.setLayoutParams(params);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class MessageAdapterViewHolder extends RecyclerView.ViewHolder{

        TextView tvTitle;
        ImageButton imgBtnDelete;
        LinearLayout llMessage;

        public MessageAdapterViewHolder(View itemView){
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            imgBtnDelete = itemView.findViewById(R.id.imgBtnDelete);
            llMessage = itemView.findViewById(R.id.llMessage);

            imgBtnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reference.child(messages.get(getAdapterPosition()).getKey()).removeValue();
                }
            });
        }
    }

}
