package utils;

import com.google.gson.*;
import com.thoughtworks.gauge.Table;
import com.thoughtworks.gauge.TableRow;
import com.thoughtworks.gauge.datastore.ScenarioDataStore;
import com.thoughtworks.gauge.datastore.SpecDataStore;
import com.thoughtworks.gauge.datastore.SuiteDataStore;
import exceptions.WrongFormatException;
import helper.ParseHelper;
import helper.XmlHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    private final Logger log = LogManager.getLogger(Utils.class);

    /**
     * String olarak gönderilen xml veya json'nu pretty print formatında düzenler.
     *
     * @param uglyJSONorXmlString String formata xml veya json.
     * @return Formatlanmış bir şekilde json veya xml li String tipinde döner.
     */
    public String  prettyPrint(String uglyJSONorXmlString) {
        if (uglyJSONorXmlString != null) {
            try {
                return jsonPrettyPrint(uglyJSONorXmlString);
            } catch (Exception e) {
                return xmlPrettyPrint(uglyJSONorXmlString);
            }
        } else {
            return null;
        }
    }

    /**
     * Düz string olarak gönderilen json'ı pretty print formatına çevirir.
     *
     * @param json Düz string tipinde json.
     * @return Düzenlenmiş durumda json döner.
     */
    private String jsonPrettyPrint(String json) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonElement je = JsonParser.parseString(json);
            return gson.toJson(je);
        } catch (JsonSyntaxException e) {
            log.info("is not a json object");
            throw e;
        }
    }

    /**
     * Düz string olarak gönderilen xml'i pretty print formatına çevirir.
     *
     * @param uglyXml Düz string tipinde xml.
     * @return Düzenlenmiş durumda xml döner.
     */
    private String xmlPrettyPrint(String uglyXml) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            StreamResult result = new StreamResult(new StringWriter());

            XmlHelper xmlHelper = new XmlHelper();
            Document doc = xmlHelper.stringXmlToDoc(uglyXml);

            DOMSource source = new DOMSource(doc);

            transformer.transform(source, result);

            return result.getWriter().toString();
        } catch (Exception e) {
            log.info("is not a xml file.");
            return null;
        }
    }

    public boolean isNumeric(Object o) {
        boolean isNumber = true;
        for (char c : String.valueOf(o).toCharArray()) {
            if (!Character.isDigit(c)) {
                isNumber = false;
            }
        }

        return isNumber;
    }

    /**
     *
     * @param table
     * @return
     * burda gauge den aldigimiz table kendi data formatinda i bize ilk row olarak getiriyor
     * biz onu alip foreach ile mape ceviriyoruz! key value halinde
     *
     * hangi durumda ihtiyac duyulur? Form parametreyi diyelim rest assured a ekleyecem ve rest assured map olarak alabilir
     * o sebeple bu table i mape ceviriyorum. Bunu yapmazsa tum header/paramleri teker teker yazmak zorunda kaliriz
     */
    public Map<String, Object> gaugeDataTableToMap(Table table) {
        List<TableRow> rows = table.getTableRows();
        var map = new HashMap<String, Object>();
        rows.forEach(row -> map.put(row.getCellValues().get(0), row.getCellValues().get(1)));
        return map;
    }

    public String[] gaugeDataTableToStringArray(Table table) {
        List<TableRow> rows = table.getTableRows();
        return  rows.stream().map(e-> e.getCellValues().get(0))
                .toArray(String[]::new);
    }

    public Integer[] gaugeDataTableToIntArray(Table table) {
        List<TableRow> rows = table.getTableRows();
        return  rows.stream().map(e-> Integer.parseInt(e.getCellValues().get(0)))
                .toArray(Integer[]::new);
    }


    public String[] convertMapToArray(Map<String,String> map) {
        return map.values().toArray(new String[0]);
    }

    /**
     * asagidaki method yardimi ile iki gelen obje variablelarini ayni tipe donusturuyor source objemizin tipine
     * bu sayede biz responsetan istedigimiz objeyi jsonPath ile cekebiliyoruz. Obje olarak geliyor gelen objeyide istedigimiz tipe donusturebiliyoruz
     * yani response u ben rest assureddan alabiliyorum response icerisinden herhangi birseyi json path ile alabiliyorum. cektigim sey integersa integer stringse string geliyor
     * ben zaten pojo yaziyorsam bubnlarin ne olacagini biliyorum e cekincede biliyorum neden pojoya ihtiyac oluyor
     * @param sourceObject
     * @param targetObject
     * @return
     */
    public Object parsSameType(Object sourceObject, Object targetObject) {
        String className = sourceObject.getClass().getSimpleName();
        ParseHelper parseHelper = new ParseHelper();
        switch (className) {
            case "Integer":
                try {
                    return parseHelper.parsStringToInt(String.valueOf(targetObject));
                } catch (Exception e) {
                    throw new WrongFormatException(sourceObject, targetObject);
                }
            case "BigInteger":
                try {
                    return parseHelper.parsStringToBigint(String.valueOf(targetObject));
                } catch (Exception e) {
                    throw new WrongFormatException(sourceObject, targetObject);
                }
            case "Boolean":
                try {
                    return parseHelper.parseStringToBoolean(String.valueOf(targetObject));
                } catch (Exception e) {
                    throw new WrongFormatException(sourceObject, targetObject);
                }
            case "Float":
                try {
                    return parseHelper.parsStringToFloat(String.valueOf(targetObject));
                } catch (Exception e) {
                    throw new WrongFormatException(sourceObject, targetObject);
                }
            case "Double":
                try {
                    return parseHelper.parsStringToDouble(String.valueOf(targetObject));
                } catch (Exception e) {
                    throw new WrongFormatException(sourceObject, targetObject);
                }
            default:
                return targetObject;

        }
    }

    /**
     * 3 tane store mantigi var gauge in
     * biz ilk basta bir key verildiginde hepsi icin kontol ettiriyorduk ama bu yavas oluyordu
     * ondan dolayoi bir data alacagimizda getFromStoreData diyoruz once bakiyor scenarioya yoksa suite yoksa spec
     * oda yoksa keyi atar
     *  String body = String.valueOf(Utils.getFromStoreData(key));
     *  implementationda storeApi info yok helperda var impde biz gauge in kendi storedata larini kullaniyoruz
     *  
     *  
     * @param key
     * @return
     */
    // TODO: 11/22/2022  11/13/2022 get li olan implerimizi artik tek stepe indirebiliriz  scenario/spec/suit yerine getFromStoreData diyecez
    public static Object getFromStoreData(String key) {
        if (ScenarioDataStore.get(key) != null)
            return ScenarioDataStore.get(key);
        else if (SuiteDataStore.get(key) != null)
            return SuiteDataStore.get(key);
        else if (SpecDataStore.get(key) != null)
            return SpecDataStore.get(key);
        else return key;

    }

    public static void removeFromStoreData(String key) {
        if (ScenarioDataStore.get(key) != null)
            ScenarioDataStore.remove(key);
        else if (SuiteDataStore.get(key) != null)
            SuiteDataStore.remove(key);
        else if (SpecDataStore.get(key) != null)
            SpecDataStore.remove(key);
    }

    public static String strip(String text) {
        return text != null && !text.isBlank() ? text.strip() : "";
    }
}
