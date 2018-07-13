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

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.ActionCodeResult;

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

public class Books extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);
        loadData(); //display all books

        final ListView listView = (ListView) findViewById(R.id.booksList);
        Button addBook = (Button) findViewById(R.id.addBook);

        //handle the add book button
        addBook.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //create a dialog box to get info for new book
                final AlertDialog OptionDialog = new AlertDialog.Builder(Books.this).create();
                View mView = getLayoutInflater().inflate(R.layout.dialog_new_book, null);
                OptionDialog.setView(mView);

                //local vars used in function
                final EditText mBookTitle = (EditText)mView.findViewById(R.id.newBookTitle);
                final EditText mAuthorFirst = (EditText)mView.findViewById(R.id.newAuthorFirst);
                final EditText mAuthorLast = (EditText)mView.findViewById(R.id.newAuthorLast);
                final EditText mPageCount = (EditText)mView.findViewById(R.id.newPageCount);
                Button mSubmit = (Button) mView.findViewById(R.id.addBookSubmit);
                Button mCancel = (Button) mView.findViewById(R.id.cancelBttn);

                //handle the submit button
                mSubmit.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v1){
                        //make sure all fields are filled in
                        if(mBookTitle.getText().toString().isEmpty() || mAuthorFirst.getText().toString().isEmpty() ||
                                mAuthorLast.getText().toString().isEmpty() || mPageCount.getText().toString().isEmpty()){
                            Toast.makeText(Books.this, "Please fill in all fields", Toast.LENGTH_LONG).show();
                        }
                        else{
                            //call function to add book to DB
                            addBook(mBookTitle.getText().toString(), mAuthorFirst.getText().toString(),
                                    mAuthorLast.getText().toString(), mPageCount.getText().toString());
                            OptionDialog.dismiss();
                        }
                    }
                });

                //handle cancel button
                mCancel.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v1){
                        OptionDialog.dismiss();
                    }
                });
                OptionDialog.show();
            }
        });

        //allow user to click on any row of books
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //create dialog boxe that shows options for that book
                final AlertDialog OptionDialog = new AlertDialog.Builder(Books.this).create();
                View mView = getLayoutInflater().inflate(R.layout.dialog_book, null);
                OptionDialog.setView(mView);

                //local vars used in function
                Button mEditBook = (Button) mView.findViewById(R.id.editBook);
                Button mDeleteBook = (Button) mView.findViewById(R.id.deleteBook);
                Button mCancel = (Button) mView.findViewById(R.id.cancelBttn);
                final TextView title = (TextView) view.findViewById(R.id.bookTitle);
                final TextView firstName = (TextView) view.findViewById(R.id.authorFirstName);
                final TextView lastName = (TextView) view.findViewById(R.id.authorLastName);
                final TextView pageCount = (TextView) view.findViewById(R.id.pageCount);
                final TextView bookSelf = (TextView) view.findViewById(R.id.bookSelf);

                //handle the delete button
                mDeleteBook.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v1){
                        //call function to delete the book
                        deleteBook(bookSelf.getText().toString());
                        Toast.makeText(Books.this, (title.getText() + " has been deleted"), Toast.LENGTH_SHORT).show();
                        OptionDialog.dismiss();
                    }
                });

                //handle the cancel button
                mCancel.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v1){
                        OptionDialog.dismiss();
                    }
                });

                //handle the edit button
                mEditBook.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v1){
                        //create a new dialog box to edit book info
                        final AlertDialog OptionDialog1 = new AlertDialog.Builder(Books.this).create();
                        View mView = getLayoutInflater().inflate(R.layout.dialog_new_book, null);
                        OptionDialog1.setView(mView);

                        //populate that dialog box with the book's current info
                        final EditText mBookTitle = (EditText)mView.findViewById(R.id.newBookTitle);
                        mBookTitle.setText(title.getText());

                        final EditText mAuthorFirst = (EditText)mView.findViewById(R.id.newAuthorFirst);
                        mAuthorFirst.setText(firstName.getText());

                        final EditText mAuthorLast = (EditText)mView.findViewById(R.id.newAuthorLast);
                        mAuthorLast.setText(lastName.getText());

                        final EditText mPageCount = (EditText)mView.findViewById(R.id.newPageCount);
                        mPageCount.setText(pageCount.getText());

                        Button mSubmit = (Button) mView.findViewById(R.id.addBookSubmit);
                        Button mCancel = (Button) mView.findViewById(R.id.cancelBttn);

                        //handle submit button
                        mSubmit.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View v1){
                                //confirm all fields are filled in
                                if(mBookTitle.getText().toString().isEmpty() || mAuthorFirst.getText().toString().isEmpty() ||
                                        mAuthorLast.getText().toString().isEmpty() || mPageCount.getText().toString().isEmpty()){
                                    Toast.makeText(Books.this, "Please fill in all fields", Toast.LENGTH_LONG).show();
                                }
                                else{
                                    //call function to submit changes to DB
                                    editBook(mBookTitle.getText().toString(), mAuthorFirst.getText().toString(),
                                            mAuthorLast.getText().toString(), mPageCount.getText().toString(), bookSelf.getText().toString());
                                    OptionDialog1.dismiss();    //dismiss the edit book dialog box
                                    OptionDialog.dismiss(); //dismiss the book's options dialog box
                                }
                            }
                        });

                        //handle the cancel button
                        mCancel.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View v1){
                                OptionDialog1.dismiss();
                                OptionDialog.dismiss();
                            }
                        });
                        OptionDialog1.show();   //shows the edit book info dialog box
                    }

                });

                OptionDialog.show();    //shows the book's options dialog box
            }
        });


    }

    //get request for all book data that populates the list view with results
    public void loadData(){
        //get request for all books
        OkHttpClient mOkHttpClient = new OkHttpClient();
        HttpUrl reqUrl = HttpUrl.parse("https://libraryfinalproject.appspot.com/books");
        Request request = new Request.Builder()
                .url(reqUrl)
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override   //populate the list view with response data
            public void onResponse(Call call, Response response) throws IOException {
                String r = response.body().string();
                List<Map<String, String>> books = new ArrayList<Map<String, String>>();
                try {
                    JSONArray items = new JSONArray(r);
                    for(int i = 0; i < items.length(); i++){
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
                            Books.this,
                            books,
                            R.layout.books_layout,
                            new String[]{"title", "author_first_name", "author_last_name", "page_count", "self", "checked_out"},
                            new int[]{R.id.bookTitle, R.id.authorFirstName, R.id.authorLastName, R.id.pageCount, R.id.bookSelf, R.id.checkedOut}
                    );
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ListView)findViewById(R.id.booksList)).setAdapter(bookAdapter);
                        }
                    });


                } catch (JSONException e1){
                    e1.printStackTrace();
                }
            }
        });
    }

    //handle the patch request to update book info
    public void editBook(String title, String fName, String lName, String pageCount, String self){
        //submit a patch request
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https://libraryfinalproject.appspot.com" + self;
        String json = "{\"title\":\"" + title + "\"," +
                "\"author_first_name\":\"" + fName + "\"," +
                "\"author_last_name\":\"" + lName + "\"," +
                "\"page_count\":" + pageCount + '}';
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .patch(body)
                .build();
        client.newCall(request).enqueue(new Callback(){

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                loadData(); //shows updated book data
            }
        });
    }

    //handles the post request for creating a new book
    public void addBook(String title, String fName, String lName, String pageCount){
        //submit  post request with new book info
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https://libraryfinalproject.appspot.com/books";
        String json = "{\"title\":\"" + title + "\"," +
                "\"author_first_name\":\"" + fName + "\"," +
                "\"author_last_name\":\"" + lName + "\"," +
                "\"page_count\":" + pageCount + '}';
            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            client.newCall(request).enqueue(new Callback(){

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                   loadData();  //show updated book list
                }
            });
    }

    //handle the delete request for a book
    public void deleteBook(String bookURL){
        OkHttpClient mOkHttpClient = new OkHttpClient();
        HttpUrl reqUrl = HttpUrl.parse("https://libraryfinalproject.appspot.com" + bookURL);
        Request request = new Request.Builder()
                .url(reqUrl)
                .delete()
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
               loadData();  //show updated table with deleted book gone
            }
        });
    }
}
