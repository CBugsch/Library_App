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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void viewBooks(View view){
        Intent intent = new Intent(this, Books.class);
        startActivity(intent);
    }

    public void viewMembers(View view){
        Intent intent = new Intent(this, Members.class);
        startActivity(intent);
    }
}
