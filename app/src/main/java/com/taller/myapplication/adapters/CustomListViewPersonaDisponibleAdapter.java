package com.taller.myapplication.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.taller.myapplication.R;
import com.taller.myapplication.SeguimientoUsuarioDisponible;

import java.util.List;

public class CustomListViewPersonaDisponibleAdapter extends BaseAdapter {

    Context context;
    List<String> uidUsuariosDisp;
    List<String> nombreUsuariosDisp;
    List<Bitmap> imagenesUsuariosDisp;
    LayoutInflater inflater;

    public CustomListViewPersonaDisponibleAdapter(Context applicationContext,List<String> uidUsuariosDisp, List<String> nombreUsuariosDisp, List<Bitmap> imagenesUsuariosDisp) {
        this.context = applicationContext;
        this.uidUsuariosDisp = uidUsuariosDisp;
        this.nombreUsuariosDisp =  nombreUsuariosDisp;
        this.imagenesUsuariosDisp = imagenesUsuariosDisp;
        inflater = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return nombreUsuariosDisp.size();
    }

    @Override
    public Object getItem(int i) {
        return uidUsuariosDisp.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        view = inflater.inflate(R.layout.item_usuarios_disponibles, null);
        ImageView icon = (ImageView) view.findViewById(R.id.imagenItemUsuarioDisp);
        TextView names = (TextView) view.findViewById(R.id.nombreItemUsuarioDisp);
        Button btn = (Button) view.findViewById(R.id.btnItemUDI);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SeguimientoUsuarioDisponible.class);
                intent.putExtra("id_usuario_presionado",uidUsuariosDisp.get(i));
                context.startActivity(intent);
            }
        });
        Bitmap imagen = imagenesUsuariosDisp.get(i);
        if( imagen != null)
            icon.setImageBitmap(imagen);
        else
            icon.setImageResource(R.drawable.profile);
        names.setText(nombreUsuariosDisp.get(i));
        names.setTextColor(Color.BLACK);
        return view;

    }
}
