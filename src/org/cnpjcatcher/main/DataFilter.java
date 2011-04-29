package org.cnpjcatcher.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Filtra os dados retornados pela receita
 * @author jaumzera
 *
 */
public class DataFilter {
	
	private String data;

	/**
	 * Construtor
	 * @param data String contendo os dados
	 */
	public DataFilter(String data) {
		try {
			this.data = data;
			clearData();
		} catch(Exception ex) {
			this.data = "";
		}
	}
	
	/**
	 * Limpa as partes do HTML que não serão utilizados
	 */
	private void clearData() {
		data = data.substring(data.indexOf("<!-- Início Cabeçalho"), data.indexOf("Fim da tabela principal -->"));
		data = data.replaceAll("\n+|[\\s\\t]+", " ")
				   .replaceAll("<!--.+?-->", "");
		
		/* gambi_mode: on;
		 * Substitui uma das palavras "Número" para evitar conflitos */
		data = data.replaceAll("NÚMERO DE INSCRIÇÃO", "CNPJ");
		
	}
	
	/**
	 * Retorna a informação de acordo com o cabeçalho
	 * @param header cabeçalho
	 * @return String contendo a informação solicitada ou ""
	 */
	public String getInfo(String header) {
		try {
			int ini = data.indexOf(header) + header.length();
			String info = data.substring(ini, data.indexOf("</td>", ini));
			return info.replaceAll("<.+?>|[\t\n]+|[\\s]{2,}", " ").trim();
		} catch(Exception ex) {
			return "*";
		}
	}
	
	/**
	 * Main para testes
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		/* abre um arquivo local com os mesmos dados que a Receita retorna */
		FileReader fr = new FileReader("/home/jaumzera/Desktop/Test/comprovante.html");
		BufferedReader br = new BufferedReader(fr);
		String aux;
		StringBuilder sb = new StringBuilder();
		while((aux = br.readLine()) != null) {
			sb.append(aux).append("\n");
		}
		
		String html = sb.substring(sb.indexOf("<!-- Início Cabeçalho"), sb.indexOf("Fim da tabela principal -->"));
		html = html.replaceAll("\n+|[\\s\\t]+", " ")
				   .replaceAll("<!--.+?-->", "");
		
		String header = "NOME EMPRESARIAL";
		int ini = html.indexOf(header) + header.length();
		String info = html.substring(ini, html.indexOf("</td>", ini));
		System.out.println(info.replaceAll("<.+?>|[\t\n]+|[\\s]{2,}", "").trim());
		
		header = "TÍTULO DO ESTABELECIMENTO (NOME DE FANTASIA)";
		ini = html.indexOf(header) + header.length();
		info = html.substring(ini, html.indexOf("</td>", ini));
		System.out.println(info.replaceAll("<.+?>|[\t\n]+|[\\s]{2,}", " ").trim());
	}
}
