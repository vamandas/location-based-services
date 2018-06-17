package com.iskconbaroda.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.iskconbaroda.R;

public class ReportBugActivity extends AppCompatActivity {

    EditText name,email,details;
    TextView tv;
    Button report;
    String type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportbug);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        name=(EditText)findViewById(R.id.name_edittext);
        email=(EditText)findViewById(R.id.email_edittext);
        details=(EditText)findViewById(R.id.bug_details_edittext);
        report=(Button) findViewById(R.id.report_bug_button);
        tv=(TextView) findViewById(R.id.bug_textview);

        type=getIntent().getStringExtra("TYPE");
        if(type.equals("bug"))
        {
            tv.setText("Bug Details");
            details.setHint("Enter Bug Details");
            getSupportActionBar().setTitle("Report Bug");
        }
        if(type.equals("feedback"))
        {
            tv.setText("Feedback");
            details.setHint("Enter Feedback");
            getSupportActionBar().setTitle("Feedback");
        }
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String temp=email.getText().toString();

                if(name.getText().toString().equals(""))
                {
                    name.setError("Please enter name");
                }
                if (temp.equals("")) {
                    email.setError("Please enter email address");
                }
                if (details.getText().toString().equals("")) {
                    details.setError("Please enter details");
                }
                if(!TextUtils.isEmpty(temp) && !Patterns.EMAIL_ADDRESS.matcher(temp).matches())
                {
                    email.setError("Enter valid email Address");
                }
                if(!name.getText().toString().equals("")
                        &&  !temp.equals("")
                        && !details.getText().toString().equals("")
                        && !(!TextUtils.isEmpty(temp) && !Patterns.EMAIL_ADDRESS.matcher(temp).matches())
                        ){
                    makeAndSendIntent();
                }
                }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
//				Intent i = new Intent(print1_MainActivity.this,MainActivity.class);
//				finish();
//				startActivity(i);
                onBackPressed();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    void makeAndSendIntent()
    {

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto","vamandas@gmail.com", null));
        if(type.equals("bug"))
        {
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Reporting Bug!");
        }
        if(type.equals("feedback"))
        {
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
        }
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Reporting Bug!");
        emailIntent.putExtra(Intent.EXTRA_TEXT, details.getText().toString()+"\n" + "\n" +
                "Reported by:\n" + "Name: " + name.getText().toString() +"\n" +
                                    "Email: " + email.getText().toString());
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    public void onBackPressed()
    {
        super.onBackPressed();
        Intent i = new Intent(ReportBugActivity.this,MapsActivity.class);
        finish();
//        startActivity(i);
    }
}
