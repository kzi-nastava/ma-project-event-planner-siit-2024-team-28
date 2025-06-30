package com.eventplanner.fragments.solutions;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.eventplanner.R;
import com.eventplanner.adapters.solutions.PriceListAdapter;
import com.eventplanner.databinding.FragmentPriceListBinding;
import com.eventplanner.model.responses.solutions.GetPriceListSolutionResponse;
import com.eventplanner.services.SolutionService;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.HttpUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PriceListFragment extends Fragment implements PriceListAdapter.OnPriceDiscountEditListener {
    private FragmentPriceListBinding binding;
    private Long businessOwnerId;
    private SolutionService solutionService;
    private PriceListAdapter adapter;
    private List<GetPriceListSolutionResponse> priceList;

    public PriceListFragment() {
        // Required empty public constructor
    }

    public static PriceListFragment newInstance() {
        PriceListFragment fragment = new PriceListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        businessOwnerId = AuthUtils.getUserId(getContext());
        solutionService = HttpUtils.getSolutionService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPriceListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.exportToPdf.setOnClickListener(v -> {
                exportToPdf();
        });

        fetchPriceList();
        return view;
    }

    // Function for making call to backend for fetching price list
    private void fetchPriceList() {
        solutionService.getPriceList(businessOwnerId).enqueue(new Callback<Collection<GetPriceListSolutionResponse>>() {
            @Override
            public void onResponse(Call<Collection<GetPriceListSolutionResponse>> call, Response<Collection<GetPriceListSolutionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    priceList = new ArrayList<>(response.body());
                    adapter = new PriceListAdapter(getContext(), priceList);
                    adapter.setOnPriceDiscountEditListener(PriceListFragment.this);
                    binding.priceListListView.setAdapter(adapter);
                } else {
                    Toast.makeText(getContext(), "There has been an error fetching your price list.", Toast.LENGTH_SHORT).show();
                    Log.i("PriceListFragment", "There has been an error fetching price list: " +  response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<Collection<GetPriceListSolutionResponse>> call, Throwable t) {
                Log.i("PriceListFragment", "Network failure. " + t.getMessage());
            }
        });
    }

    // Overriding function for resolving onClick on EditPriceButton inherited from PriceListAdapter
    @Override
    public void onEditPriceClick(Long solutionId, Double newPrice) {
        solutionService.updateSolutionPrice(solutionId, newPrice).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    adapter.updatePrice(solutionId, newPrice);
                    Toast.makeText(getContext(), "Price successfully updated.", Toast.LENGTH_SHORT).show();
                    Log.d("PriceListFragment", "Price successfully updated.");
                } else {
                    Toast.makeText(getContext(), "There has been an error while updating your price.", Toast.LENGTH_SHORT).show();
                    Log.e("PriceListFragment", "An error while updating price" + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Network failure.", Toast.LENGTH_SHORT).show();
                Log.e("PriceListFragment", "Network failure. " + t.getMessage());
            }
        });
    }

    // Overriding function for resolving onClick on EditPriceButton inherited from PriceListAdapter
    @Override
    public void onEditDiscountClick(Long solutionId, Double newDiscount) {
        solutionService.updateSolutionDiscount(solutionId, newDiscount).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    adapter.updateDiscount(solutionId,newDiscount);
                    Toast.makeText(getContext(), "Discount successfully updated.", Toast.LENGTH_SHORT).show();
                    Log.d("PriceListFragment", "Discount successfully updated.");
                } else {
                    Toast.makeText(getContext(), "There has been an error while updating your discount.", Toast.LENGTH_SHORT).show();
                    Log.e("PriceListFragment", "An error while updating discount" + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Network failure.", Toast.LENGTH_SHORT).show();
                Log.e("PriceListFragment", "Network failure. " + t.getMessage());
            }
        });
    }

    // Function for exporting price list data to PDF file
    // Note: WRITE_EXTERNAL_STORAGE is deprecated so I used MediaStore
    private void exportToPdf() {
        if (priceList == null || priceList.isEmpty()) {
            Toast.makeText(getContext(), "No data to export.", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        int pageWidth = 595;
        int pageHeight = 842;
        int margin = 40;
        int y = margin;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        canvas.drawText("Price List Report", margin, y, paint);

        paint.setTextSize(12);
        paint.setFakeBoldText(false);
        y += 30;

        DecimalFormat df = new DecimalFormat("0.##");

        for (GetPriceListSolutionResponse item : priceList) {
            if (y > pageHeight - margin) {
                pdfDocument.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
                page = pdfDocument.startPage(pageInfo);
                canvas = page.getCanvas();
                y = margin;
            }

            String line = "Solution: " + item.getName()
                    + " | Price: " + df.format(item.getPrice()) + "$"
                    + " | Discount: " + df.format(item.getDiscount()) + "%"
                    + " | Final: " + df.format(item.getFinalPrice()) + "$";

            canvas.drawText(line, margin, y, paint);
            y += 25;
        }

        pdfDocument.finishPage(page);

        String fileName = "price_list_" + System.currentTimeMillis() + ".pdf";

        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = requireContext().getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

            if (uri != null) {
                try (OutputStream out = requireContext().getContentResolver().openOutputStream(uri)) {
                    pdfDocument.writeTo(out);
                    Toast.makeText(getContext(), "PDF saved to Downloads: " + fileName, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getContext(), "Failed to create file URI.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to save PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        pdfDocument.close();
    }

}