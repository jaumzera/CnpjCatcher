package org.cnpjcatcher.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class Main2 {
	private URL url;

	private JFrame mainFrame = new JFrame("Consultar CNPJ");

	private JEditorPane htmlArea;
	private JScrollPane scroll;


	public static void main(String[] args) {
		try {
			URL imgUrl = new URL("http://www.receita.fazenda.gov.br/scripts/srf/intercepta/captcha.aspx?opt=image");
			//URL imgUrl = new URL("http://t1.gstatic.com/images?q=tbn:ANd9GcRi0r2NDo2VhE4h4AAWYXvTK3ojdmHyHy4wBrTyjR8T0MFRhhAl0A");
			HttpURLConnection http = (HttpURLConnection) imgUrl.openConnection();
			http.connect();
			BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream()));
			String aux;
			while((aux = br.readLine()) != null) {
				System.out.println(aux + "\n");
			}
			http.disconnect();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	public void initMainFrame() {
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setSize(800, 600);

		try {

			if(url == null) {
				url = new URL("http://www.receita.fazenda.gov.br/pessoajuridica/cnpj/cnpjreva/Cnpjreva_Solicitacao2.asp");
			}
			htmlArea = new JEditorPane();
			htmlArea.setPage(url);
		} catch(IOException ex) {
			ex.printStackTrace();
		}

		htmlArea.addHyperlinkListener(new HyperlinkListener() {

			@Override
			public void hyperlinkUpdate(HyperlinkEvent evt) {
				processHyperlinkAction(evt);
			}
		});
		
		scroll = new JScrollPane(htmlArea);
		mainFrame.getContentPane().add(scroll);
		mainFrame.setVisible(true);
	}

	public void processHyperlinkAction(HyperlinkEvent evt) {
		url = evt.getURL();
		initMainFrame();
	}
}
