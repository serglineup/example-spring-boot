package ua.com.lineup.enrollmentSystem.service.sms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.lineup.enrollmentSystem.model.entity.statistic.RegistrationStatisticSms;
import ua.com.lineup.enrollmentSystem.model.entity.statistic.enumeration.StepSms;
import ua.com.lineup.enrollmentSystem.service.sms.steps.AllSmsStepsService;

@Service
public class SmsEnrolmentService {

    private RegistrationStatisticSms registrationStatisticSms;
    private AllSmsStepsService allSmsStepsService;

    @Autowired
    public void setAllSmsStepsService(AllSmsStepsService allSmsStepsService) {
        this.allSmsStepsService = allSmsStepsService;
    }

    public String processSmsRequest(String smsText, String senderNumber, RegistrationStatisticSms registrationStatisticSms) {

        this.registrationStatisticSms = registrationStatisticSms;
        return allSmsStepsService.getStepServiceByName(getLastStep()).initStep(smsText, senderNumber, registrationStatisticSms).processSmsRequest();
    }

    private StepSms getLastStep() {
        return registrationStatisticSms.getSteps().get(registrationStatisticSms.getSteps().size() - 1);
    }

}
