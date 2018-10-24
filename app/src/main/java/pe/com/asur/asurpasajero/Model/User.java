package pe.com.asur.asurpasajero.Model;

/**
 * Created by agus on 7/03/2018.
 */

public class User {
    private String email,password,name,phone,placa,modelo,avatarUrl,rates;
    int car_number;

    public String getRates() {
        return rates;
    }

    public void setRates(String rates) {
        this.rates = rates;
    }

    public User() {
    }

    public User(String email, String password, String name, String phone, String placa, String modelo, int car_number) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.placa = placa;
        this.modelo = modelo;
        this.car_number = car_number;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public void setCar_number(int car_number) {
        this.car_number = car_number;
    }
}