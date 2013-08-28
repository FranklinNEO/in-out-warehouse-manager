package com.redinfo.daq.util;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import android.util.Log;

public class WriteXML {
	/**
	 * SAX方式生成XML
	 * 
	 * @param list
	 * @return
	 */
	public String orderType[] = { "IA", "IC", "ID", "IB", "OA", "OB", "OD",
			"OF", "OE" };
	public String FuncText[] = { "ProduceWareHouseIn", "PurchaseWareHouseIn",
			"AllocateWareHouseIn", "ReturnWareHouseIn", "SalesWareHouseOut",
			"ReworkWareHouseOut", "AllocateWareHouseOut", "CheckWareHouseOut",
			"DestoryWareHouseOut" };
	public String MainAction[] = { "WareHouseIn", "WareHouseIn", "WareHouseIn",
			"WareHouseIn", "WareHouseOut", "WareHouseOut", "WareHouseOut",
			"WareHouseOut", "WareHouseOut" };
	public int flag = 100;

	public String saxToXml(OutputStream output, ArrayList<String> ActDate,
			ArrayList<String> Code, ArrayList<String> Actor,
			String CorpOrderID, String ToCorpID, int flag) {
		String xmlStr = null;
		// File destDir = new File(Environment.getExternalStorageDirectory(),
		// "/RedInfo/OrderList/");
		// File CacheFile = new File(destDir, "Cache.xml");// 取得sd卡的目录及要保存的文件名

		try {
			// FileOutputStream outStream = new FileOutputStream(CacheFile);
			// 用来生成XML文件
			// 实现此接口的对象包含构建转换结果树所需的信息
			// Writer foutw = new BufferedWriter(new OutputStreamWriter(output,
			// "UTF-8"));
			output.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
			Result resultXml = new StreamResult(new OutputStreamWriter(output,
					"UTF-8"));

			// 用来得到XML字符串形式
			// 一个字符流，可以用其回收在字符串缓冲区中的输出来构造字符串
			// StringWriter writerStr = new StringWriter();
			StringWriter writerStr = new StringWriter();
			// 构建转换结果树所需的信息。

			// 创建SAX转换工厂
			SAXTransformerFactory sff = (SAXTransformerFactory) SAXTransformerFactory
					.newInstance();
			// 转换处理器，侦听 SAX ContentHandler
			// 解析事件，并将它们转换为结果树 Result
			TransformerHandler th = sff.newTransformerHandler();
			// 将源树转换为结果树
			Transformer transformer = th.getTransformer();
			// 设置字符编码
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			// 是否缩进
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			// 设置与用于转换的此 TransformerHandler 关联的 Result
			// 注：这两个th.setResult不能同时启用
			th.setResult(resultXml);
			// 创建根元素<Document>,并设置其属性为空
			th.startDocument();
			AttributesImpl attr = new AttributesImpl();
			attr.addAttribute("", "Version", "Version", "", "3.0");
			attr.addAttribute("", "xmlns:xsi", "xmlns:xsi", "",
					"http://www.w3.org/2001/XMLSchema-instance");
			th.startElement("", "Document", "Document", attr);

			// 创建一级子元素<Events>,并设置其属性
			attr.clear();
			th.startElement("", "", "Events", null);
			attr.addAttribute("", "MainAction", "MainAction", "",
					MainAction[flag]);
			attr.addAttribute("", "Name", "Name", "", FuncText[flag]);
			// th.startElement("", "", "Events", attr);
			// 创建二级子元素<DataField>,并设置其属性
			th.startElement("", "", "Event", attr);
			attr.clear();
			th.startElement("", "", "DataField", attr);
			for (int i = 0; i < Code.size(); i++) {
				// 创建三级子元素<Data>,并设置其值
				attr.clear();
				if (flag < 4) {
					if (ToCorpID.equals("-1")) {
						attr.addAttribute("", "", "FromCorpID", "FromCorpID",
								"");
					} else {

						attr.addAttribute("", "", "FromCorpID", "FromCorpID",
								ToCorpID);
					}
				} else {
					if (ToCorpID.equals("-1")) {
						attr.addAttribute("", "", "ToCorpID", "ToCorpID", "");
					} else {
						attr.addAttribute("", "", "ToCorpID", "ToCorpID",
								ToCorpID);
					}
				}
				attr.addAttribute("", "", "ActDate", "ActDate", ActDate.get(i));
				attr.addAttribute("", "", "Actor", "Actor", Actor.get(i));
				attr.addAttribute("", "", "CorpOrderID", "CorpOrderID",
						CorpOrderID);
				attr.addAttribute("", "", "Code", "Code", Code.get(i));
				th.startElement("", "", "Data", attr);
				th.endElement("", "", "Data");
			}

			th.endElement("", "", "DataField");
			th.endElement("", "", "Event");
			th.endElement("", "", "Events");
			th.endElement("", "Document", "Document");
			th.endDocument();
			xmlStr = writerStr.getBuffer().toString();
		} catch (TransformerConfigurationException e) {
			Log.e("TEST", "" + e.toString());
		} catch (SAXException e) {
			Log.e("TEST", "" + e.toString());
		} catch (Exception e) {
			Log.e("TEST", "" + e.toString());
		}
		Log.e("TEST", "生成的" + xmlStr);
		return getUTF8XMLString(xmlStr);
	}

	public static String getUTF8XMLString(String xml) {
		// A StringBuffer Object
		StringBuffer sb = new StringBuffer();
		sb.append(xml);
		String xmString = "";
		String xmlUTF8 = "";
		try {
			xmString = new String(sb.toString().getBytes("UTF-8"));
			xmlUTF8 = URLEncoder.encode(xmString, "UTF-8");
			System.out.println("utf-8 编码：" + xmlUTF8);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// return to String Formed
		return xmlUTF8;
	}
}
