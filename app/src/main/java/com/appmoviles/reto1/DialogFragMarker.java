package com.appmoviles.reto1;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class DialogFragMarker extends AppCompatDialogFragment {

    private EditText et_nombre_marcador;

    private DialogFragMarkerActions listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_marker_frag,null);
        builder.setView(view)
                .setTitle("Agregar Marcador")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.agregarMarcador(et_nombre_marcador.getText().toString());
                    }
                });

        et_nombre_marcador = view.findViewById(R.id.et_nombre_marcador);

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (DialogFragMarkerActions) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+" DialogFragMarkerActions");
        }
    }

    public interface DialogFragMarkerActions{
        void agregarMarcador(String nombreMarcador);
    }


}