package lol.kek.lab4;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    String feedUrl = "https://news.tut.by/rss/index.rss";
    ArrayList<RssItem> rssItems = new ArrayList<RssItem>();
    ArrayAdapter<RssItem> adapter = null;

    ListView rssListView = null;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = findViewById(R.id.pullToRefresh);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshNow();
            }
        });

        //requestNewFeedURL();
        rssListView = (ListView) findViewById(R.id.list);
        adapter = new NewsProvider(this, R.layout.listview_item, rssItems);
        rssListView.setAdapter(adapter);
        rssListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                // по позиции получаем выбранный элемент
                RssItem selectedItem = rssItems.get(position);
                //Toast.makeText(getApplicationContext(),selectedItem.getTitle(),Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                intent.putExtra("url", selectedItem.getLink());
                startActivity(intent);
            }
        });
        refreshNow();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.buttonChangeURL :
                requestNewFeedURL();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestNewFeedURL () {
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_feed, null);

        final EditText editText = (EditText) dialogView.findViewById(R.id.edt_comment);
        editText.setText(feedUrl);
        Button button1 = (Button) dialogView.findViewById(R.id.buttonSubmit);
        Button button2 = (Button) dialogView.findViewById(R.id.buttonCancel);

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                feedUrl = editText.getText().toString();
                Toast.makeText(getApplicationContext(), feedUrl, Toast.LENGTH_SHORT).show();
                refreshNow();
                dialogBuilder.dismiss();
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogBuilder.dismiss();
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    private void refreshNow() {
        new TestConnectionNew().execute(feedUrl);
    }




    public class TestConnectionNew extends AsyncTask<String, Void, Void> {
        ArrayList<RssItem> rssItemsNew = new ArrayList<RssItem>();
        @Override
        protected Void doInBackground(String... feedUrl) {

            // TODO Auto-generated method stub
            try {
                //open an URL connection make GET to the server and
                //take xml RSS data
                URL url = new URL(feedUrl[0]);
                HttpsURLConnection.setFollowRedirects(false);
                HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
                conn.setRequestMethod( "GET" );

                conn.connect();
                int code = conn.getResponseCode();
                if (code == HttpsURLConnection.HTTP_OK) {
                    InputStream stream = conn.getInputStream();

                    rssItemsNew = RssItem.getRssItems(stream);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            rssItems.clear();
            rssItems.addAll(rssItemsNew);

            adapter.notifyDataSetChanged();
            rssListView.invalidateViews();
            rssListView.refreshDrawableState();

            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
