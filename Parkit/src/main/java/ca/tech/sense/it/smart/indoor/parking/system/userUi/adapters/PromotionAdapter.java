package ca.tech.sense.it.smart.indoor.parking.system.userUi.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.Promotion;

public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.PromotionViewHolder> {
    private List<Promotion> promotionList;

    public PromotionAdapter(List<Promotion> promotionList) {
        this.promotionList = promotionList;
    }

    @NonNull
    @Override
    public PromotionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_promotions, parent, false);
        return new PromotionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromotionViewHolder holder, int position) {
        Promotion promotion = promotionList.get(position);
        holder.title.setText(promotion.getTitle());
        holder.description.setText(promotion.getDescription());
        holder.discount.setText(MessageFormat.format("{0}%", promotion.getDiscount()));

        // Check if promo code is already generated
        if (promotion.getPromoCode() == null || promotion.getPromoCode().isEmpty()) {
            String promoCode = UUID.randomUUID().toString().substring(0, 8);
            promotion.setPromoCode(promoCode);
            DatabaseReference promotionsRef = FirebaseDatabase.getInstance().getReference("Promotions");
            promotionsRef.child(promotion.getId()).setValue(promotion);
        }

        holder.promoCode.setText(promotion.getPromoCode());
        holder.claimButton.setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("promo_code", promotion.getPromoCode());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(view.getContext(), view.getContext().getString(R.string.promo_code_copied), Toast.LENGTH_SHORT).show();

            // Add promotion to user's promotions sub-node
            String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(); // Get the current user's UID
            DatabaseReference userPromotionsRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("promotions");
            userPromotionsRef.child(promotion.getId()).setValue(promotion);
        });

    }

    @Override
    public int getItemCount() {
        return promotionList.size();
    }

    static class PromotionViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, discount, promoCode;
        Button claimButton;

        public PromotionViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.PromotionTitle);
            description = itemView.findViewById(R.id.PromotionDescription);
            discount = itemView.findViewById(R.id.PromotionDiscount);
            promoCode = itemView.findViewById(R.id.PromotionCode);
            claimButton = itemView.findViewById(R.id.ClaimPromoButton);
        }
    }
}
