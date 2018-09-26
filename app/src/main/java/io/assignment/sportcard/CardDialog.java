package io.assignment.sportcard;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;

public class CardDialog {
    public void showDialog(final Activity activity, String msg, final Context context) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_card);
        final View view = dialog.getWindow().getDecorView().getRootView();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        final ImageView imageView = dialog.findViewById(R.id.imageView_profile);
        final TextView tvName = dialog.findViewById(R.id.name);
        final TextView tvSportName = dialog.findViewById(R.id.sport_name);
        final TextView tvHeight = dialog.findViewById(R.id.height);
        final TextView tvWeight = dialog.findViewById(R.id.weight);
        final TextView tvAge = dialog.findViewById(R.id.age);
        final Button share = dialog.findViewById(R.id.share);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("Name").getValue(String.class);
                String age = dataSnapshot.child("Age").getValue(String.class);
                String height = dataSnapshot.child("Height").getValue(String.class);
                String weight = dataSnapshot.child("Weight").getValue(String.class);
                String sportName = dataSnapshot.child("SportsName").getValue(String.class);
                String image = dataSnapshot.child("profileUrl").getValue(String.class);

                if (name != null) {
                    tvName.setText(name);
                } else {
                    Log.d("name", "null");
                }
                if (age != null) {
                    tvAge.setText("Age- " + age + " years");
                } else {
                    Log.d("age", "null");
                }
                if (height != null) {
                    tvHeight.setText("Height- " + height);
                } else {
                    Log.d("height", "null");
                }
                if (weight != null) {
                    tvWeight.setText("Weight- " + weight + " Kg");
                } else {
                    Log.d("weight", "null");
                }
                if (sportName != null) {
                    tvSportName.setText(sportName);
                } else {
                    Log.d("sportName", "null");
                }

                Picasso.get().load(image).into(imageView);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share.setVisibility(View.INVISIBLE);
                takeScreenShotAndShare(context, view);
                dialog.dismiss();
            }
        });

        dialog.show();


    }

    public static void takeScreenShotAndShare(final Context context, View view) {
        try {

            File mPath = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "screenshot.png");
            //File imageDirectory = new File(mPath, "screenshot.png");

            view.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
            view.setDrawingCacheEnabled(false);

            FileOutputStream fOut = new FileOutputStream(mPath);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, fOut);
            fOut.flush();
            fOut.close();

            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
            Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", mPath);
            shareIntent.setType("image/*");

            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(shareIntent, "Share image using"));
        } catch (Throwable tr) {
            Log.d("ssss---", "Couldn't save screenshot", tr);
        }

    }
}
