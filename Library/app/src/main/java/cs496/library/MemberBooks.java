/*
Christopher Bugsch
CS 496
Final Project
6/10/18
The following app was created to demonstrate my ability to connect a mobile app
to my own API and NDB. Code to make requests to my API, creating clickable listviews
and creating custom dialog boxes has been adapted from various sources.
For a full list of the sources used to create this app please view the attached API documentation.
 */

package cs496.library;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MemberBooks extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_books);
        Intent intent = getIntent();
        String mSelf = intent.getStringExtra(Members.EXTRA_MESSAGE);
        String mAction = intent.getStringExtra(Members.Action);

        //figure out what function to call based on the action message
        if (mAction.equals("view")) {
            viewBooks(mSelf);
        } else if (mAction.equals("checkout")) {
            checkOut(mSelf);
        } else {
            returnBook(mSelf);
        }
    }

    // view books that the specific member current has
    public void viewBooks(String mSelf) {
        OkHttpClient mOkHttpClient = new OkHttpClient();
        HttpUrl reqUrl = HttpUrl.parse("https://libraryfinalproject.appspot.com" + mSelf + "/books");
        Request request = new Request.Builder()
                .url(reqUrl)
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String r = response.body().string();
                List<Map<String, String>> books = new ArrayList<Map<String, String>>();
                try {
                    JSONArray items = new JSONArray(r);
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject j = items.getJSONObject(i);
                        HashMap<String, String> m = new HashMap<String, String>();
                        m.put("title", j.getString("title"));
                        m.put("author_first_name", j.getString("author_first_name"));
                        m.put("author_last_name", j.getString("author_last_name"));
                        m.put("page_count", String.valueOf(j.getInt("page_count")));
                        m.put("self", j.getString("self"));
                        m.put("checked_out", String.valueOf(j.getBoolean("checked_out")));
                        books.add(m);
                    }
                    final SimpleAdapter bookAdapter = new SimpleAdapter(
                            MemberBooks.this,
                            books,
                            R.layout.books_layout,
                            new String[]{"title", "author_first_name", "author_last_name", "page_count", "self", "checked_out"},
                            new int[]{R.id.bookTitle, R.id.authorFirstName, R.id.authorLastName, R.id.pageCount, R.id.bookSelf, R.id.checkedOut}
                    );
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ListView) findViewById(R.id.booksList)).setAdapter(bookAdapter);
                        }
                    });


                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    //get a list of books available to checkout and display the list
    public void checkoutBook(String mSelf) {
        OkHttpClient mOkHttpClient = new OkHttpClient();
        HttpUrl reqUrl = HttpUrl.parse("https://libraryfinalproject.appspot.com/books/available");
        Request request = new Request.Builder()
                .url(reqUrl)
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String r = response.body().string();
                List<Map<String, String>> books = new ArrayList<Map<String, String>>();
                try {
                    JSONArray items = new JSONArray(r);
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject j = items.getJSONObject(i);
                        HashMap<String, String> m = new HashMap<String, String>();
                        m.put("title", j.getString("title"));
                        m.put("author_first_name", j.getString("author_first_name"));
                        m.put("author_last_name", j.getString("author_last_name"));
                        m.put("page_count", String.valueOf(j.getInt("page_count")));
                        m.put("self", j.getString("self"));
                        m.put("checked_out", String.valueOf(j.getBoolean("checked_out")));
                        books.add(m);
                    }
                    final SimpleAdapter bookAdapter = new SimpleAdapter(
                            MemberBooks.this,
                            books,
                            R.layout.books_layout,
                            new String[]{"title", "author_first_name", "author_last_name", "page_count", "self", "checked_out"},
                            new int[]{R.id.bookTitle, R.id.authorFirstName, R.id.authorLastName, R.id.pageCount, R.id.bookSelf, R.id.checkedOut}
                    );
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ListView) findViewById(R.id.booksList)).setAdapter(bookAdapter);
                        }
                    });


                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    //allow the user to choose an available book to checkout
    public void checkOut(final String mSelf){
        checkoutBook(mSelf);    //get list of available books

        //allow the user to click on a book
        final ListView listView = (ListView) findViewById(R.id.booksList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // create a dialog box to confirm or cancel checking out that book
                final AlertDialog OptionDialog = new AlertDialog.Builder(MemberBooks.this).create();
                View mView = getLayoutInflater().inflate(R.layout.dialog_member_books, null);
                OptionDialog.setView(mView);

                //local vars used for the function
                Button mCheckout = (Button) mView.findViewById(R.id.confirmReturn);
                mCheckout.setText("Check Out");
                Button mCancel = (Button) mView.findViewById(R.id.cancelBttn);
                final TextView title = (TextView) view.findViewById(R.id.bookTitle);
                final TextView bSelf = (TextView) view.findViewById(R.id.bookSelf);

                final TextView message = (TextView)mView.findViewById(R.id.messageTV);
                message.setText("Are you sure you want to check out " + title.getText().toString() + "?");

                //handle checkout button
                mCheckout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v1) {
                        String mID = mSelf;
                        processBook(bSelf.getText().toString(), mID);   //call function to handle checking out a book
                        Toast.makeText(MemberBooks.this, (title.getText() + " has been checked out"), Toast.LENGTH_SHORT).show();
                        OptionDialog.dismiss();
                    }
                });

                //handle cancel button
                mCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v1) {
                        OptionDialog.dismiss();
                    }
                });

                OptionDialog.show();
            }
        });

    }

    //handles the patch request needed to checkout the book. should get the book self url and member id passed in
    public void processBook(String bSelf, final String mID){
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient mOkHttpClient = new OkHttpClient();
        HttpUrl reqUrl = HttpUrl.parse("https://libraryfinalproject.appspot.com" + bSelf + "/checkout");
        String json = "{\"id\":\"" + mID + "\"}";
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(reqUrl)
                .patch(body)
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                checkoutBook(mID);  //shows updated list of available books
            }
        });
    }

    //handles the patch request needed to return a book
    public void returned(String bSelf, String mSelf){
        final String self = mSelf;
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient mOkHttpClient = new OkHttpClient();
        HttpUrl reqUrl = HttpUrl.parse("https://libraryfinalproject.appspot.com" + bSelf + "/return");
        String json = "{}";
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(reqUrl)
                .patch(body)
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                viewBooks(self);    //shows updated list of books the member has
            }
        });
    }

    //show a dialog box to confirm returning a book
    public void returnBook(final String mSelf) {
        viewBooks(mSelf);   //get list of members book currently checked out

        //allow the user to click on the rows of books
        final ListView listView = (ListView) findViewById(R.id.booksList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //create dialog box to confirm returning book
                final AlertDialog OptionDialog = new AlertDialog.Builder(MemberBooks.this).create();
                View mView = getLayoutInflater().inflate(R.layout.dialog_member_books, null);
                OptionDialog.setView(mView);

                //local vars used in function
                Button mReturn = (Button) mView.findViewById(R.id.confirmReturn);
                Button mCancel = (Button) mView.findViewById(R.id.cancelBttn);
                final TextView title = (TextView) view.findViewById(R.id.bookTitle);
                final TextView bSelf = (TextView) view.findViewById(R.id.bookSelf);

                //handle the return button
                mReturn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v1) {
                        //call function to return the book
                        returned(bSelf.getText().toString(), mSelf);

                        Toast.makeText(MemberBooks.this, (title.getText() + " has been returned"), Toast.LENGTH_SHORT).show();
                        OptionDialog.dismiss();
                    }
                });

                //handle the cancel button
                mCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v1) {
                        OptionDialog.dismiss();
                    }
                });

                OptionDialog.show();
            }
        });

    }
}
