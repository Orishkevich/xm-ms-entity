package com.icthh.xm.ms.entity.domain.spec;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Map;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "key", "name" })
@Data
public class EventSpec {

    @JsonProperty("key")
    private String key;
    @JsonProperty("name")
    private Map<String, String> name;
    @JsonProperty("color")
    private String color;
    @JsonProperty("dataTypeKey")
    private String dataTypeKey;

    /**
     * Event timeZone display strategy on interface. It could be:
     * STANDARD - default,
     * SUBJECT - timeZoneId for display is saved to Event.timeZoneId
     * and default timeZoneId is taken from XmEntity data by path timeZoneDataRef
     */
    @JsonProperty("timeZoneStrategy")
    private String timeZoneStrategy;
    @JsonProperty("timeZoneDataRef")
    private String timeZoneDataRef;
}
