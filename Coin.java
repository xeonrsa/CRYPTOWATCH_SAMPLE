package com.xendev.xeon.cryptowatch.Classes;

import android.graphics.Bitmap;

import java.lang.reflect.Constructor;

/**
 * Created by Xeon on 09/06/2017.
 */

public class Coin {
    public String coinName;             // This will hold the coin Name (eg BTC)
    public String coinFullName;         // This will hold the full name of the coin (eg Bitcoin)
    public Integer coinID;              // This will hold the coin ID
    public String coinImageUrl;         // This will hold the coin ImageUrl
    public Bitmap coinIcon;             // This will hold the actual coin Icon
    public String coinExchangeType1;    // This will hold the Type first of exchange rate
    public String coinExchangeType2;    // This will hold the Type second of exchange rate
    public String coinExchangeRate1;    // This will hold the first exchange rate of the coin (1 coin = X selected exchange rate)
    public String coinExchangeRate2;     // This will hold the second exhange rate of the coin (1 coin = X selected exchange rate)

    // Constructor to create with only coin name, sets can be used to add the rest
    public Coin(String cCoinName){
        coinName = cCoinName;
    }
    // Set the ID of the coin
    public void setCoinID(Integer aCoinID){
        coinID = aCoinID;
    }
    // Set the Full Name of the coin
    public  void setCoinFullName(String aCoinFullName){
        coinFullName = aCoinFullName;
    }
    // Set the ImageUrl of the coin
    public void setCoinImageUrl(String aCoinImageUrl){
        coinImageUrl = aCoinImageUrl;
    }
    // Set the Icon of the coin
    public void setCoinIcon(Bitmap aCoinIcoin){
        coinIcon = aCoinIcoin;
    }
    // Set the first exchange rate of the coin
    public void setCoinExchange1(String aCoinExchangeType1, String aCoinExchangeRate1){
        coinExchangeType1 = aCoinExchangeType1;
        coinExchangeRate1 = aCoinExchangeRate1;
    }
    // Set the second exchange rate of the coin
    public void setCoinExchange2(String aCoinExchangeType2, String aCoinExchangeRate2){
        coinExchangeType2 = aCoinExchangeType2;
        coinExchangeRate2 = aCoinExchangeRate2;
    }
}
