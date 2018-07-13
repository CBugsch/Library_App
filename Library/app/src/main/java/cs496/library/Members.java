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

import static android.support.v7.media.MediaControlIntent.EXTRA_MESSAGE;

public class Members extends AppCompatActivity {
    //vars to send to new activity
    public static final String EXTRA_MESSAGE = "mSelf";
    public static final String Action = "action";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);

        loadData(); //this will load all members data to screen

        final ListView listView = (ListView) findViewById(R.id.membersList);    //list view of members
        Button addMember = (Button) findViewById(R.id.addMember);

        //code to show fields to create a new member
        addMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //create a new dialog view
                final AlertDialog OptionDialog = new AlertDialog.Builder(Members.this).create();
                View mView = getLayoutInflater().inflate(R.layout.dialog_member_details, null);
                OptionDialog.setView(mView);

                //create local vars to use in functions
                final EditText mFirst = (EditText) mView.findViewById(R.id.newFirstName);
                final EditText mLast = (EditText) mView.findViewById(R.id.newLastName);
                final EditText mPNum = (EditText) mView.findViewById(R.id.newPhoneNumber);
                Button mSubmit = (Button) mView.findViewById(R.id.addMemberSubmit);
                Button mCancel = (Button) mView.findViewById(R.id.cancelBttn);

                //handle submit button
                mSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v1) {
                        //make sure all fields are filled in
                        if (mFirst.getText().toString().isEmpty() ||
                                mLast.getText().toString().isEmpty() || mPNum.getText().toString().isEmpty()) {
                            Toast.makeText(Members.this, "Please fill in all fields", Toast.LENGTH_LONG).show();
                        } else {
                            addMember(mFirst.getText().toString(), mLast.getText().toString(),
                                    mPNum.getText().toString());
                            OptionDialog.dismiss();
                        }
                    }
                });

                //handle cancel button
                mCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v1) {
                        OptionDialog.dismiss();
                    }
                });
                OptionDialog.show();    //show the dialog box
            }
        });

        //all user to click on a row of member data to see options
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // create a new dialog box with the options
                final AlertDialog OptionDialog = new AlertDialog.Builder(Members.this).create();
                View mView = getLayoutInflater().inflate(R.layout.dialog_member, null);
                OptionDialog.setView(mView);

                //create local vars for functions
                Button mViewBooks = (Button) mView.findViewById(R.id.viewBooks);
                Button mCheckOutBook = (Button) mView.findViewById(R.id.checkOutBook);
                Button mReturnBook = (Button) mView.findViewById(R.id.returnBook);
                Button mEditMember = (Button) mView.findViewById(R.id.editMember);
                Button mDeleteMember = (Button) mView.findViewById(R.id.deleteMember);
                Button mCancel = (Button) mView.findViewById(R.id.cancelBttn);
                final TextView fName = (TextView) view.findViewById(R.id.firstName);
                final TextView lName = (TextView) view.findViewById(R.id.lastName);
                final TextView pNum = (TextView) view.findViewById(R.id.phoneNumber);
                final TextView mID = (TextView) view.findViewById(R.id.memberID);
                final TextView mSelf = (TextView) view.findViewById(R.id.memberSelf);

                //handle delete button
                mDeleteMember.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v1) {
                        deleteMember(mSelf.getText().toString());
                        Toast.makeText(Members.this, (fName.getText() + " " + lName.getText() + " has been deleted"), Toast.LENGTH_SHORT).show();
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

                // start a new activity to view books checked out by that member
                mViewBooks.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v1) {
                        Intent intent = new Intent(Members.this, MemberBooks.class);
                        String message = mSelf.getText().toString();
                        intent.putExtra(EXTRA_MESSAGE, message);
                        String action = "view";
                        intent.putExtra(Action, action);
                        startActivity(intent);
                    }
                });

                //start a new activity that shows a list of books available to be checked out
                mCheckOutBook.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v1) {
                        Intent intent = new Intent(Members.this, MemberBooks.class);
                        String message = mID.getText().toString();
                        intent.putExtra(EXTRA_MESSAGE, message);
                        String action = "checkout";
                        intent.putExtra(Action, action);
                        startActivity(intent);
                    }
                });

                // start a new activity that shows current books member has so they can return them
                mReturnBook.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v1) {
                        Intent intent = new Intent(Members.this, MemberBooks.class);
                        String message = mSelf.getText().toString();
                        intent.putExtra(EXTRA_MESSAGE, message);
                        String action = "return";
                        intent.putExtra(Action, action);
                        startActivity(intent);
                    }
                });

                // handle editing member info
                mEditMember.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v1) {
                        //create a dialog box to edit the member info
                        final AlertDialog OptionDialog1 = new AlertDialog.Builder(Members.this).create();
                        View mView = getLayoutInflater().inflate(R.layout.dialog_member_details, null);
                        OptionDialog1.setView(mView);

                        // get info from member and populate the dialog box
                        final EditText mFirstName = (EditText) mView.findViewById(R.id.newFirstName);
                        mFirstName.setText(fName.getText());

                        final EditText mLastName = (EditText) mView.findViewById(R.id.newLastName);
                        mLastName.setText(lName.getText());

                        final EditText mPhone = (EditText) mView.findViewById(R.id.newPhoneNumber);
                        mPhone.setText(pNum.getText());

                        Button mSubmit = (Button) mView.findViewById(R.id.addMemberSubmit);
                        Button mCancel = (Button) mView.findViewById(R.id.cancelBttn);

                        // handle submitting the changes
                        mSubmit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v1) {
                                //make sure no fields were left blank
                                if (mFirstName.getText().toString().isEmpty() || mLastName.getText().toString().isEmpty() ||
                                        mPhone.getText().toString().isEmpty()) {
                                    Toast.makeText(Members.this, "Please fill in all fields", Toast.LENGTH_LONG).show();
                                } else {
                                    editMember(mFirstName.getText().toString(), mLastName.getText().toString(),
                                            mPhone.getText().toString(), mSelf.getText().toString());
                                    OptionDialog1.dismiss();
                                    OptionDialog.dismiss();
                                }
                            }
                        });

                        // handle canceling edit
                        mCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v1) {
                                OptionDialog1.dismiss();
                                OptionDialog.dismiss();
                            }
                        });
                        OptionDialog1.show();
                    }

                });

                OptionDialog.show();
            }
        });

    }

    // function gets all members data and fills list view with info
    public void loadData() {
        //make get call for all members
        OkHttpClient mOkHttpClient = new OkHttpClient();
        HttpUrl reqUrl = HttpUrl.parse("https://libraryfinalproject.appspot.com/members");
        Request request = new Request.Builder()
                .url(reqUrl)
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override   // fill list view with results
            public void onResponse(Call call, Response response) throws IOException {
                String r = response.body().string();
                List<Map<String, String>> members = new ArrayList<Map<String, String>>();
                try {
                    JSONArray items = new JSONArray(r);
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject j = items.getJSONObject(i);
                        HashMap<String, String> m = new HashMap<String, String>();
                        m.put("first_name", j.getString("first_name"));
                        m.put("last_name", j.getString("last_name"));
                        m.put("phone_number", j.getString("phone_number"));
                        m.put("self", j.getString("self"));
                        m.put("id", j.getString("id"));

                        members.add(m);
                    }
                    final SimpleAdapter memberAdapter = new SimpleAdapter(
                            Members.this,
                            members,
                            R.layout.members_layout,
                            new String[]{"first_name", "last_name", "phone_number", "self", "id"},
                            new int[]{R.id.firstName, R.id.lastName, R.id.phoneNumber, R.id.memberSelf, R.id.memberID}
                    );
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ListView) findViewById(R.id.membersList)).setAdapter(memberAdapter);
                        }
                    });


                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    // make a delete call on member passed in
    public void deleteMember(String selfURL) {
        OkHttpClient mOkHttpClient = new OkHttpClient();
        HttpUrl reqUrl = HttpUrl.parse("https://libraryfinalproject.appspot.com" + selfURL);
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
                loadData(); //reload data to show member has been removed
            }
        });
    }

    // submit edit changes based on info passed in
    public void editMember(String fName, String lName, String pNum, String selfURL) {
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https://libraryfinalproject.appspot.com" + selfURL;
        String json = "{\"first_name\":\"" + fName + "\"," +
                "\"last_name\":\"" + lName + "\"," +
                "\"phone_number\":\"" + pNum + "\"}";
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .patch(body)
                .build();
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                loadData(); //reload data to shows changes
            }
        });
    }

    // create a new member in the DB based on info passed in
    public void addMember(String fName, String lName, String pNum) {
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https://libraryfinalproject.appspot.com/members";
        String json = "{\"first_name\":\"" + fName + "\"," +
                "\"last_name\":\"" + lName + "\"," +
                "\"phone_number\":\"" + pNum + "\"}";
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                loadData(); //reload the data to show new member
            }
        });
    }
}
