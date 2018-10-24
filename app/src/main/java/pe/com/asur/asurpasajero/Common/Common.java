package pe.com.asur.asurpasajero.Common;

import android.location.Location;

import pe.com.asur.asurpasajero.Model.User;
import pe.com.asur.asurpasajero.Remote.FCMClient;
import pe.com.asur.asurpasajero.Remote.IFCMService;
import pe.com.asur.asurpasajero.Remote.IGoogleAPI;
import pe.com.asur.asurpasajero.Remote.RetrofitClient;

/**
 * Created by agus on 13/03/2018.
 */

public class Common {



    public static final String driver_tbl = "Drivers";
    public static final String user_driver_tbl = "DriversInformation";
    public static final String user_rider_tbl = "RidersInformation";
    public static final String pickup_request_tbl = "PickupRequest";
    public static final String token_tbl = "Tokens";
    public static final String register_current_order = "CurrentOrder";//if
    public static final String order_table = "Orders";


    public static final int PICK_IMAGE_REQUEST=9999;

    public static User currentUser;

    public static boolean acceptance = false; // current accept order so driver is serviced for customer
    public static boolean ordering = false; //current ordering state, customer wait for you accepting his/her order

    public static String passengerID = "";
    public static String orderID = "";//current order ID of Order table


    public static Location mLastLocation=null;
    public static Location mFirstLocation = null;

    public static final String baseURL = "https://maps.googleapis.com";
    public static final String fcmURL = "https://fcm.googleapis.com/";
    public static final String user_field="usr";
    public static final String pwd_field="pwd";

    public static double base_fare = 3.5;
    private static double time_rate = 0.15;
    private static double distance_rate = 0.73;

    public static String current_orerder_state = "r";// no order is "r"(rest), waiting passenger is "w"(wait), during order is "b"(busy)


    public static double formulaPrice(double km,double min)
    {
        return base_fare+(distance_rate*km)+(time_rate*min);
    }


    public static IGoogleAPI getGoogleAPI()
    {
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }
    public static IFCMService getFCMService()
    {
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }
}

