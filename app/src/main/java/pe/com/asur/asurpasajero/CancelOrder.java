package pe.com.asur.asurpasajero;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import pe.com.asur.asurpasajero.Common.Common;

public class CancelOrder extends AppCompatActivity {

    Button ok_button;
    TextView reason_txt;

    String reason;

    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_order);

        ok_button = (Button)findViewById(R.id.ok_button);
        reason_txt = (TextView)findViewById(R.id.reason_txt);

        if (getIntent() != null) {

            Common.ordering = false;
            Common.acceptance = false;
            reason = getIntent().getStringExtra("reason");

            mediaPlayer = MediaPlayer.create(this,R.raw.ringtone);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();

            reason_txt.setText(reason);

            ok_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getBaseContext(), ChoferHome.class);
                    intent.putExtra("state", "cancel");
                    startActivity(intent);
                    finish();
                }
            });



        } else {
            Toast.makeText(this, "It doesn't receive reason for canceling order", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onStop() {
        if(mediaPlayer.isPlaying())
            mediaPlayer.release();
        super.onStop();
    }

    @Override
    protected void onPause() {
        if(mediaPlayer.isPlaying())
            mediaPlayer.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mediaPlayer!=null && !mediaPlayer.isPlaying())
            mediaPlayer.start();
    }
}
