package ua.com.lineup.enrollmentSystem.service.sms.steps;

import org.springframework.beans.factory.annotation.Autowired;
import ua.com.lineup.enrollmentSystem.model.dto.request.SMSRequest;
import ua.com.lineup.enrollmentSystem.model.entity.statistic.RegistrationStatisticSms;
import ua.com.lineup.enrollmentSystem.model.entity.statistic.enumeration.StepSms;
import ua.com.lineup.enrollmentSystem.properties.TranslationProperties;
import ua.com.lineup.enrollmentSystem.service.client.OzekiApiCaller;
import ua.com.lineup.enrollmentSystem.service.organizationalstructure.LocationService;
import ua.com.lineup.enrollmentSystem.service.statistic.RegistrationStatisticSmsService;
import ua.com.lineup.enrollmentSystem.service.statistic.SessionStatisticService;
import ua.com.lineup.enrollmentSystem.service.userbiometric.CitizenService;

import java.util.Arrays;
import java.util.List;


public class StepChooseGeographicalObjectService {

    protected OzekiApiCaller ozekiApiCaller;
    protected RegistrationStatisticSmsService registrationStatisticSmsService;
    protected CitizenService citizenService;
    protected LocationService locationService;
    protected SessionStatisticService sessionStatisticService;

    protected String smsText;
    protected String senderNumber;
    protected RegistrationStatisticSms registrationStatisticSms;
    protected AllSmsStepsService allSmsStepsService;

    protected StepSms stepBackDisplayMsg;
    protected StepSms stepBackName;
    protected StepSms nextStepName;
    protected final List<Long> CITY_IDS_WITH_DISTRICTS = Arrays.asList(97L, 120L);

    public String getEntityName() {
        return entityName;
    }

    protected String entityName;
    protected TranslationProperties translationProperties;

    @Autowired
    public StepChooseGeographicalObjectService(
            RegistrationStatisticSmsService registrationStatisticSmsService,
            OzekiApiCaller ozekiApiCaller,
            CitizenService citizenService,
            LocationService locationService, SessionStatisticService sessionStatisticService,
            TranslationProperties translationProperties) {
        this.sessionStatisticService = sessionStatisticService;
        this.registrationStatisticSmsService = registrationStatisticSmsService;
        this.ozekiApiCaller = ozekiApiCaller;
        this.citizenService = citizenService;
        this.locationService = locationService;
        this.translationProperties = translationProperties;
    }

    public StepChooseGeographicalObjectService() {
    }

    @Autowired
    public void setAllSmsStepsService(AllSmsStepsService allSmsStepsService) {
        this.allSmsStepsService = allSmsStepsService;
    }

    public String processSmsRequest() {
        return "";
    }

    protected String showNextStepSms() {

        String msgText = getSmsText();
        SMSRequest smsRequest = new SMSRequest(senderNumber, msgText);
        ozekiApiCaller.sendMessage(smsRequest);
        registrationStatisticSms.getSteps().add(nextStepName);
        registrationStatisticSms.setWrongAttempts(0);
        registrationStatisticSmsService.updateStatisticSms(registrationStatisticSms);
        return msgText;
    }

    public StepChooseGeographicalObjectService initStep(String smsText, String senderNumber, RegistrationStatisticSms registrationStatisticSms) {
        this.smsText = smsText;
        this.senderNumber = senderNumber;
        this.registrationStatisticSms = registrationStatisticSms;
        return this;
    }

    public String errorDetect() {
        return errorDetectAction(translationProperties.getCHOICE_INVALID_LOCATION_PLEASE_START_AGAIN());
    }

    protected String errorDetectAction(String finalErrorText) {
        String msgText;
        SMSRequest smsRequest;
        if (registrationStatisticSms.getWrongAttempts() == 2) {
            msgText = finalErrorText;
            registrationStatisticSms.setIsProcessFinished(true);
            registrationStatisticSms.getSteps().add(StepSms.ENDED_FAILED_ATTEMPTS);
            registrationStatisticSmsService.updateStatisticSms(registrationStatisticSms);
            smsRequest = new SMSRequest(senderNumber, msgText);
            ozekiApiCaller.sendMessage(smsRequest);
            return msgText;
        }

        StepChooseGeographicalObjectService stepIdentificationSummaryService = allSmsStepsService.getStepServiceByName(stepBackName);
        stepIdentificationSummaryService.initStep(smsText, senderNumber, registrationStatisticSms);
        msgText = stepIdentificationSummaryService.getSmsText();

        registrationStatisticSms.setWrongAttempts(registrationStatisticSms.getWrongAttempts() + 1);
        registrationStatisticSmsService.updateStatisticSms(registrationStatisticSms);
        smsRequest = new SMSRequest(senderNumber, msgText);
        ozekiApiCaller.sendMessage(smsRequest);
        return msgText;
    }

     /*
     For example we have step1, step2, step3. User at step3 and send "back" command, user should be moved to step2,
     in this case system use method "getSmsText" from step1 to display message and set step2 flag for receiving data on step2
     */
    public String back() {
        StepChooseGeographicalObjectService stepChooseGeographicalObjectService = allSmsStepsService.getStepServiceByName(stepBackDisplayMsg);
        stepChooseGeographicalObjectService.initStep(smsText, senderNumber, registrationStatisticSms);
        String msgText;
        if(StepSms.CHOOSE_COUNTRY == stepBackName) {
            StepIdentificationSummaryService stepIdentificationSummary = (StepIdentificationSummaryService) stepChooseGeographicalObjectService;
            msgText = stepIdentificationSummary.getSmsTextModifyData();
        }else{
            msgText = stepChooseGeographicalObjectService.getSmsText();
        }
        SMSRequest smsRequest = new SMSRequest(senderNumber, msgText);
        ozekiApiCaller.sendMessage(smsRequest);
        registrationStatisticSms.getSteps().add(stepBackName);
        registrationStatisticSms.setWrongAttempts(0);
        registrationStatisticSmsService.updateStatisticSms(registrationStatisticSms);
        return msgText;
    }


    public String getSmsText() {
        return "";
    }
    public String getSmsTextModifyData() {
        return "";
    }

    public <T> String displayOptionList(List<T> allGeographicalObjects, String geographicalObjectName) {
        StringBuilder msg = new StringBuilder(translationProperties.getPLEASE_CHOOSE_A() + " " + geographicalObjectName + ": ");
        int counter = 1;
        for (T geographicalObject : allGeographicalObjects) {
            java.lang.reflect.Method method;
            try {
                method = geographicalObject.getClass().getMethod("getName");
                String name = (String) method.invoke(geographicalObject);
                msg.append(counter).append(":").append(name).append(",");
            } catch (Exception e) {
                e.printStackTrace();
            }
            counter++;
        }

        msg.append(counter).append(":" + translationProperties.getGO_BACK());
        return msg.toString();
    }

    public <T> T getLocationsByUserAnswer(List<T> allSpecificLocations, int answer) {
        int counter = 1;
        for (T specificLocation : allSpecificLocations) {
            if (counter == answer) {
                return specificLocation;
            }
            counter++;
        }
        return null;
    }

    protected boolean isInteger(String s) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) == '-') {
                if (s.length() == 1) return false;
                else continue;
            }
            if (Character.digit(s.charAt(i), 10) < 0) return false;
        }
        return true;
    }

}
