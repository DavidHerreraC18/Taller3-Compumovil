package com.taller.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.taller.myapplication.adapters.CustomListViewPersonaDisponibleAdapter;
import com.taller.myapplication.model.Usuario;
import com.taller.myapplication.services.permissionService.PermissionService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListarUsuariosDisponiblesActivity extends AppCompatActivity {

    ListView listaUsuarios;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private FirebaseDatabase database;
    private StorageReference mStorageRef;
    public  static final String TAG = "Taller3";
    public  static final String PATH_USERS="usuarios/";
    private FirebaseUser user;
    private CustomListViewPersonaDisponibleAdapter adapter;

    List<String> idUsuarios;
    List<String> nombreUsuarios;
    List<Bitmap> imagenUsuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_usuarios_disponibles);

        idUsuarios = new ArrayList<>();
        nombreUsuarios = new ArrayList<>();
        imagenUsuarios = new ArrayList<>();
        listaUsuarios = findViewById(R.id.listaUsuariosDisp);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        myRef = database.getReference(PATH_USERS);
        myRef.orderByChild("disponible").equalTo("true").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                idUsuarios.clear();
                nombreUsuarios.clear();
                imagenUsuarios.clear();
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren())
                {
                    if(!singleSnapshot.getKey().equals(user.getUid()))
                    {
                        Usuario usuarioActual = singleSnapshot.getValue(Usuario.class);
                        nombreUsuarios.add(usuarioActual.getNombreUsuario() + " " + usuarioActual.getApellidoUsuario());
                        idUsuarios.add(singleSnapshot.getKey());
                    }

                }
                for(int i = 0; i<nombreUsuarios.size();i++)
                {
                    imagenUsuarios.add(null);
                }
                int i = 0;
                for(String idUsuario: idUsuarios)
                {
                    try {
                        downloadFile(idUsuario,i);
                    } catch (IOException e) {
                        Log.w(TAG,"Problemas con lectura archivos");
                    }
                    i++;
                }
                adapter = new CustomListViewPersonaDisponibleAdapter(ListarUsuariosDisponiblesActivity.this,idUsuarios,nombreUsuarios,imagenUsuarios);
                listaUsuarios.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "error en la consulta",databaseError.toException());
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        user = mAuth.getCurrentUser();
    }

    @Override
    protected void onResume() {
        super.onResume();
        user = mAuth.getCurrentUser();
    }

    private void downloadFile(String id, int pos) throws IOException {
        File localFile = File.createTempFile("images", "jpg");
        StorageReference imageRef = mStorageRef.child("images/profile/"+id+"/fotoperfil.jpg");
        imageRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        try {
                            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(localFile));
                            imagenUsuarios.set(pos,b);
                            adapter.notifyDataSetChanged();
                        }
                        catch (FileNotFoundException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
        imageRef.getFile(localFile)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception)
                    {
                        imagenUsuarios.add(pos,null);
                    }
                });
    }




    public void toAtras(View v)
    {
        finish();
    }
}