package module.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import module.domain.enums.RegSteps;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UserCash {

    private RegSteps regStep = RegSteps.NAME;

    private String currentMethod;

    private Integer lastMessageId;


    @Setter(AccessLevel.NONE)
    private List<Integer> lastMessagesId = new ArrayList<>();

    @Setter(AccessLevel.NONE)
    private List<Integer> lastMessagesPhotoIds = new ArrayList<>();

    @Setter(AccessLevel.NONE)
    private List<Integer> lastSearchMessagesPhotoIds = new ArrayList<>();

    private Address[] addresses;

    public void nextStep() {
        if (regStep == RegSteps.NAME) regStep = RegSteps.SEX;
        else if (regStep == RegSteps.SEX) regStep = RegSteps.AGE;
        else if (regStep == RegSteps.AGE) regStep = RegSteps.ABOUT;
        else if (regStep == RegSteps.ABOUT) regStep = RegSteps.LOCATION;
    }

}
