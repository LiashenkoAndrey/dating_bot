package module.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import module.domain.enums.RegSteps;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UserCash {

    private RegSteps regStep;

    private String currentMethod;

    private Integer lastMessageId;

    @Setter(AccessLevel.NONE)
    private List<Integer> lastMessagesId = new ArrayList<>();

    private Address[] addresses;

    public void nextStep() {
        if (regStep == RegSteps.NAME) regStep = RegSteps.SEX;
        else if (regStep == RegSteps.SEX) regStep = RegSteps.AGE;
        else if (regStep == RegSteps.AGE) regStep = RegSteps.ABOUT;
        else if (regStep == RegSteps.ABOUT) regStep = RegSteps.PHOTOS;
        else if (regStep == RegSteps.PHOTOS) regStep = RegSteps.END;
    }

}
