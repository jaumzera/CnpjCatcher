package org.cnpjcatcher.main;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contém os métodos para trazer o captcha, sessão e os dados empresa 
 * 
 * @author jaumzera
 * @version Mar 09th, 2011
 */
public class CnpjCatcher {
	
	/* cookie de sessão do site da receita.fazenda.gov.br */
	private String sessionId;
	
	/* cookie do captcha da imagem da receita.fazenda.gov.br */
	private String cookieCaptcha;
	
	/* classe que filtra os dados retornados por receita.fazenda.gov.br */
	private DataFilter dataFilter;
	
	/**
	 * Construtor
	 */
	public CnpjCatcher() {
		/* carrega os dados de sessão da receita.fazenda.gov.br */
		loadReceitaSessionData();
	}
		
	/**
	 * Retira par cookie do source
	 * @param src String de origem
	 * @return String contendo o par=valor do cookie
	 */
	private String getCookieCaptcha(String src) {
		String[] parts = src.split(";");
		for(String item : parts) {
			if(item.trim().startsWith("cookieCaptcha")) {
				return item.trim();
			}
		}
		return null;
	}
	
	/**
	 * Retorna o array de bytes da imagem
	 * @param is
	 * @return byte[]
	 */
	private byte[] getImageByteArray(InputStream is) {
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
	 * Retorna uma conexão Http a partir de uma URL
	 * @param url String representando a URL	
	 * @return HttpURLConnection
	 */
	private HttpURLConnection getHttpUrlConnection(String url) {
		try {
			return (HttpURLConnection) new URL(url).openConnection();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Pega os dados de sessão do site da Receita
	 * @return String contendo o cookie correspondente ao session
	 * 		   gerado pelo site receita.fazenda.gov.br
	 */
	private void loadReceitaSessionData() {
		try {
			HttpURLConnection http = getHttpUrlConnection("http://www.receita.fazenda.gov.br/pessoajuridica/cnpj/cnpjreva/Cnpjreva_Solicitacao2.asp");
			http.setRequestProperty("Cookie", "flag=0");
			http.setInstanceFollowRedirects(true);
			http.connect();
			
			Map<String, List<String>> reqMap = http.getHeaderFields();
			StringBuilder sb = new StringBuilder();
			for(Map.Entry<String, List<String>> e : reqMap.entrySet()) {
				for(String i : e.getValue()) {
					if(e.getKey() != null && e.getKey().startsWith("Set-Cookie") && i.contains("SESSION")) {
						sessionId = i.split(";")[0].trim();
					}
				}
			}
			
			http.disconnect();
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Retorna o array de bytes correspondente à imagem captcha 
	 * do site receita.fazenda.gov.br e armazena o cookie enviado
	 * na variável cookieCaptcha
	 * 
	 * @return byte[] correspondente à imagem
	 */
	public byte[] getCaptchaImageBytes() {
		if(sessionId == null) {
			throw new RuntimeException("Não há id de sessão");
		}

		try {
			HttpURLConnection http = getHttpUrlConnection("http://www.receita.fazenda.gov.br/scripts/srf/intercepta/captcha.aspx?opt=image");
			http.setRequestProperty("Cookie", sessionId + "; cookieCaptcha=;");
			http.connect();
			cookieCaptcha = getCookieCaptcha(http.getHeaderField("Set-Cookie"));
			byte[] imageByteArray = getImageByteArray(http.getInputStream());
			http.disconnect();
			return imageByteArray;
		} catch(IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Exemplo do post:  
	 * 		origem=comprovante&cnpj=31456338000186&idLetra=hyhb&idSom=&submit1=Consultar&search_type=cnpj
	 * 
	 * Retorna os dados lidos da receita
	 * @return String
	 */
	public void postData(String captcha, String cnpj) {
		try {
			/* conecta e configura o header HTTP */
			HttpURLConnection http = getHttpUrlConnection("http://www.receita.fazenda.gov.br/pessoajuridica/cnpj/cnpjreva/valida.asp");
			http.setRequestMethod("POST");
			String cookieProperty = "flag=1; " + sessionId + "; " + cookieCaptcha;
			http.setRequestProperty("Cookie", cookieProperty);
			http.setInstanceFollowRedirects(false);
			http.setDoInput(true);
			http.setDoOutput(true);
			http.setUseCaches(false);
			
			String params = URLEncoder.encode("origem",      "UTF-8")  + "=" + URLEncoder.encode("comprovante",			      "UTF-8")
		    		+ "&" + URLEncoder.encode("cnpj",        "UTF-8")  + "=" + URLEncoder.encode(cnpj,	  					  "UTF-8") 
		    		+ "&" + URLEncoder.encode("idLetra",     "UTF-8")  + "=" + URLEncoder.encode(captcha,  					  "UTF-8")
		    		+ "&" + URLEncoder.encode("idSom",       "UTF-8")  + "=" + URLEncoder.encode("", 						  "UTF-8")
		    		+ "&" + URLEncoder.encode("submit1", 	 "UTF-8")  + "=" + URLEncoder.encode("Consultar",				  "UTF-8")
		    		+ "&" + URLEncoder.encode("search_type", "UTF-8")  + "=" + URLEncoder.encode("cnpj",		 			  "UTF-8");

			http.setRequestProperty("Content-Length", "" + Integer.toString(params.getBytes().length));
			http.connect();

			/* faz o POST */
			DataOutputStream dos = new DataOutputStream(http.getOutputStream());
			dos.writeBytes(params);
			dos.flush();
			dos.close();

			/* recebe os dados e armazena em um StringBuffer a página retornada */
			/* NOTE: a página retornada, neste caso, é uma página de redirecionamento que 
			 * o HttpURLConnection não consegue seguir por si só. O método getCnpjData
			 * retira a URL retornada dessa página e faz uma outra requisição ao servidor
			 * receita.fazenda.gov.br para então ter acesso ao cartão de informações do
			 * CNPJ especificado */
			BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream()));
			String aux;
			StringBuilder inputStr = new StringBuilder();
			while((aux = br.readLine()) != null) {
				inputStr.append(aux).append("\n");
			}
			br.close();
			http.disconnect();
			dataFilter = new DataFilter(getCnpjData(inputStr.toString()));
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("A requisição retornou um erro: " + ex.getMessage());
		}
	}
	
	/**
	 * Recebe a página com o link de redirecionamento e busca os dados da empresa
	 * @param source
	 * @return String
	 */
	public String getCnpjData(String source) {
		try {
			/* Retira somente a URL do endereço href="..." */
			source = source.substring(source.indexOf("\"") + 1, source.lastIndexOf("\""));
			
			HttpURLConnection http = getHttpUrlConnection("http://www.receita.fazenda.gov.br/pessoajuridica/cnpj/cnpjreva/" 
					+ source.replaceAll("&amp;", "&"));
			http.setDoInput(true);
			http.setDoOutput(true);
			http.setInstanceFollowRedirects(true);
			http.setRequestProperty("Cookie", "flag=1; " + cookieCaptcha + "; " + sessionId);
			http.connect();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream(), 
					Charset.forName("ISO-8859-1")));
			String aux;
			StringBuilder inputStr = new StringBuilder();
			while((aux = br.readLine()) != null) {
				inputStr.append(aux)
						.append("\n");
			}
			br.close();
			http.disconnect();
			return inputStr.toString();
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		return source;
	}
	
	/**
	 * Retorna um Map com os campos retornados pelo site receita.fazenda.gov.br
	 * no formato: {nomeDoCampo=valor}.
	 * 
	 * @param fieldList lista de campos que serão consultados
	 * @return Map<String, String> contendo os valores associados à lista
	 * 		   especificada
	 */
	public Map<String, String> getCnpjDataMap(List<String> fieldList) {
		if(dataFilter == null) {
			throw new RuntimeException("Não existem dados no filtro");
		}
		
		Map<String, String> resultMap = new HashMap<String, String>();
		for(String item : fieldList) {
			resultMap.put(item, dataFilter.getInfo(item));
		}
		
		return resultMap;
	}
}
