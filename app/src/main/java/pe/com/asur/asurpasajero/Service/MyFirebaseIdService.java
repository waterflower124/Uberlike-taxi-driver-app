package pe.com.asur.asurpasajero.Service;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import pe.com.asur.asurpasajero.Common.Common;
import pe.com.asur.asurpasajero.Model.Token;

/**
 * Created by agus on 14/03/2018.
 */

public class MyFirebaseIdService extends FirebaseInstanceIdService {

    //pres contro +o

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("chiclayo",refreshedToken);
        updateTokenToServer(refreshedToken); //when have refresh token we need update to our realtime database

    }

    private void updateTokenToServer(String refreshedToken) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tbl);

        Token token = new Token(refreshedToken);
        if (FirebaseAuth.getInstance().getCurrentUser() !=null)  // if already login must update token
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(token);
    }
}
