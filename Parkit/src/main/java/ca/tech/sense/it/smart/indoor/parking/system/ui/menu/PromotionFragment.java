package ca.tech.sense.it.smart.indoor.parking.system.ui.menu;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.Promotion;
import ca.tech.sense.it.smart.indoor.parking.system.ui.adapters.PromotionAdapter;

public class PromotionFragment extends Fragment {
    private RecyclerView recyclerView;
    private PromotionAdapter adapter;
    private List<Promotion> promotionList = new ArrayList<>();
    private CollectionReference collectionReference;


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

        adapter.notifyDataSetChanged();

         collectionReference = FirebaseFirestore.getInstance().collection("Promotions");
         fetchPromotions();

        return view;
    }

    private void fetchPromotions() {
        collectionReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                promotionList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Promotion promotion = document.toObject(Promotion.class);
                    promotionList.add(promotion);
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "Failed to load promotions.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
