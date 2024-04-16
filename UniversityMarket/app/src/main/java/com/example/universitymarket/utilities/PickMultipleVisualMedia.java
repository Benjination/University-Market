package com.example.universitymarket.utilities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.MediaStore;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

public class PickMultipleVisualMedia extends ActivityResultContracts.PickMultipleVisualMedia {
    private int maxItems;

    public PickMultipleVisualMedia(int maxItems) {
        super(maxItems);
        this.maxItems = maxItems;
    }

    public void updateMaxItems(int newMaxItems) {
        maxItems = newMaxItems;
    }

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, @NonNull PickVisualMediaRequest input) {
        Intent intent = super.createIntent(context, input);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, maxItems);
        }
        return intent;
    }
}