package com.taller.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class InicioSesionActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    public static final String TAG = "Taller3";

    private EditText textCorreo;
    private EditText textContra;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_sesion);
        textCorreo = findViewById(R.id.usernameInicioSesion);
        textContra = findViewById(R.id.passwordInicioSesion);
        mAuth = FirebaseAuth.getInstance();
    }

    public void iniciarSesion(View view)
    {
        if(validarInicioSesion())
        {
            intentarLogIn();
        }
        else{
            textContra.setText("");
            textCorreo.setText("");
        }
    }

    public void intentarLogIn()
    {
        String correo = textCorreo.getText().toString();
        String contra = textContra.getText().toString();
        mAuth.signInWithEmailAndPassword(correo, contra)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(InicioSesionActivity.this, "Por favor verifique la información",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                    }
                });
    }

    @Override
    protected void onStart(){
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        updateUI(user);
    }

    @Override
    protected void onResume(){
        super.onResume();
        FirebaseUser user = mAuth.getCurrentUser();
        updateUI(user);
    }

    private void updateUI(FirebaseUser user){
        if(user != null){
            startActivity(new Intent(this, HomeUsuarioActivity.class));
        }else{
            textCorreo.setText("");
            textContra.setText("");
        }
    }


    public boolean validarInicioSesion()
    {
        boolean valido = true;
        String correo,contra;
        correo = textCorreo.getText().toString();
        if(TextUtils.isEmpty(correo))
        {
            textCorreo.setError("Requerido");
            valido = false;
        }
        if(!isEmailValid(correo))
        {
            Toast.makeText(this, "Ingrese un correo válido por favor",Toast.LENGTH_LONG).show();
            valido = false;
        }
        contra = textContra.getText().toString();
        if(TextUtils.isEmpty(contra))
        {
            textContra.setError("Requerido");
            valido = false;
        }

        return  valido;
    }

    private boolean isEmailValid(String email) {
        if (!email.contains("@") ||
                !email.contains(".") ||
                email.length() < 5)
            return false;
        return true;
    }

    public void toRegistro(View view)
    {
        startActivity(new Intent(this, RegistroActivity.class));
    }
}