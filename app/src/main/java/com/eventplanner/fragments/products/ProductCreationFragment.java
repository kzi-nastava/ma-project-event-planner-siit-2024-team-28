package com.eventplanner.fragments.products;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.CheckBox;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventplanner.R;
import com.eventplanner.adapters.products.ProductImageAdapter;

import com.eventplanner.model.constants.UserRoles;
import com.eventplanner.model.enums.SolutionStatus;
import com.eventplanner.model.requests.products.CreateProductRequest;
import com.eventplanner.model.requests.products.CreateProductWithPendingCategoryRequest;
import com.eventplanner.model.requests.products.UpdateProductRequest;
import com.eventplanner.model.requests.solutionCategories.CreatePendingCategoryRequest;
import com.eventplanner.model.responses.eventTypes.GetEventTypeResponse;
import com.eventplanner.model.responses.products.GetProductResponse;
import com.eventplanner.model.responses.solutionCateogries.GetSolutionCategoryResponse;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.HttpUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductCreationFragment extends Fragment {
    private final List<String> base64Images = new ArrayList<>();
    private EditText nameEditText, descriptionEditText, priceEditText, discountEditText, customCategoryEditText;
    private CheckBox visibilityCheckBox, availabilityCheckBox;
    private Spinner categorySpinner;
    private Button selectImagesButton, submitButton, deleteButton;
    private CheckBox customCategoryCheckBox;
    private TextView imageCountText;
    private RecyclerView imageRecyclerView;
    private ProductImageAdapter imageAdapter;
    private boolean isEditMode = false;
    private Long productId;
    private static final int IMAGE_PICK_CODE = 1000;
    private LinearLayout eventTypesContainer;
    private List<Long> selectedEventTypeIds = new ArrayList<>();
    private List<GetEventTypeResponse> eventTypes = new ArrayList<>();
    private boolean isReadOnly = false;
    private Long selectedCategoryId = null;
    private final List<GetSolutionCategoryResponse> solutionCategories = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_creation, container, false);
        
        initializeViews(view);
        setupFormSubmission();

        // Check if editing existing product
        Bundle args = getArguments();
        if (args != null && args.containsKey("productId")) {
            isEditMode = true;
            productId = args.getLong("productId");
            loadProduct(productId);
        }

        // Check if user is logged in and is business owner
        if (AuthUtils.getToken(requireContext()) == null) {
            Toast.makeText(getContext(), R.string.please_log_in_to_create_products, Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).navigateUp();
            return view;
        }

        if (!AuthUtils.getUserRoles(requireContext()).contains(UserRoles.BusinessOwner)) {
            Toast.makeText(getContext(), R.string.only_business_owners_can_create_edit_products, Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).navigateUp();
            return view;
        }

        loadSolutionCategories();
        loadEventTypes();
        setupValidation();

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
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < solutionCategories.size()) {
                    selectedCategoryId = solutionCategories.get(position).getId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategoryId = null;
            }
        });

        customCategoryCheckBox = view.findViewById(R.id.checkbox_custom_category);
        customCategoryEditText = view.findViewById(R.id.editText_custom_category);
        selectImagesButton = view.findViewById(R.id.button_select_images);
        submitButton = view.findViewById(R.id.button_create_product);
        deleteButton = view.findViewById(R.id.button_delete_product);
        imageCountText = view.findViewById(R.id.text_image_count);
        imageRecyclerView = view.findViewById(R.id.image_recycler_view);
        eventTypesContainer = view.findViewById(R.id.event_types_container);

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
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_images)), IMAGE_PICK_CODE);
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
            nameEditText.setError(getString(R.string.name_is_required));
            return false;
        }
        if (name.length() < 3) {
            nameEditText.setError(getString(R.string.name_must_be_at_least_3_characters));
            return false;
        }
        nameEditText.setError(null);
        return true;
    }

    private boolean validateDescription() {
        String description = descriptionEditText.getText().toString().trim();
        if (description.isEmpty()) {
            descriptionEditText.setError(getString(R.string.description_is_required));
            return false;
        }
        if (description.length() < 10) {
            descriptionEditText.setError(getString(R.string.description_must_be_at_least_10_characters));
            return false;
        }
        descriptionEditText.setError(null);
        return true;
    }

    private boolean validatePrice() {
        String priceStr = priceEditText.getText().toString().trim();
        if (priceStr.isEmpty()) {
            priceEditText.setError(getString(R.string.price_is_required));
            return false;
        }
        try {
            double price = Double.parseDouble(priceStr);
            if (price < 0.01) {
                priceEditText.setError(getString(R.string.price_must_be_at_least_0_01));
                return false;
            }
        } catch (NumberFormatException e) {
            priceEditText.setError(getString(R.string.invalid_price_format));
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
                    discountEditText.setError(getString(R.string.discount_cannot_be_negative));
                    return false;
                }
                if (discount > 99) {
                    discountEditText.setError(getString(R.string.discount_cannot_exceed_99));
                    return false;
                }
            } catch (NumberFormatException e) {
                discountEditText.setError(getString(R.string.invalid_discount_format));
                return false;
            }
        }
        discountEditText.setError(null);
        return true;
    }

    private void loadSolutionCategories() {
        HttpUtils.getSolutionCategoryService().getAcceptedCategories().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Collection<GetSolutionCategoryResponse>> call, @NonNull Response<Collection<GetSolutionCategoryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    solutionCategories.clear();
                    solutionCategories.addAll(response.body());

                    List<String> names = new ArrayList<>();
                    for (GetSolutionCategoryResponse c : solutionCategories) {
                        names.add(c.getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, names);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    categorySpinner.setAdapter(adapter);

                    // Preselect first category if not in edit mode
                    if (!isEditMode && !solutionCategories.isEmpty()) {
                        selectedCategoryId = solutionCategories.get(0).getId();
                    }

                    // Preselect in edit mode
                    if (isEditMode && productId != null) {
                        for (int i = 0; i < solutionCategories.size(); i++) {
                            if (solutionCategories.get(i).getId().equals(selectedCategoryId)) {
                                categorySpinner.setSelection(i);
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Collection<GetSolutionCategoryResponse>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), R.string.failed_to_load_categories, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProduct(Long productId) {
        HttpUtils.getProductService().getProductById(productId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GetProductResponse> call, @NonNull Response<GetProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GetProductResponse product = response.body();
                    selectedEventTypeIds = product.getEventTypeIds() != null ? new ArrayList<>(product.getEventTypeIds()) : new ArrayList<>();
                    populateEventTypeCheckboxes();

                    // Check if current user is the owner of this product
                    Long currentUserId = AuthUtils.getUserId(requireContext());
                    if (currentUserId == null || !currentUserId.equals(product.getBusinessOwnerId())) {
                        makeFormReadOnly();
                        Toast.makeText(getContext(), R.string.you_can_only_edit_your_own_products, Toast.LENGTH_SHORT).show();
                    }

                    populateForm(product);

                    // Disable category selection in edit mode
                    categorySpinner.setEnabled(false);
                    customCategoryCheckBox.setEnabled(false);
                    customCategoryEditText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetProductResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), R.string.error_loading_product, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeFormReadOnly() {
        isReadOnly = true;

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
            imageCountText.setText(R.string.no_images_selected);
            imageRecyclerView.setVisibility(View.GONE);
        } else {
            imageCountText.setText(String.format(getString(R.string.d_image_s_selected), base64Images.size()));
            imageRecyclerView.setVisibility(View.VISIBLE);
            if (imageAdapter != null) {
                imageAdapter.updateImages(base64Images);
            }
        }
    }

    private void loadEventTypes() {
        HttpUtils.getEventTypeService().getActiveEventTypes().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Collection<GetEventTypeResponse>> call, @NonNull Response<Collection<GetEventTypeResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventTypes = new ArrayList<>(response.body());
                    populateEventTypeCheckboxes();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Collection<GetEventTypeResponse>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), getString(R.string.failed_to_load_event_types), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateEventTypeCheckboxes() {
        eventTypesContainer.removeAllViews();

        for (GetEventTypeResponse eventType : eventTypes) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(eventType.getName());
            checkBox.setChecked(selectedEventTypeIds.contains(eventType.getId()));
            checkBox.setEnabled(!isReadOnly);

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && !selectedEventTypeIds.contains(eventType.getId())) {
                    selectedEventTypeIds.add(eventType.getId());
                } else if (!isChecked) {
                    selectedEventTypeIds.remove(eventType.getId());
                }
            });

            eventTypesContainer.addView(checkBox);
        }
    }

    private void createProduct() {
        if (!validateForm()) {
            return;
        }

        boolean hasCategorySelected = selectedCategoryId != null;
        boolean hasCustomCategory = customCategoryCheckBox.isChecked() &&
                !customCategoryEditText.getText().toString().trim().isEmpty();

        if (!hasCategorySelected && !hasCustomCategory) {
            Toast.makeText(getContext(), R.string.please_select_or_enter_category, Toast.LENGTH_SHORT).show();
            return;
        }

        if (hasCategorySelected && hasCustomCategory) {
            Toast.makeText(getContext(), R.string.category_or_custom_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (hasCustomCategory) {
            createProductWithCustomCategory();
        } else {
            createProductWithExistingCategory();
        }
    }

    private void createProductWithExistingCategory() {
        CreateProductRequest request = new CreateProductRequest(
                nameEditText.getText().toString().trim(),
                descriptionEditText.getText().toString().trim(),
                Double.parseDouble(priceEditText.getText().toString()),
                Double.parseDouble(discountEditText.getText().toString()),
                base64Images,
                visibilityCheckBox.isChecked(),
                availabilityCheckBox.isChecked(),
                selectedCategoryId,
                AuthUtils.getUserId(requireContext()),
                selectedEventTypeIds
        );

        HttpUtils.getProductService().createProduct(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Long> call, @NonNull Response<Long> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), R.string.product_created_successfully, Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                } else {
                    Toast.makeText(getContext(), R.string.error_creating_product, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Long> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), R.string.error_creating_product, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createProductWithCustomCategory() {
        CreateProductWithPendingCategoryRequest request = new CreateProductWithPendingCategoryRequest(
                nameEditText.getText().toString().trim(),
                descriptionEditText.getText().toString().trim(),
                Double.parseDouble(priceEditText.getText().toString()),
                Double.parseDouble(discountEditText.getText().toString()),
                base64Images,
                visibilityCheckBox.isChecked(),
                availabilityCheckBox.isChecked(),
                customCategoryEditText.getText().toString().trim(),
                AuthUtils.getUserId(requireContext()),
                selectedEventTypeIds
        );

        HttpUtils.getProductService().createProductWithPendingCategory(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Long> call, @NonNull Response<Long> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), R.string.product_created_successfully, Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                } else {
                    Toast.makeText(getContext(), R.string.error_creating_product, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Long> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), R.string.error_creating_product, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProduct() {
        if (!validateForm()) {
            return;
        }

        // Show confirmation dialog for updates
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.update_product))
                .setMessage(getString(R.string.are_you_sure_you_want_to_update_this_product))
                .setPositiveButton(R.string.update, (dialog, which) -> performProductUpdate())
                .setNegativeButton(R.string.cancel, null)
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

        HttpUtils.getProductService().updateProduct(productId, request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), R.string.product_updated_successfully, Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                } else {
                    Toast.makeText(getContext(), getString(R.string.error_updating_product) + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), getString(R.string.error_updating_product) + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = validateName();

        if (!validateDescription()) isValid = false;
        if (!validatePrice()) isValid = false;
        if (!validateDiscount()) isValid = false;

        if (customCategoryCheckBox.isChecked() && customCategoryEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), R.string.please_enter_a_custom_category_name, Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        if (selectedEventTypeIds.isEmpty()) {
            Toast.makeText(getContext(), R.string.please_select_at_least_one_event_type, Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private void setupFormSubmission() {
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
            submitButton.setText(R.string.update_product);
        } else {
            deleteButton.setVisibility(View.GONE);
            submitButton.setText(R.string.create_product);
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_product)
                .setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_product))
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteProduct())
                .setNegativeButton(R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteProduct() {
        HttpUtils.getProductService().logicalDeleteProduct(productId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), R.string.product_deleted_successfully, Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                } else {
                    Toast.makeText(getContext(), R.string.failed_to_delete_product, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), getString(R.string.failed_to_delete_product), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    addImageToList(imageUri);
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                addImageToList(imageUri);
            }
            updateImageDisplay();
        }
    }

    private void addImageToList(Uri imageUri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();
            String base64String = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

            base64Images.add(base64String);
        } catch (Exception e) {
            Toast.makeText(getContext(), R.string.error_loading_image, Toast.LENGTH_SHORT).show();
        }
    }
}