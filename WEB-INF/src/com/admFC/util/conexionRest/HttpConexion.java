package com.admFC.util.conexionRest;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
public class HttpConexion {

	public final static String POST ="POST";
	public final static String GET ="GET";
	public final static String PUT = "PUT";
	public final static String PATCH ="PATCH";
	public final static String DELETE ="DELETE";
	
	/**
	 * 
	 * Este metodo solo sirve para consumir con REST JSON
	 * 
	 * @param link url a se consumida
	 * @param method metodo POST, GET, PUT, PATCH, DELETE
	 * @param json el cuerpo en json para enviar
	 * @return
	 * @throws IOException
	 */
	public ResultRest consumirREST(String link, String method, String json) throws IOException {
		
		ResultRest respuesta = new ResultRest();
		
		URL url = new URL(link);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

		urlConnection.setRequestMethod(method);

		
		if (method.compareTo(GET) != 0) {
			
			urlConnection.addRequestProperty("Content-Type", "application/json");
			urlConnection.setDoOutput(true);
			
		}else {
			
			urlConnection.setDoOutput(false);
			
		}
	   
		urlConnection.setConnectTimeout(10000);
		urlConnection.setReadTimeout(10000);

		urlConnection.addRequestProperty("Accept-Charset", "UTF-8");

		urlConnection.connect();

		if (method.compareTo(GET) != 0) {
			
			OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
			out.write(json);
			out.close();
			
		}

		respuesta.setCode(urlConnection.getResponseCode());

		StringBuffer sb = new StringBuffer();
		BufferedReader br = null;

		
		if (respuesta.getCode() < 300) {
			
			br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			
		} 
			
		if (respuesta.getCode() >= 400) {
				
			br = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
		
		}

		
		String line = null;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		br.close();

		respuesta.setMensaje(sb.toString());
		
		urlConnection.disconnect();
		
		return respuesta;
		
	}
	
}
