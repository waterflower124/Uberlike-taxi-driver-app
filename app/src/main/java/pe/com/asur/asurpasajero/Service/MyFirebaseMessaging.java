package pe.com.asur.asurpasajero.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import pe.com.asur.asurpasajero.CancelOrder;
import pe.com.asur.asurpasajero.Common.Common;
import pe.com.asur.asurpasajero.CustommerCall;
import pe.com.asur.asurpasajero.DriverTracking;
import pe.com.asur.asurpasajero.Helper.NotificationHelper;
import pe.com.asur.asurpasajero.R;

/**
 * Created by agus on 14/03/2018.
 */

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

//        Toast.makeText(getBaseContext(), "Message received:::::Message received", Toast.LENGTH_LONG).show();

        if(remoteMessage.getData()!=null) {

            Map<String, String> data = remoteMessage.getData();//get data from notification
            String type = data.get("type");
            if (type.equals("order")){
                String customer = data.get("customer");
                String lat = data.get("lat");
                String lng = data.get("lng");
                String recvstr = data.get("str");
                String orderID = data.get("orderID");

                if (recvstr.isEmpty()) {
                    if(!Common.ordering) {
                        Common.ordering = true;
                        Common.passengerID = customer;
                        Common.orderID = orderID;
//                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
//                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//
//                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
//                                if (customer.equals(userSnapshot.child("token").getValue())) {
//                                    Common.passengerID = userSnapshot.getKey();
//                                    break;
//                                }
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//                            throw databaseError.toException();
//                        }
//                    });

                        //  Log.d("RAPICITY",remoteMessage.getNotification().getBody());

                        //Because i will send the Firebase message with contain lat and lng from rider app
                        //so i need convert message to LatLng


                        Intent intent = new Intent(getBaseContext(), CustommerCall.class);
                        intent.putExtra("lat", lat);
                        intent.putExtra("lng", lng);
                        intent.putExtra("customer", customer);
                        intent.putExtra("orderID", orderID);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            /*startActivity(Intent.createChooser(sendIntent, "Compartir en")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));*/
                        startActivity(intent);
                    }
                } else {

                    Intent intent = new Intent(getBaseContext(), CancelOrder.class);
                    intent.putExtra("reason", recvstr);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                }
            } else if (type.equals("message")) {
                String message = data.get("message");
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
                    showArrivedNotificationApi26(message);
                else
                    showArrivedNotification(message);
            }

        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showArrivedNotificationApi26(String body) {
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(),
                0,new Intent(),PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper=new NotificationHelper(getBaseContext());


        Notification.Builder builder=notificationHelper.getUberNotification("Llego",body,contentIntent,defaultSound);
        notificationHelper.getManager().notify(1,builder.build());


    }


    private void showArrivedNotification(String body) {
        //this code only work Android API 25 and below
        //From Android API 26 or higher you need create Notification Channel
        //I have publish tutorial about this content you can watch on my channel
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(),
                0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());

        builder.setAutoCancel(true)
                .setDefaults(android.app.Notification.DEFAULT_LIGHTS| android.app.Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.car)
                .setContentTitle("LLEGO")
                .setContentText(body)
                .setContentIntent(contentIntent);
        NotificationManager manager = (NotificationManager)getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1,builder.build());
    }

}
