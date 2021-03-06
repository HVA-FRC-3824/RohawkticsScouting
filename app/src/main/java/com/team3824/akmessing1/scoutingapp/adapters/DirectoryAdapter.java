package com.team3824.akmessing1.scoutingapp.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.team3824.akmessing1.scoutingapp.R;
import com.team3824.akmessing1.scoutingapp.utilities.Utilities;
import com.team3824.akmessing1.scoutingapp.utilities.file_manager.FileManager;
import com.team3824.akmessing1.scoutingapp.utilities.file_manager.ThumbnailCreator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Adapter that sets up the list view in the file view with the files in the directory
 *
 * @author Andrew Messing
 */
public class DirectoryAdapter extends ArrayAdapter<String> {

    private final String TAG = "DirectoryAdapter";

    private final int KB = 1024;
    private FileManager fileManager;
    private ArrayList<String> directory;
    private Context context;
    private Activity activity;
    private NfcAdapter nfcAdapter = null;

    /**
     * @param c
     * @param objects
     * @param fm      The file mangager
     */
    public DirectoryAdapter(Context c, ArrayList<String> objects, FileManager fm) {
        super(c, R.layout.list_item_file, objects);
        fileManager = fm;
        context = c;
        directory = objects;
    }

    public void setNfcAdapter(NfcAdapter n) {
        nfcAdapter = n;
    }

    public void setActivity(Activity a) {
        activity = a;
    }

    /**
     * @param file The file to check the permissions of
     * @return String that represents the permissions
     */
    private String getFilePermissions(File file) {
        String per = "-";

        if (file.isDirectory())
            per += "d";
        if (file.canRead())
            per += "r";
        if (file.canWrite())
            per += "w";

        return per;
    }

    /**
     * Updates the list view with the new directory
     *
     * @param newDirectory List of the filenames in the new directory
     */
    public void updateDirectory(ArrayList<String> newDirectory) {
        clear();
        addAll(newDirectory);
        directory = newDirectory;
        notifyDataSetChanged();
    }

    /**
     * @param position
     * @return
     */
    public String get(int position) {
        if (position >= 0 && position < directory.size())
            return directory.get(position);
        return "";
    }


    /**
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int num_items = 0;
        String temp = fileManager.getCurrentDir();
        final File file = new File(temp + "/" + directory.get(position));
        String[] list = file.list();

        if (list != null)
            num_items = list.length;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_file, parent, false);

        }

        TextView topView = (TextView) convertView.findViewById(R.id.top_view);
        TextView bottomView = (TextView) convertView.findViewById(R.id.bottom_view);
        ImageView icon = (ImageView) convertView.findViewById(R.id.row_image);

        if (file != null && file.isFile()) {
            String ext = file.toString();
            String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);

            /*
                The icon displayed is based on the type of file
    		*/
            if (sub_ext.equalsIgnoreCase("pdf")) {
                icon.setImageResource(R.drawable.pdf);
            } else if (sub_ext.equalsIgnoreCase("mp3") || sub_ext.equalsIgnoreCase("wma") ||
                    sub_ext.equalsIgnoreCase("m4a") || sub_ext.equalsIgnoreCase("m4p")) {
                icon.setImageResource(R.drawable.music);
            } else if (sub_ext.equalsIgnoreCase("png") || sub_ext.equalsIgnoreCase("jpg") ||
                    sub_ext.equalsIgnoreCase("jpeg") || sub_ext.equalsIgnoreCase("gif") ||
                    sub_ext.equalsIgnoreCase("tiff")) {
                if (file.length() != 0) {
                    Bitmap thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(file.getAbsolutePath()), 100, 100);
                    icon.setImageBitmap(thumbnail);
                    if(activity != null) {
                        icon.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                LayoutInflater inflater = activity.getLayoutInflater();
                                View view = inflater.inflate(R.layout.dialog_preview_image, null);
                                ImageView image = (ImageView) view.findViewById(R.id.preview_image);
                                image.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                                builder.setView(view);
                                builder.show();

                            }
                        });
                    }
                } else {
                    icon.setImageResource(R.drawable.image);
                }
            } else if (sub_ext.equalsIgnoreCase("zip") || sub_ext.equalsIgnoreCase("gzip") ||
                    sub_ext.equalsIgnoreCase("gz")) {
                icon.setImageResource(R.drawable.zip);
            } else if (sub_ext.equalsIgnoreCase("m4v") || sub_ext.equalsIgnoreCase("wmv") ||
                    sub_ext.equalsIgnoreCase("3gp") || sub_ext.equalsIgnoreCase("mp4")) {
                icon.setImageResource(R.drawable.movies);
            } else if (sub_ext.equalsIgnoreCase("doc") || sub_ext.equalsIgnoreCase("docx")) {
                icon.setImageResource(R.drawable.word);
            } else if (sub_ext.equalsIgnoreCase("xls") || sub_ext.equalsIgnoreCase("xlsx")) {
                icon.setImageResource(R.drawable.excel);
            } else if (sub_ext.equalsIgnoreCase("ppt") || sub_ext.equalsIgnoreCase("pptx")) {
                icon.setImageResource(R.drawable.ppt);
            } else if (sub_ext.equalsIgnoreCase("html")) {
                icon.setImageResource(R.drawable.html32);
            } else if (sub_ext.equalsIgnoreCase("xml")) {
                icon.setImageResource(R.drawable.xml32);

            } else if (sub_ext.equalsIgnoreCase("conf")) {
                icon.setImageResource(R.drawable.config32);

            } else if (sub_ext.equalsIgnoreCase("apk")) {
                icon.setImageResource(R.drawable.appicon);

            } else if (sub_ext.equalsIgnoreCase("jar")) {
                icon.setImageResource(R.drawable.jar32);

            } else if (sub_ext.equalsIgnoreCase("txt")) {
                icon.setImageResource(R.drawable.text);
            } else {
                icon.setImageResource(R.drawable.text);
            }

        } else if (file != null && file.isDirectory()) {
            if (file.canRead() && file.list().length > 0)
                icon.setImageResource(R.drawable.folder_full);
            else
                icon.setImageResource(R.drawable.folder);
        }

        String permission = getFilePermissions(file);

        if (file.isFile()) {
            double size = file.length();
            String display_size;
            int MG = KB * KB;
            int GB = MG * KB;
            if (size > GB)
                display_size = String.format("%.2f Gb ", size / GB);
            else if (size < GB && size > MG)
                display_size = String.format("%.2f Mb ", size / MG);
            else if (size < MG && size > KB)
                display_size = String.format("%.2f Kb ", size / KB);
            else
                display_size = String.format("%.2f bytes ", (double) size);

            if (file.isHidden())
                bottomView.setText("(hidden) | " + display_size + " | " + permission);
            else
                bottomView.setText(display_size + " | " + permission);

        } else {
            if (file.isHidden())
                bottomView.setText("(hidden) | " + num_items + " items | " + permission);
            else
                bottomView.setText(num_items + " items | " + permission);
        }

        topView.setText(file.getName());

        convertView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                directory.remove(file.getName());
                file.delete();
                DirectoryAdapter.this.notifyDataSetChanged();
            }
        });

        if (nfcAdapter != null) {
            Button button = (Button) convertView.findViewById(R.id.send);
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Attempting to send");
                    File fileDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    File transferFile = new File(fileDirectory, file.getName());
                    transferFile.setReadable(true, false);
                    try {
                        Utilities.copyFile(new FileInputStream(file), new FileOutputStream(transferFile));
                        nfcAdapter.setBeamPushUris(
                                new Uri[]{Uri.fromFile(transferFile)}, activity);
                        Toast.makeText(context, "Sending Picture via NFC beam", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Log.d(TAG, e.getMessage());
                    }
                }
            });
        }

        return convertView;
    }
}
