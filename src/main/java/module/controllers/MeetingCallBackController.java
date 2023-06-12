package module.controllers;

import lombok.NoArgsConstructor;
import module.util.telegramUtils.annotations.CallBackFun;
import org.open.cdi.annotations.DIBean;

@DIBean
@NoArgsConstructor
public class MeetingCallBackController extends CallBackController {


    @CallBackFun
    public void meeting_like() {

    }

    @CallBackFun
    public void meeting_next() {

    }

    @CallBackFun
    public void meeting_partnerIsHere() {

    }

    @CallBackFun
    public void meeting_impressions() {

    }

    @CallBackFun
    public void meeting_muteUser() {

    }

}

