package ca.tech.sense.it.smart.indoor.parking.system.ui.menu;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.Promotion;
import ca.tech.sense.it.smart.indoor.parking.system.network.BaseNetworkFragment;
import ca.tech.sense.it.smart.indoor.parking.system.ui.adapters.PromotionAdapter;
import ca.tech.sense.it.smart.indoor.parking.system.utility.PromotionHelper;

public class PromotionFragment extends BaseNetworkFragment {
    private RecyclerView recyclerView;
    private PromotionAdapter adapter;
    private final List<Promotion> promotionList = new ArrayList<>();

    // Views for "No Promotions" message
    private ImageView imgNoPromotions;
    private TextView tvNoPromotions;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_promotion, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up adapter
        adapter = new PromotionAdapter(promotionList);
        recyclerView.setAdapter(adapter);

        // Initialize "No Promotions" views
        imgNoPromotions = view.findViewById(R.id.imgNoPromotions);
        tvNoPromotions = view.findViewById(R.id.tvNoPromotions);

        // Save hardcoded promotions to Firebase (if they don't exist already)
        PromotionHelper.saveHardcodedPromotionsToFirebase();

        // Copy promotions to each user's promotions node
        PromotionHelper.copyPromotionsToUsers();

        // Sync promotions with users
        PromotionHelper.syncPromotionsWithUsers();

        fetchPromotions();

        return view;
    }

    public void fetchPromotions() {
        DatabaseReference promotionsRef;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get the current user's UID

        promotionsRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("promotions");

        promotionsRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                promotionList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Promotion promotion = snapshot.getValue(Promotion.class);
                    // Only add promotions that are not used (used == false)
                    if (promotion != null && !promotion.isUsed()) {
                        promotionList.add(promotion);
                    }
                }

                // Check if there are promotions to display
                if (promotionList.isEmpty()) {
                    // Show "No Promotions" message and hide RecyclerView
                    recyclerView.setVisibility(View.GONE);
                    imgNoPromotions.setVisibility(View.VISIBLE);
                    tvNoPromotions.setVisibility(View.VISIBLE);
                } else {
                    // Show RecyclerView and hide "No Promotions" message
                    recyclerView.setVisibility(View.VISIBLE);
                    imgNoPromotions.setVisibility(View.GONE);
                    tvNoPromotions.setVisibility(View.GONE);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), getString(R.string.failed_to_load_promotions), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
