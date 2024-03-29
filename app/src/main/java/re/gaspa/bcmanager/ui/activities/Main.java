package re.gaspa.bcmanager.ui.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;

import de.hdodenhof.circleimageview.CircleImageView;
import re.gaspa.bcmanager.R;
import re.gaspa.bcmanager.databinding.ActivityMainBinding;
import re.gaspa.bcmanager.ui.fragments.Credits;
import re.gaspa.bcmanager.ui.fragments.EditProfile;
import re.gaspa.bcmanager.ui.fragments.Home;
import re.gaspa.bcmanager.ui.fragments.Map;
import re.gaspa.bcmanager.ui.fragments.Settings;
import re.gaspa.bcmanager.ui.models.BusinessCard;
import re.gaspa.bcmanager.utils.Database;
import re.gaspa.bcmanager.utils.Preferences;

public class Main extends AppCompatActivity
        implements NfcAdapter.CreateNdefMessageCallback, NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, NfcAdapter.OnNdefPushCompleteCallback {

    private FragmentManager fragmentManager;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentManager = getSupportFragmentManager();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setSupportActionBar(binding.toolbar);

        Preferences.getPreferences(binding.getRoot().getContext());

        DrawerLayout drawer = binding.drawerLayout;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.setDrawerListener(toggle);
        toggle.syncState();

        this.loadFragment(Home.class);

        binding.navView.getMenu().getItem(0).setChecked(true);
        binding.navView.setNavigationItemSelectedListener(this);

        updateProfile();

        binding.navView.getHeaderView(0).setOnClickListener(this);

        PackageManager pm = this.getPackageManager();

        if (pm.hasSystemFeature(PackageManager.FEATURE_NFC)) {
            NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
            if (mNfcAdapter != null) {
                mNfcAdapter.setNdefPushMessageCallback(this, this);
                mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.preferite) {
            item.setChecked(!item.isChecked());
            try {
                Home fragment = (Home) fragmentManager.findFragmentById(R.id.flContent);
                fragment.setPreferite(item.isChecked());
            } catch (Exception ignored) {

            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_share) {
            item.setChecked(false);
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.condividi_messaggio));
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, getString(R.string.condividi)));
        } else if (id == R.id.nav_help) {
            item.setChecked(false);
            Intent intent = new Intent(this.getApplicationContext(), Help.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.getApplicationContext().startActivity(intent);
        } else {
            Class fragmentClass = Home.class;

            if (id == R.id.nav_home) {
                fragmentClass = Home.class;
            } else if (id == R.id.nav_map) {
                fragmentClass = Map.class;
            } else if (id == R.id.nav_setting) {
                fragmentClass = Settings.class;
            } else if (id == R.id.nav_credits) {
                fragmentClass = Credits.class;
            } else if (id == R.id.nav_profile) {
                fragmentClass = EditProfile.class;
            }

            this.loadFragment(fragmentClass);

            item.setChecked(true);

        }

        binding.drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    private void loadFragment(Class fragmentClass) {
        Fragment fragment;

        try {
            fragment = (Fragment) fragmentClass.newInstance();
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
        } catch (Exception ignored) {
        }

    }

    @Override
    public void onBackPressed() {

        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else if (binding.navView.getMenu().getItem(0).isChecked()) {
            finish();
        } else {
            this.loadFragment(Home.class);
            binding.navView.getMenu().getItem(0).setChecked(true);
        }

    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(view.getContext(), BusinessCardActivity.class);
        intent.putExtra("businesscard", Preferences.getPersonalBusinessCard(null));
        view.getContext().startActivity(intent);
    }

    public void updateProfile() {
        BusinessCard personal = Preferences.getPersonalBusinessCard(null);
        if (personal != null) {
            Bitmap profile = personal.getProfilo();
            Bitmap sfondo = personal.getSfondo();
            String nome = personal.getNome();
            String role = personal.getLavoroRuolo();

            if (profile != null) {
                CircleImageView profileImage = binding.navView.getHeaderView(0).findViewById(R.id.profile_image);
                profileImage.setImageBitmap(profile);
            }
            if (sfondo != null) {
                ImageView backgroundImage = binding.navView.getHeaderView(0).findViewById(R.id.default_background);
                backgroundImage.setImageBitmap(sfondo);
            }
            if (nome != null) {
                TextView nameText = binding.navView.getHeaderView(0).findViewById(R.id.text_name);
                nameText.setText(nome);
            }
            if (role != null) {
                TextView roleText = binding.navView.getHeaderView(0).findViewById(R.id.text_role);
                roleText.setText(role);
            }
        }
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        BusinessCard personal = Preferences.getPersonalBusinessCard(null);
        String text = personal.toVCard();
        NdefMessage ret = new NdefMessage(new NdefRecord[]{createMimeRecord("application/re.gaspa.bcmanager", text.getBytes())});
        return ret;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        String vcard = new String(msg.getRecords()[0].getPayload());
        BusinessCard toAdd = BusinessCard.loadFromBuffer(vcard.split("\n"));
        Database.addBusinessCard(toAdd);
        Toast.makeText(this, R.string.nuovo_contatto, Toast.LENGTH_LONG).show();
    }

    public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
        return new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
    }

    @Override
    public void onNdefPushComplete(NfcEvent nfcEvent) {
        Toast.makeText(this.getApplicationContext(), R.string.send_complete, Toast.LENGTH_LONG).show();
    }
}
