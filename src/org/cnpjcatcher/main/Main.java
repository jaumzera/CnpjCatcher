package org.cnpjcatcher.main;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

/**
 * testando egt eclipse mendingo...rsrsrs
 * @author tiagovaz
 *
 */

public class Main {
	
	private JFrame mainFrame = new JFrame("CNPJ Catcher");
	private JFrame popup	 = new JFrame("Resultado");
	
	private JLabel cnpjLabel = new JLabel("Informe o CNPJ");
		
	private JTextField cnpjTextField = new JTextField();
	private JTextField captchaTextField = new JTextField();
	
	private JButton enviarJButton = new JButton("Enviar");
	private JButton trocarImagemJButton = new JButton("Trocar imagem");
	
	private CnpjCatcher cnpjCatcher;
	
	
	public static void main(String[] args) {
		new Main().initMainFrame();
	}
	
	public void initMainFrame() {
		cnpjCatcher = new CnpjCatcher();
		
		GridLayout gl = new GridLayout(3, 2);
		JFrame jf = new JFrame("CNPJ Catcher");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//mainFrame.add
		jf.setLayout(gl);
		jf.setSize(400, 250);
		
		jf.add(cnpjLabel);
		cnpjTextField.setSize(20, 100);
		cnpjTextField.setText("12906174000105"); // remover
		jf.add(cnpjTextField);
		
		final JLabel imageLabel = getCaptchaLabel();
		imageLabel.setSize(200, 90);
		jf.add(imageLabel);
		jf.add(captchaTextField);

		/* classe anônima para o evento do btn Enviar */
		enviarJButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(popup == null) {
					popup = new JFrame("Resultado " + cnpjTextField.getText() + " "  + new Date());
				} else {
					popup.setTitle("Resultado " + cnpjTextField.getText() + " " + new Date());
				}
				
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
				ImageIcon ii = new ImageIcon(cnpjCatcher.getCaptchaImageBytes());
				imageLabel.setIcon(ii);
				mainFrame.repaint();
			}
		});
		
		jf.add(enviarJButton);
		jf.add(trocarImagemJButton);
		jf.setVisible(true);
	}
	
	/**
	 * Retorna um JLabel contendo a imagem do captcha
	 * @return JLabel
	 */
	public JLabel getCaptchaLabel() {
		JLabel jl = new JLabel();
		Icon icon = new ImageIcon(cnpjCatcher.getCaptchaImageBytes());
		jl.setIcon(icon);
		return jl;
	}
	
	/**
	 * Faz o post e joga as informações nos campos do 
	 * form de apresentação
	 */
	public String getData() {
		cnpjCatcher.postData(captchaTextField.getText(), cnpjTextField.getText());
		List<String> fieldList = new ArrayList<String>(
				Arrays.asList(new String[] {
						"NOME EMPRESARIAL", "TÍTULO DO ESTABELECIMENTO (NOME DE FANTASIA)",
						"NÚMERO DE INSCRIÇÃO", "COMPROVANTE DE INSCRIÇÃO E DE SITUAÇÃO CADASTRAL",
						"CÓDIGO E DESCRIÇÃO DA ATIVIDADE ECONÔMICA PRINCIPAL", 
						"CÓDIGO E DESCRIÇÃO DAS ATIVIDADES ECONÔMICAS SECUNDÁRIAS", 
						"CÓDIGO E DESCRIÇÃO DA NATUREZA JURÍDICA", "LOGRADOURO", "NÚMERO", 
						"COMPLEMENTO", "CEP", "BAIRRO/DISTRITO", "UF", "SITUAÇÃO CADASTRAL", 
						"DATA DA SITUAÇÃO CADASTRAL", "MOTIVO DE SITUAÇÃO CADASTRAL", 
						"MOTIVO DE SITUAÇÃO CADASTRAL", "DATA DA SITUAÇÃO ESPECIAL"
				})
			);
		return cnpjCatcher.getCnpjDataMap(fieldList).toString();
	}
}
