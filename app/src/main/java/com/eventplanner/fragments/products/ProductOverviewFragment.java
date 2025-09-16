package com.eventplanner.fragments.products;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventplanner.R;
import com.eventplanner.adapters.products.ProductsAdapter;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductOverviewFragment extends Fragment implements ProductsAdapter.OnProductClickListener {
    private RecyclerView recyclerView;
    private ProductsAdapter adapter;
    private ProductService productService;
    private SolutionCategoryService categoryService;
    private EventTypeService eventTypeService;
    private EditText searchEditText;
    private Button filterButton;

    // Filter parameters
    private int pageNumber = 0;
    private String searchTerm;
    private Double minPrice;
    private Double maxPrice;
    private Long selectedCategoryId;
    private Long selectedEventTypeId;
    private Boolean showAvailable = false;
    private Boolean showUnavailable = false;
    private Boolean isAvailable;
    private Boolean showVisible = false;
    private Boolean showInvisible = false;
    private Boolean isVisible;
    private final List<GetProductResponse> products = new ArrayList<>();
    private List<GetSolutionCategoryResponse> categories = new ArrayList<>();
    private List<GetEventTypeResponse> eventTypes = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        productService = HttpUtils.getProductService();
        categoryService = HttpUtils.getSolutionCategoryService();
        eventTypeService = HttpUtils.getEventTypeService();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_overview, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupSearchAndFilter();
        loadReferenceData();
        loadProducts();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.products_recycler_view);
        searchEditText = view.findViewById(R.id.search_edit_text);
        filterButton = view.findViewById(R.id.filter_button);
        FloatingActionButton fabCreateProduct = view.findViewById(R.id.fab_create_product);

        // Only show create button for logged in business owners
        if (AuthUtils.getToken(requireContext()) == null ||
            !AuthUtils.getUserRoles(requireContext()).contains(UserRoles.BusinessOwner)) {
            fabCreateProduct.setVisibility(View.GONE);
        }

        fabCreateProduct.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_product_overview_to_product_creation);
        });
    }

    private void setupRecyclerView() {
        adapter = new ProductsAdapter(getContext(), products);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Set the click listener using this fragment since it implements the interface
        adapter.setOnProductClickListener(this);
    }

    private void setupSearchAndFilter() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchTerm = s.toString().trim();
                if (searchTerm.isEmpty()) {
                    searchTerm = null;
                }
                pageNumber = 0;
                loadProducts();
            }
        });

        filterButton.setOnClickListener(v -> showFilterDialog());
    }

    private void loadReferenceData() {
        // Load categories
        categoryService.getAcceptedCategories().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Collection<GetSolutionCategoryResponse>> call, @NonNull Response<Collection<GetSolutionCategoryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = new ArrayList<>(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Collection<GetSolutionCategoryResponse>> call, @NonNull Throwable t) {
            }
        });

        // Load event types
        eventTypeService.getAllEventTypes().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Collection<GetEventTypeResponse>> call, @NonNull Response<Collection<GetEventTypeResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventTypes = new ArrayList<>(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Collection<GetEventTypeResponse>> call, @NonNull Throwable t) {
            }
        });
    }

    private void loadProducts() {
        Long businessOwnerId = AuthUtils.getUserId(requireContext());

        int pageSize = 10;
        Call<PagedResponse<GetProductResponse>> call = productService.filterProductsByBusinessOwner(
                businessOwnerId,
                searchTerm,
                selectedCategoryId,
                selectedEventTypeId,
                minPrice,
                maxPrice,
                isAvailable,
                isVisible,
                false, // isDeleted - only show non-deleted products
                pageNumber,
                pageSize
        );

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PagedResponse<GetProductResponse>> call, @NonNull Response<PagedResponse<GetProductResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PagedResponse<GetProductResponse> pageResponse = response.body();
                    if (pageNumber == 0) {
                        products.clear();
                    }
                    products.addAll(pageResponse.getContent());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), R.string.failed_to_load_products, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PagedResponse<GetProductResponse>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), getString(R.string.error_loading_products), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFilterDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_product_filter, null);

        // Initialize filter controls
        EditText minPriceEditText = dialogView.findViewById(R.id.min_price_edit_text);
        EditText maxPriceEditText = dialogView.findViewById(R.id.max_price_edit_text);
        Spinner categorySpinner = dialogView.findViewById(R.id.category_spinner);
        Spinner eventTypeSpinner = dialogView.findViewById(R.id.event_type_spinner);
        CheckBox availableCheckBox = dialogView.findViewById(R.id.available_checkbox);
        CheckBox unavailableCheckBox = dialogView.findViewById(R.id.unavailable_checkbox);
        CheckBox visibleCheckBox = dialogView.findViewById(R.id.visible_checkbox);
        CheckBox invisibleCheckBox = dialogView.findViewById(R.id.invisible_checkbox);
        Button applyButton = dialogView.findViewById(R.id.apply_filter_button);
        Button clearButton = dialogView.findViewById(R.id.clear_filter_button);

        // Setup spinners
        setupCategorySpinner(categorySpinner);
        setupEventTypeSpinner(eventTypeSpinner);

        // Set current filter values
        if (minPrice != null) minPriceEditText.setText(String.valueOf(minPrice));
        if (maxPrice != null) maxPriceEditText.setText(String.valueOf(maxPrice));
        availableCheckBox.setChecked(showAvailable);
        unavailableCheckBox.setChecked(showUnavailable);
        visibleCheckBox.setChecked(showVisible);
        invisibleCheckBox.setChecked(showInvisible);

        applyButton.setOnClickListener(v -> {
            applyFilters(minPriceEditText, maxPriceEditText, categorySpinner, eventTypeSpinner,
                    availableCheckBox, unavailableCheckBox, visibleCheckBox, invisibleCheckBox);
            bottomSheetDialog.dismiss();
        });

        clearButton.setOnClickListener(v -> {
            clearFilters();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(dialogView);
        bottomSheetDialog.show();
    }

    private void setupCategorySpinner(Spinner categorySpinner) {
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add(getString(R.string.all_categories));
        for (GetSolutionCategoryResponse category : categories) {
            categoryNames.add(category.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void setupEventTypeSpinner(Spinner eventTypeSpinner) {
        List<String> eventTypeNames = new ArrayList<>();
        eventTypeNames.add(getString(R.string.all_event_types));
        for (GetEventTypeResponse eventType : eventTypes) {
            eventTypeNames.add(eventType.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, eventTypeNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventTypeSpinner.setAdapter(adapter);
    }

    private void applyFilters(EditText minPriceEditText, EditText maxPriceEditText, Spinner categorySpinner,
                              Spinner eventTypeSpinner, CheckBox availableCheckBox, CheckBox unavailableCheckBox,
                              CheckBox visibleCheckBox, CheckBox invisibleCheckBox) {
        // Parse price filters
        String minPriceText = minPriceEditText.getText().toString().trim();
        String maxPriceText = maxPriceEditText.getText().toString().trim();

        minPrice = minPriceText.isEmpty() ? null : Double.parseDouble(minPriceText);
        maxPrice = maxPriceText.isEmpty() ? null : Double.parseDouble(maxPriceText);

        // Parse category filter
        int categoryPosition = categorySpinner.getSelectedItemPosition();
        selectedCategoryId = (categoryPosition == 0) ? null : categories.get(categoryPosition - 1).getId();

        // Parse event type filter
        int eventTypePosition = eventTypeSpinner.getSelectedItemPosition();
        selectedEventTypeId = (eventTypePosition == 0) ? null : eventTypes.get(eventTypePosition - 1).getId();

        // Parse availability filters
        showAvailable = availableCheckBox.isChecked();
        showUnavailable = unavailableCheckBox.isChecked();

        if (showAvailable && !showUnavailable) {
            isAvailable = true;
        } else if (!showAvailable && showUnavailable) {
            isAvailable = false;
        } else {
            isAvailable = null;
        }

        // Parse visibility filters
        showVisible = visibleCheckBox.isChecked();
        showInvisible = invisibleCheckBox.isChecked();

        if (showVisible && !showInvisible) {
            isVisible = true;
        } else if (!showVisible && showInvisible) {
            isVisible = false;
        } else {
            isVisible = null;
        }

        pageNumber = 0;
        loadProducts();
    }

    private void clearFilters() {
        minPrice = null;
        maxPrice = null;
        selectedCategoryId = null;
        selectedEventTypeId = null;
        showAvailable = false;
        showUnavailable = false;
        isAvailable = null;
        showVisible = false;
        showInvisible = false;
        isVisible = null;
        pageNumber = 0;
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
        navController.navigate(R.id.action_product_overview_to_product_edit, bundle);
    }

    @Override
    public void onDeleteProduct(GetProductResponse product) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_product)
                .setMessage(getString(R.string.are_you_sure_you_want_to_update_this_product))
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteProduct(product.getId()))
                .setNegativeButton(R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteProduct(Long productId) {
        productService.logicalDeleteProduct(productId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), getString(R.string.product_deleted_successfully), Toast.LENGTH_SHORT).show();
                    loadProducts(); // Refresh the list
                } else {
                    Toast.makeText(getContext(), getString(R.string.failed_to_delete_product), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), getString(R.string.failed_to_delete_product), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
