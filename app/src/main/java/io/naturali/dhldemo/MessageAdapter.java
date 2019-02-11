package io.naturali.dhldemo;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import io.naturali.common.data.bean.MessageResult;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    private List<MessageResult> mDatas;

    public MessageAdapter(List<MessageResult> datas) {
        mDatas = datas;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new MessageViewHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, null, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        try {
            holder.bind(mDatas.get(position).getContent().getMessage());
        } catch (Exception ignore) {
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    public void replace(List<MessageResult> msgResults) {
            mDatas.clear();
            mDatas.addAll(msgResults);
            notifyDataSetChanged();
    }
}
