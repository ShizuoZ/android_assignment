package example.com.taptiptap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by edward on 15/11/2.
 */
public class DrawActivity extends Activity {
    private Button paintButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        paintButton = (Button)findViewById(R.id.paintbutton);
        setContentView(R.layout.activity_draw);
        // Getting reference to PaintView
        DrawView drawView = (DrawView)findViewById(R.id.draw_view);

        // Getting reference to TextView tv_cooridinate
        TextView tvCoordinates = (TextView)findViewById(R.id.tv_draw_coordinates);

        // Passing reference of textview to PaintView object to update on coordinate changes
        drawView.setTextView(tvCoordinates);
    }
    public void paintlistener(View view){
        Intent intent = new Intent(DrawActivity.this, taptiptap.class);
        startActivity(intent);
    }
}