package lol.kek.lab4;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    String feedUrl;
    ArrayList<RssItem> rssItems = new ArrayList<RssItem>();
    ArrayAdapter<RssItem> adapter = null;
    private DatabaseAdapter adapterDB;
    SharedPreferences sharedPref;
    boolean isConnected, isFirst=true;

    ListView rssListView;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView titleTextView;
    MenuItem mNetworkStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = findViewById(R.id.pullToRefresh);
        rssListView = (ListView) findViewById(R.id.list);
        titleTextView = (TextView) findViewById(R.id.titleTextView);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshNow();
            }
        });

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String defaultValue = getResources().getString(R.string.saved_high_score_default_key);
        feedUrl = sharedPref.getString(getString(R.string.saved_high_score_key), defaultValue);

        adapterDB = new DatabaseAdapter(this);
        //requestNewFeedURL();
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
        swipeRefreshLayout.setRefreshing(true);
    }


        private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = manager.getActiveNetworkInfo();
                isConnected = ni != null && ni.isConnected();

                if (isConnected) {
                    mNetworkStatus.setIcon(R.drawable.ic_rss_black_24dp);
                } else {
                    mNetworkStatus.setIcon(R.drawable.ic_rss_grey600_24dp);
                }
                if (isFirst) {
                    isFirst = false;
                    refreshNow();
                }
            }
        };


        @Override
        public boolean onCreateOptionsMenu (Menu menu){
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            mNetworkStatus = (MenuItem) menu.findItem(R.id.buttonChangeURL);
            registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            return true;
        }


        @Override
        public boolean onOptionsItemSelected (MenuItem item){
            int id = item.getItemId();
            switch (id) {
                case R.id.buttonChangeURL:
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
                    String localFeedUrl = editText.getText().toString();
                    if (URLUtil.isValidUrl(localFeedUrl)) {
                        feedUrl = localFeedUrl;
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.saved_high_score_key), feedUrl);
                        editor.commit();
                        Toast.makeText(getApplicationContext(), feedUrl, Toast.LENGTH_SHORT).show();
                        refreshNow();
                        dialogBuilder.dismiss();
                    } else if (localFeedUrl.equals("")) {
                        Toast.makeText(getApplicationContext(), "Invalid url. Try again", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Invalid url. Try again", Toast.LENGTH_SHORT).show();
                    }

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


        private void refreshNow () {
            new GetRssItems().execute(feedUrl);
        }


        public class GetRssItems extends AsyncTask<String, Void, Void> {
            ArrayList<RssItem> rssItemsNew = new ArrayList<RssItem>();
            Pair<String, ArrayList<RssItem>> pair;

            @Override
            protected void onPreExecute() {
                if (isConnected) {
                    //Toast.makeText(getApplicationContext(),"Connecting",Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(getApplicationContext(),"Disconnecting",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            protected Void doInBackground(String... feedUrl) {

                try {
                    if (isConnected) {
                        //open an URL connection make GET to the server and
                        //take xml RSS data
                        URL url = new URL(feedUrl[0]);
                        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.connect();
                        int code = conn.getResponseCode();
                        if (code == HttpsURLConnection.HTTP_OK) {
                            InputStream stream = conn.getInputStream();
                            pair = RssItem.getRssItems(stream);
                            rssItemsNew = pair.second;
                            return null;
                        }
                    } else {
                        adapterDB.open();
                        rssItemsNew = adapterDB.getRsses();
                        adapterDB.close();
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
                if (isConnected) {
                    new updateDB().execute();
                    titleTextView.setText(pair.first);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(getString(R.string.title_rss_channel), pair.first);
                    editor.commit();
                } else {
                    String OldTitleValue = sharedPref.getString(getString(R.string.title_rss_channel), getString(R.string.title_rss_channel));
                    titleTextView.setText(OldTitleValue);
                }

                adapter.notifyDataSetChanged();
                rssListView.invalidateViews();
                rssListView.refreshDrawableState();

                swipeRefreshLayout.setRefreshing(false);
            }
        }


        public class updateDB extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... aVoid) {
                try {
                    adapterDB.open();
                    adapterDB.refreshDB(rssItems);
                    adapterDB.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
    }
