package helper;


import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.Utils;

import java.math.BigInteger;
import java.util.Map;

import static com.jayway.jsonpath.JsonPath.read;
import static com.jayway.jsonpath.JsonPath.using;


public class JsonHelper {
    // TODO: 11/6/2022 7. buradan baslanilacak anlatmaya
    private final Logger log = LogManager.getLogger(JsonHelper.class);


    /**
     * it is convert the json string to jayway DocumentContext.
     *
     * @param json is string json.
     * @return return is json string as DocumentContext
     * string jsonu DocumentContext e ceviriyor cunku string uzerinden islem yapamiyoruz keylerinden bulacaz jsonu
     * ayni xmlhelperdaki gibi java objesi haline getiriyoruz ama json ozellikli sekilde cviriyor sonra gidip biz icinden erisim sagliyoruz objeyi kullanarak
     */
    protected DocumentContext getJsonDocumentContext(String json) {
        Configuration configuration = Configuration.builder()
                .jsonProvider(new JacksonJsonNodeJsonProvider())
                .mappingProvider(new JacksonMappingProvider())
                .build();
        return using(configuration).parse(json);
    }

    /**
     * it finds json object by json key and then update the json value according
     * the jsonobject type (String, int, bool, etc.)
     *
     * @param json     is json as string
     * @param jsonKey  is specified pattern json key.
     * @param newValue is new value tu update
     * @return is the json string after update.
     *
     * asagida getJsonDocumentContext(json) ile gittik string jsonu verdik ve bize json objesi donuldu
     * jsonkey= tags[0].name gibi dusunebiliriz newvalue=mert gibi dusunebiliriz
     * xmlde tum veriler string ama jsonda farkli farkli tipler oluyor string,integer,boolean gibi olabilir zaten pojo bir anlamda bunun icin yaziliyor
     *
     * simdi biz jsonu ve guncellemek sitenen jsonkeyi aldik ama objeye atadik cunku ne tip donecek bilmiyorum ama bunu  o.getClass().getSimpleName() ile ogrenirim string integer mi
     *
     * sonra value type ina gore switch casede ben parse islemi yapiyorum integersa inte boolean ise boolean a ceviriyorum
     * eger o value null degilse direk yeni degeri atiyorum  context.set(jsonKey, integerValue);  nullsa o degeri yaistiramam hata gelir jsondan
     * enson guncellenmis valuelari iceren bir json elde etmis oluyoruz elimizde  return context.jsonString( );
     */
    protected String updateJsonValue(String json, String jsonKey, String newValue) {
        DocumentContext context = getJsonDocumentContext(json);
        ParseHelper parseHelper = new ParseHelper();
        try {
            Object o = read(json, jsonKey);
            String valueType = o.getClass().getSimpleName();

            switch (valueType) {
                case "Integer" -> {
                    Integer integerValue = parseHelper.parsStringToInt(newValue);
                    if (integerValue != null)
                        context.set(jsonKey, integerValue);
                }
                case "BigInteger" -> {
                    BigInteger bigInteger = parseHelper.parsStringToBigint(newValue);
                    if (bigInteger != null)
                        context.set(jsonKey, bigInteger);
                }
                case "Boolean" -> {
                    Boolean boolValue = parseHelper.parseStringToBoolean(newValue);
                    if (boolValue != null)
                        context.set(jsonKey, boolValue);
                }
                case "Float" -> {
                    Float floatValue = parseHelper.parsStringToFloat(newValue);
                    if (floatValue != null)
                        context.set(json, floatValue);
                }
                case "Double" -> {
                    Double doubleValue = parseHelper.parsStringToDouble(newValue);
                    if (doubleValue != null)
                        context.set(jsonKey, doubleValue);
                }
                default -> context.set(jsonKey, newValue);
            }
            return context.jsonString();
        } catch (PathNotFoundException je) {
            Utils utils = new Utils();
            log.warn("{} is couldn't find, json detail:\n" + utils.prettyPrint(json), jsonKey);
            return json;
        }
    }

    public Map<String, Object> getJsonValueAsMap(String json, String jsonKey) {
        try {
            return read(json, jsonKey);
        } catch (Exception e) {
            log.fatal("Json path couldn't read error message: '{}'", e.getMessage());
            throw e;
        }
    }

    public Map<String, String> getJsonValueAsMapString(String json, String jsonKey) {
        return read(json, jsonKey);
    }

    public String getJsonValueAsString(String json, String jsonKey) {
        return read(json, jsonKey);
    }


}
