package com.ats.bray;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by wuzhiyuan on 2017/3/3.
 */
@Controller
@RequestMapping("/")
public class ESClient4 extends Thread{
    private int typeVal;

    Map<String, String> exMap = new HashMap<String, String>();

    private TransportClient client;

    private static int INDEX_ID = 1;

    public Map<String, String> getExMap() {
        return exMap;
    }
    //es集群服务初始化连接
    public void init(int i) {
        try {
            this.typeVal = i;
            Settings settings = Settings.builder().put("cluster.name", "my-application").build();
            //创建client
            client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.130.191"), 9300));

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    /**
     * 读取目录下的xml文件，解析成json格式，并建立索引
     */
    public void createIndexFromDir(String pathName) {
        //遍历目录并读取xml文件
        File path = new File(pathName);
        if(path.exists()) {
            File[] files = path.listFiles();
            if(files.length == 0) {
                System.out.println("文件夹是空的！");
                return;
            }else {
                for(File f : files) {
                    if(f.isDirectory()) {
                        System.out.println("文件夹：" + f.getAbsolutePath());
                        createIndexFromDir(f.getAbsolutePath());
                    }else {
                        if(f.getAbsolutePath().endsWith(".xml"))
                        //读取xml文件，并转为json格式
                            readAndParseXml(f.getAbsolutePath());
                    }
                }
            }
        }else {
            System.out.println("目录不存在！");
        }
    }

    /**
     * 读取xml并解析为json
     * @param filePath
     */
    private void readAndParseXml(String filePath) {
        try {
            File file = new File(filePath);

            System.out.println();
            System.out.println();
            System.out.println("#####################################################################################################");
            System.out.println("正在读取 ：" + filePath);

            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String str = null;
            StringBuffer sb = new StringBuffer();
            while((str = reader.readLine()) != null) {
                sb.append(str);
            }
            reader.close();
            String jsonString = null;
            if(sb.length() > 0) {
                //转换为json字符串
                jsonString = parseToJson(sb.toString());

                System.out.println();
                System.out.println("===>xml转换json完毕");
//                System.out.println(jsonString);
                System.out.println("----------------------------------------------------------------------------------------------------");
                System.out.println("===>开始筛选json项，封装结果如下：");

                // 筛选要索引的项
                JSONObject jsonObject = JSONObject.fromObject(jsonString);
                reconstructJson(jsonObject, filePath);
            }else {
                exMap.put(filePath, "文件内容为空！");
            }


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String parseToJson(String str) {
        return new XMLSerializer().read(str).toString(1);
    }


    private void reconstructJson(JSONObject jsonObject, String filePath) {
        if (!jsonObject.isNullObject()) {
            // 获得typeId
            JSONObject typeID = jsonObject.getJSONObject("typeId");
            String typeId = typeID.get("@root").toString() + typeID.get("@extension");
//            System.out.println("typeId===>" + typeId);
            // 获得templateId
            JSONArray jsonArray = jsonObject.getJSONArray("templateId");
            JSONObject row = null;
            StringBuffer sb1 = new StringBuffer();
            for (int i = 0; i < jsonArray.size(); i++) {
                row = jsonArray.getJSONObject(i);
                if (row.containsKey("@assigningAuthorityName")) {
                    sb1.append(row.get("@assigningAuthorityName").toString());
                }
                if (row.containsKey("@extension")) {
                    sb1.append(row.get("@extension").toString());
                }
                if (row.containsKey("@root")) {
                    sb1.append(row.get("@root").toString());
                }
            }
            String templateId = sb1.toString();
//            System.out.println("templateId===>" + templateId);

            // 获取recordTarget
            StringBuffer sb2 = new StringBuffer();
            JSONObject recordTarget = jsonObject.getJSONObject("recordTarget");
            JSONObject patient = recordTarget.getJSONObject("patientRole").getJSONObject("patient");
            if (patient.containsKey("name")) {
                sb2.append(patient.get("name").toString());
            }
            sb2.append(patient.getJSONObject("administrativeGenderCode").get("@codeSystem"));
            sb2.append(patient.getJSONObject("administrativeGenderCode").get("@codeSystemName"));
            sb2.append(patient.getJSONObject("administrativeGenderCode").get("@displayName"));
            sb2.append(patient.getJSONObject("birthTime").get("@value"));

            String recordTargetStr = sb2.toString();
//            System.out.println("recordTarget===> " + recordTargetStr);

            //获得custodian
            JSONObject custodian = jsonObject.getJSONObject("custodian");
            StringBuffer sb3 = new StringBuffer();
            sb3.append(custodian.getJSONObject("assignedCustodian").getJSONObject("representedCustodianOrganization").get("name"));

            String custodianStr = sb3.toString();
//            System.out.println("custodian===>" + custodianStr);

            //获取component
//            String headAndBody = getComponent(jsonObject, filePath);
//            System.out.println("headAndBody===>" + jsonObject.toString());
            //建立索引
            System.out.println();
            System.out.println("===>开始一次索引");

            try {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i <30 ; i++) {
                    sb.append(jsonObject.toString());
                }
                //构造json
                XContentBuilder builder = XContentFactory.jsonBuilder().startObject()
                        .field("typeId", typeId)
                        .field("templateId", templateId)
                        .field("recordTarget", recordTargetStr)
                        .field("custodian", custodianStr)
                        .field("component", sb.toString())
                        .field("@timestamp",new Date())
                        .endObject();
//                System.out.println("builder.toString 的值："+builder.string());
                IndexResponse response = client.prepareIndex("filesystem_es10", "es_data1"+this.typeVal)
                        .setSource(builder.string())
                        .execute()
                        .actionGet();
                System.out.println(response.getResult());
                System.out.println(response.getIndex());
                System.out.println(response.getVersion());
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else {
            System.out.println("转换后json对象为空！");
            exMap.put(filePath, filePath);
        }
    }

    //取得header和body里的内容
    private String getComponent(JSONObject jsonObject, String filepath) {
        StringBuffer sb4 = new StringBuffer();
        JSONObject structuredBody = jsonObject.getJSONObject("component").getJSONObject("structuredBody");
        JSONArray componentArray = null;
        if(structuredBody.toString() != "null") {
            componentArray = structuredBody.getJSONArray("component");
        }
        Iterator iterator = componentArray.iterator();
        while (iterator.hasNext()) {
            JSONObject component = (JSONObject) iterator.next();
            JSONObject section = null;
            if(component != null) {
                if(component.toString() != "null") {
                    section = component.getJSONObject("section");
                    sb4.append(section.toString());
                }
            }
        }

        String headAndBody = sb4.toString();
//        System.out.println("headAndBody===>" + headAndBody);
        return headAndBody;
    }
    public void close() {
        client.close();
    }
  public void sendClientData() {
//      GetResponse response = client.prepareGet("applog", "logs", "2").execute().actionGet();
//      IndexResponse response = client.prepareIndex("applog", "logs")
//              .setId("1")
//              .setSource("{name:bray}")
//              .execute()
//              .actionGet();
      IndexResponse response = client.prepareIndex("filesystem_es5", "EStype","2")
              .setSource("{ \"title\":\"test2\",\"content\":\"test content\"}")
              .execute()
              .actionGet();
      System.out.println(response.getResult());
      System.out.println(response.getIndex());
      System.out.println(response.getVersion());
  }

    public static void main(String[] args) {
        ESClient4 esClient = new ESClient4();
//        esClient.init();
        while (true) {
            try {
                String pathname = "D:/TEST_XML";
                esClient.createIndexFromDir(pathname);

                System.out.println();
                System.out.println("异常文件列表：");
                for (Map.Entry<String, String> entry : esClient.getExMap().entrySet()) {
                    System.out.println(entry.getKey() + "  ###具体异常原因：  " + entry.getValue());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        esClient.sendClientData();
    }
    @RequestMapping("/toClient4.do")
    public void transportClient() {
            ESClient4 esClient = new ESClient4();
            esClient.init(1);
            esClient.start();
    }
    @Override
    public void run() {
        while (true) {
            try {
                String pathname = "Y:/";
                this.createIndexFromDir(pathname);
                System.out.println();
                System.out.println("异常文件列表：");
                for (Map.Entry<String, String> entry : this.getExMap().entrySet()) {
                    System.out.println(entry.getKey() + "  ###具体异常原因：  " + entry.getValue());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
