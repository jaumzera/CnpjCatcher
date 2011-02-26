package org.cnpjcatcher.main;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class Main {
	
	private JFrame mainFrame = new JFrame("CNPJ Catcher");
	private JFrame popup	 = new JFrame("Resultado");
	
	private JLabel cnpjLabel = new JLabel("Informe o CNPJ");
	private JLabel captchaLabel = new JLabel("Informe o captcha");
		
	private JTextField cnpjTextField = new JTextField();
	private JTextField captchaTextField = new JTextField();
	
	private JButton enviarJButton = new JButton("Enviar");
	private JButton trocarImagemJButton = new JButton("Trocar imagem");
	
	private String cookie = "";
	private String cookieCaptcha = "";
	
	
	public static void main(String[] args) {
		new Main().initMainFrame();
		
	}
	
	public void initMainFrame() {
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
//				popup = new JFrame("Resultado " + new Date());
//				popup.setSize(640, 480);
//				JTextArea textArea = new JTextArea();
//				textArea.setSize(620, 460);
//				textArea.setText(getData());
//				JScrollPane scrollPane = new JScrollPane(textArea);
//				scrollPane.setSize(620, 460);
//				popup.getContentPane().add(scrollPane, BorderLayout.CENTER);
//				popup.setVisible(true);
				popup = new JFrame("Resultado " + new Date());
				popup.setSize(640, 480);
				JEditorPane textArea = new JEditorPane();
				textArea.setSize(620, 460);
				textArea.setContentType("text/html");
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
			
			/* resgata os valores do cookie */
			String aux;
			int i = 0;
			while((aux = http.getHeaderField(i)) != null) {
				if(aux.startsWith("cookieCaptcha")) {
					cookieCaptcha = getCookieCaptcha(aux);
					System.out.println("cookieCaptcha=" + cookieCaptcha);
				}
				i++;
			}
			
			/* cria a imagem a partir dos bytes da requisição http */
			Image img = java.awt.Toolkit.getDefaultToolkit().createImage(getImageByteArray(http.getInputStream()));
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
	 * Exemplo do post:  
	 * cnpj	31456338000186
	 * idLetra	vjsl
     * idSom	
     * origem	comprovante
	 * search_type	cnpj
	 * submit1	Consultar
	 * 
	 * Retorna os dados lidos da receita
	 * @return String
	 */
	public String getData() {
		try {
			//String urlStr = "HTTP://www.receita.fazenda.gov.br/PessoaJuridica/CNPJ/cnpjreva/cnpjreva_solicitacao2.asp";
			String urlStr = "http://www.receita.fazenda.gov.br/pessoajuridica/cnpj/cnpjreva/valida.asp";
			//String urlStr = "http://www.receita.fazenda.gov.br/pessoajuridica/cnpj/cnpjreva/Cnpjreva_Comprovante.asp";
			URL url = new URL(urlStr);
			HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setRequestProperty("Request-Method", "POST");
			httpCon.setRequestProperty("Cookie", "flag=1; st8id_.receita.fazenda.gov.br_%2F_wlf=YXNuJTVGNDNf?tix/xc2tp0+rV+boaEK7jkm1uIcA&; cookieCaptcha=_COOKIE_CAPTCHA_"
			//httpCon.setRequestProperty("Cookie", "flag=1; st8id_.receita.fazenda.gov.br_%2F_wlf=YXNuJTVGNDNf?tix/xc2tp0+rV+boaEK7jkm1uIcA&; cookieCaptcha=_COOKIE_CAPTCHA_; ASPSESSIONIDCSTQTRBQ=OAGKNEFDMHBINJFCJKJPLNGP; ASPSESSIONIDSARTBDDC=CCBHBAMBGJLGBKCAICDPKGME; ASPSESSIONIDSATSACDC=KJJDLCGCEIDFMCMCCFAOGNMF; ASPSESSIONIDSARRACCA=BKJBBBLDPBOKNPLBGIAAKLNM"
			//httpCon.setRequestProperty("Cookie", "flag=1; cookieCaptcha=_COOKIE_CAPTCHA_"
					.replaceAll("_COOKIE_CAPTCHA_", cookieCaptcha));
			httpCon.setDoInput(true);
			httpCon.setDoOutput(true);
			
			OutputStreamWriter osw = new OutputStreamWriter(httpCon.getOutputStream());
			String params = URLEncoder.encode("cnpj",        "UTF-8") + "=" + URLEncoder.encode(cnpjTextField.getText(),	"UTF-8")
				    + "&" + URLEncoder.encode("origem",      "UTF-8") + "=" + URLEncoder.encode("comprovante",				"UTF-8")
				    + "&" + URLEncoder.encode("idField",     "UTF-8") + "=" + URLEncoder.encode(captchaTextField.getText(), "UTF-8")
				    + "&" + URLEncoder.encode("search_type", "UTF-8") + "=" + URLEncoder.encode("cnpj",						"UTF-8");
			osw.write(params);
			osw.flush();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
			String aux;
			StringBuilder inputStr = new StringBuilder();
			while((aux = br.readLine()) != null) {
				inputStr.append(aux)
						.append("\n");
			}
			osw.close();
			br.close();
			httpCon.disconnect();
			return inputStr.toString();
		} catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Recebe uma string no formato cookieCaptcha=XXXXXXXXX e retorna só o XXXXXXXXX
	 * @param s
	 * @return String
	 */
	public String getCookieCaptcha(String s) {
		String[] parts = s.split("=");
		return parts[1];
	}
	
	public URL getUrl() {
		return null;
	}
}
