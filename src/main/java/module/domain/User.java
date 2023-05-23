package module.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import module.domain.enums.RegSteps;
import module.domain.enums.Sex;
import module.domain.util.LocalDateTimeDeserializer;
import module.domain.util.LocalDateTimeSerializer;
import module.domain.util.SexDeserializer;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    private String name;

    private int age;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime last_activity;

    @JsonDeserialize(using = SexDeserializer.class)
    private Sex sex;

    private String about;

    private Long telegram_id;

    private List<String> photos;


    transient RegSteps regStep;

    transient String currentCommand;

    transient Integer lastMessageId;

    public void nextStep() {
        if (regStep == RegSteps.NAME) regStep = RegSteps.SEX;
        else if (regStep == RegSteps.SEX) regStep = RegSteps.AGE;
        else if (regStep == RegSteps.AGE) regStep = RegSteps.ABOUT;
        else if (regStep == RegSteps.ABOUT) regStep = RegSteps.PHOTOS;
        else if (regStep == RegSteps.PHOTOS) regStep = RegSteps.END;
    };

}


