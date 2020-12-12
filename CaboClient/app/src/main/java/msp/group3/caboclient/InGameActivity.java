package msp.group3.caboclient;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * this is an example for a zoomable and scrollable layout
 */
public class InGameActivity extends AppCompatActivity {

    private ZoomLayout zoomLayout;
    private ImageButton player1_card1;
    private ImageButton player1_card2;
    private ImageButton player1_card3;
    private ImageButton player1_card4;

    private ImageButton player2_card1;
    private ImageButton player2_card2;
    private ImageButton player2_card3;
    private ImageButton player2_card4;

    private ImageButton player3_card1;
    private ImageButton player3_card2;
    private ImageButton player3_card3;
    private ImageButton player3_card4;

    private ImageButton player4_card1;
    private ImageButton player4_card2;
    private ImageButton player4_card3;
    private ImageButton player4_card4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.ingame_activity);
        zoomLayout = (ZoomLayout) findViewById(R.id.zoom_layout);
        player1_card1 = (ImageButton) findViewById(R.id.player1_card1_imageButton);
        ViewTreeObserver vto = zoomLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                zoomLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = zoomLayout.getMeasuredWidth();
                int height = zoomLayout.getMeasuredHeight();
                zoomLayout.setContentSize(width, height);
            }
        });

        player1_card1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),
                        "Card 1 Player 1 is clicked", Toast.LENGTH_SHORT).show();
                //bounds remain the same only image changes
                //ObjectAnimator.ofFloat(player1_card1_imageButton, "scaleX", 1.0f, 2.0f).setDuration(600).start();
                //ObjectAnimator.ofFloat(player1_card1_imageButton, "scaleY", 1.0f, 2.0f).setDuration(600).start();
            }
        });
    }

    private void instantiatePlayerCardDecks(){
        player1_card1 = findViewById(R.id.player1_card1_imageButton);
        player1_card2 = findViewById(R.id.player1_card2_imageButton);
        player1_card3 = findViewById(R.id.player1_card3_imageButton);
        player1_card4 = findViewById(R.id.player1_card4_imageButton);

        player2_card1 = findViewById(R.id.player2_card1_imageButton);
        player2_card2 = findViewById(R.id.player2_card2_imageButton);
        player2_card3 = findViewById(R.id.player2_card3_imageButton);
        player2_card4 = findViewById(R.id.player2_card4_imageButton);

        player3_card1 = findViewById(R.id.player3_card1_imageButton);
        player3_card2 = findViewById(R.id.player3_card2_imageButton);
        player3_card3 = findViewById(R.id.player3_card3_imageButton);
        player3_card4 = findViewById(R.id.player3_card4_imageButton);

        player4_card1 = findViewById(R.id.player4_card1_imageButton);
        player4_card2 = findViewById(R.id.player4_card2_imageButton);
        player4_card3 = findViewById(R.id.player4_card3_imageButton);
        player4_card4 = findViewById(R.id.player4_card4_imageButton);
    }

}
