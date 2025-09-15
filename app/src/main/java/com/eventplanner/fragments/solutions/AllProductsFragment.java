package com.eventplanner.fragments.solutions;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
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
    private int totalPages = 0;
    private TextView textCurrentPage;
    private EditText editPageNumber;
    private Spinner spinnerPageSize;
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

        setupPagination(rootView);
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


        call.enqueue(new Callback<PagedResponse<GetProductResponse>>() {
            @Override
            public void onResponse(Call<PagedResponse<GetProductResponse>> call, Response<PagedResponse<GetProductResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PagedResponse<GetProductResponse> pageResponse = response.body();

                    products.clear();

                    products.addAll(pageResponse.getContent());
                    adapter.updateProducts(products);

                    totalPages = pageResponse.getTotalPages();
                    textCurrentPage.setText("Page " + (currentPage + 1) + " of " + totalPages);
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

    private void showFilterDialog() {
        Log.i("AllServicesFragment", "Filter button clicked");
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
        View dialogView = getLayoutInflater().inflate(R.layout.bottom_sheet_filter_solutions, null);

        // Filling filter dialog with categories
        RadioGroup radioGroupCategories = dialogView.findViewById(R.id.radio_group_categories);
        if (radioGroupCategories != null) {
            RadioButton allCategories = new RadioButton(getActivity());
            allCategories.setText("All Categories");
            allCategories.setTag(null);
            allCategories.setChecked(selectedCategoryId == null);
            radioGroupCategories.addView(allCategories);

            for (GetSolutionCategoryResponse category : categories) {
                RadioButton rb = new RadioButton(getActivity());
                rb.setText(category.getName());
                rb.setTag(category.getId());
                rb.setChecked(selectedCategoryId != null && selectedCategoryId.equals(category.getId()));
                radioGroupCategories.addView(rb);
            }
        }

        RadioGroup radioGroupEventTypes = dialogView.findViewById(R.id.radio_group_event_types);
        if (radioGroupEventTypes != null) {
            RadioButton allTypes = new RadioButton(getActivity());
            allTypes.setText("All Types");
            allTypes.setTag(null);
            allTypes.setChecked(selectedEventTypeId == null);
            radioGroupEventTypes.addView(allTypes);

            for (GetEventTypeResponse eventType : eventTypes) {
                RadioButton rb = new RadioButton(getActivity());
                rb.setText(eventType.getName());
                rb.setTag(eventType.getId());
                rb.setChecked(selectedEventTypeId != null && selectedEventTypeId.equals(eventType.getId()));
                radioGroupEventTypes.addView(rb);
            }
        }
        // Availability
        RadioGroup availabilityGroup = dialogView.findViewById(R.id.radio_group_availabilityFilter);
        if (availabilityGroup != null) {
            if (isAvailable == null) {
                availabilityGroup.clearCheck(); // "All" case
            } else if (isAvailable) {
                availabilityGroup.check(R.id.radio_button_available);
            } else {
                availabilityGroup.check(R.id.radio_button_unavailable);
            }
        }

// Min price
        EditText minPriceInput = dialogView.findViewById(R.id.editText_min_price);
        if (minPrice != null) {
            minPriceInput.setText(String.valueOf(minPrice));
        }

// Max price
        EditText maxPriceInput = dialogView.findViewById(R.id.editText_max_price);
        if (maxPrice != null) {
            maxPriceInput.setText(String.valueOf(maxPrice));
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
        // Category
        RadioGroup categoryGroup = dialogView.findViewById(R.id.radio_group_categories);
        if (categoryGroup != null) {
            int selectedId = categoryGroup.getCheckedRadioButtonId();
            if (selectedId != -1) {
                RadioButton selectedRadio = dialogView.findViewById(selectedId);
                selectedCategoryId = (Long) selectedRadio.getTag();
            }
        }

// Event type
        RadioGroup typeGroup = dialogView.findViewById(R.id.radio_group_event_types);
        if (typeGroup != null) {
            int selectedId = typeGroup.getCheckedRadioButtonId();
            if (selectedId != -1) {
                RadioButton selectedRadio = dialogView.findViewById(selectedId);
                selectedEventTypeId = (Long) selectedRadio.getTag();
            }
        }

// Availability
        RadioGroup availabilityGroup = dialogView.findViewById(R.id.radio_group_availabilityFilter);
        if (availabilityGroup != null) {
            int selectedId = availabilityGroup.getCheckedRadioButtonId();
            if (selectedId == R.id.radio_button_available) {
                isAvailable = true;
            } else if (selectedId == R.id.radio_button_unavailable) {
                isAvailable = false;
            } else {
                isAvailable = null;
            }
        }

// Price
        EditText minInput = dialogView.findViewById(R.id.editText_min_price);
        EditText maxInput = dialogView.findViewById(R.id.editText_max_price);

        try {
            String minText = minInput.getText().toString().trim();
            minPrice = minText.isEmpty() ? null : Double.parseDouble(minText);

            String maxText = maxInput.getText().toString().trim();
            maxPrice = maxText.isEmpty() ? null : Double.parseDouble(maxText);
        } catch (NumberFormatException e) {
            minPrice = null;
            maxPrice = null;
        }

        // Reset page and reload services with filters
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

    private void setupPagination(View rootView)
    {
        textCurrentPage = rootView.findViewById(R.id.text_current_page);
        editPageNumber = rootView.findViewById(R.id.edit_page_number);
        spinnerPageSize = rootView.findViewById(R.id.spinner_page_size);

// Page size spinner setup
        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(
                getContext(),
                R.array.page_size_options,
                android.R.layout.simple_spinner_item);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPageSize.setAdapter(adapterSpinner);
        spinnerPageSize.setSelection(1); // default = 10

        spinnerPageSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int newSize = Integer.parseInt(parent.getItemAtPosition(position).toString());
                if (pageSize != newSize) {
                    pageSize = newSize;
                    currentPage = 0;
                    loadProducts();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

// Button listeners
        rootView.findViewById(R.id.button_first_page).setOnClickListener(v -> {
            currentPage = 0;
            loadProducts();
        });

        rootView.findViewById(R.id.button_prev_page).setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                loadProducts();
            }
        });

        rootView.findViewById(R.id.button_next_page).setOnClickListener(v -> {
            if (currentPage + 1 < totalPages) {
                currentPage++;
                loadProducts();
            }
        });

        rootView.findViewById(R.id.button_last_page).setOnClickListener(v -> {
            if (totalPages > 0) {
                currentPage = totalPages - 1;
                loadProducts();
            }
        });

        rootView.findViewById(R.id.button_go_page).setOnClickListener(v -> {
            String input = editPageNumber.getText().toString();
            if (!input.isEmpty()) {
                int page = Integer.parseInt(input) - 1;
                if (page >= 0 && page < totalPages) {
                    currentPage = page;
                    loadProducts();
                } else {
                    Toast.makeText(getContext(), "Invalid page number", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
