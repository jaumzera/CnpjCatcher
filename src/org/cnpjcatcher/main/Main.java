package org.cnpjcatcher.main;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

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
				popup.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
	
	/**
	 * Recebe os dados da receita
	 */
	public void getReceitaSessionData() {
		try {
			HttpURLConnection http = (HttpURLConnection) new URL("http://www.receita.fazenda.gov.br/pessoajuridica/cnpj/cnpjreva/Cnpjreva_Solicitacao2.asp")
									 .openConnection();
			http.setRequestProperty("Cookie", "flag=0");
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
			
			JOptionPane.showMessageDialog(null, session + "\n" + sb.toString());
			
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
			http.setRequestProperty("Cookie", session + "; cookieCaptcha=;");
			http.connect();
			
			System.out.println("getCnpjCaptcha(): session enviada para a gerar a imagem: " + session);
			
			cookie = getCookieCaptcha(http.getHeaderField("Set-Cookie"));
			
			System.out.println("getCnpjCaptcha(): Set-Cookie: " + http.getHeaderField("Set-Cookie"));
			System.out.println("\n\n\nHeader: " + getRequestHeaders(http.getHeaderFields()));
			
			/* cria a imagem a partir dos bytes da requisição http */
			Image img = java.awt.Toolkit.getDefaultToolkit().createImage(getImageByteArray(http.getInputStream()));
			System.out.println("getCnpjCaptcha(): cookie: " + cookie);
			
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
		System.out.println("Full-cookie: " + src);
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
//			String urlStr = "http://localhost:8080/CaptchaServerTest/CaptchaServlet";
			URL url = new URL("http://www.receita.fazenda.gov.br/pessoajuridica/cnpj/cnpjreva/valida.asp");
			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			http.setRequestMethod("POST");
			
			String cookieProperty = "flag=1; " + session + "; " + cookie;
			//String cookieProperty = "flag=1; " + "ASPSESSIONIDAQRCBSCQ=OEODKMPCOOKIBFJFAILAOMDK;" + " 	cookieCaptcha=v8K1lINaHPNbJPY5n3gwwaLjaSPI5UU4vpwxbqDbi08=";
			
			// --- teste com dados do browser
			String bcookie  = "ASPSESSIONIDCSRCCSDQ=JHCNLAHDKDHEMFNEEBEFIKBF" + "; ";
			String bsession = "	cookieCaptcha=8UpRMTBPcqzDYOJdsqWCp+iVu32WJb0+nueNqqJeNxk=" + "; ";
//			String cookieProperty = "flag=1; " + bcookie + bsession;
			// --- fim teste com dados do browser
			
			
			System.out.println(cookieProperty);
			http.setRequestProperty("Cookie", cookieProperty);
			
			http.setInstanceFollowRedirects(false);
			http.setDoInput(true);
			http.setDoOutput(true);
			http.setUseCaches(false);
			
			System.out.println("[" + captchaTextField.getText() + "]");
			
			String params = URLEncoder.encode("origem",      "UTF-8")  + "=" + URLEncoder.encode("comprovante",			      "UTF-8")
		    		+ "&" + URLEncoder.encode("cnpj",        "UTF-8")  + "=" + URLEncoder.encode(cnpjTextField.getText(),	  "UTF-8") 
		    		+ "&" + URLEncoder.encode("idLetra",     "UTF-8")  + "=" + URLEncoder.encode(captchaTextField.getText(),  "UTF-8")
		    		+ "&" + URLEncoder.encode("idSom",       "UTF-8")  + "=" + URLEncoder.encode("", 						  "UTF-8")
		    		+ "&" + URLEncoder.encode("submit1", 	 "UTF-8")  + "=" + URLEncoder.encode("Consultar",				  "UTF-8")
		    		+ "&" + URLEncoder.encode("search_type", "UTF-8")  + "=" + URLEncoder.encode("cnpj",		 			  "UTF-8");

			http.setRequestProperty("Content-Length", "" + Integer.toString(params.getBytes().length));
			http.connect();
			System.out.println("Method: "  		   + http.getRequestMethod());

			DataOutputStream dos = new DataOutputStream(http.getOutputStream());
			dos.writeBytes(params);
			dos.flush();
			
			System.out.println("getCnpjCaptcha(): Response Cookie: " + http.getHeaderField("Set-Cookie"));
			System.out.println("getCnpjCaptcha(): Method: "  		   + http.getRequestMethod());
			System.out.println("getCnpjCaptcha(): Content: " 		   + http.getContent().toString());
			System.out.println("getCnpjCaptcha(): Content Type: "    + http.getContentType().toString());
			System.out.println("getCnpjCaptcha(): Response Code: "   + http.getResponseCode());
			
			BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream()));
			String aux;
			StringBuilder inputStr = new StringBuilder();
			while((aux = br.readLine()) != null) {
				inputStr.append(aux)
						.append("\n");
			}
			br.close();
			dos.close();
			
			// System.out.println(getRequestHeaders(http.getHeaderFields()));
			
			http.disconnect();
			return getCnpjData(inputStr.toString());
		} catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Mostra o header da requisição
	 * @param headerMap
	 * @return String 
	 */
	public String getRequestHeaders(Map<String, List<String>> headerMap) {
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, List<String>> e : headerMap.entrySet()) {
			for(String i : e.getValue()) {
				sb.append(e.getKey())
				  .append(": ")
				  .append(i)
				  .append("\n");
			}
		}
		return sb.toString();
	}
	
	/**
	 * Recebe o link de redirecionamento e busca os dados da empresa
	 * @param source
	 * @return String
	 */
	public String getCnpjData(String source) {
		try {
			source = source.substring(source.indexOf("\"") + 1, source.lastIndexOf("\""));
			System.out.println(source);
			String url = "http://www.receita.fazenda.gov.br/pessoajuridica/cnpj/cnpjreva/" + source.replaceAll("&amp;", "&");
			HttpURLConnection http = (HttpURLConnection) new URL(url).openConnection();
			http.setDoInput(true);
			http.setDoOutput(true);
			http.setInstanceFollowRedirects(true);
			http.setRequestProperty("Cookie", "flag=1; " + cookie + "; " + session);
			http.connect();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream()));
			String aux;
			StringBuilder inputStr = new StringBuilder();
			while((aux = br.readLine()) != null) {
				inputStr.append(aux)
						.append("\n");
			}
			br.close();
			return inputStr.toString();
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		return source;
	}
}
