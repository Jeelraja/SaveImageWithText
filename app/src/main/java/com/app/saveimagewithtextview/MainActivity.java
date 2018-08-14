package com.app.saveimagewithtextview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mIvProfile;
    private EditText mEdtType;
    private Button mBtnSave;
    private TextView mTvOnImage;
    private RelativeLayout rlImage;

    String galleryPath, cameraPath;
    Bitmap bmp, bmpGallery, bmpCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
    }

    private void findViews() {

        mIvProfile = findViewById(R.id.ivProfile);
        mEdtType = findViewById(R.id.edtType);
        mBtnSave = findViewById(R.id.btnSave);
        mTvOnImage = findViewById(R.id.tvOnImage);
        rlImage = findViewById(R.id.rlImage);
        mIvProfile.setOnClickListener(this);
        mBtnSave.setOnClickListener(this);
        mEdtType.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!mEdtType.getText().toString().equals("")) {
                    mTvOnImage.setText(mEdtType.getText().toString().trim());
                } else {
                    Toast.makeText(MainActivity.this, "Please Type Something!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSave:
                rlImage.setDrawingCacheEnabled(true);
                //Without the below line the view will have a dimension of 0,0 and the bitmap will be null
                rlImage.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                rlImage.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                rlImage.buildDrawingCache();
                Bitmap bm = rlImage.getDrawingCache();
                Bitmap bitmap = Bitmap.createBitmap(bm);
                try {
                    Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                    File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    Toast.makeText(this, "" + path, Toast.LENGTH_SHORT).show();
                    File imageFile = new File(path, "" + System.currentTimeMillis() + ".jpg");
                    FileOutputStream fileOutPutStream = new FileOutputStream(imageFile);
                    Log.i("path", "++" + path);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutPutStream);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.ivProfile:
                selectImage();
                break;
        }
    }


    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, 1);
                } else if (options[item].equals("Choose from Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });

        builder.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                File f = new File(Environment.getExternalStorageDirectory().toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        f = temp;
                        break;
                    }
                }
                try {
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                    bmpCamera = BitmapFactory.decodeFile(f.getAbsolutePath(),
                            bitmapOptions);
                    mIvProfile.setImageBitmap(bmpCamera);
                    cameraPath = android.os.Environment
                            .getExternalStorageDirectory()
                            + File.separator
                            + "Phoenix" + File.separator + "default";
                    f.delete();
                    OutputStream outFile = null;
                    File file = new File(cameraPath, String.valueOf(System.currentTimeMillis()) + ".jpg");
                    try {
                        outFile = new FileOutputStream(file);
                        bmpCamera.compress(Bitmap.CompressFormat.JPEG, 85, outFile);
                        outFile.flush();
                        outFile.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == 2) {
                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                galleryPath = c.getString(columnIndex);
                c.close();
                bmpGallery = (BitmapFactory.decodeFile(galleryPath));
                Log.i("path gallery", "" + galleryPath);
                mIvProfile.setImageBitmap(bmpGallery);
            }
        }
    }
}
