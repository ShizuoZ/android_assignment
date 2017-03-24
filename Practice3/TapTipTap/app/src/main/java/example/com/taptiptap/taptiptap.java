//package example.com.taptiptap;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//
//public class taptiptap extends Activity {
//    private Button paintButton;
//    private Button drawButton;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_homepage);
//        paintButton = (Button)findViewById(R.id.button);
//        drawButton =(Button)findViewById(R.id.button2);
//    }
//
//    public void paintlistener(View view){
//        Intent intent = new Intent(taptiptap.this, PaintActivity.class);
//        startActivity(intent);
//    }
//    public void drawlistener(View view){
//        Intent intent = new Intent(taptiptap.this, DrawActivity.class);
//        startActivity(intent);
//    }
//}
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
public class taptiptap extends Activity {

    private Button drawButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        drawButton =(Button)findViewById(R.id.drawbutton);
        setContentView(R.layout.activity_taptaptap);
        // Getting reference to PaintView
        PaintView paintView = (PaintView)findViewById(R.id.paint_view);

        // Getting reference to TextView tv_cooridinate
        TextView tvCoordinates = (TextView)findViewById(R.id.tv_coordinates);

        // Passing reference of textview to PaintView object to update on coordinate changes
        paintView.setTextView(tvCoordinates);
    }
    public void drawlistener(View view){
        Intent intent = new Intent(taptiptap.this, DrawActivity.class);
        startActivity(intent);
    }
}
