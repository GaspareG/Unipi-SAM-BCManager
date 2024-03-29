package re.gaspa.bcmanager.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import re.gaspa.bcmanager.R;
import re.gaspa.bcmanager.ui.models.BusinessCard;
import re.gaspa.bcmanager.utils.Database;

public class OpenVcard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_vcard);

        Intent i = getIntent();
        String action = i.getAction();
        String type = i.getType();


        if (Intent.ACTION_VIEW.equals(action) && type != null) {
            Uri uri = i.getData();
            if (uri != null) {
                Toast.makeText(this, uri.toString(), Toast.LENGTH_SHORT).show();
            }
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                BusinessCard toAdd = BusinessCard.loadFromBuffered(reader);
                Database.addBusinessCard(toAdd);
                Intent intent = new Intent(getApplicationContext(), Main.class);
                getApplicationContext().startActivity(intent);

            } catch (IOException ignored) {
            }
        }

        setResult(RESULT_OK);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }


}
