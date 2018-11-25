package com.golendukhin.YevaSololearn.adapters;

        import android.content.Context;
        import android.support.annotation.NonNull;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;

        import com.golendukhin.YevaSololearn.Feed;
        import com.golendukhin.YevaSololearn.R;

        import java.util.ArrayList;

public class RecyclerViewAdapter extends StaggeredRecyclerViewAdapter {

    public RecyclerViewAdapter(ArrayList<Feed> feedItems, Context context) {
        super(feedItems, context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_layout, viewGroup, false);
        return new ViewHolder(view);
    }
}