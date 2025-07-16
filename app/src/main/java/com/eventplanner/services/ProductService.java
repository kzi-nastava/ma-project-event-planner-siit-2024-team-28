package com.eventplanner.services;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ProductService {
    @POST("products/buy-product")
    Call<Void> buyProduct(
            @Query("productId") Long productId,
            @Query("eventId") Long eventId
    );
}
