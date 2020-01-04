package lol.kek.lab4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NewsProvider extends ArrayAdapter<RssItem> {
    private LayoutInflater inflater;
    private int layout;
    private ArrayList<RssItem> productList;
    private Boolean isGrid = false;

    NewsProvider(Context context, int resource, ArrayList<RssItem> products, Boolean isGrid) {
        this(context, resource, products);
        this.isGrid = isGrid;
    }

    NewsProvider(Context context, int resource, ArrayList<RssItem> products) {
        super(context, resource, products);
        this.productList = products;
        this.layout = resource;
        this.inflater = LayoutInflater.from(context);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;
        if(convertView==null){
            convertView = inflater.inflate(this.layout, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final RssItem rss = productList.get(position);

        viewHolder.titleView.setText(rss.getTitle());
        viewHolder.descriptionView.setText(rss.getDescription());
        viewHolder.pubdateView.setText(rss.getPubDate().toString());
        Picasso.get()
                .load(rss.getImageUrl())
                .resize(200, 200)
                .centerInside()
                .placeholder(R.drawable.newspaper)
                .into(viewHolder.docIcon);
        

        return convertView;
    }

    private String formatValue(int count, String unit){
        return String.valueOf(count) + " " + unit;
    }
    private class ViewHolder {
        final TextView titleView, descriptionView, pubdateView;
        final ImageView docIcon;
        ViewHolder(View view){
            if(isGrid){
                titleView = (TextView) view.findViewById(R.id.textViewTitle);
                descriptionView = (TextView) view.findViewById(R.id.textViewDescription);
                pubdateView = (TextView) view.findViewById(R.id.textViewPubDate);
                docIcon = (ImageView) view.findViewById(R.id.imageDocumentIcon);
            }
            else {
                titleView = (TextView) view.findViewById(R.id.textViewTitle);
                descriptionView = (TextView) view.findViewById(R.id.textViewDescription);
                pubdateView = (TextView) view.findViewById(R.id.textViewPubDate);
                docIcon = (ImageView) view.findViewById(R.id.imageDocumentIcon);
            }

        }
    }
}
