package io.naturali.dhldemo;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MessageViewHolder extends RecyclerView.ViewHolder {

    public MessageViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void bind(String string) {
        TextView textView = itemView.findViewById(R.id.item_text);
        textView.setText(string);
    }
}
