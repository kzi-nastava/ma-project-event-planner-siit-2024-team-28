package com.eventplanner.fragments.services;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.eventplanner.R;
import com.eventplanner.adapters.solutions.ServiceListAdapter;
import com.eventplanner.model.enums.RequestStatus;
import com.eventplanner.model.responses.PagedResponse;
import com.eventplanner.model.responses.eventTypes.GetEventTypeResponse;
import com.eventplanner.model.responses.services.GetServiceResponse;
import com.eventplanner.model.responses.solutionCateogries.GetSolutionCategoryResponse;
import com.eventplanner.model.solutions.Service;
import com.eventplanner.services.EventTypeService;
import com.eventplanner.services.ServiceService;
import com.eventplanner.services.SolutionCategoryService;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.HttpUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServicesOverviewFragment extends Fragment {
    private View rootView;
    private ServiceService serviceService;
    private SolutionCategoryService categoryService;
    private EventTypeService eventTypeService;
    private int pageNumber;
    private int pageSize;
    private String searchTerm;
    private Double minPrice;
    private Double maxPrice;
    private Long selectedCategoryId;
    private Long selectedEventTypeId;
    private Boolean showAvailable = false;
    private Boolean showUnavailable = false;
    private Boolean isAvailable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceService = HttpUtils.getServiceService();
        categoryService = HttpUtils.getSolutionCategoryService();
        eventTypeService = HttpUtils.getEventTypeService();
        // on default show first page with 5 elements
        pageNumber = 0;
        pageSize = 5;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_services_overview, container, false);

        // Apply window insets to handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(rootView.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        NavController navController = Navigation.findNavController(getActivity(), R.id.fragment_nav_content_main);

        EditText searchEditText = rootView.findViewById(androidx.appcompat.R.id.search_bar);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchTerm = s.toString().trim();
                filter();
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        // set up add services button to navigate to ServiceCreationActivity
        Button addServicesButton = rootView.findViewById(R.id.button_add_service);
        addServicesButton.setOnClickListener(v -> {
            navController.navigate(R.id.nav_service_creation);
        });

        // set up filter button to show BottomSheetDialog
        Button filterButton = rootView.findViewById(R.id.button_filter_services);
        filterButton.setOnClickListener(v -> {
            Log.i("ServicesOverviewFragment", "Filter button clicked");
            setupFilterBottomSheet();
        });

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // fetching services
        fetchServices();
    }

    // function for fetching services that business owner owns
    private void fetchServices() {
        Call<PagedResponse<GetServiceResponse>> call = serviceService.getServicesByBusinessOwnerId(
                AuthUtils.getUserId(getContext()), pageNumber, pageSize);

        call.enqueue(new Callback<PagedResponse<GetServiceResponse>>() {
            @Override
            public void onResponse(Call<PagedResponse<GetServiceResponse>> call, Response<PagedResponse<GetServiceResponse>> response) {
                if (response.isSuccessful()) {
                    PagedResponse<GetServiceResponse> pagedResponse = response.body();
                    if (pagedResponse != null && pagedResponse.getContent() != null) {
                        List<GetServiceResponse> services = pagedResponse.getContent();
                        Log.i("ServicesOverviewFragment", "Services successfully fetched, number of services: " + services.size());
                        populateServiceListView(services);
                    } else {
                        Log.e("ServicesOverviewFragment", "Empty response or no content.");
                        Toast.makeText(getContext(), "You have no services.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("ServicesOverviewFragment", "Error while fetching services: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PagedResponse<GetServiceResponse>> call, Throwable t) {
                Log.e("ServiceOverviewFragment", "Network failure", t);
            }
        });
    }


    // separated code for fetching all categories and creating radio buttons in bottom sheet filter
    private void populateCategoriesFilter(View dialogView) {
        Spinner spinner = dialogView.findViewById(R.id.spinner_categories);

        Call<Collection<GetSolutionCategoryResponse>> call = categoryService.getAllSolutionCategories();
        call.enqueue(new Callback<Collection<GetSolutionCategoryResponse>>() {
            @Override
            public void onResponse(Call<Collection<GetSolutionCategoryResponse>> call, Response<Collection<GetSolutionCategoryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GetSolutionCategoryResponse> allCategories = new ArrayList<>();

                    List<String> categoryNames = new ArrayList<>();
                    categoryNames.add("Select a category..."); // placeholder so nothing is initially selected

                    for (GetSolutionCategoryResponse category : response.body()) {
                        if (category.getRequestStatus().equals(RequestStatus.ACCEPTED) && !category.getIsDeleted()) {
                            allCategories.add(category);
                            categoryNames.add(category.getName());
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            getContext(),
                            android.R.layout.simple_spinner_item,
                            categoryNames
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (position == 0) {
                                selectedCategoryId = null; // null is initially selected
                                filter();
                            } else {
                                selectedCategoryId = allCategories.get(position - 1).getId();
                                filter();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedCategoryId = null;
                        }
                    });

                } else {
                    Log.e("ServicesOverviewFragment", "Error while fetching categories: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Collection<GetSolutionCategoryResponse>> call, Throwable t) {
                Log.e("ServicesOverviewFragment", "Network failure", t);
            }
        });
    }

    // separated code for fetching event types and creating radio buttons in bottom sheet filter
    private void populateEventTypesFilter(View dialogView) {
        Spinner spinner = dialogView.findViewById(R.id.spinner_eventTypes);

        Call<Collection<GetEventTypeResponse>> call = eventTypeService.getAllEventTypes();
        call.enqueue(new Callback<Collection<GetEventTypeResponse>>() {
            @Override
            public void onResponse(Call<Collection<GetEventTypeResponse>> call, Response<Collection<GetEventTypeResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GetEventTypeResponse> allEventTypes = new ArrayList<>();
                    List<String> eventTypeNames = new ArrayList<>();

                    eventTypeNames.add("Select event type..."); // nothing is initially selected

                    for (GetEventTypeResponse eventType : response.body()) {
                        if (eventType.getIsActive()) {
                            allEventTypes.add(eventType);
                            eventTypeNames.add(eventType.getName());
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            getContext(),
                            android.R.layout.simple_spinner_item,
                            eventTypeNames
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (position == 0) {
                                selectedEventTypeId = null; // null is initially selected
                                filter();
                            } else {
                                selectedEventTypeId = allEventTypes.get(position - 1).getId();
                                filter();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedEventTypeId = null;
                        }
                    });

                } else {
                    Log.e("ServicesOverviewFragment", "Error while fetching event types: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Collection<GetEventTypeResponse>> call, Throwable t) {
                Log.e("ServicesOverviewFragment", "Network failure", t);
            }
        });
    }

    // code for making filterService request to backend
    private void filter() {
        if(showAvailable && showUnavailable)
            isAvailable = null;
        else if(showAvailable && !showUnavailable)
            isAvailable = true;
        else if(!showAvailable && showUnavailable)
            isAvailable = false;
        else
            isAvailable = null;

        Log.i("ServicesOverviewFragment", "FILTER PARAMS\nsearchTerm: " + searchTerm
                + ", minPrice: " + minPrice
                + ", maxPrice: " + maxPrice
                + ", selectedCategoryId: " + selectedCategoryId
                + ", selectedEventTypeId: " + selectedEventTypeId
                + ", isAvailable: " + isAvailable);

        Call<PagedResponse<GetServiceResponse>> call = serviceService.filterServicesByBusinessOwner(
                AuthUtils.getUserId(getContext()),  // businessOwnerId
                searchTerm,                         // solution name
                selectedCategoryId,                 // categoryId
                selectedEventTypeId,                // eventTypeId
                minPrice,                           // minPrice
                maxPrice,                           // maxPrice
                isAvailable,                        // isAvailable
                null,                               // isVisible
                false,                              // isDeleted
                pageNumber,                         // page
                pageSize                            // size
        );
        call.enqueue(new Callback<PagedResponse<GetServiceResponse>>() {
            @Override
            public void onResponse(Call<PagedResponse<GetServiceResponse>> call, Response<PagedResponse<GetServiceResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GetServiceResponse> services = response.body().getContent();
                    populateServiceListView(services);
                } else {
                    Log.e("ServiceOverviewFragment", "Error filtering services: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PagedResponse<GetServiceResponse>> call, Throwable t) {
                Log.e("ServiceOverviewFragment", "Network failure", t);
            }
        });
    }

    // creating adapter after fetching services
    private void populateServiceListView(List<GetServiceResponse> services) {
        ListView serviceListView = rootView.findViewById(R.id.serviceListView);
        ServiceListAdapter adapter = new ServiceListAdapter(getContext(), services);
        serviceListView.setAdapter(adapter);
    }

    private void setupFilterBottomSheet() {
        // setting all filter parameters to null
        minPrice = null; maxPrice = null; selectedCategoryId = null; selectedEventTypeId = null;
        showUnavailable = false; showAvailable = false; isAvailable = null;

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.bottom_sheet_filter_services, null);

        // populating filter with available categories
        populateCategoriesFilter(dialogView);
        // populating filter with available eventTypes
        populateEventTypesFilter(dialogView);

        // setting AVAILABLE UNAVAILABLE switch listeners and connecting them to filter params
        Switch switchAvailable = dialogView.findViewById(R.id.switch_show_available);
        Switch switchUnavailable = dialogView.findViewById(R.id.switch_show_unavailable);
        switchAvailable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showAvailable = isChecked;
            filter();
        });
        switchUnavailable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showUnavailable = isChecked;
            filter();
        });

        // setting MIN PRICE MAX PRICE edit text listeners and connecting them to filter params
        EditText maxPriceInput = dialogView.findViewById(R.id.maxPriceInput);
        maxPriceInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    String input = s.toString().trim();
                    if (input.isEmpty()) {
                        maxPrice = null;
                    } else {
                        maxPrice = Double.parseDouble(input);
                    }
                } catch (NumberFormatException e) {
                    maxPrice = null;
                }
                filter();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        EditText minPriceInput = dialogView.findViewById(R.id.minPriceInput);
        minPriceInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    String input = s.toString().trim();
                    if (input.isEmpty()) {
                        minPrice = null;
                    } else {
                        minPrice = Double.parseDouble(input);
                    }
                } catch (NumberFormatException e) {
                    minPrice = null;
                }
                filter();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        bottomSheetDialog.setContentView(dialogView);
        bottomSheetDialog.show();
    }

}
