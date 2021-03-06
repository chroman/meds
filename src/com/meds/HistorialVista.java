package com.meds;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

public class HistorialVista extends Activity {

	DatabaseHelper db;
	private Cursor c;
	AdaptadorTitulares adaptador;
	private int idVista;
	private String title;
	private ListView lv1;
	private ArrayList<ListItem> datos = new ArrayList<ListItem>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.historialvista);

		TextView t = (TextView) findViewById(R.id.textPrincipal);
		idVista = this.getIntent().getExtras().getInt("id");

		title = this.getIntent().getExtras().getString("cabecera");

		ImageView imagen = ((ImageView) findViewById(R.id.imagenVista));

		switch (idVista) {
		case 0:
			imagen.setImageResource(R.drawable.tipo);
			break;
		case 1:
			imagen.setImageResource(R.drawable.via);
			break;
		case 2:
			imagen.setImageResource(R.drawable.fecha);
			break;
		}

		if (idVista == 2) {
			Calendar cal = Calendar.getInstance();
			long time = Long.parseLong(title);
			cal.setTimeInMillis(time);
			SimpleDateFormat postFormater = new SimpleDateFormat(
					"dd MMMM, yyyy");
			title = postFormater.format(cal.getTime());
			t.setText(title);
			title = String.valueOf(time);
		} else
			t.setText(title);

		db = new DatabaseHelper(this);

		if (db.getCantidadDosis() > 0) {

			try {
				switch (idVista) {
				case 0:
					c = db.getByTipo(title);
					break;
				case 1:
					c = db.getByVia(title);
					break;
				case 2:
					c = db.getByFecha(title);
					break;
				}

				if (c.getCount() > 0) {

					adaptador = new AdaptadorTitulares(this);

					lv1 = (ListView) findViewById(R.id.LstOpciones);

					while (c.moveToNext()) {

						Calendar c_inicio = Calendar.getInstance();
						c_inicio.setTimeInMillis(c.getLong(1));

						Calendar c_fin = Calendar.getInstance();
						c_fin.setTimeInMillis(c.getLong(6));

						String repeticion = c.getString(3)
								+ " "
								+ this.getString(R.string.Cada)
								+ " "
								+ c.getString(2)
								+ " "
								+ this.getString(R.string.Hora)
								+ ((Integer.parseInt(c.getString(2)) == 1) ? ""
										: "s");

						datos.add(new ListItem(c.getString(0), c_inicio
								.getTime().toLocaleString(), repeticion, c
								.getString(4), c.getString(5), c_fin.getTime()
								.toLocaleString(), c.getString(7), c
								.getString(8), c.getString(9), c.getString(10),
								c.getInt(11), false));
					}
					lv1.setAdapter(adaptador);
					lv1.setClickable(true);
					lv1.setOnItemClickListener(funcionClick);
				} else {
					((ListView) findViewById(R.id.LstOpciones))
							.setVisibility(View.GONE);
					((LinearLayout) findViewById(R.id.avisoHistorial))
							.setVisibility(View.VISIBLE);
				}
			} finally {
				if (c != null) {
					c.close();
				}
				db.getReadableDatabase().close();
			}
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (c != null) {
			c.close();
		}
		if (db != null) {
			db.close();
		}
	}

	private OnItemClickListener funcionClick = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			datos.get(position).setVisible(!datos.get(position).isVisible());
			lv1.setAdapter(adaptador);
		}
	};

	public void cancelarAlarma(int id) {
		db.eliminaMedicamento(id);
		db.eliminarDosis(id);
		db.close();

		Toast.makeText(this, this.getString(R.string.dosisEliminada),
				Toast.LENGTH_LONG).show();
	}

	@SuppressWarnings("rawtypes")
	class AdaptadorTitulares extends ArrayAdapter implements OnClickListener {
		RelativeLayout LLExpandir;
		Activity context;

		@SuppressWarnings("unchecked")
		AdaptadorTitulares(Activity context) {
			super(context, R.layout.listitem, datos);
			this.context = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = context.getLayoutInflater();
			View item = inflater.inflate(R.layout.listitem, null);

			((Button) item.findViewById(R.id.botonItem)).getBackground()
					.setColorFilter(0xFFFFE25B, PorterDuff.Mode.MULTIPLY);

			Button btn = (Button) item.findViewById(R.id.botonItem);
			btn.setOnClickListener(new OnItemClickListener(position));

			TextView lblTitulo = (TextView) item.findViewById(R.id.tvNombre);
			lblTitulo.setText(datos.get(position).getNombre());

			TextView lblSubtitulo = (TextView) item
					.findViewById(R.id.tvFechaInicio);
			lblSubtitulo.setText(datos.get(position).getFechainicio());

			TextView lblHora = (TextView) item.findViewById(R.id.tvRepeticion);
			lblHora.setText(datos.get(position).getRepeticion());

			TextView tipo = (TextView) item.findViewById(R.id.tvTipo);
			tipo.setText(datos.get(position).getTipo());

			TextView via = (TextView) item.findViewById(R.id.tvVia);
			via.setText(datos.get(position).getVia());

			TextView fecha = (TextView) item.findViewById(R.id.tvFechafin);
			fecha.setText(datos.get(position).getFechafin());

			String persona = datos.get(position).getPersona();
			if (!persona.equals("")) {
				((LinearLayout) item.findViewById(R.id.personaLayout))
						.setVisibility(View.VISIBLE);
				((TextView) item.findViewById(R.id.tvPersona)).setText(persona);
			}

			String doctor = datos.get(position).getDoctor();
			if (!doctor.equals("")) {
				((LinearLayout) item.findViewById(R.id.doctorLayout))
						.setVisibility(View.VISIBLE);
				((TextView) item.findViewById(R.id.tvDoctor)).setText(doctor);
			}

			String farmacia = datos.get(position).getFarmacia();
			if (!farmacia.equals("")) {
				((LinearLayout) item.findViewById(R.id.farmaciaLayout))
						.setVisibility(View.VISIBLE);
				((TextView) item.findViewById(R.id.tvFarmacia))
						.setText(farmacia);
			}

			String nota = datos.get(position).getNota();
			if (!nota.equals("")) {
				((LinearLayout) item.findViewById(R.id.notaLayout))
						.setVisibility(View.VISIBLE);
				((TextView) item.findViewById(R.id.tvNota)).setText(nota);
			}

			LLExpandir = (RelativeLayout) item.findViewById(R.id.LLExpandir);
			LLExpandir
					.setVisibility(datos.get(position).isVisible() ? View.VISIBLE
							: View.GONE);

			return (item);
		}

		class OnItemClickListener implements OnClickListener {
			int position;

			public OnItemClickListener(int pos) {
				this.position = pos;
			}

			@SuppressWarnings("unchecked")
			public void onClick(View v) {
				{
					cancelarAlarma(datos.get(position).getDosisID());
					adaptador.remove(adaptador.getItem(position));
					adaptador.notifyDataSetChanged();

					Cursor c = null;
					switch (idVista) {
					case 0:
						c = db.getByTipo(title);
						break;
					case 1:
						c = db.getByVia(title);
						break;
					case 2:
						c = db.getByFecha(title);
						break;
					}
					db.close();

					if (c.getCount() < 1) {

						((ListView) findViewById(R.id.LstOpciones))
								.setVisibility(View.GONE);
						((LinearLayout) findViewById(R.id.avisoHistorial))
								.setVisibility(View.VISIBLE);

					}
					c.close();

				}
			}
		}

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub

		}

	}

}
