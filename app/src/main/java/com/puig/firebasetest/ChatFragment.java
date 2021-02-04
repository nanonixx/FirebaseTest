package com.puig.firebasetest;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.puig.firebasetest.databinding.FragmentChatBinding;
import com.puig.firebasetest.databinding.ViewholderMensajeBinding;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@RequiresApi(api = Build.VERSION_CODES.O)
public class ChatFragment extends Fragment {
    private FragmentChatBinding binding;
    private NavController navController;
    private FirebaseAuth mAuth;
    private List<Mensaje> chat = new ArrayList<>();
    private FirebaseFirestore mDB;
    private String COLECCION_MENSAJES = "mensaje";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return (binding = FragmentChatBinding.inflate(inflater, container, false)).getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        mAuth = FirebaseAuth.getInstance();
        mDB = FirebaseFirestore.getInstance();

        ChatAdapter chatAdapter = new ChatAdapter();
        binding.chat.setAdapter(chatAdapter);

        binding.enviar.setOnClickListener(v -> {
            String texto = binding.mensaje.getText().toString();
            String fecha = LocalDateTime.now().toString();
            String email = mAuth.getCurrentUser().getEmail().toString();
            String nombre = mAuth.getCurrentUser().getDisplayName().toString();
            String photo = mAuth.getCurrentUser().getPhotoUrl().toString();

            mDB.collection("mensaje").add(new Mensaje(texto, fecha, email, nombre, photo));

            binding.mensaje.setText("");
        });

        mDB.collection("mensaje")
                .orderBy("fecha")
                .addSnapshotListener((value, error) -> {
                    chat.clear();
                    value.forEach(document -> {
                        chat.add(new Mensaje(
                                document.getString("mensaje"),
                                document.getString("fecha"),
                                document.getString("email"),
                                document.getString("nombre"),
                                document.getString("photo")
                        ));
                    });

                    chatAdapter.notifyDataSetChanged();
                    binding.chat.scrollToPosition(chat.size() - 1);
                });
    }


    class ChatAdapter extends RecyclerView.Adapter<MensajeViewHolder> {

        @NonNull
        @Override
        public MensajeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MensajeViewHolder(ViewholderMensajeBinding.inflate(getLayoutInflater(), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MensajeViewHolder holder, int position) {
            Mensaje mensaje = chat.get(position);
            Log.e("tag", "cosingas");

            holder.binding.nombre.setText(mensaje.nombre);

            if (mensaje.meme != null){
                Glide.with(requireView()).load(mensaje.meme).into(holder.binding.meme);
                holder.binding.mensaje.setVisibility(View.GONE);
                holder.binding.meme.setVisibility(View.VISIBLE);
            }else{
                holder.binding.mensaje.setText(mensaje.mensaje);
                holder.binding.mensaje.setVisibility(View.VISIBLE);
                holder.binding.meme.setVisibility(View.GONE);
            }


            holder.binding.fecha.setText(mensaje.fecha);
            Glide.with(requireView()).load(mensaje.photo).into(holder.binding.foto);

            if (mensaje.email.equals(mAuth.getCurrentUser().getEmail())) {
                holder.binding.getRoot().setGravity(Gravity.END);
            } else {
                holder.binding.getRoot().setGravity(Gravity.START);
            }
        }

        @Override
        public int getItemCount() {
            return chat.size();
        }
    }


    static class MensajeViewHolder extends RecyclerView.ViewHolder {
        ViewholderMensajeBinding binding;

        public MensajeViewHolder(@NonNull ViewholderMensajeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    ActivityResultLauncher<String> galeria = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        FirebaseStorage.getInstance().getReference("memes/*"+ UUID.randomUUID())
                .putFile(uri)
                .continueWithTask(task -> task.getResult().getStorage().getDownloadUrl())
                .addOnSuccessListener(url ->{
                    mDB.collection(COLECCION_MENSAJES).add(new Mensaje ("", LocalDateTime.now().toString(),
                            mAuth.getCurrentUser().getEmail(),
                            mAuth.getCurrentUser().getPhotoUrl().toString(),
                            url.toString()
                            ));
                })
        ;
    });
}