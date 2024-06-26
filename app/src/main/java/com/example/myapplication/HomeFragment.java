package com.example.myapplication;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Adaptor.ImageSliderAdapter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HomeFragment extends Fragment {
    private List<byte[]> imageUrlList;
    private ViewPager viewPager;

    private RecyclerView categoryRecycler;
    private RecyclerView productRecycler;

    MyDatabaseHelper myDB;
    private UserCategoryAdaptor userCategoryAdaptor;
    private UserProductAdaptor userProductAdaptor;
    private ImageSliderAdapter imageSliderAdapter;

    ArrayList<String> categoryIds, categoryNames;
    ArrayList<byte[]> categoryImages;
    ArrayList<String> productIds, productNames, productDescriptions, productMRPs, productSPs, productcategory;
    ArrayList<byte[]> productcov_img, productselected_img;

    private final int AUTO_SLIDE_DELAY = 3000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);


        viewPager = rootView.findViewById(R.id.viewPager);
        myDB = new MyDatabaseHelper(getActivity());
        categoryRecycler = rootView.findViewById(R.id.categoryRecycler);
        productRecycler = rootView.findViewById(R.id.productRecycler);

        categoryIds = new ArrayList<>();
        categoryNames = new ArrayList<>();
        categoryImages = new ArrayList<>();
        productIds = new ArrayList<>();
        productNames = new ArrayList<>();
        productDescriptions = new ArrayList<>();
        productMRPs = new ArrayList<>();
        productSPs = new ArrayList<>();
        productcategory = new ArrayList<>();
        productcov_img = new ArrayList<>();
        productselected_img = new ArrayList<>();
        imageUrlList = new ArrayList<>();

        try {
            storeDataInArrays();
            storeDataInArraysproducts();
            storeDataInArraysForSlider();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        setupRecyclerView();

        return rootView;
    }

    private void storeDataInArraysForSlider() throws IOException {
        Cursor cursor = myDB.getSliderImagePaths();
        if (cursor != null && cursor.getCount() > 0) {
            int imagePathColumnIndex = cursor.getColumnIndex(MyDatabaseHelper.SLIDER_IMAGE);

            if (imagePathColumnIndex != -1) {
                while (cursor.moveToNext()) {
                    String imagePath = cursor.getString(imagePathColumnIndex);

                    if (imagePath != null) {
                        File f = new File(imagePath);

                        if (f.exists()) {
                            Bitmap myBitmap = BitmapFactory.decodeFile(imagePath);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            myBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] bytes = stream.toByteArray();

                            imageUrlList.add(bytes);
                        } else {
                            Log.e("Image File", "File does not exist: " + imagePath);
                        }
                    } else {
                        Log.e("Image File", "Image path is null");
                    }
                }
            }
        }
    }

    private void storeDataInArrays() throws IOException {
        Cursor cursor = myDB.viewCategoryDetails();
        if (cursor != null && cursor.getCount() > 0) {
            int imagePathColumnIndex = cursor.getColumnIndex(MyDatabaseHelper.CATEGORY_IMG_PATH);

            if (imagePathColumnIndex != -1) {
                while (cursor.moveToNext()) {
                    categoryIds.add(cursor.getString(0));
                    categoryNames.add(cursor.getString(1));

                    String imagePath = cursor.getString(imagePathColumnIndex);

                    if (imagePath != null) {
                        File f = new File(imagePath);

                        if (f.exists()) {
                            Bitmap myBitmap = BitmapFactory.decodeFile(imagePath);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            myBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] bytes = stream.toByteArray();

                            categoryImages.add(bytes);
                        } else {
                            Log.e("Image File", "File does not exist: " + imagePath);
                        }
                    } else {
                        Log.e("Image File", "Image path is null");
                    }
                }
            }
        }
    }

    void storeDataInArraysproducts() throws IOException {
        Cursor cursor = myDB.viewProductDetails();

        if (cursor != null && cursor.getCount() > 0) {
            int coverImagePathColumnIndex = cursor.getColumnIndex(MyDatabaseHelper.PRODUCT_COVER_IMAGE);

            while (cursor.moveToNext()) {
                productIds.add(cursor.getString(0));
                productNames.add(cursor.getString(1));
                productDescriptions.add(cursor.getString(2));
                productMRPs.add(cursor.getString(3));
                productSPs.add(cursor.getString(4));
                productcategory.add(cursor.getString(5));

                if (coverImagePathColumnIndex != -1) {
                    String coverImagePath = cursor.getString(coverImagePathColumnIndex);
                    if (coverImagePath != null) {
                        handleImage(coverImagePath, productcov_img);
                    }
                }
            }
        } else {
            Toast.makeText(getActivity(), "No data", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleImage(String imagePath, ArrayList<byte[]> imageList) {
        File f = new File(imagePath);
        if (f.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imagePath);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            myBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] bytes = stream.toByteArray();
            imageList.add(bytes);
        } else {
            Log.e("Image File", "File does not exist: " + imagePath);
        }
    }

    private void startAutoSlide() {
        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable update = new Runnable() {
            public void run() {
                int currentPage = viewPager.getCurrentItem();
                int nextPage = (currentPage + 1) % imageUrlList.size();
                viewPager.setCurrentItem(nextPage, true);
            }
        };
        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(update);
            }
        }, AUTO_SLIDE_DELAY, AUTO_SLIDE_DELAY);
    }


    private void setupRecyclerView() {
        imageSliderAdapter = new ImageSliderAdapter(getActivity(), imageUrlList);
        viewPager.setAdapter(imageSliderAdapter);
        startAutoSlide();

        userCategoryAdaptor = new UserCategoryAdaptor(getActivity(), categoryIds, categoryNames, categoryImages);
        categoryRecycler.setAdapter(userCategoryAdaptor);
        categoryRecycler.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        userProductAdaptor = new UserProductAdaptor(getActivity(), myDB, productIds, productNames, productDescriptions, productMRPs, productSPs, productcategory, productcov_img, productselected_img);
        productRecycler.setAdapter(userProductAdaptor);
        productRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
    }
}
