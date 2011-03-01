package org.cnpjcatcher.main;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class Main {
	
	private JFrame mainFrame = new JFrame("CNPJ Catcher");
	private JFrame popup	 = new JFrame("Resultado");
	
	private JLabel cnpjLabel = new JLabel("Informe o CNPJ");
		
	private JTextField cnpjTextField = new JTextField();
	private JTextField captchaTextField = new JTextField();
	
	private JButton enviarJButton = new JButton("Enviar");
	private JButton trocarImagemJButton = new JButton("Trocar imagem");
	
	private String cookie = "";
	private String session = "";
	
	public static void main(String[] args) {
		new Main().initMainFrame();
		
	}
	
	public void initMainFrame() {
		getReceitaSessionData();
		
		GridLayout gl = new GridLayout(3, 2);
		JFrame jf = new JFrame("CNPJ Catcher");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setLayout(gl);
		jf.setSize(400, 250);
		
		jf.add(cnpjLabel);
		cnpjTextField.setSize(20, 100);
		cnpjTextField.setText("31456338000186"); // remover
		jf.add(cnpjTextField);
		
		final JLabel imageLabel = getCaptchaLabel();
		imageLabel.setSize(200, 90);
		jf.add(imageLabel);
		jf.add(captchaTextField);

		/* classe anônima para o evento do btn Enviar */
		enviarJButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				popup = new JFrame("Resultado " + new Date());
				popup.setSize(640, 480);
				JEditorPane textArea = new JEditorPane();
				textArea.setSize(620, 460);
				//textArea.setContentType("text/html");
				textArea.setText(getData());
				JScrollPane scrollPane = new JScrollPane(textArea);
				scrollPane.setSize(620, 460);
				popup.getContentPane().add(scrollPane, BorderLayout.CENTER);
				popup.setVisible(true);
			}
		});
		
		/* classe anônima para o evento do btn Trocar Imagem */
		trocarImagemJButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ImageIcon ii = new ImageIcon(getCnpjCaptcha());
				imageLabel.setIcon(ii);
			}
		});
		
		jf.add(enviarJButton);
		jf.add(trocarImagemJButton);
		
		jf.setVisible(true);
	}
	
	public void getReceitaSessionData() {
		try {
			HttpURLConnection http = (HttpURLConnection) new URL("http://www.receita.fazenda.gov.br/pessoajuridica/cnpj/cnpjreva/Cnpjreva_Solicitacao2.asp")
									 .openConnection();
			http.setDoInput(true);
			http.setDoOutput(true);
			http.setInstanceFollowRedirects(true);
			http.connect();
			
			Map<String, List<String>> reqMap = http.getHeaderFields();
			StringBuilder sb = new StringBuilder();
			for(Map.Entry<String, List<String>> e : reqMap.entrySet()) {
				for(String i : e.getValue()) {
					sb.append(e.getKey())
					  .append(": ")
					  .append(i)
					  .append("\n");
					
					if(e.getKey() != null && e.getKey().startsWith("Set-Cookie")) {
						session = i.split(";")[0].trim();
					}
				}
			}
			
			JOptionPane.showMessageDialog(null, session);
			
			
			http.disconnect();
		} catch(IOException ex) {
			System.out.println("Não foi possível conectar");
			ex.printStackTrace();
		}
	}
	
	/**
	 * Retorna o array de bytes da imagem
	 * @param is
	 * @return byte[]
	 */
	public byte[] getImageByteArray(InputStream is) {
		try {
			byte[] b1 = new byte[99999];
			int b;
			int i = 0;
			while((b = is.read()) != -1) {
				b1[i] = (byte) b;
				i++;
			}
			byte[] b2 = Arrays.copyOf(b1, i);
			return b2;
		} catch(IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Retorna o objeto imagem correspondente ao captcha
	 * @return Image
	 */
	public Image getCnpjCaptcha() {
		try {
			URL imgUrl = new URL("http://www.receita.fazenda.gov.br/scripts/srf/intercepta/captcha.aspx?opt=image");
			HttpURLConnection http = (HttpURLConnection) imgUrl.openConnection();
			http.connect();
			
			/* resgata os valores do cookie */
//			StringBuilder sb = new StringBuilder();
//			String aux;
//			int i = 0;
//			while((aux = http.getHeaderField(i)) != null) {
//				sb.append(aux)
//				  .append("\n");
//				i++;
//			}
//
			/* cria a imagem a partir dos bytes da requisição http */
			Image img = java.awt.Toolkit.getDefaultToolkit().createImage(getImageByteArray(http.getInputStream()));
			cookie = getCookieCaptcha(http.getHeaderField("Set-Cookie"));
			System.out.println("Cookie: " + cookie);
			http.disconnect();
			return img;
		} catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Retorna um JLabel contendo a imagem do captcha
	 * @return JLabel
	 */
	public JLabel getCaptchaLabel() {
		JLabel jl = new JLabel();
		Icon icon = new ImageIcon(getCnpjCaptcha());
		jl.setIcon(icon);
		return jl;
	}
	
	/**
	 * Retira par cookie do source
	 * @param src String de origem
	 * @return String contendo o par=valor do cookie
	 */
	public String getCookieCaptcha(String src) {
		String[] parts = src.split(";");
		for(String item : parts) {
			if(item.trim().startsWith("cookieCaptcha")) {
				return item.trim();
			}
		}
		return null;
	}
	
	/**
	 * Exemplo do post:  
	 * origem=comprovante&cnpj=31456338000186&idLetra=hyhb&idSom=&submit1=Consultar&search_type=cnpj
	 * 
	 * Retorna os dados lidos da receita
	 * @return String
	 */
	public String getData() {
		try {
			String urlStr = "http://www.receita.fazenda.gov.br/pessoajuridica/cnpj/cnpjreva/valida.asp";
//			String urlStr = "http://localhost:8080/CaptchaServerTest/CaptchaServlet";
			URL url = new URL(urlStr);
			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			
			//http.setRequestProperty("Cookie", "flag=1; cookieCaptcha" + URLEncoder.encode(cookie.substring(cookie.indexOf("=")), "UTF-8"));
			http.setRequestProperty("Host",	"www.receita.fazenda.gov.br");
			http.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.2.13) Gecko/20101206 Ubuntu/10.10 (maverick) Firefox/3.6.13");
			http.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			http.setRequestProperty("Accept-Language", "en-us,en;q=0.5");
			http.setRequestProperty("Accept-Encoding", "gzip,deflate");
			http.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
			http.setRequestProperty("Keep-Alive", "115");
			http.setRequestProperty("Connection", "keep-alive");
			http.setRequestProperty("Referer", "http://www.receita.fazenda.gov.br/pessoajuridica/cnpj/cnpjreva/Cnpjreva_Solicitacao2.asp?cnpj=" + cnpjTextField.getText());

			http.setRequestProperty("Cookie", "flag=1; " + cookie + session);
			http.setRequestMethod("POST");
			http.setInstanceFollowRedirects(true);
			http.setDoInput(true);
			http.setDoOutput(true);
			
			String params = URLEncoder.encode("origem",      "ISO-8859-1")  + "=" + URLEncoder.encode("comprovante",			  "ISO-8859-1")
		    		+ "&" + URLEncoder.encode("cnpj",        "ISO-8859-1")  + "=" + URLEncoder.encode(cnpjTextField.getText(),	  "ISO-8859-1") 
		    		+ "&" + URLEncoder.encode("idLetra",     "ISO-8859-1")  + "=" + URLEncoder.encode(captchaTextField.getText(), "ISO-8859-1")
		    		+ "&" + URLEncoder.encode("idSom",       "ISO-8859-1")  + "=" + URLEncoder.encode("", 						  "ISO-8859-1")
		    		+ "&" + URLEncoder.encode("submit1", 	 "ISO-8859-1")  + "=" + URLEncoder.encode("Consultar",				  "ISO-8859-1")
		    		+ "&" + URLEncoder.encode("search_type", "ISO-8859-1")  + "=" + URLEncoder.encode("cnpj",		 			  "ISO-8859-1");
			

			http.setRequestProperty("Content-Length", "" + Integer.toString(params.getBytes().length));
			System.out.println("[" + params.length() + "] " + params);
			http.setUseCaches(false);
			http.connect();
			

			DataOutputStream dos = new DataOutputStream(http.getOutputStream());
			dos.writeBytes(params);
			dos.flush();
			dos.close();
			
			System.out.println("Method: "  		 + http.getRequestMethod());
			System.out.println("Content: " 		 + http.getContent().toString());
			System.out.println("Content Type: "  + http.getContentType().toString());
			System.out.println("Response Code: " + http.getResponseCode());
			
			for(Map.Entry<String, List<String>> e : http.getHeaderFields().entrySet()) {
				System.out.println(e.getKey());
				for(String i : e.getValue()) {
					System.out.println("\t" + i);
				}
			}

//			OutputStreamWriter osw = new OutputStreamWriter(http.getOutputStream());
//			osw.write(params);
//			osw.flush();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream()));
			String aux;
			StringBuilder inputStr = new StringBuilder();
			while((aux = br.readLine()) != null) {
				inputStr.append(aux)
						.append("\n");
			}
//			osw.close();
			br.close();
			http.disconnect();
			return inputStr.toString();
		} catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
