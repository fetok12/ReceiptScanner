package labProject;

import java.util.Date;
import java.util.GregorianCalendar;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.JList;
import java.awt.GridLayout;
import java.awt.Image;

import javax.swing.JLabel;
import java.awt.FlowLayout;

import static org.bytedeco.opencv.global.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.opencv.global.opencv_core.cvCreateImage;
import static org.bytedeco.opencv.global.opencv_core.cvSize;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_MEDIAN;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_THRESH_BINARY;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_THRESH_OTSU;
import static org.bytedeco.opencv.global.opencv_imgproc.cvCvtColor;
import static org.bytedeco.opencv.global.opencv_imgproc.cvResize;
import static org.bytedeco.opencv.global.opencv_imgproc.cvSmooth;
import static org.bytedeco.opencv.global.opencv_imgproc.cvThreshold;
import static org.bytedeco.opencv.helper.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.opencv.helper.opencv_imgcodecs.cvSaveImage;

import java.awt.CardLayout;
import javax.swing.SpringLayout;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.swing.JScrollPane;
import java.awt.Font;
import javax.swing.JTextArea;
import javax.swing.filechooser.*;

import org.bytedeco.opencv.opencv_core.IplImage; 

public class Project {

	private JFrame frame;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	static DefaultListModel scannedReceipt = new DefaultListModel();
	DefaultListModel search = new DefaultListModel();
	static DefaultListModel addedReceipts = new DefaultListModel();
	static int Counter = 0;
	static String Company = null;
	static String Date = null;;
	static String ReceiptNo = null;
	static String TotalCost = null;
	static String ProductForDb = null;
	static String Path = null;
	static ArrayList<String> products = new ArrayList<String>(); 
	static ArrayList<String> dateArr = new ArrayList<String>(); 
	 private static final String SQL_INSERT = "INSERT INTO \"receipt\" (\"id\",\"ReceiptNo\",\"Date\",\"Products\",\"CompanyId\",\"Total\") VALUES (DEFAULT,?,?,?,?,?) RETURNING *";
	 private static final String SQL_INSERT2 = "INSERT INTO \"company\" (\"id\",\"Company\") VALUES (DEFAULT,?) RETURNING *";
	 private static final String SQL_INSERT3 = "SELECT \"id\", \"Company\" FROM \"company\" AS \"company\" WHERE \"company\".\"Company\" = ?";
	 private static final String SQL_INSERT4 = "SELECT * FROM (SELECT * FROM \"company\") AS \"company\" LEFT OUTER JOIN \"receipt\" AS \"receipts\" ON \"company\".\"id\" = \"receipts\".\"CompanyId\"";
	 private static final String SQL_INSERT5 = "SELECT * FROM (SELECT * FROM \"company\" AS \"company\") AS \"company\" LEFT OUTER JOIN \"receipt\" AS \"receipts\" ON \"company\".\"id\" = \"receipts\".\"CompanyId\" WHERE \"company\".\"Company\" ILIKE ?";
	 private static final String SQL_INSERT6 = "SELECT * FROM (SELECT * FROM \"company\" AS \"company\") AS \"company\" LEFT OUTER JOIN \"receipt\" AS \"receipts\" ON \"company\".\"id\" = \"receipts\".\"CompanyId\" WHERE \"company\".\"Company\" ILIKE ? AND date(\"receipts\".\"Date\") = ?";
	
	/**
	 * Launch the application.
	 */
	private static IplImage upScaleImage(IplImage srcImage) {
		System.out.println("srcImage - height - " + srcImage.height()
							+ ", width - " + srcImage.width());
		IplImage destImage = cvCreateImage(
				cvSize((srcImage.width() * 200) / 100,
				(srcImage.height() * 200) / 100), srcImage.depth(),
				srcImage.nChannels());
		cvResize(srcImage, destImage);
		File f = new File(System.getProperty("user.dir") + File.separator + "receipt.jpg");
		
		System.out.println("Resized - height - " + destImage.height()
		+ ", width - " + destImage.width());
		cvSaveImage(f.getAbsolutePath(), destImage);
		return destImage;
		
		
	}
	

	private static  void cleanImageSmoothingForOCR(IplImage  srcImage)  {
		IplImage resizedImage = upScaleImage(srcImage);
		
		IplImage  destImage =  cvCreateImage(resizedImage.asCvMat().cvSize(), IPL_DEPTH_8U, 1);
		cvCvtColor(resizedImage,  destImage, CV_BGR2GRAY);
		cvSmooth(destImage,  destImage, CV_MEDIAN, 3, 0, 0, 0);
		//cvAdaptiveThreshold(destImage, destImage,  255,  CV_ADAPTIVE_THRESH_GAUSSIAN_C, CV_THRESH_BINARY,3,2);
		
		cvThreshold(destImage, destImage,  0, 255,  CV_THRESH_OTSU);
		File f = new File(System.getProperty("user.dir") + File.separator + "receipt.jpg");
		cvSaveImage(f.getAbsolutePath(), destImage);
		extractTextAndSaveToDatabase(f);
	
	}
	private static  void cleanImageSmoothingForOCR2(IplImage  srcImage)  {
		IplImage resizedImage = upScaleImage(srcImage);
		
		IplImage  destImage =  cvCreateImage(resizedImage.asCvMat().cvSize(), IPL_DEPTH_8U, 1);
		cvCvtColor(resizedImage,  destImage, CV_BGR2GRAY);
		cvSmooth(destImage,  destImage, CV_MEDIAN, 3, 0, 0, 0);
		cvThreshold(destImage, destImage,  90, 255, CV_THRESH_BINARY);
		File f = new File(System.getProperty("user.dir") + File.separator + "receipt.jpg");
		cvSaveImage(f.getAbsolutePath(), destImage);
		extractTextAndSaveToDatabase(f);
	}
	
	
	private static void extractTextAndSaveToDatabase(File  receiptImageFile) {
	      ITesseract image = new Tesseract();
	      image.setLanguage("tur");
	      image.setDatapath(System.getProperty("user.dir") + File.separator + "tessdata");
	     
	      
	     Company = null;
	     Date = null;;
	  	 ReceiptNo = null;
	  	 TotalCost = null;
	  	 ProductForDb = null;
	  	 products.clear();
	  	 dateArr.clear();
	  	
	      
	      try {
				String str = image.doOCR(receiptImageFile);

			String []myarr = str.split("\n");
			System.out.println(str);
			
			boolean pass = false;
			boolean pass2 = false;
			
			for(int i = 0; i<myarr.length; i++) {
				if(myarr[i].indexOf("A.") >= 0 || myarr[i].indexOf("A.S") >= 0 || myarr[i].indexOf("A.Ş") >= 0) {
				    if(myarr[i].contains(" ")){
				    	
				    	if(myarr[i].matches(".*[(_)^&$#@!~%+0123456789].*")) {
				    		Company= myarr[i].substring(0, myarr[i].indexOf(" ")); 
				    	
				    	}else {
				    		Company= myarr[i];
				    	}
				    	
				        continue;
				     }
			
				}
				if(myarr[i].indexOf("TAR") >= 0 || myarr[i].indexOf("TARIH") >= 0 || myarr[i].indexOf("TARİH") >= 0) {
					pass= true;	
					   Pattern p = Pattern.compile("\\d+");
				        Matcher m = p.matcher(myarr[i]);
				        while(m.find()) {
				        	dateArr.add(m.group());
				        }
				        continue;
				        
				}
				if(myarr[i].indexOf("FIŞ") >= 0 || myarr[i].indexOf("FIŞ NO") >= 0 || myarr[i].indexOf("FİŞ") >= 0 || myarr[i].indexOf("FIŞ") >= 0 || myarr[i].indexOf(" NO") >= 0 || myarr[i].indexOf("Fiş") >= 0) {
					pass2= true;	
					 Pattern p = Pattern.compile("\\d+");
				        Matcher m = p.matcher(myarr[i]);
				        while(m.find()) {
				        	ReceiptNo = m.group();
				        }
				        continue;
				        
				}
				
				if(myarr[i].indexOf("TOPLAM") >= 0 || myarr[i].indexOf("TOPL") >= 0 || myarr[i].indexOf("TOPLA") >= 0) {
					
					   Pattern p = Pattern.compile("\\d+");
				        Matcher m = p.matcher(myarr[i]);
				        while(m.find()) {
				        	if(TotalCost == null) {
				        		TotalCost = m.group();
				        	}else {
				        		TotalCost += m.group();
				        	}
				        	
				        }
				        break;
				        
				}
				if(myarr[i].contains("TOPK")) {
					continue;
				}
				if((myarr[i].contains("x") || myarr[i].contains("%") || myarr[i].contains("*")) && !myarr[i].contains("TOPL") && !myarr[i].contains("SAAT") && !myarr[i].contains("KDV") &&  !myarr[i].contains("TOPK")  &&!myarr[i].contains(" NO") && !myarr[i].contains("A.") && !myarr[i].contains("GARANT")) {
					if(pass || pass2) {
						products.add(myarr[i]);
					}		
				}
			
				
			}
			
			if(Company == null || ReceiptNo == null) {
				Counter++;
//				 final String receiptImagePathFile = receiptImageFile.getAbsolutePath();
				if(Counter != 2) {
					 IplImage receiptImage = cvLoadImage(Path);
			     cleanImageSmoothingForOCR2(receiptImage);
				}else {
					JOptionPane.showMessageDialog(null, "Fis Okuma Basarisiz Oldu ");
					Counter = 0;
				}
				
			     
				
			}else {
				scannedReceipt.removeAllElements();
				
				scannedReceipt.addElement(Company);
				scannedReceipt.addElement(dateArr);
				scannedReceipt.addElement(ReceiptNo);
				scannedReceipt.addElement(TotalCost);
				
				for(int i = 0; i< products.size(); i++) {
					scannedReceipt.addElement(products.get(i));
					
					if(ProductForDb == null) {
						ProductForDb = products.get(i);
					}else {
						ProductForDb +=  ", " + products.get(i);
					}
					
				}
				
				
				System.out.println(Company + " " + dateArr + " " + ReceiptNo + " " +  TotalCost + " "  );
				System.out.println(products);
				System.out.println(ProductForDb);
				
			}

							
			} catch (TesseractException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	public static void main(String[] args) {
		
		  try (Connection conn = DriverManager.getConnection(
	                "jdbc:postgresql://localhost:5432/receipt", "postgres", "112233")){
		

			  PreparedStatement  stmt4 = conn.prepareStatement(SQL_INSERT4);
			  ResultSet Rs3 =  stmt4.executeQuery();
				addedReceipts.removeAllElements();
		    	while(Rs3.next()) {
		    		Date date3 = Rs3.getDate("Date");
		    		Calendar calendar = new GregorianCalendar();
		    		calendar.setTime(date3);
		    		
		    		int year = calendar.get(Calendar.YEAR);
		    		int month = calendar.get(Calendar.MONTH) + 1;
		    		int day = calendar.get(Calendar.DAY_OF_MONTH);
		    	
		    		 
		    		String string = String.format("<html><b>Isletme:</b> %s/ <b>Fis No:</b> %d/ <b>Tarih:</b> %d-%d-%d/ <b>Urunler:</b> %s/ <b>Toplam:</b> %d</html>", Rs3.getString("Company"), Rs3.getInt("ReceiptNo"), day,month,year, Rs3.getString("Products"), Rs3.getInt("Total"));
		    		addedReceipts.addElement(string);
		    		System.out.println(Rs3.getInt("id")+ " "+Rs3.getString("Company") + " " + Rs3.getInt("ReceiptNo"));
						
				
					}
			  
			
			
			
		}catch(Exception ex) {
			System.out.println("asdsads");
			System.out.println(ex.getMessage());
		}
		
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Project window = new Project();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Project() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1920, 1080);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JButton btnNewButton = new JButton("Ara");
		btnNewButton.setBounds(1411, 195, 97, 40);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if(textField_1.getText().isEmpty() && textField_2.getText().isEmpty() && textField_3.getText().isEmpty()) {
					
					String CompanyText = textField.getText();
				
					  try (Connection conn = DriverManager.getConnection(
				                "jdbc:postgresql://localhost:5432/receipt", "postgres", "112233")){
						  
						  PreparedStatement  stmt = conn.prepareStatement(SQL_INSERT5);
						  stmt.setString(1, CompanyText + "%");
						  ResultSet Rs3 =  stmt.executeQuery();
						  search.removeAllElements();
					    	while(Rs3.next()) {
					    		Date date3 = Rs3.getDate("Date");
					    		Calendar calendar = new GregorianCalendar();
					    		calendar.setTime(date3);
					    		
					    		int year2 = calendar.get(Calendar.YEAR);
					    		int month2 = calendar.get(Calendar.MONTH) + 1;
					    		int day2 = calendar.get(Calendar.DAY_OF_MONTH);
					    	
					    		 
					    		String string = String.format("<html><b>Isletme:</b> %s/ <b>Fis No:</b> %d/ <b>Tarih:</b> %d-%d-%d/ <b>Urunler:</b> %s/ <b>Toplam:</b> %d</html>", Rs3.getString("Company"), Rs3.getInt("ReceiptNo"), day2,month2,year2, Rs3.getString("Products"), Rs3.getInt("Total"));
					    		search.addElement(string);
					    	
								}
						  
						  
						  
						  
					  }catch(Exception ex) {
						
							System.out.println(ex.getMessage());
				}
					
					  
				}else {
					String year = "2019";
					String month = "01";
					String day = "01";
					
					if(!textField_3.getText().isEmpty()) {
						year = textField_3.getText();
					}
					if(!textField_2.getText().isEmpty()) {
						day = textField_2.getText();
					}
					if(!textField_1.getText().isEmpty()) {
						month = textField_1.getText();
					}
					
					String Date = year + "-" + month + "-" + day;
					String CompanyText = textField.getText();
					SimpleDateFormat formatter2=new SimpleDateFormat("yyyy-MM-dd");  
					Date date = null;
					try {
						date = formatter2.parse(Date);
					} catch (ParseException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}  
					 java.sql.Date sqlDate = new java.sql.Date(date.getTime()); 
					  try (Connection conn = DriverManager.getConnection(
				                "jdbc:postgresql://localhost:5432/receipt", "postgres", "112233")){
						  
						  PreparedStatement  stmt = conn.prepareStatement(SQL_INSERT6);
						  stmt.setString(1, CompanyText + "%");
						  stmt.setDate(2, sqlDate);
						  ResultSet Rs3 =  stmt.executeQuery();
						  search.removeAllElements();
					    	while(Rs3.next()) {
					    		Date date3 = Rs3.getDate("Date");
					    		Calendar calendar = new GregorianCalendar();
					    		calendar.setTime(date3);
					    		
					    		int year2 = calendar.get(Calendar.YEAR);
					    		int month2 = calendar.get(Calendar.MONTH) + 1;
					    		int day2 = calendar.get(Calendar.DAY_OF_MONTH);
					    	
					    		 
					    		String string = String.format("<html><b>Isletme:</b> %s/ <b>Fis No:</b> %d/ <b>Tarih:</b> %d-%d-%d/ <b>Urunler:</b> %s/ <b>Toplam:</b> %d</html>", Rs3.getString("Company"), Rs3.getInt("ReceiptNo"), day2,month2,year2, Rs3.getString("Products"), Rs3.getInt("Total"));
					    		search.addElement(string);
					    		
								}
						  
						  
						  
						  
					  }catch(Exception ex) {
						
							System.out.println(ex.getMessage());
				}
					
					
					
				}
				
				//JOptionPane.showMessageDialog(null, "hello");
				//textField.setText("HELLO");
			}
		});
		
		
		
	
		frame.getContentPane().setLayout(null);

		frame.getContentPane().add(btnNewButton);
	
		
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(
			 "sample.png"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		  ImageIcon icon = new ImageIcon(img);
		  JLabel lbl = new JLabel();
		  lbl.setIcon(icon);
		  lbl.setBounds(204, 0, 331, 350);
		  frame.getContentPane().add(lbl);


		
		
	
		    

		
		
		JList list_1 = new JList(addedReceipts);
		JList list_2 = new JList(search);
	//	list_2.setFont(list_2.getFont().deriveFont(18.0f));
		list_2.setFont(new Font("Arial",Font.PLAIN,17));
		list_1.setFont(new Font("Arial",Font.PLAIN,17));
	
	
		
		
		JScrollPane scrollPane = new JScrollPane(list_2);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(23, 654, 1854, 304);
		frame.getContentPane().add(scrollPane);
		
		JScrollPane scrollPane2 = new JScrollPane(list_1);
		scrollPane2.setBounds(23, 363, 1854, 258);;
		scrollPane2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		frame.getContentPane().add(scrollPane2);
		
		textField = new JTextField();
		textField.setBounds(1284, 64, 208, 40);
		textField.setFont(textField.getFont().deriveFont(16.0f));
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		textField_1 = new JTextField();
		textField_1.setBounds(1364, 139, 46, 32);
		frame.getContentPane().add(textField_1);
		textField_1.setColumns(10);
		
		textField_2 = new JTextField();
		textField_2.setBounds(1422, 139, 46, 32);
		frame.getContentPane().add(textField_2);
		textField_2.setColumns(10);
		
		textField_3 = new JTextField();
		textField_3.setBounds(1284, 139, 73, 32);
		frame.getContentPane().add(textField_3);
		textField_3.setColumns(10);
		
		JLabel lblGun = new JLabel("Gun");
		lblGun.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblGun.setBounds(1423, 117, 56, 16);
		frame.getContentPane().add(lblGun);
		
		JLabel lblAy = new JLabel("Ay");
		lblAy.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblAy.setBounds(1375, 117, 56, 16);
		frame.getContentPane().add(lblAy);
		
		JLabel lblYil = new JLabel("Yil");
		lblYil.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblYil.setBounds(1307, 117, 56, 16);
		frame.getContentPane().add(lblYil);
		
		JLabel lblIsletmeAdi = new JLabel("Isletme Adi");
		lblIsletmeAdi.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblIsletmeAdi.setBounds(1284, 35, 98, 16);
		frame.getContentPane().add(lblIsletmeAdi);
		
		

		//scannedReceipt.addElement(" ");
		
		
		JList list = new JList(scannedReceipt);
		
		list.setFont(list.getFont().deriveFont(16.0f));
		JScrollPane scrollPane3 = new JScrollPane(list);
		scrollPane3.setBounds(566, 25, 260, 311);
		scrollPane3.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		frame.getContentPane().add(scrollPane3);
		
		JButton btnFisSec = new JButton("Fis Sec");
		btnFisSec.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				  JFileChooser fileChooser = new JFileChooser();
			        int returnValue = fileChooser.showOpenDialog(null);
			        if (returnValue == JFileChooser.APPROVE_OPTION) {
			          File selectedFile = fileChooser.getSelectedFile();
			          System.out.println(selectedFile.getPath());
			          BufferedImage img2 = null;
			          try {
			        	  
						img2 = ImageIO.read(new File(selectedFile.getPath()));
						ImageIcon icon2 = new ImageIcon(img2);
						Image image = icon2.getImage(); // transform it 
						Image newimg = image.getScaledInstance(280, 350,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way 
						icon2 = new ImageIcon(newimg); 
						lbl.setIcon(icon2);
						Path = selectedFile.getPath();
						IplImage  receiptImage = cvLoadImage(selectedFile.getPath());
				        cleanImageSmoothingForOCR(receiptImage);
						
						
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			          
			        }
			          
			}
		});
		

		  
		btnFisSec.setBounds(86, 146, 106, 40);
		frame.getContentPane().add(btnFisSec);
		
		JButton btnKaydet = new JButton("Kaydet");
		btnKaydet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				  try (Connection conn = DriverManager.getConnection(
			                "jdbc:postgresql://localhost:5432/receipt", "postgres", "112233")){
					  
					 int ReceiptNoForDB = Integer.parseInt(ReceiptNo);
					 int TotalCostForDB = Integer.parseInt(TotalCost);
					 int DayForDB = 0;
					 int MonthForDB = 0;
					 int YearForDB = 2019;
					 
					 int Id = 0;
				
						 if(dateArr.get(0).length() == 2) {
							 if(Integer.parseInt(dateArr.get(0).substring(0,1)) == 0) {
					
								 DayForDB =  Integer.parseInt(dateArr.get(0).substring(1,2));
							
								 
							 }else {
								 DayForDB =  Integer.parseInt(dateArr.get(0));
							 }
						 }else {
							 DayForDB =Integer.parseInt(dateArr.get(0));
						 }
						 if(dateArr.get(1).length() == 2) {
							 if(Integer.parseInt(dateArr.get(1).substring(0,1)) == 0) {
								 MonthForDB =  Integer.parseInt(dateArr.get(1).substring(1,2));
								 
							 }else {
								 MonthForDB =  Integer.parseInt(dateArr.get(1));
							 }
						 }else {
							 MonthForDB =  Integer.parseInt(dateArr.get(1));
						 }
						 
						 YearForDB = Integer.parseInt(dateArr.get(2));
					 

					 System.out.println(ReceiptNoForDB + " " + TotalCostForDB + " " + DayForDB + " " + MonthForDB + " " + YearForDB );
					 String sDate1=  YearForDB + "-" + MonthForDB + "-" + DayForDB; 
					 SimpleDateFormat formatter2=new SimpleDateFormat("yyyy-MM-dd");  
					 Date date=formatter2.parse(sDate1);  
					 java.sql.Date sqlDate = new java.sql.Date(date.getTime()); 
				

					 PreparedStatement stmt = conn.prepareStatement(SQL_INSERT3);
					 stmt.setString(1, Company);
					 ResultSet Rs = stmt.executeQuery();
			
					 
					 
					 
					 if (Rs.next() == false) {
						
						  PreparedStatement  stmt2 = conn.prepareStatement(SQL_INSERT2);
						  stmt2.setString(1, Company);
						  ResultSet Rs2 =  stmt2.executeQuery();
					    	while(Rs2.next()) {
								
									Id=Rs2.getInt("id");
									
							
								}
					    	
					    	  PreparedStatement  stmt3 = conn.prepareStatement(SQL_INSERT);
					    	 
					    	  stmt3.setInt(1, ReceiptNoForDB);
					    	  stmt3.setDate(2, sqlDate);
					    	  stmt3.setString(3, ProductForDb);
					    	  stmt3.setInt(4, Id);
					    	  stmt3.setInt(5, TotalCostForDB);
					    	 
							  stmt3.executeQuery();
					
							  PreparedStatement  stmt4 = conn.prepareStatement(SQL_INSERT4);
							  ResultSet Rs3 =  stmt4.executeQuery();
								addedReceipts.removeAllElements();
						    	while(Rs3.next()) {
						    		Date date3 = Rs3.getDate("Date");
						    		Calendar calendar = new GregorianCalendar();
						    		calendar.setTime(date3);
						    		
						    		int year = calendar.get(Calendar.YEAR);
						    		int month = calendar.get(Calendar.MONTH) + 1;
						    		int day = calendar.get(Calendar.DAY_OF_MONTH);
						    	
						    		 
						    		String string = String.format("<html><b>Isletme:</b> %s/ <b>Fis No:</b> %d/ <b>Tarih:</b> %d-%d-%d/ <b>Urunler:</b> %s/ <b>Toplam:</b> %d</html>", Rs3.getString("Company"), Rs3.getInt("ReceiptNo"), day,month,year, Rs3.getString("Products"), Rs3.getInt("Total"));
						    		addedReceipts.addElement(string);							
								
									}
						  
					      } else {

					        do {
					        	Id=Rs.getInt("id");
					        	System.out.println(Rs.getInt("id")+ " "+Rs.getString("Company"));
					        	
					      	 
					        	
					        } while (Rs.next());
					        
					        PreparedStatement  stmt3 = conn.prepareStatement(SQL_INSERT);
					    	 
					          stmt3.setInt(1, ReceiptNoForDB);
					          stmt3.setDate(2, sqlDate);
					    	  stmt3.setString(3, ProductForDb);
					    	  stmt3.setInt(4, Id);
					    	  stmt3.setInt(5, TotalCostForDB);
							  stmt3.executeQuery();
				
							  PreparedStatement  stmt4 = conn.prepareStatement(SQL_INSERT4);
							  ResultSet Rs3 =  stmt4.executeQuery();
							  addedReceipts.removeAllElements();
						    	while(Rs3.next()) {
						    		Date date3 = Rs3.getDate("Date");
						    		Calendar calendar = new GregorianCalendar();
						    		calendar.setTime(date3);
						    		
						    		int year = calendar.get(Calendar.YEAR);
						    		int month = calendar.get(Calendar.MONTH) + 1;
						    		int day = calendar.get(Calendar.DAY_OF_MONTH);
						    		 
						    		String string = String.format("<html><b>Isletme:</b> %s/ <b>Fis No:</b> %d/ <b>Tarih:</b> %d-%d-%d/ <b>Urunler:</b> %s/ <b>Toplam:</b> %d</html>", Rs3.getString("Company"), Rs3.getInt("ReceiptNo"), day,month,year, Rs3.getString("Products"), Rs3.getInt("Total"));
						    		addedReceipts.addElement(string);
					
								
								}
							  
							  
					      }
					 
					 
					
					
				}catch(Exception ex) {
					System.out.println("asdsads");
					System.out.println(ex.getMessage());
				}
				
			}
		});
		btnKaydet.setBounds(896, 154, 106, 32);
		frame.getContentPane().add(btnKaydet);
		
		
	}
}
