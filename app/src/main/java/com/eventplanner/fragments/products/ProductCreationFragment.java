package com.eventplanner.fragments.products;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.CheckBox;
import android.widget.Toast;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventplanner.R;
import com.eventplanner.adapters.products.ProductImageAdapter;

import com.eventplanner.model.constants.UserRoles;
import com.eventplanner.model.enums.SolutionStatus;
import com.eventplanner.model.requests.products.CreateProductRequest;
import com.eventplanner.model.requests.products.UpdateProductRequest;
import com.eventplanner.model.requests.solutionCategories.CreatePendingCategoryRequest;
import com.eventplanner.model.responses.products.GetProductResponse;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.HttpUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;

public class ProductCreationFragment extends Fragment {
    private List<String> base64Images = new ArrayList<>();
    private EditText nameEditText, descriptionEditText, priceEditText, discountEditText, customCategoryEditText;
    private CheckBox visibilityCheckBox, availabilityCheckBox;
    private Spinner categorySpinner;
    private Button selectImagesButton, submitButton, deleteButton;
    private CheckBox customCategoryCheckBox;
    private TextView imageCountText;
    private RecyclerView imageRecyclerView;
    private ProductImageAdapter imageAdapter;
    private Long selectedCategoryId = 1L; // Default category
    private List<Long> selectedEventTypeIds = new ArrayList<>();
    private boolean isEditMode = false;
    private Long productId;
    private boolean isReadOnly = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_creation, container, false);
        
        initializeViews(view);
        setupFormSubmission(view);
        
        // Check if editing existing product
        Bundle args = getArguments();
        if (args != null && args.containsKey("productId")) {
            isEditMode = true;
            productId = args.getLong("productId");
            loadProduct(productId);
        }

        // Check if user is logged in and is business owner
        if (AuthUtils.getToken(requireContext()) == null) {
            Toast.makeText(getContext(), "Please log in to create products", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).navigateUp();
            return view;
        }

        if (!AuthUtils.getUserRoles(requireContext()).contains(UserRoles.BusinessOwner)) {
            Toast.makeText(getContext(), "Only business owners can create/edit products", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).navigateUp();
            return view;
        }
        
        return view;
    }

    private void initializeViews(View view) {
        nameEditText = view.findViewById(R.id.editText_name);
        descriptionEditText = view.findViewById(R.id.editText_description);
        priceEditText = view.findViewById(R.id.editText_price);
        discountEditText = view.findViewById(R.id.editText_discount);
        visibilityCheckBox = view.findViewById(R.id.checkbox_visible);
        availabilityCheckBox = view.findViewById(R.id.checkbox_available);
        categorySpinner = view.findViewById(R.id.spinner_category);
        customCategoryCheckBox = view.findViewById(R.id.checkbox_custom_category);
        customCategoryEditText = view.findViewById(R.id.editText_custom_category);
        selectImagesButton = view.findViewById(R.id.button_select_images);
        submitButton = view.findViewById(R.id.button_create_product);
        deleteButton = view.findViewById(R.id.button_delete_product);
        imageCountText = view.findViewById(R.id.text_image_count);
        imageRecyclerView = view.findViewById(R.id.image_recycler_view);

        // Setup custom category toggle
        customCategoryCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                customCategoryEditText.setVisibility(View.VISIBLE);
                categorySpinner.setEnabled(false);
            } else {
                customCategoryEditText.setVisibility(View.GONE);
                categorySpinner.setEnabled(true);
            }
        });
        
        // Set default values
        visibilityCheckBox.setChecked(true);
        availabilityCheckBox.setChecked(true);
        discountEditText.setText("0");
        
        // Setup image RecyclerView
        imageAdapter = new ProductImageAdapter(getContext(), base64Images);
        imageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        imageRecyclerView.setAdapter(imageAdapter);

        selectImagesButton.setOnClickListener(v -> {
            // TODO: Implement image selection
            Toast.makeText(getContext(), "Image selection not yet implemented", Toast.LENGTH_SHORT).show();
        });

        updateImageDisplay();
    }

    private void setupValidation() {
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateName();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        descriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateDescription();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        priceEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePrice();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        discountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateDiscount();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private boolean validateName() {
        String name = nameEditText.getText().toString().trim();
        if (name.isEmpty()) {
            nameEditText.setError("Name is required");
            return false;
        }
        if (name.length() < 3) {
            nameEditText.setError("Name must be at least 3 characters");
            return false;
        }
        nameEditText.setError(null);
        return true;
    }

    private boolean validateDescription() {
        String description = descriptionEditText.getText().toString().trim();
        if (description.isEmpty()) {
            descriptionEditText.setError("Description is required");
            return false;
        }
        if (description.length() < 10) {
            descriptionEditText.setError("Description must be at least 10 characters");
            return false;
        }
        descriptionEditText.setError(null);
        return true;
    }

    private boolean validatePrice() {
        String priceStr = priceEditText.getText().toString().trim();
        if (priceStr.isEmpty()) {
            priceEditText.setError("Price is required");
            return false;
        }
        try {
            double price = Double.parseDouble(priceStr);
            if (price < 0.01) {
                priceEditText.setError("Price must be at least 0.01");
                return false;
            }
        } catch (NumberFormatException e) {
            priceEditText.setError("Invalid price format");
            return false;
        }
        priceEditText.setError(null);
        return true;
    }

    private boolean validateDiscount() {
        String discountStr = discountEditText.getText().toString().trim();
        if (!discountStr.isEmpty()) {
            try {
                double discount = Double.parseDouble(discountStr);
                if (discount < 0) {
                    discountEditText.setError("Discount cannot be negative");
                    return false;
                }
                if (discount > 99) {
                    discountEditText.setError("Discount cannot exceed 99%");
                    return false;
                }
            } catch (NumberFormatException e) {
                discountEditText.setError("Invalid discount format");
                return false;
            }
        }
        discountEditText.setError(null);
        return true;
    }

    private void loadProduct(Long productId) {
        HttpUtils.getProductService().getProductById(productId).enqueue(new Callback<GetProductResponse>() {
            @Override
            public void onResponse(Call<GetProductResponse> call, Response<GetProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GetProductResponse product = response.body();

                    // Check if current user is the owner of this product
                    Long currentUserId = AuthUtils.getUserId(requireContext());
                    if (currentUserId == null || !currentUserId.equals(product.getBusinessOwnerId())) {
                        isReadOnly = true;
                        makeFormReadOnly();
                        Toast.makeText(getContext(), "You can only edit your own products", Toast.LENGTH_SHORT).show();
                    }

                    populateForm(product);

                    // Disable category selection in edit mode
                    categorySpinner.setEnabled(false);
                    customCategoryCheckBox.setEnabled(false);
                    customCategoryEditText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<GetProductResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error loading product", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeFormReadOnly() {
        nameEditText.setEnabled(false);
        descriptionEditText.setEnabled(false);
        priceEditText.setEnabled(false);
        discountEditText.setEnabled(false);

        visibilityCheckBox.setEnabled(false);
        availabilityCheckBox.setEnabled(false);
        categorySpinner.setEnabled(false);

        selectImagesButton.setEnabled(false);
        submitButton.setVisibility(View.GONE);
        deleteButton.setVisibility(View.GONE);
        customCategoryCheckBox.setEnabled(false);
        customCategoryEditText.setEnabled(false);
    }

    private void populateForm(GetProductResponse product) {
        nameEditText.setText(product.getName() != null ? product.getName() : "");
        descriptionEditText.setText(product.getDescription() != null ? product.getDescription() : "");
        priceEditText.setText(product.getPrice() != null ? String.valueOf(product.getPrice()) : "0");
        discountEditText.setText(product.getDiscount() != null ? String.valueOf(product.getDiscount()) : "0");
        visibilityCheckBox.setChecked(product.getIsVisibleForEventOrganizers() != null && product.getIsVisibleForEventOrganizers());
        availabilityCheckBox.setChecked(product.getIsAvailable() != null && product.getIsAvailable());
        
        if (product.getImagesBase64() != null) {
            base64Images.clear();
            base64Images.addAll(product.getImagesBase64());
            updateImageDisplay();
        }

        selectedEventTypeIds = product.getEventTypeIds() != null ?
            new ArrayList<>(product.getEventTypeIds()) : new ArrayList<>();
    }

    private void updateImageDisplay() {
        if (base64Images.isEmpty()) {
            imageCountText.setText("No images selected");
            imageRecyclerView.setVisibility(View.GONE);
        } else {
            imageCountText.setText(base64Images.size() + " image(s) selected");
            imageRecyclerView.setVisibility(View.VISIBLE);
            if (imageAdapter != null) {
                imageAdapter.updateImages(base64Images);
            }
        }
    }

    private void createProduct() {
        if (!validateForm()) {
            return;
        }

        if (customCategoryCheckBox.isChecked()) {
            String customCategory = customCategoryEditText.getText().toString().trim();
            if (!customCategory.isEmpty()) {
                createProductWithCustomCategory(customCategory);
            } else {
                Toast.makeText(getContext(), "Please enter a custom category name", Toast.LENGTH_SHORT).show();
            }
        } else {
            createProductWithExistingCategory();
        }
    }



    private void createProductWithExistingCategory() {
        CreateProductRequest request = CreateProductRequest.builder()
                .name(nameEditText.getText().toString().trim())
                .description(descriptionEditText.getText().toString().trim())
                .price(Double.parseDouble(priceEditText.getText().toString()))
                .discount(Double.parseDouble(discountEditText.getText().toString()))
                .imagesBase64(base64Images)
                .isVisible(visibilityCheckBox.isChecked())
                .isAvailable(availabilityCheckBox.isChecked())
                .solutionCategoryId(selectedCategoryId)
                .businessOwnerId(AuthUtils.getUserId(requireContext()))
                .eventTypeIds(selectedEventTypeIds)
                .status(SolutionStatus.ACTIVE)
                .build();

        HttpUtils.getProductService().createProduct(request).enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Product created successfully", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                } else {
                    Toast.makeText(getContext(), "Error creating product", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Toast.makeText(getContext(), "Error creating product", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createProductWithCustomCategory(String customCategoryName) {
        // First create the pending category
        CreatePendingCategoryRequest categoryRequest = new CreatePendingCategoryRequest(customCategoryName);

        HttpUtils.getSolutionCategoryService().createPendingCategory(categoryRequest).enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Long pendingCategoryId = response.body();

                    // Create product with pending status
                    CreateProductRequest request = CreateProductRequest.builder()
                            .name(nameEditText.getText().toString().trim())
                            .description(descriptionEditText.getText().toString().trim())
                            .price(Double.parseDouble(priceEditText.getText().toString()))
                            .discount(Double.parseDouble(discountEditText.getText().toString()))
                            .imagesBase64(base64Images)
                            .isVisible(visibilityCheckBox.isChecked())
                            .isAvailable(availabilityCheckBox.isChecked())
                            .businessOwnerId(AuthUtils.getUserId(requireContext()))
                            .solutionCategoryId(pendingCategoryId)
                            .eventTypeIds(selectedEventTypeIds)
                            .status(SolutionStatus.PENDING) // Set to pending
                            .build();

                    HttpUtils.getProductService().createProduct(request).enqueue(new Callback<Long>() {
                        @Override
                        public void onResponse(Call<Long> call, Response<Long> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(),
                                    "Product created with pending status. Admin will review the new category suggestion.",
                                    Toast.LENGTH_LONG).show();
                                Navigation.findNavController(requireView()).navigateUp();
                            } else {
                                Toast.makeText(getContext(), "Error creating product", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Long> call, Throwable t) {
                            Toast.makeText(getContext(), "Error creating product", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Error creating category suggestion", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Toast.makeText(getContext(), "Error creating category suggestion", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProduct() {
        if (!validateForm()) {
            return;
        }

        // Show confirmation dialog for updates
        new AlertDialog.Builder(requireContext())
                .setTitle("Update Product")
                .setMessage("Are you sure you want to update this product?\n\n" +
                           "Note: Changes will only apply to future purchases. " +
                           "Existing purchases will retain the previous product details.")
                .setPositiveButton("Update", (dialog, which) -> {
                    performProductUpdate();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performProductUpdate() {
        UpdateProductRequest request = UpdateProductRequest.builder()
                .name(nameEditText.getText().toString().trim())
                .description(descriptionEditText.getText().toString().trim())
                .price(Double.parseDouble(priceEditText.getText().toString()))
                .discount(Double.parseDouble(discountEditText.getText().toString()))
                .imagesBase64(base64Images)
                .isVisible(visibilityCheckBox.isChecked())
                .isAvailable(availabilityCheckBox.isChecked())
                .eventTypeIds(selectedEventTypeIds)
                .build();

        HttpUtils.getProductService().updateProduct(productId, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Product updated successfully", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                } else {
                    Log.e("ProductCreation", "Update failed with code: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e("ProductCreation", "Error body: " + errorBody);
                        Toast.makeText(getContext(), "Error updating product: " + response.code(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error updating product: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ProductCreation", "Update request failed", t);
                Toast.makeText(getContext(), "Error updating product: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;
        
        if (!validateName()) isValid = false;
        if (!validateDescription()) isValid = false;
        if (!validatePrice()) isValid = false;
        if (!validateDiscount()) isValid = false;
        
        if (!isEditMode && selectedCategoryId == null && !customCategoryCheckBox.isChecked()) {
            Toast.makeText(getContext(), "Please select a category or suggest a new one", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (customCategoryCheckBox.isChecked() && customCategoryEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "Please enter a custom category name", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        if (selectedEventTypeIds.isEmpty()) {
            Toast.makeText(getContext(), "Please select at least one event type", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        return isValid;
    }

    private void setupFormSubmission(View view) {
        submitButton.setOnClickListener(v -> {
            if (isEditMode) {
                updateProduct();
            } else {
                createProduct();
            }
        });

        deleteButton.setOnClickListener(v -> {
            if (isEditMode && productId != null) {
                showDeleteConfirmationDialog();
            }
        });

        // Show delete button only in edit mode and hide it initially
        if (isEditMode) {
            deleteButton.setVisibility(View.VISIBLE);
            submitButton.setText("Update Product");
        } else {
            deleteButton.setVisibility(View.GONE);
            submitButton.setText("Create Product");
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete this product?\n\n" +
                           "This will:\n" +
                           "• Hide the product from all customers\n" +
                           "• Preserve existing purchases and history\n" +
                           "• Allow you to restore it later if needed\n\n" +
                           "This action can be undone by contacting support.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteProduct();
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteProduct() {
        HttpUtils.getProductService().logicalDeleteProduct(productId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Product deleted successfully", Toast.LENGTH_SHORT).show();
                    // Navigate back to product overview
                    Navigation.findNavController(requireView()).navigateUp();
                } else {
                    Toast.makeText(getContext(), "Failed to delete product", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ProductCreation", "Failed to delete product", t);
                Toast.makeText(getContext(), "Error deleting product", Toast.LENGTH_SHORT).show();
            }
        });
    }
}