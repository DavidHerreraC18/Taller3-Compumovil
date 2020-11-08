package com.taller.myapplication.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.io.FileReader;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain =  true)
public class Usuario implements Parcelable {

    private String nombreUsuario;

    private String apellidoUsuario;

    private String correo;

    private String contra;

    private String identificacion;

    private Ubicacion ubicacionActual;



    protected Usuario(Parcel in) {
        nombreUsuario = in.readString();
        apellidoUsuario = in.readString();
        correo = in.readString();
        contra = in.readString();
        identificacion = in.readString();
    }

    public static final Creator<Usuario> CREATOR = new Creator<Usuario>() {
        @Override
        public Usuario createFromParcel(Parcel in) {
            return new Usuario(in);
        }

        @Override
        public Usuario[] newArray(int size) {
            return new Usuario[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(nombreUsuario);
        parcel.writeString(apellidoUsuario);
        parcel.writeString(correo);
        parcel.writeString(contra);
        parcel.writeString(identificacion);
    }
}
