package ua.com.lineup.enrollmentSystem.service.sms.steps;

import org.springframework.stereotype.Service;
import ua.com.lineup.enrollmentSystem.model.entity.organizationalstructure.Province;
import ua.com.lineup.enrollmentSystem.model.entity.organizationalstructure.Region;
import ua.com.lineup.enrollmentSystem.model.entity.statistic.enumeration.StepSms;
import ua.com.lineup.enrollmentSystem.properties.TranslationProperties;
import ua.com.lineup.enrollmentSystem.service.client.OzekiApiCaller;
import ua.com.lineup.enrollmentSystem.service.organizationalstructure.LocationService;
import ua.com.lineup.enrollmentSystem.service.statistic.RegistrationStatisticSmsService;
import ua.com.lineup.enrollmentSystem.service.statistic.SessionStatisticService;
import ua.com.lineup.enrollmentSystem.service.userbiometric.CitizenService;

import java.util.List;

@Service
public class StepChooseRegionService extends StepChooseGeographicalObjectService {

    public StepChooseRegionService(RegistrationStatisticSmsService registrationStatisticSmsService,
                                   OzekiApiCaller ozekiApiCaller, CitizenService citizenService,
                                   LocationService locationService, SessionStatisticService sessionStatisticService,
                                   TranslationProperties translationProperties) {
        super(registrationStatisticSmsService, ozekiApiCaller, citizenService, locationService, sessionStatisticService, translationProperties);
        stepBackDisplayMsg = StepSms.IDENTIFICATION_SUMMARY;
        stepBackName = StepSms.CHOOSE_COUNTRY;
        nextStepName = StepSms.CHOOSE_PROVINCE;
        entityName = translationProperties.getREGION();
    }

    public String processSmsRequest() {

        if (!isInteger(smsText)) {
            return errorDetect();
        }
        int answer = Integer.parseInt(smsText);
        List<Region> allRegions = locationService.getAllRegionByCountryCode(registrationStatisticSms.getCountryCode());
        if (answer == allRegions.size() + 1) {
            // step BACK detected
            return back();
        }
        Region region = getLocationsByUserAnswer(allRegions, answer);
        if (region == null) {
            return errorDetect();
        }
        registrationStatisticSms.setRegionId(region.getId());
        return showNextStepSms();
    }


    public String getSmsText() {
        List<Province> allProvinces = locationService.getAllProvinceByRegionId(registrationStatisticSms.getRegionId());
        return displayOptionList(allProvinces,allSmsStepsService.getStepServiceByName(nextStepName).getEntityName());
    }

}
