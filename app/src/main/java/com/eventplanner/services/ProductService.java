package com.eventplanner.services;

import com.eventplanner.model.requests.products.CreateProductRequest;
import com.eventplanner.model.requests.products.CreateProductWithPendingCategoryRequest;
import com.eventplanner.model.requests.products.UpdateProductRequest;
import com.eventplanner.model.responses.PagedResponse;
import com.eventplanner.model.responses.products.GetProductHistoryResponse;
import com.eventplanner.model.responses.products.GetProductResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProductService {
    @POST("products")
    Call<Long> createProduct(@Body CreateProductRequest request);

    @POST("products/with-pending-category")
    Call<Long> createProductWithPendingCategory(@Body CreateProductWithPendingCategoryRequest request);

    @GET("products/{id}")
    Call<GetProductResponse> getProductById(@Path("id") Long id);

    @GET("products")
    Call<PagedResponse<GetProductResponse>> getAllProducts(
        @Query("page") int page,
        @Query("size") int size
    );

    @GET("products/filter")
    Call<PagedResponse<GetProductResponse>> filterProducts(
        @Query("name") String name,
        @Query("categoryId") Long categoryId,
        @Query("eventTypeId") Long eventTypeId,
        @Query("minPrice") Double minPrice,
        @Query("maxPrice") Double maxPrice,
        @Query("isAvailable") Boolean isAvailable,
        @Query("businessOwnerId") Long businessOwnerId,
        @Query("isVisible") Boolean isVisible,
        @Query("page") int page,
        @Query("size") int size
    );

    @PUT("products/{id}")
    Call<Void> updateProduct(@Path("id") Long id, @Body UpdateProductRequest request);

    @DELETE("products/{id}")
    Call<Void> logicalDeleteProduct(@Path("id") Long id);

    @POST("products/buy-product")
    Call<Void> buyProduct(@Query("productId") Long productId, @Query("eventId") Long eventId);

    @GET("products/business-owner/{businessOwnerId}/filter")
    Call<PagedResponse<GetProductResponse>> filterProductsByBusinessOwner(
            @Path("businessOwnerId") Long businessOwnerId,
            @Query("name") String
                    name,
            @Query("categoryId") Long categoryId,
            @Query("eventTypeId") Long eventTypeId,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice,
            @Query("isAvailable") Boolean isAvailable,
            @Query("isVisible") Boolean isVisible,
            @Query("isDeleted") Boolean isDeleted,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("products/{id}/history")
    Call<List<GetProductHistoryResponse>> getProductHistory(@Path("id") Long productId);
}
