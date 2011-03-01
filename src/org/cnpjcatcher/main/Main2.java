package org.cnpjcatcher.main;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

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
			Socket socket = new Socket("http://www.receita.fazenda.gov.br/pessoajuridica/cnpj/cnpjreva/Cnpjreva_Solicitacao2.asp", 80);
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			int i;
			while((i = dis.read()) != -1) {
				System.out.print(i);
			}
			socket.close();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
