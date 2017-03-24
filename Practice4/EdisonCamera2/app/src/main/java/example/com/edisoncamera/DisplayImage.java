package example.com.edisoncamera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DisplayImage extends Activity {
    private ImageView mImageView;
    private Button mPhotoButton;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_image);
        mTextView=(TextView)findViewById(R.id.address);
        mImageView = (ImageView) findViewById(R.id.imageView);
//        mPhotoButton = (Button)findViewById(R.id.back_button);
        String filePath = this.getIntent().getStringExtra("path");
        Log.d("filePath", filePath);
        if (filePath != null) {
            String address=this.getIntent().getStringExtra("address");
            Bitmap selectedphoto = BitmapFactory.decodeFile(filePath);
            mTextView.setText(address);
            final int maxSize = 4096;
            int outWidth;
            int outHeight;
            int inWidth = selectedphoto.getWidth();
            int inHeight = selectedphoto.getHeight();
            if (inWidth > inHeight) {
                outWidth = maxSize;
                outHeight = (inHeight * maxSize) / inWidth;
            } else {
                outHeight = maxSize;
                outWidth = (inWidth * maxSize) / inHeight;
            }
            Log.d("heigh", "" + outHeight);
            Log.d("width", "" + outWidth);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(selectedphoto, outWidth, outHeight, false);
            mImageView.setImageBitmap(resizedBitmap);
        }
    }
}
