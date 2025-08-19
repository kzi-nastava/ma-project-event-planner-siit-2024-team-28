package com.eventplanner.fragments.solutions;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.eventplanner.R;
import com.eventplanner.adapters.products.AllProductsAdapter;
import com.eventplanner.model.constants.UserRoles;
import com.eventplanner.model.responses.PagedResponse;
import com.eventplanner.model.responses.eventTypes.GetEventTypeResponse;
import com.eventplanner.model.responses.products.GetProductResponse;
import com.eventplanner.model.responses.solutionCateogries.GetSolutionCategoryResponse;
import com.eventplanner.services.EventTypeService;
import com.eventplanner.services.ProductService;
import com.eventplanner.services.SolutionCategoryService;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.HttpUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllProductsFragment extends Fragment implements AllProductsAdapter.OnProductClickListener {

    private ProductService productService;
    private EventTypeService eventTypeService;
    private SolutionCategoryService categoryService;
    private AllProductsAdapter adapter;
    private List<GetProductResponse> products = new ArrayList<>();
    private List<GetEventTypeResponse> eventTypes = new ArrayList<>();
    private List<GetSolutionCategoryResponse> categories = new ArrayList<>();

    // Filter parameters
    private int currentPage = 0;
    private int pageSize = 10;
    private String searchQuery;
    private Long selectedCategoryId;
    private Long selectedEventTypeId;
    private Double minPrice;
    private Double maxPrice;
    private Boolean isAvailable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment layout
        View rootView = inflater.inflate(R.layout.fragment_all_products, container, false);

        // Initialize services
        productService = HttpUtils.getProductService();
        eventTypeService = HttpUtils.getEventTypeService();
        categoryService = HttpUtils.getSolutionCategoryService();

        setupRecyclerView(rootView);
        setupSearchView(rootView);
        setupCreateProductFab(rootView);
        loadReferenceData();
        loadProducts();

        // Set up the filter button
        Button filterButton = rootView.findViewById(R.id.button_filter_products);
        filterButton.setOnClickListener(v -> showFilterDialog());

        // Set up the clear filters button
        Button clearFiltersButton = rootView.findViewById(R.id.button_clear_filters);
        clearFiltersButton.setOnClickListener(v -> clearFilters());

        return rootView;
    }

    private void setupRecyclerView(View rootView) {
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view_products);

        adapter = new AllProductsAdapter(getContext(), products);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Set the click listener using this fragment since it implements the interface
        adapter.setOnProductClickListener(this);
    }

    private void setupSearchView(View rootView) {
        SearchView searchView = rootView.findViewById(R.id.search_bar);
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchQuery = query.trim().isEmpty() ? null : query.trim();
                    currentPage = 0;
                    loadProducts();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    // Optional: implement real-time search
                    return false;
                }
            });
        }
    }

    private void setupCreateProductFab(View rootView) {
        FloatingActionButton fabCreateProduct = rootView.findViewById(R.id.fab_create_product);

        // Show FAB only for logged in business owners
        if (AuthUtils.getToken(requireContext()) != null &&
            AuthUtils.getUserRoles(requireContext()).contains(UserRoles.BusinessOwner)) {
            fabCreateProduct.setVisibility(View.VISIBLE);

            fabCreateProduct.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_allProducts_to_product_creation);
            });
        } else {
            fabCreateProduct.setVisibility(View.GONE);
        }
    }

    private void loadReferenceData() {
        // Load categories
        categoryService.getAcceptedCategories().enqueue(new Callback<Collection<GetSolutionCategoryResponse>>() {
            @Override
            public void onResponse(Call<Collection<GetSolutionCategoryResponse>> call, Response<Collection<GetSolutionCategoryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = new ArrayList<>(response.body());
                    Log.d("AllProductsFragment", "Loaded " + categories.size() + " categories");
                }
            }

            @Override
            public void onFailure(Call<Collection<GetSolutionCategoryResponse>> call, Throwable t) {
                Log.e("AllProductsFragment", "Failed to load categories", t);
            }
        });

        // Load event types
        eventTypeService.getAllEventTypes().enqueue(new Callback<Collection<GetEventTypeResponse>>() {
            @Override
            public void onResponse(Call<Collection<GetEventTypeResponse>> call, Response<Collection<GetEventTypeResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventTypes = new ArrayList<>(response.body());
                    Log.d("AllProductsFragment", "Loaded " + eventTypes.size() + " event types");
                }
            }

            @Override
            public void onFailure(Call<Collection<GetEventTypeResponse>> call, Throwable t) {
                Log.e("AllProductsFragment", "Failed to load event types", t);
            }
        });
    }

    private void loadProducts() {
        Call<PagedResponse<GetProductResponse>> call;

        if (hasFilters()) {
            call = productService.filterProducts(
                searchQuery,
                selectedCategoryId,
                selectedEventTypeId,
                minPrice,
                maxPrice,
                isAvailable,
                null, // businessOwnerId - null for all products
                true, // isVisible - only show visible products
                currentPage,
                pageSize
            );
        } else {
            call = productService.getAllProducts(currentPage, pageSize);
        }

        call.enqueue(new Callback<PagedResponse<GetProductResponse>>() {
            @Override
            public void onResponse(Call<PagedResponse<GetProductResponse>> call, Response<PagedResponse<GetProductResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PagedResponse<GetProductResponse> pageResponse = response.body();
                    if (currentPage == 0) {
                        products.clear();
                    }
                    products.addAll(pageResponse.getContent());
                    adapter.updateProducts(products);
                    Log.d("AllProductsFragment", "Loaded " + products.size() + " products");
                } else {
                    Log.e("AllProductsFragment", "Failed to load products: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PagedResponse<GetProductResponse>> call, Throwable t) {
                Log.e("AllProductsFragment", "Failed to load products", t);
            }
        });
    }

    private boolean hasFilters() {
        return searchQuery != null || selectedCategoryId != null || selectedEventTypeId != null ||
               minPrice != null || maxPrice != null || isAvailable != null;
    }

    private void showFilterDialog() {
        Log.i("AllProductsFragment", "Filter button clicked");
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
        View dialogView = getLayoutInflater().inflate(R.layout.bottom_sheet_filter_products, null);

        // Filling filter dialog with categories
        RadioGroup radioGroupCategories = dialogView.findViewById(R.id.radio_group_categories);
        if (radioGroupCategories != null) {
            // Add "All Categories" option
            RadioButton allCategoriesButton = new RadioButton(getActivity());
            allCategoriesButton.setText("All Categories");
            allCategoriesButton.setTag(-1L);
            allCategoriesButton.setChecked(selectedCategoryId == null);
            radioGroupCategories.addView(allCategoriesButton);

            for (GetSolutionCategoryResponse category : categories) {
                RadioButton radioButton = new RadioButton(getActivity());
                radioButton.setText(category.getName());
                radioButton.setTag(category.getId());
                radioButton.setChecked(category.getId().equals(selectedCategoryId));
                radioButton.setButtonTintList(getResources().getColorStateList(R.color.cool_purple));
                radioGroupCategories.addView(radioButton);
            }
        }

        // Filling filter dialog with event types checkboxes
        LinearLayout eventTypesCheckboxes = dialogView.findViewById(R.id.event_types_checkboxes);
        if (eventTypesCheckboxes != null) {
            for (GetEventTypeResponse eventType : eventTypes) {
                CheckBox checkBox = new CheckBox(getActivity());
                checkBox.setText(eventType.getName());
                checkBox.setTag(eventType.getId());
                checkBox.setChecked(eventType.getId().equals(selectedEventTypeId));
                checkBox.setButtonTintList(getResources().getColorStateList(R.color.cool_purple));
                eventTypesCheckboxes.addView(checkBox);
            }
        }

        // Add Apply and Cancel buttons
        Button applyButton = dialogView.findViewById(R.id.button_apply_filters);
        Button cancelButton = dialogView.findViewById(R.id.button_cancel_filters);

        if (applyButton != null) {
            applyButton.setOnClickListener(v -> {
                applyFilters(dialogView);
                bottomSheetDialog.dismiss();
            });
        }

        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        bottomSheetDialog.setContentView(dialogView);
        bottomSheetDialog.show();
    }

    private void applyFilters(View dialogView) {
        // Get selected category
        RadioGroup radioGroupCategories = dialogView.findViewById(R.id.radio_group_categories);
        if (radioGroupCategories != null) {
            int selectedId = radioGroupCategories.getCheckedRadioButtonId();
            if (selectedId != -1) {
                RadioButton selectedRadio = dialogView.findViewById(selectedId);
                if (selectedRadio != null) {
                    Long categoryId = (Long) selectedRadio.getTag();
                    selectedCategoryId = categoryId.equals(-1L) ? null : categoryId;
                }
            }
        }

        // Get selected event types
        LinearLayout eventTypesCheckboxes = dialogView.findViewById(R.id.event_types_checkboxes);
        selectedEventTypeId = null; // Reset
        if (eventTypesCheckboxes != null) {
            for (int i = 0; i < eventTypesCheckboxes.getChildCount(); i++) {
                View child = eventTypesCheckboxes.getChildAt(i);
                if (child instanceof CheckBox) {
                    CheckBox checkBox = (CheckBox) child;
                    if (checkBox.isChecked()) {
                        selectedEventTypeId = (Long) checkBox.getTag();
                        break; // For now, just take the first selected event type
                    }
                }
            }
        }

        // Reset page and reload products with filters
        currentPage = 0;
        loadProducts();
    }

    private void clearFilters() {
        searchQuery = null;
        selectedCategoryId = null;
        selectedEventTypeId = null;
        minPrice = null;
        maxPrice = null;
        isAvailable = null;
        currentPage = 0;
        loadProducts();
    }

    @Override
    public void onProductClick(GetProductResponse product) {
        Bundle bundle = new Bundle();
        bundle.putString("solutionId", String.valueOf(product.getId()));
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_allProducts_to_solutionDetails, bundle);
    }

    @Override
    public void onEditProduct(GetProductResponse product) {
        Bundle bundle = new Bundle();
        bundle.putLong("productId", product.getId());
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_allProducts_to_product_edit, bundle);
    }

    @Override
    public void onDeleteProduct(GetProductResponse product) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete '" + product.getName() + "'?\n\n" +
                        "This will:\n" +
                        "• Hide the product from all customers\n" +
                        "• Preserve existing purchases and history\n" +
                        "• Allow you to restore it later if needed\n\n" +
                        "This action can be undone by contacting support.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteProduct(product.getId());
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteProduct(Long productId) {
        productService.logicalDeleteProduct(productId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Product deleted successfully", Toast.LENGTH_SHORT).show();
                    loadProducts(); // Refresh the list
                } else {
                    Toast.makeText(getContext(), "Failed to delete product", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("AllProductsFragment", "Failed to delete product", t);
                Toast.makeText(getContext(), "Error deleting product", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
