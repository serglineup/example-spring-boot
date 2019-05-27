package ua.com.lineup.enrollmentSystem.service.sms.steps;

import org.springframework.stereotype.Service;
import ua.com.lineup.enrollmentSystem.model.dto.request.SMSRequest;
import ua.com.lineup.enrollmentSystem.model.entity.organizationalstructure.Country;
import ua.com.lineup.enrollmentSystem.model.entity.organizationalstructure.Place;
import ua.com.lineup.enrollmentSystem.model.entity.statistic.enumeration.StepSms;
import ua.com.lineup.enrollmentSystem.properties.TranslationProperties;
import ua.com.lineup.enrollmentSystem.service.client.OzekiApiCaller;
import ua.com.lineup.enrollmentSystem.service.organizationalstructure.LocationService;
import ua.com.lineup.enrollmentSystem.service.statistic.RegistrationStatisticSmsService;
import ua.com.lineup.enrollmentSystem.service.statistic.SessionStatisticService;
import ua.com.lineup.enrollmentSystem.service.userbiometric.CitizenService;

import java.util.List;

@Service
public class StepIdentificationSummaryService extends StepChooseGeographicalObjectService {


    public StepIdentificationSummaryService(RegistrationStatisticSmsService registrationStatisticSmsService,
                                            OzekiApiCaller ozekiApiCaller, CitizenService citizenService,
                                            LocationService locationService, SessionStatisticService sessionStatisticService,
                                            TranslationProperties translationProperties) {
        super(registrationStatisticSmsService, ozekiApiCaller, citizenService, locationService, sessionStatisticService, translationProperties);

        stepBackName = StepSms.PROCESS_CNIB_PASSPORT_VERIFICATION;
    }

    public String processSmsRequest() {

        String msgText;
        if (smsText.equals("1")) {
            msgText = getSmsText();
            SMSRequest smsRequest = new SMSRequest(senderNumber, msgText);
            ozekiApiCaller.sendMessage(smsRequest);
            registrationStatisticSms.getSteps().add(StepSms.CHOOSE_PLACE);
            registrationStatisticSms.setWrongAttempts(0);
            registrationStatisticSms.setSummaryConfirm(true);
            registrationStatisticSmsService.updateStatisticSms(registrationStatisticSms);
            return msgText;

        } else if (smsText.equals("2")) {
            registrationStatisticSms.setProvinceId(0L);
            registrationStatisticSms.setCityId(0L);
            registrationStatisticSms.setDistrictId(0L);
            registrationStatisticSms.setVillageId(0L);

            msgText = getSmsTextModifyData();
            SMSRequest smsRequest = new SMSRequest(senderNumber, msgText);
            ozekiApiCaller.sendMessage(smsRequest);
            registrationStatisticSms.getSteps().add(StepSms.CHOOSE_COUNTRY);
            registrationStatisticSms.setWrongAttempts(0);
            registrationStatisticSms.setSummaryConfirm(false);
            registrationStatisticSmsService.updateStatisticSms(registrationStatisticSms);
            return msgText;
        }

        return errorDetect();
    }

    @Override
    public String errorDetect() {
        return errorDetectAction(translationProperties.getWRONG_MESSAGES_PLEASE_START_AGAIN());
    }

    @Override
    public String back() {
        return null;
    }


    public String getSmsText() {
        List<Place> allPlace = locationService.getAllPlaceByVillageId(registrationStatisticSms.getVillageId());
        return displayOptionList(allPlace, allSmsStepsService.getStepServiceByName(StepSms.CHOOSE_PLACE).getEntityName());
    }


    public String getSmsTextModifyData() {
        List<Country> allCountry = locationService.getAllCountry();
        return displayOptionList(allCountry, allSmsStepsService.getStepServiceByName(StepSms.CHOOSE_COUNTRY).getEntityName());
    }

}
