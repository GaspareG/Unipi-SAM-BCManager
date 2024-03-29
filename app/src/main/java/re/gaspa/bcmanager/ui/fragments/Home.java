package re.gaspa.bcmanager.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import re.gaspa.bcmanager.R;
import re.gaspa.bcmanager.asynctask.AsyncLoadDatabase;
import re.gaspa.bcmanager.databinding.FragmentHomeBinding;
import re.gaspa.bcmanager.ui.adapters.BusinessCardAdapter;
import re.gaspa.bcmanager.listeners.OnDatabaseLoadListener;
import re.gaspa.bcmanager.ui.models.BusinessCard;
import re.gaspa.bcmanager.utils.Preferences;

public class Home extends Fragment implements OnDatabaseLoadListener, View.OnClickListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private FragmentHomeBinding mBinding;
    private BusinessCardAdapter mBcAdapter;
    private boolean preferite = false;
    private String search = "";
    private boolean mAndroidBeamAvailable = false;

    public Home() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()),
                R.layout.fragment_home, container, false);

        setHasOptionsMenu(true);

        mAndroidBeamAvailable = getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC);

        mBcAdapter = new BusinessCardAdapter(getContext());
        new AsyncLoadDatabase(null, this, false, search, preferite).execute();

        mBinding.bcList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mBinding.bcList.setAdapter(mBcAdapter);

        mBinding.fab.setOnClickListener(this);

        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("RESUME", "HOME RESUME");
        new AsyncLoadDatabase(null, this, false, search, preferite).execute();
    }

    @Override
    public void onClick(View view) {

        if (view.getId() != R.id.fab) return;

        final CharSequence[] items = {
                getString(R.string.export_vcard), getString(R.string.export_text), getString(R.string.export_image), getString(R.string.share_nfc)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle(R.string.share_profile);
        final Context context = this.getContext();
        final Activity activity = this.getActivity();
        final Home home = this;

        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0: // VCard
                        try {
                            File folder = new File(context.getCacheDir(), "shared/");
                            File outputVCard = new File(folder, "personal.vc");
                            folder.mkdir();
                            outputVCard.createNewFile();

                            outputVCard.setReadable(true, false);
                            outputVCard.setWritable(true);

                            Uri contentUri = FileProvider.getUriForFile(context, getString(R.string.fileprovider), outputVCard);
;
                            String toWrite = Preferences.getPersonalBusinessCard(null).toVCard();

                            FileWriter fw = new FileWriter(outputVCard);
                            BufferedWriter bw = new BufferedWriter(fw);
                            bw.write(toWrite);
                            bw.close();

                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                            shareIntent.setDataAndType(contentUri, context.getContentResolver().getType(contentUri));
                            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                            context.startActivity(Intent.createChooser(shareIntent, getString(R.string.choose_app)));
                        } catch (IOException ignored) {
                        }
                        break;

                    case 1: // Testo
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, Preferences.getPersonalBusinessCard(null).toTextMessage(context));
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_profile_text)));
                        break;
                    case 2: // Immagine
                        try {
                            File folder = new File(context.getCacheDir(), "shared/");
                            File outputVCard = new File(folder, "personal.png");
                            folder.mkdir();
                            outputVCard.createNewFile();

                            outputVCard.setReadable(true, false);
                            outputVCard.setWritable(true);

                            Uri contentUri = FileProvider.getUriForFile(context, getString(R.string.fileprovider), outputVCard);

                            FileOutputStream out = new FileOutputStream(outputVCard);
                            Bitmap image = Preferences.getPersonalBusinessCard(null).getImage(context);
                            image.compress(Bitmap.CompressFormat.PNG, 100, out);
                            out.flush();
                            out.close();

                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                            shareIntent.setDataAndType(contentUri, context.getContentResolver().getType(contentUri));
                            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                            context.startActivity(Intent.createChooser(shareIntent, getString(R.string.choose_app)));
                        } catch (IOException ignored) {
                        }
                        break;

                    case 3: // NFC
                        if (mAndroidBeamAvailable) {
                            Toast.makeText(context, R.string.near_phone, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(context, R.string.no_nfc, Toast.LENGTH_LONG).show();
                        }
                        break;
                }
            }


        });
        builder.create().show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);

        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        search = query;
        new AsyncLoadDatabase(null, this, false, search, preferite).execute();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        search = newText;
        new AsyncLoadDatabase(null, this, false, search, preferite).execute();
        return true;
    }

    @Override
    public boolean onClose() {
        search = "";
        new AsyncLoadDatabase(null, this, false, search, preferite).execute();
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setPreferite(boolean preferite) {
        this.preferite = preferite;
        new AsyncLoadDatabase(null, this, false, search, preferite).execute();
    }


    @Override
    public void OnDatabaseLoad(ArrayList<BusinessCard> businessCards) {
        mBcAdapter.setBusinessCardItems(businessCards);
        mBcAdapter.notifyDataSetChanged();
    }
}
